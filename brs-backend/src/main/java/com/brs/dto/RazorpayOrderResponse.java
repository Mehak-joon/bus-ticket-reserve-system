package com.brs.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RazorpayOrderResponse {
    private String orderId;
    private BigDecimal amount;
    private String currency;
    private String keyId;
    private Long bookingId;
    private String bookingReference;
    private boolean mockMode;
}
