package com.brs.service;

import com.brs.dto.PaymentVerifyRequest;
import com.brs.dto.RazorpayOrderResponse;
import com.brs.entity.Booking;
import com.brs.entity.BookingStatus;
import com.brs.entity.Payment;
import com.brs.entity.PaymentStatus;
import com.brs.repository.BookingRepository;
import com.brs.repository.PaymentRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final TicketService ticketService;

    @Value("${app.razorpay.key-id:rzp_test_college_demo_key}")
    private String keyId;

    @Value("${app.razorpay.key-secret:test_college_demo_secret}")
    private String keySecret;

    @Value("${app.razorpay.mock-mode:true}")
    private boolean mockMode;

    @Transactional
    public RazorpayOrderResponse createOrder(Long bookingId, String username) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found: " + bookingId));

        if (!booking.getUser().getUsername().equals(username)) {
            throw new IllegalArgumentException("Unauthorized to make payment for this booking");
        }

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new IllegalStateException("Booking is not in pending state");
        }

        BigDecimal amount = booking.getTotalAmount();
        String orderId;

        // Try Razorpay API if keys look real and mockMode is disabled, otherwise use mock order for college viva
        if (!mockMode && !keyId.contains("demo")) {
            try {
                RazorpayClient razorpay = new RazorpayClient(keyId, keySecret);
                JSONObject orderRequest = new JSONObject();
                orderRequest.put("amount", amount.multiply(new BigDecimal(100)).intValue()); // in paise
                orderRequest.put("currency", "INR");
                orderRequest.put("receipt", booking.getBookingReference());

                Order order = razorpay.orders.create(orderRequest);
                orderId = order.get("id");
            } catch (Exception e) {
                log.warn("Razorpay API call failed: {}. Falling back to test mock order.", e.getMessage());
                orderId = "order_mock_" + UUID.randomUUID().toString().substring(0, 12);
            }
        } else {
            orderId = "order_mock_" + UUID.randomUUID().toString().substring(0, 12);
        }

        // Save or update pending payment record
        Payment payment = paymentRepository.findByBookingId(bookingId)
                .orElse(Payment.builder()
                        .booking(booking)
                        .amount(amount)
                        .currency("INR")
                        .status(PaymentStatus.PENDING)
                        .paymentMethod("RAZORPAY_TEST")
                        .build());

        payment.setRazorpayOrderId(orderId);
        paymentRepository.save(payment);

        return RazorpayOrderResponse.builder()
                .orderId(orderId)
                .amount(amount)
                .currency("INR")
                .keyId(keyId)
                .bookingId(booking.getId())
                .bookingReference(booking.getBookingReference())
                .mockMode(mockMode)
                .build();
    }

    @Transactional
    public Payment verifyPayment(PaymentVerifyRequest request, String username) {
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new IllegalArgumentException("Booking not found: " + request.getBookingId()));

        if (!booking.getUser().getUsername().equals(username)) {
            throw new IllegalArgumentException("Unauthorized to verify payment for this booking");
        }

        Payment payment = paymentRepository.findByBookingId(request.getBookingId())
                .orElseThrow(() -> new IllegalArgumentException("Payment record not found for booking: " + request.getBookingId()));

        boolean isValid = false;

        if (mockMode || request.getRazorpayOrderId().startsWith("order_mock_")) {
            // Test mode for college project evaluation
            isValid = true;
        } else {
            try {
                JSONObject attributes = new JSONObject();
                attributes.put("razorpay_order_id", request.getRazorpayOrderId());
                attributes.put("razorpay_payment_id", request.getRazorpayPaymentId());
                attributes.put("razorpay_signature", request.getRazorpaySignature());

                isValid = Utils.verifyPaymentSignature(attributes, keySecret);
            } catch (Exception e) {
                log.error("Signature verification error: {}", e.getMessage());
                isValid = false;
            }
        }

        if (isValid) {
            payment.setRazorpayPaymentId(request.getRazorpayPaymentId());
            payment.setRazorpaySignature(request.getRazorpaySignature());
            payment.setStatus(PaymentStatus.SUCCESS);
            paymentRepository.save(payment);

            // Confirm Booking
            booking.setStatus(BookingStatus.CONFIRMED);
            bookingRepository.save(booking);

            // Mark schedule seats as permanently booked
            booking.getBookingSeats().forEach(bSeat -> {
                bSeat.getScheduleSeat().setBooked(true);
                bSeat.getScheduleSeat().setLocked(false);
                bSeat.getScheduleSeat().setLockedAt(null);
                bSeat.getScheduleSeat().setLockedByUser(null);
            });

            // Generate Electronic Ticket & QR Code
            ticketService.generateTicket(booking);

            return payment;
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
            throw new IllegalStateException("Payment verification failed! Invalid signature.");
        }
    }
}
