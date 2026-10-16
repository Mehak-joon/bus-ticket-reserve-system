package com.brs.dto;

import com.brs.entity.BookingStatus;
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
public class BookingResponse {
    private Long id;
    private String bookingReference;
    private Long scheduleId;
    private String busNumber;
    private String busName;
    private String sourceCity;
    private String destinationCity;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private BigDecimal totalAmount;
    private BookingStatus status;
    private LocalDateTime bookingDate;
    private List<String> seatNumbers;
    private List<PassengerDto> passengers;
    private String paymentStatus;
    private String razorpayOrderId;
    private String ticketNumber;
    private BigDecimal refundAmount;
    private String cancellationReason;
}
