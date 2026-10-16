package com.brs.service;

import com.brs.entity.Booking;
import com.brs.entity.Passenger;
import com.brs.entity.Ticket;
import com.brs.entity.TicketStatus;
import com.brs.repository.TicketRepository;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd-MMM-yyyy hh:mm a");

    @Transactional
    public Ticket generateTicket(Booking booking) {
        String ticketNumber = "TKT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // 1. Generate QR Code Content
        String qrContent = String.format(
                "PNR: %s\nTicket: %s\nBus: %s (%s)\nRoute: %s -> %s\nDeparture: %s\nTotal Amount: INR %.2f\nStatus: %s",
                booking.getBookingReference(),
                ticketNumber,
                booking.getSchedule().getBus().getBusName(),
                booking.getSchedule().getBus().getBusNumber(),
                booking.getSchedule().getRoute().getSourceCity(),
                booking.getSchedule().getRoute().getDestinationCity(),
                booking.getSchedule().getDepartureTime().format(DATE_TIME_FORMATTER),
                booking.getTotalAmount(),
                booking.getStatus()
        );

        String qrBase64 = generateQrCodeBase64(qrContent, 200, 200);

        // 2. Generate PDF bytes and save locally
        byte[] pdfBytes = createPdfTicket(booking, ticketNumber, qrBase64);
        String savedFilePath = savePdfToFile(booking.getBookingReference(), pdfBytes);

        Ticket ticket = Ticket.builder()
                .booking(booking)
                .ticketNumber(ticketNumber)
                .qrCodeBase64(qrBase64)
                .pdfFilePath(savedFilePath)
                .status(TicketStatus.ISSUED)
                .build();

        return ticketRepository.save(ticket);
    }

    public String generateQrCodeBase64(String content, int width, int height) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, width, height);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
            byte[] qrBytes = outputStream.toByteArray();

            return Base64.getEncoder().encodeToString(qrBytes);
        } catch (Exception e) {
            log.error("Failed to generate QR Code: {}", e.getMessage());
            return "";
        }
    }

    public byte[] createPdfTicket(Booking booking, String ticketNumber, String qrBase64) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter.getInstance(document, baos);
            document.open();

            // Header Banner
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, new Color(24, 76, 120));
            Font subTitleFont = FontFactory.getFont(FontFactory.HELVETICA, 11, Color.DARK_GRAY);
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, Color.BLACK);
            Font regularFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);
            Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, new Color(24, 76, 120));

            Paragraph header = new Paragraph("BUS RESERVATION SYSTEM (BRS)", titleFont);
            header.setAlignment(Element.ALIGN_CENTER);
            document.add(header);

            Paragraph subHeader = new Paragraph("Official Electronic Boarding Pass & Confirmation Receipt", subTitleFont);
            subHeader.setAlignment(Element.ALIGN_CENTER);
            subHeader.setSpacingAfter(15f);
            document.add(subHeader);

            // Top Summary Table (PNR, Ticket #, Booking Date)
            PdfPTable topTable = new PdfPTable(3);
            topTable.setWidthPercentage(100);
            topTable.setSpacingAfter(15f);

            addCellWithBackground(topTable, "PNR / REFERENCE:\n" + booking.getBookingReference(), boldFont, new Color(240, 244, 248));
            addCellWithBackground(topTable, "TICKET NUMBER:\n" + ticketNumber, boldFont, new Color(240, 244, 248));
            addCellWithBackground(topTable, "BOOKING STATUS:\n" + booking.getStatus(), boldFont, new Color(240, 244, 248));
            document.add(topTable);

            // Journey Details Table with QR Code on side
            PdfPTable journeyTable = new PdfPTable(new float[]{3.2f, 1.8f});
            journeyTable.setWidthPercentage(100);
            journeyTable.setSpacingAfter(15f);

            PdfPCell journeyCell = new PdfPCell();
            journeyCell.setBorder(Rectangle.BOX);
            journeyCell.setPadding(10f);

            journeyCell.addElement(new Paragraph("JOURNEY DETAILS", sectionFont));
            journeyCell.addElement(new Paragraph("Bus: " + booking.getSchedule().getBus().getBusName() + " (" + booking.getSchedule().getBus().getBusNumber() + ")", boldFont));
            journeyCell.addElement(new Paragraph("Bus Type: " + booking.getSchedule().getBus().getBusType() + " | Amenities: " + booking.getSchedule().getBus().getAmenities(), regularFont));
            journeyCell.addElement(new Paragraph("Route: " + booking.getSchedule().getRoute().getSourceCity() + " ➔ " + booking.getSchedule().getRoute().getDestinationCity(), boldFont));
            journeyCell.addElement(new Paragraph("Departure Time: " + booking.getSchedule().getDepartureTime().format(DATE_TIME_FORMATTER), regularFont));
            journeyCell.addElement(new Paragraph("Arrival Time: " + booking.getSchedule().getArrivalTime().format(DATE_TIME_FORMATTER), regularFont));
            journeyCell.addElement(new Paragraph("Duration: " + booking.getSchedule().getRoute().getEstimatedDurationHours() + " hrs (" + booking.getSchedule().getRoute().getDistanceKm() + " km)", regularFont));

            journeyTable.addCell(journeyCell);

            // QR Code Cell
            PdfPCell qrCell = new PdfPCell();
            qrCell.setBorder(Rectangle.BOX);
            qrCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            qrCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            qrCell.setPadding(8f);

            if (qrBase64 != null && !qrBase64.isEmpty()) {
                byte[] qrImgBytes = Base64.getDecoder().decode(qrBase64);
                Image qrImage = Image.getInstance(qrImgBytes);
                qrImage.scaleToFit(140, 140);
                qrImage.setAlignment(Element.ALIGN_CENTER);
                qrCell.addElement(qrImage);
                Paragraph scanNotice = new Paragraph("Scan at Boarding Gate", regularFont);
                scanNotice.setAlignment(Element.ALIGN_CENTER);
                qrCell.addElement(scanNotice);
            }
            journeyTable.addCell(qrCell);
            document.add(journeyTable);

            // Passenger Details Table
            Paragraph passengerHeading = new Paragraph("PASSENGER DETAILS", sectionFont);
            passengerHeading.setSpacingAfter(6f);
            document.add(passengerHeading);

            PdfPTable pTable = new PdfPTable(new float[]{1.5f, 4f, 1.5f, 1.5f});
            pTable.setWidthPercentage(100);
            pTable.setSpacingAfter(15f);

            addHeaderCell(pTable, "Seat No", boldFont);
            addHeaderCell(pTable, "Passenger Name", boldFont);
            addHeaderCell(pTable, "Age", boldFont);
            addHeaderCell(pTable, "Gender", boldFont);

            for (Passenger p : booking.getPassengers()) {
                pTable.addCell(new Phrase(p.getSeatNumber(), regularFont));
                pTable.addCell(new Phrase(p.getFullName(), regularFont));
                pTable.addCell(new Phrase(String.valueOf(p.getAge()), regularFont));
                pTable.addCell(new Phrase(p.getGender().name(), regularFont));
            }
            document.add(pTable);

            // Fare Breakdown
            PdfPTable fareTable = new PdfPTable(new float[]{4f, 2f});
            fareTable.setWidthPercentage(100);
            fareTable.setSpacingAfter(20f);

            PdfPCell termsCell = new PdfPCell();
            termsCell.setBorder(Rectangle.NO_BORDER);
            termsCell.addElement(new Paragraph("Important Instructions:", boldFont));
            termsCell.addElement(new Paragraph("• Please carry a government-issued photo ID.\n• Report at the boarding point 15 mins before departure.\n• Ticket is valid only for the specified schedule and seats.", regularFont));
            fareTable.addCell(termsCell);

            PdfPCell totalCell = new PdfPCell();
            totalCell.setBorder(Rectangle.BOX);
            totalCell.setBackgroundColor(new Color(245, 247, 250));
            totalCell.setPadding(8f);
            totalCell.addElement(new Paragraph("TOTAL FARE PAID", boldFont));
            Paragraph amountP = new Paragraph("INR " + booking.getTotalAmount().toPlainString(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, new Color(0, 128, 0)));
            totalCell.addElement(amountP);
            totalCell.addElement(new Paragraph("Payment Method: Online (Razorpay)", regularFont));
            fareTable.addCell(totalCell);

            document.add(fareTable);

            // Footer
            Paragraph footer = new Paragraph("Thank you for traveling with us! For 24/7 customer support contact support@brs.com", subTitleFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Failed to generate PDF ticket: {}", e.getMessage(), e);
            throw new RuntimeException("Error generating PDF ticket: " + e.getMessage());
        }
    }

    private void addHeaderCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(new Color(230, 235, 242));
        cell.setPadding(6f);
        table.addCell(cell);
    }

    private void addCellWithBackground(PdfPTable table, String text, Font font, Color bgColor) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bgColor);
        cell.setPadding(8f);
        table.addCell(cell);
    }

    private String savePdfToFile(String bookingRef, byte[] pdfBytes) {
        try {
            File ticketsDir = new File("tickets");
            if (!ticketsDir.exists()) {
                ticketsDir.mkdirs();
            }
            File file = new File(ticketsDir, bookingRef + ".pdf");
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(pdfBytes);
            }
            return file.getAbsolutePath();
        } catch (Exception e) {
            log.warn("Could not save PDF file to disk: {}", e.getMessage());
            return null;
        }
    }

    public Ticket getTicketByNumber(String ticketNumber) {
        return ticketRepository.findByTicketNumber(ticketNumber)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found: " + ticketNumber));
    }

    public Ticket getTicketByBookingId(Long bookingId) {
        return ticketRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found for booking: " + bookingId));
    }
}
