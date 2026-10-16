package com.brs.service;

import com.brs.dto.AdminDashboardStats;
import com.brs.entity.BookingStatus;
import com.brs.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final UserRepository userRepository;
    private final BusRepository busRepository;
    private final RouteRepository routeRepository;
    private final ScheduleRepository scheduleRepository;
    private final BookingRepository bookingRepository;

    public AdminDashboardStats getDashboardStats() {
        long totalUsers = userRepository.count();
        long totalBuses = busRepository.count();
        long totalRoutes = routeRepository.count();
        long totalSchedules = scheduleRepository.count();
        long totalBookings = bookingRepository.count();
        long confirmedBookings = bookingRepository.countByStatus(BookingStatus.CONFIRMED);
        long cancelledBookings = bookingRepository.countByStatus(BookingStatus.CANCELLED);

        BigDecimal totalRevenue = bookingRepository.calculateTotalRevenue();
        LocalDateTime firstDayOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        BigDecimal monthlyRevenue = bookingRepository.calculateRevenueSince(firstDayOfMonth);

        return AdminDashboardStats.builder()
                .totalUsers(totalUsers)
                .totalBuses(totalBuses)
                .totalRoutes(totalRoutes)
                .totalSchedules(totalSchedules)
                .totalBookings(totalBookings)
                .confirmedBookings(confirmedBookings)
                .cancelledBookings(cancelledBookings)
                .totalRevenue(totalRevenue != null ? totalRevenue : BigDecimal.ZERO)
                .monthlyRevenue(monthlyRevenue != null ? monthlyRevenue : BigDecimal.ZERO)
                .build();
    }
}
