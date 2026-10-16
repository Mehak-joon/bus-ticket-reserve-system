package com.brs.controller;

import com.brs.dto.ApiResponse;
import com.brs.entity.Ticket;
import com.brs.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @GetMapping("/{ticketNumber}")
    public ResponseEntity<ApiResponse<Ticket>> getTicket(@PathVariable("ticketNumber") String ticketNumber) {
        Ticket ticket = ticketService.getTicketByNumber(ticketNumber);
        return ResponseEntity.ok(ApiResponse.ok("Ticket details retrieved", ticket));
    }

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<ApiResponse<Ticket>> getTicketByBookingId(@PathVariable("bookingId") Long bookingId) {
        Ticket ticket = ticketService.getTicketByBookingId(bookingId);
        return ResponseEntity.ok(ApiResponse.ok("Ticket details retrieved", ticket));
    }

    @GetMapping("/{ticketNumber}/pdf")
    public ResponseEntity<byte[]> downloadPdfTicket(@PathVariable("ticketNumber") String ticketNumber) {
        Ticket ticket = ticketService.getTicketByNumber(ticketNumber);
        byte[] pdfBytes = ticketService.createPdfTicket(ticket.getBooking(), ticket.getTicketNumber(), ticket.getQrCodeBase64());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=ticket_" + ticket.getBooking().getBookingReference() + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @GetMapping("/{ticketNumber}/qr")
    public ResponseEntity<byte[]> getQrCodeImage(@PathVariable("ticketNumber") String ticketNumber) {
        Ticket ticket = ticketService.getTicketByNumber(ticketNumber);
        byte[] qrBytes = Base64.getDecoder().decode(ticket.getQrCodeBase64());

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(qrBytes);
    }
}
