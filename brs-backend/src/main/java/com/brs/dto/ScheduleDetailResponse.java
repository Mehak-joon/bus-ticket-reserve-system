package com.brs.dto;

import com.brs.entity.BusType;
import com.brs.entity.ScheduleStatus;
import com.brs.entity.SeatType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleDetailResponse {
    private Long id;
    private Long busId;
    private String busNumber;
    private String busName;
    private BusType busType;
    private String amenities;
    private Long routeId;
    private String sourceCity;
    private String destinationCity;
    private BigDecimal distanceKm;
    private BigDecimal estimatedDurationHours;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private BigDecimal baseFare;
    private ScheduleStatus status;
    private long totalSeats;
    private long availableSeats;
    private List<ScheduleSeatDto> seats;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScheduleSeatDto {
        private Long id;
        private String seatNumber;
        private SeatType seatType;
        private BigDecimal fare;
        private boolean booked;
        private boolean locked;
        private boolean available;
    }
}
