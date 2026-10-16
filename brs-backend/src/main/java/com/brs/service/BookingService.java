package com.brs.service;

import com.brs.dto.BookingCreateRequest;
import com.brs.dto.BookingResponse;
import com.brs.dto.CancellationRequest;
import com.brs.dto.PassengerDto;
import com.brs.entity.*;
import com.brs.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ScheduleRepository scheduleRepository;
    private final ScheduleSeatRepository scheduleSeatRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final TicketRepository ticketRepository;

    @Value("${app.seat-lock.timeout-minutes:10}")
    private int lockTimeoutMinutes;

    @Transactional
    public BookingResponse createBooking(BookingCreateRequest request, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        Schedule schedule = scheduleRepository.findById(request.getScheduleId())
                .orElseThrow(() -> new IllegalArgumentException("Schedule not found: " + request.getScheduleId()));

        if (schedule.getDepartureTime().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Cannot book tickets for past or departed buses");
        }

        List<String> requestedSeatNumbers = request.getPassengers().stream()
                .map(PassengerDto::getSeatNumber)
                .collect(Collectors.toList());

        List<ScheduleSeat> seats = scheduleSeatRepository.findByScheduleIdAndSeatNumberIn(
                schedule.getId(), requestedSeatNumbers
        );

        if (seats.size() != requestedSeatNumbers.size()) {
            throw new IllegalArgumentException("One or more selected seats are invalid");
        }

        // Validate seats are not already booked and locked by this user
        for (ScheduleSeat seat : seats) {
            if (seat.isBooked()) {
                throw new IllegalStateException("Seat " + seat.getSeatNumber() + " is already booked");
            }
            if (seat.isCurrentlyLocked(lockTimeoutMinutes)
                    && (seat.getLockedByUser() == null || !seat.getLockedByUser().getId().equals(user.getId()))) {
                throw new IllegalStateException("Seat " + seat.getSeatNumber() + " is locked by another user");
            }
        }

        BigDecimal totalAmount = seats.stream()
                .map(ScheduleSeat::getFare)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        String bookingReference = "BRS-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Booking booking = Booking.builder()
                .bookingReference(bookingReference)
                .user(user)
                .schedule(schedule)
                .totalAmount(totalAmount)
                .status(BookingStatus.PENDING)
                .bookingDate(LocalDateTime.now())
                .build();

        // Attach seats
        for (ScheduleSeat seat : seats) {
            BookingSeat bookingSeat = BookingSeat.builder()
                    .booking(booking)
                    .scheduleSeat(seat)
                    .seatNumber(seat.getSeatNumber())
                    .fare(seat.getFare())
                    .build();
            booking.getBookingSeats().add(bookingSeat);
        }

        // Attach passengers
        for (PassengerDto pDto : request.getPassengers()) {
            Passenger passenger = Passenger.builder()
                    .booking(booking)
                    .seatNumber(pDto.getSeatNumber())
                    .fullName(pDto.getFullName().trim())
                    .age(pDto.getAge())
                    .gender(pDto.getGender())
                    .build();
            booking.getPassengers().add(passenger);
        }

        Booking savedBooking = bookingRepository.save(booking);

        return mapToResponse(savedBooking);
    }

    @Transactional
    public BookingResponse cancelBooking(Long bookingId, CancellationRequest request, String username) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found: " + bookingId));

        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        boolean isAdmin = currentUser.getRole() == Role.ROLE_ADMIN;
        if (!isAdmin && !booking.getUser().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("Unauthorized to cancel this booking");
        }

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new IllegalStateException("Booking is already cancelled");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime departure = booking.getSchedule().getDepartureTime();

        if (departure.isBefore(now)) {
            throw new IllegalStateException("Cannot cancel booking after bus departure time");
        }

        // Calculate refund based on time until departure
        long hoursUntilDeparture = Duration.between(now, departure).toHours();
        double refundPercentage;

        if (hoursUntilDeparture >= 24) {
            refundPercentage = 0.90; // 90% refund
        } else if (hoursUntilDeparture >= 12) {
            refundPercentage = 0.70; // 70% refund
        } else if (hoursUntilDeparture >= 4) {
            refundPercentage = 0.50; // 50% refund
        } else {
            refundPercentage = 0.00; // No refund within 4 hours
        }

        BigDecimal refundAmount = booking.getTotalAmount()
                .multiply(BigDecimal.valueOf(refundPercentage))
                .setScale(2, RoundingMode.HALF_UP);

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancellationReason(request.getReason());
        booking.setRefundAmount(refundAmount);

        // Free up the schedule seats
        for (BookingSeat bSeat : booking.getBookingSeats()) {
            ScheduleSeat sSeat = bSeat.getScheduleSeat();
            sSeat.setBooked(false);
            sSeat.setLocked(false);
            sSeat.setLockedAt(null);
            sSeat.setLockedByUser(null);
            scheduleSeatRepository.save(sSeat);
        }

        // Update payment status if paid
        paymentRepository.findByBookingId(bookingId).ifPresent(payment -> {
            if (payment.getStatus() == PaymentStatus.SUCCESS) {
                payment.setStatus(PaymentStatus.REFUNDED);
                paymentRepository.save(payment);
            }
        });

        // Update ticket status
        ticketRepository.findByBookingId(bookingId).ifPresent(ticket -> {
            ticket.setStatus(TicketStatus.CANCELLED);
            ticketRepository.save(ticket);
        });

        Booking updated = bookingRepository.save(booking);
        return mapToResponse(updated);
    }

    public List<BookingResponse> getUserBookings(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        return bookingRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public BookingResponse getBookingByReference(String bookingReference, String username) {
        Booking booking = bookingRepository.findByBookingReference(bookingReference)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found: " + bookingReference));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        if (user.getRole() != Role.ROLE_ADMIN && !booking.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Unauthorized to view this booking");
        }

        return mapToResponse(booking);
    }

    public List<BookingResponse> getAllBookings() {
        return bookingRepository.findAll().stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public BookingResponse mapToResponse(Booking booking) {
        List<String> seatNumbers = booking.getBookingSeats().stream()
                .map(BookingSeat::getSeatNumber)
                .collect(Collectors.toList());

        List<PassengerDto> passengerDtos = booking.getPassengers().stream()
                .map(p -> PassengerDto.builder()
                        .seatNumber(p.getSeatNumber())
                        .fullName(p.getFullName())
                        .age(p.getAge())
                        .gender(p.getGender())
                        .build())
                .collect(Collectors.toList());

        String paymentStatus = paymentRepository.findByBookingId(booking.getId())
                .map(p -> p.getStatus().name())
                .orElse("UNPAID");

        String razorpayOrderId = paymentRepository.findByBookingId(booking.getId())
                .map(Payment::getRazorpayOrderId)
                .orElse(null);

        String ticketNumber = ticketRepository.findByBookingId(booking.getId())
                .map(Ticket::getTicketNumber)
                .orElse(null);

        return BookingResponse.builder()
                .id(booking.getId())
                .bookingReference(booking.getBookingReference())
                .scheduleId(booking.getSchedule().getId())
                .busNumber(booking.getSchedule().getBus().getBusNumber())
                .busName(booking.getSchedule().getBus().getBusName())
                .sourceCity(booking.getSchedule().getRoute().getSourceCity())
                .destinationCity(booking.getSchedule().getRoute().getDestinationCity())
                .departureTime(booking.getSchedule().getDepartureTime())
                .arrivalTime(booking.getSchedule().getArrivalTime())
                .totalAmount(booking.getTotalAmount())
                .status(booking.getStatus())
                .bookingDate(booking.getBookingDate())
                .seatNumbers(seatNumbers)
                .passengers(passengerDtos)
                .paymentStatus(paymentStatus)
                .razorpayOrderId(razorpayOrderId)
                .ticketNumber(ticketNumber)
                .refundAmount(booking.getRefundAmount())
                .cancellationReason(booking.getCancellationReason())
                .build();
    }
}
