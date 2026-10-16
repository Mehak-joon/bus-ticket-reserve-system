package com.brs.controller;

import com.brs.dto.ApiResponse;
import com.brs.dto.PaymentVerifyRequest;
import com.brs.dto.RazorpayOrderRequest;
import com.brs.dto.RazorpayOrderResponse;
import com.brs.entity.Payment;
import com.brs.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create-order")
    public ResponseEntity<ApiResponse<RazorpayOrderResponse>> createOrder(
            @Valid @RequestBody RazorpayOrderRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        RazorpayOrderResponse response = paymentService.createOrder(request.getBookingId(), userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Razorpay order created", response));
    }

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<Payment>> verifyPayment(
            @Valid @RequestBody PaymentVerifyRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Payment payment = paymentService.verifyPayment(request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Payment verified and booking confirmed successfully", payment));
    }
}
