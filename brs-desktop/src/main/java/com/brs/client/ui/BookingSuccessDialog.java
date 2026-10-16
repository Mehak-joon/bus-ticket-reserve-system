package com.brs.client.ui;

import com.brs.client.api.ApiClient;
import com.brs.client.model.BookingModel;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.io.File;

public class BookingSuccessDialog extends JDialog {

    private final String bookingReference;
    private final Frame parentFrame;
    private JLabel qrLabel;
    private JLabel pnrLabel;
    private JLabel ticketLabel;
    private JLabel detailsLabel;
    private String ticketNumber;
    private File savedPdfFile;

    public BookingSuccessDialog(Frame parent, String bookingReference) {
        super(parent, "Booking Confirmed! - Electronic Ticket", true);
        this.parentFrame = parent;
        this.bookingReference = bookingReference;
        initUi();
        loadBookingAndTicket();
    }

    private void initUi() {
        setSize(560, 600);
        setLocationRelativeTo(parentFrame);
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(new EmptyBorder(20, 25, 20, 25));
        mainPanel.setBackground(Color.WHITE);

        // Header with checkmark
        JPanel headerPanel = new JPanel(new GridLayout(2, 1, 4, 4));
        headerPanel.setBackground(Color.WHITE);

        JLabel successTitle = new JLabel("Booking Confirmed!", IconUtils.getCheckIcon(24, UiUtils.COLOR_SUCCESS), SwingConstants.CENTER);
        successTitle.setIconTextGap(10);
        successTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        successTitle.setForeground(UiUtils.COLOR_SUCCESS);

        JLabel subTitle = new JLabel("Your payment was successful and your e-ticket has been generated.", SwingConstants.CENTER);
        subTitle.setFont(UiUtils.FONT_SMALL);
        subTitle.setForeground(Color.GRAY);

        headerPanel.add(successTitle);
        headerPanel.add(subTitle);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Center: Details & QR Code display
        JPanel centerCard = new JPanel(new BorderLayout(10, 10));
        centerCard.setBackground(new Color(248, 250, 252));
        centerCard.setBorder(new CompoundBorder(new LineBorder(UiUtils.COLOR_BORDER, 1), new EmptyBorder(15, 15, 15, 15)));

        JPanel textDetails = new JPanel(new GridLayout(4, 1, 6, 6));
        textDetails.setBackground(new Color(248, 250, 252));

        pnrLabel = new JLabel("PNR / Reference: " + bookingReference);
        pnrLabel.setFont(UiUtils.FONT_HEADER);
        pnrLabel.setForeground(UiUtils.COLOR_PRIMARY);

        ticketLabel = new JLabel("Ticket Number: Loading...");
        ticketLabel.setFont(UiUtils.FONT_BODY);

        detailsLabel = new JLabel("Trip: Loading...");
        detailsLabel.setFont(UiUtils.FONT_BODY);

        JLabel noteLabel = new JLabel("Scan the QR code below at the boarding point:");
        noteLabel.setFont(UiUtils.FONT_SMALL);
        noteLabel.setForeground(Color.DARK_GRAY);

        textDetails.add(pnrLabel);
        textDetails.add(ticketLabel);
        textDetails.add(detailsLabel);
        textDetails.add(noteLabel);
        centerCard.add(textDetails, BorderLayout.NORTH);

        // QR Code image container
        JPanel qrContainer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        qrContainer.setBackground(new Color(248, 250, 252));
        qrLabel = new JLabel("Loading QR Code...", SwingConstants.CENTER);
        qrLabel.setPreferredSize(new Dimension(200, 200));
        qrLabel.setBorder(new LineBorder(UiUtils.COLOR_BORDER, 1));
        qrContainer.add(qrLabel);
        centerCard.add(qrContainer, BorderLayout.CENTER);

        mainPanel.add(centerCard, BorderLayout.CENTER);

        // Action Buttons: Download PDF, Open PDF, Close
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        footer.setBackground(Color.WHITE);

        JButton downloadPdfBtn = UiUtils.createPrimaryButton("Download PDF Ticket");
        downloadPdfBtn.setIcon(IconUtils.getDownloadIcon(16, Color.WHITE));
        downloadPdfBtn.setIconTextGap(8);

        JButton openPdfBtn = UiUtils.createSecondaryButton("Open PDF");
        openPdfBtn.setIcon(IconUtils.getDocumentIcon(16, UiUtils.COLOR_PRIMARY));
        openPdfBtn.setIconTextGap(8);

        JButton closeBtn = UiUtils.createSecondaryButton("Close");

        downloadPdfBtn.addActionListener(e -> downloadPdf());
        openPdfBtn.addActionListener(e -> openDownloadedPdf());
        closeBtn.addActionListener(e -> dispose());

        footer.add(downloadPdfBtn);
        footer.add(openPdfBtn);
        footer.add(closeBtn);
        mainPanel.add(footer, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private void loadBookingAndTicket() {
        SwingWorker<BookingModel, Void> worker = new SwingWorker<>() {
            @Override
            protected BookingModel doInBackground() throws Exception {
                // Find booking details
                java.util.List<BookingModel> bookings = ApiClient.getInstance().getMyBookings();
                return bookings.stream()
                        .filter(b -> b.getBookingReference().equalsIgnoreCase(bookingReference))
                        .findFirst()
                        .orElse(null);
            }

            @Override
            protected void done() {
                try {
                    BookingModel b = get();
                    if (b != null) {
                        ticketNumber = b.getTicketNumber();
                        ticketLabel.setText("Ticket Number: " + (ticketNumber != null ? ticketNumber : "Generated"));
                        detailsLabel.setText("Trip: " + b.getSourceCity() + " -> " + b.getDestinationCity() +
                                " | Seats: " + String.join(", ", b.getSeatNumbers()) +
                                " | Fare: " + UiUtils.FORMAT_CURRENCY.format(b.getTotalAmount()));

                        if (ticketNumber != null) {
                            loadQrImage(ticketNumber);
                        }
                    }
                } catch (Exception ex) {
                    detailsLabel.setText("Could not load booking details");
                }
            }
        };
        worker.execute();
    }

    private void loadQrImage(String tktNum) {
        SwingWorker<byte[], Void> worker = new SwingWorker<>() {
            @Override
            protected byte[] doInBackground() throws Exception {
                return ApiClient.getInstance().getQrCodeBytes(tktNum);
            }

            @Override
            protected void done() {
                try {
                    byte[] qrBytes = get();
                    if (qrBytes != null && qrBytes.length > 0) {
                        ImageIcon icon = new ImageIcon(qrBytes);
                        Image scaled = icon.getImage().getScaledInstance(180, 180, Image.SCALE_SMOOTH);
                        qrLabel.setText("");
                        qrLabel.setIcon(new ImageIcon(scaled));
                    }
                } catch (Exception ex) {
                    qrLabel.setText("QR Code unavailable");
                }
            }
        };
        worker.execute();
    }

    private void downloadPdf() {
        if (ticketNumber == null) {
            JOptionPane.showMessageDialog(this, "Ticket number is not yet ready", "Wait", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File("Ticket_" + bookingReference + ".pdf"));
        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            try {
                ApiClient.getInstance().savePdfToFile(ticketNumber, fileToSave);
                this.savedPdfFile = fileToSave;
                JOptionPane.showMessageDialog(this, "PDF Ticket saved successfully to:\n" + fileToSave.getAbsolutePath(),
                        "Download Successful", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to save PDF: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void openDownloadedPdf() {
        try {
            if (savedPdfFile != null && savedPdfFile.exists()) {
                Desktop.getDesktop().open(savedPdfFile);
            } else if (ticketNumber != null) {
                // Save to temp file and open
                File tempFile = File.createTempFile("ticket_" + bookingReference, ".pdf");
                ApiClient.getInstance().savePdfToFile(ticketNumber, tempFile);
                this.savedPdfFile = tempFile;
                Desktop.getDesktop().open(tempFile);
            } else {
                JOptionPane.showMessageDialog(this, "No PDF ticket loaded yet.", "Info", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Could not open PDF viewer: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
