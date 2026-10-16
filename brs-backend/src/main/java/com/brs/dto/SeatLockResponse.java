package com.brs.dto;

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
public class SeatLockResponse {
    private Long scheduleId;
    private List<String> lockedSeats;
    private BigDecimal totalFare;
    private LocalDateTime lockedUntil;
    private int timeoutMinutes;
}
