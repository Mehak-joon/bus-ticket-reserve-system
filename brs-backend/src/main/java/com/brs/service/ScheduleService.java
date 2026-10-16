package com.brs.service;

import com.brs.dto.ScheduleDetailResponse;
import com.brs.dto.ScheduleRequest;
import com.brs.dto.ScheduleSearchRequest;
import com.brs.dto.SeatLockRequest;
import com.brs.dto.SeatLockResponse;
import com.brs.entity.*;
import com.brs.repository.BusRepository;
import com.brs.repository.RouteRepository;
import com.brs.repository.ScheduleRepository;
import com.brs.repository.ScheduleSeatRepository;
import com.brs.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final BusRepository busRepository;
    private final RouteRepository routeRepository;
    private final ScheduleSeatRepository scheduleSeatRepository;
    private final UserRepository userRepository;

    @Value("${app.seat-lock.timeout-minutes:10}")
    private int lockTimeoutMinutes;

    public List<Schedule> getAllSchedules() {
        return scheduleRepository.findAll();
    }

    public Schedule getScheduleById(Long id) {
        return scheduleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Schedule not found with id: " + id));
    }

    @Transactional
    public Schedule createSchedule(ScheduleRequest request) {
        if (request.getArrivalTime().isBefore(request.getDepartureTime())) {
            throw new IllegalArgumentException("Arrival time must be after departure time");
        }

        Bus bus = busRepository.findById(request.getBusId())
                .orElseThrow(() -> new IllegalArgumentException("Bus not found with id: " + request.getBusId()));

        Route route = routeRepository.findById(request.getRouteId())
                .orElseThrow(() -> new IllegalArgumentException("Route not found with id: " + request.getRouteId()));

        Schedule schedule = Schedule.builder()
                .bus(bus)
                .route(route)
                .departureTime(request.getDepartureTime())
                .arrivalTime(request.getArrivalTime())
                .baseFare(request.getBaseFare())
                .status(request.getStatus() != null ? request.getStatus() : ScheduleStatus.SCHEDULED)
                .build();

        Schedule savedSchedule = scheduleRepository.save(schedule);

        // Auto-generate seats for this schedule
        generateSeatsForSchedule(savedSchedule);

        return savedSchedule;
    }

    private void generateSeatsForSchedule(Schedule schedule) {
        int totalSeats = schedule.getBus().getTotalSeats();
        BusType busType = schedule.getBus().getBusType();
        List<ScheduleSeat> seats = new ArrayList<>();

        for (int i = 1; i <= totalSeats; i++) {
            String seatNum = String.valueOf(i);
            SeatType seatType;

            if (busType == BusType.SLEEPER || busType == BusType.AC_SLEEPER) {
                seatType = (i % 2 == 1) ? SeatType.LOWER : SeatType.UPPER;
                seatNum = (seatType == SeatType.LOWER ? "L" : "U") + ((i + 1) / 2);
            } else {
                // 2x2 layout: 1 (Window), 2 (Aisle), 3 (Aisle), 4 (Window)
                int col = (i - 1) % 4;
                seatType = (col == 0 || col == 3) ? SeatType.WINDOW : SeatType.AISLE;
            }

            seats.add(ScheduleSeat.builder()
                    .schedule(schedule)
                    .seatNumber(seatNum)
                    .seatType(seatType)
                    .fare(schedule.getBaseFare())
                    .booked(false)
                    .locked(false)
                    .build());
        }

        scheduleSeatRepository.saveAll(seats);
    }

    @Transactional
    public void cleanupExpiredLocks() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(lockTimeoutMinutes);
        int released = scheduleSeatRepository.releaseExpiredLocks(cutoff);
        if (released > 0) {
            log.info("Released {} expired seat locks", released);
        }
    }

    @Scheduled(fixedRate = 60000) // run every 1 minute
    public void scheduledLockCleanup() {
        try {
            cleanupExpiredLocks();
        } catch (Exception e) {
            log.error("Error during scheduled seat lock cleanup: {}", e.getMessage());
        }
    }

    @Transactional
    public List<ScheduleDetailResponse> searchSchedules(ScheduleSearchRequest search) {
        cleanupExpiredLocks();

        if (search.getSourceCity() != null && search.getDestinationCity() != null
                && !search.getSourceCity().isBlank() && !search.getDestinationCity().isBlank()
                && search.getSourceCity().trim().equalsIgnoreCase(search.getDestinationCity().trim())) {
            return java.util.Collections.emptyList();
        }

        LocalDate travelDate = search.getTravelDate() != null ? search.getTravelDate() : LocalDate.now();
        LocalDateTime startTime = travelDate.atStartOfDay();
        LocalDateTime endTime = travelDate.atTime(LocalTime.MAX);

        List<Schedule> schedules = scheduleRepository.searchSchedules(
                search.getSourceCity(),
                search.getDestinationCity(),
                startTime,
                endTime,
                ScheduleStatus.SCHEDULED
        );

        return schedules.stream().map(this::toDetailResponse).collect(Collectors.toList());
    }

    @Transactional
    public ScheduleDetailResponse getScheduleDetails(Long id) {
        cleanupExpiredLocks();
        Schedule schedule = getScheduleById(id);
        return toDetailResponse(schedule);
    }

    private ScheduleDetailResponse toDetailResponse(Schedule schedule) {
        List<ScheduleSeat> seats = scheduleSeatRepository.findByScheduleIdOrderBySeatNumberAsc(schedule.getId());
        long availableCount = seats.stream().filter(s -> !s.isBooked() && !s.isCurrentlyLocked(lockTimeoutMinutes)).count();

        List<ScheduleDetailResponse.ScheduleSeatDto> seatDtos = seats.stream().map(s -> {
            boolean currentlyLocked = s.isCurrentlyLocked(lockTimeoutMinutes);
            return ScheduleDetailResponse.ScheduleSeatDto.builder()
                    .id(s.getId())
                    .seatNumber(s.getSeatNumber())
                    .seatType(s.getSeatType())
                    .fare(s.getFare())
                    .booked(s.isBooked())
                    .locked(currentlyLocked)
                    .available(!s.isBooked() && !currentlyLocked)
                    .build();
        }).collect(Collectors.toList());

        return ScheduleDetailResponse.builder()
                .id(schedule.getId())
                .busId(schedule.getBus().getId())
                .busNumber(schedule.getBus().getBusNumber())
                .busName(schedule.getBus().getBusName())
                .busType(schedule.getBus().getBusType())
                .amenities(schedule.getBus().getAmenities())
                .routeId(schedule.getRoute().getId())
                .sourceCity(schedule.getRoute().getSourceCity())
                .destinationCity(schedule.getRoute().getDestinationCity())
                .distanceKm(schedule.getRoute().getDistanceKm())
                .estimatedDurationHours(schedule.getRoute().getEstimatedDurationHours())
                .departureTime(schedule.getDepartureTime())
                .arrivalTime(schedule.getArrivalTime())
                .baseFare(schedule.getBaseFare())
                .status(schedule.getStatus())
                .totalSeats(schedule.getBus().getTotalSeats())
                .availableSeats(availableCount)
                .seats(seatDtos)
                .build();
    }

    @Transactional
    public SeatLockResponse lockSeats(SeatLockRequest request, String username) {
        cleanupExpiredLocks();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        List<ScheduleSeat> seats = scheduleSeatRepository.findByScheduleIdAndSeatNumberIn(
                request.getScheduleId(), request.getSeatNumbers()
        );

        if (seats.size() != request.getSeatNumbers().size()) {
            throw new IllegalArgumentException("Some selected seats were not found for this schedule");
        }

        LocalDateTime now = LocalDateTime.now();
        BigDecimal totalFare = BigDecimal.ZERO;

        for (ScheduleSeat seat : seats) {
            if (seat.isBooked()) {
                throw new IllegalStateException("Seat " + seat.getSeatNumber() + " is already booked");
            }
            if (seat.isCurrentlyLocked(lockTimeoutMinutes)
                    && (seat.getLockedByUser() == null || !seat.getLockedByUser().getId().equals(user.getId()))) {
                throw new IllegalStateException("Seat " + seat.getSeatNumber() + " is currently locked by another user");
            }

            seat.setLocked(true);
            seat.setLockedAt(now);
            seat.setLockedByUser(user);
            totalFare = totalFare.add(seat.getFare());
        }

        scheduleSeatRepository.saveAll(seats);

        return SeatLockResponse.builder()
                .scheduleId(request.getScheduleId())
                .lockedSeats(request.getSeatNumbers())
                .totalFare(totalFare)
                .lockedUntil(now.plusMinutes(lockTimeoutMinutes))
                .timeoutMinutes(lockTimeoutMinutes)
                .build();
    }

    @Transactional
    public void deleteSchedule(Long id) {
        Schedule schedule = getScheduleById(id);
        scheduleRepository.delete(schedule);
    }
}
