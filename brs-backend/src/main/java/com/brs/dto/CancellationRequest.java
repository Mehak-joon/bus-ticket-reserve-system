package com.brs.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CancellationRequest {

    @NotBlank(message = "Cancellation reason is required")
    private String reason;
}
