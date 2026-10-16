package com.brs.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RouteRequest {

    @NotBlank(message = "Source city is required")
    private String sourceCity;

    @NotBlank(message = "Destination city is required")
    private String destinationCity;

    @NotNull(message = "Distance is required")
    @DecimalMin(value = "1.0", message = "Distance must be greater than 0")
    private BigDecimal distanceKm;

    @NotNull(message = "Estimated duration is required")
    @DecimalMin(value = "0.1", message = "Estimated duration must be greater than 0")
    private BigDecimal estimatedDurationHours;

    private Boolean active = true;
}
