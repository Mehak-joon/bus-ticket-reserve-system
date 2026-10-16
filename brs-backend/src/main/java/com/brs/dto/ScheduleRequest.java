package com.brs.dto;

import com.brs.entity.ScheduleStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ScheduleRequest {

    @NotNull(message = "Bus ID is required")
    private Long busId;

    @NotNull(message = "Route ID is required")
    private Long routeId;

    @NotNull(message = "Departure time is required")
    private LocalDateTime departureTime;

    @NotNull(message = "Arrival time is required")
    private LocalDateTime arrivalTime;

    @NotNull(message = "Base fare is required")
    @DecimalMin(value = "10.0", message = "Base fare must be at least 10")
    private BigDecimal baseFare;

    private ScheduleStatus status = ScheduleStatus.SCHEDULED;
}
