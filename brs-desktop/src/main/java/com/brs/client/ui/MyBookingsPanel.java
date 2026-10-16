package com.brs.client.ui;

import com.brs.client.api.ApiClient;
import com.brs.client.model.BookingModel;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class MyBookingsPanel extends JPanel {

    private final Frame parentFrame;
    private final DefaultTableModel tableModel;
    private final JTable bookingsTable;
    private final JLabel statusLabel = new JLabel("Your personal bus reservations and tickets");
    private final List<BookingModel> bookingList = new ArrayList<>();

    public MyBookingsPanel(Frame parent) {
        this.parentFrame = parent;
        setLayout(new BorderLayout(15, 15));
        setBackground(UiUtils.COLOR_BG);
        setBorder(new EmptyBorder(20, 25, 20, 25));

        // Top Header
        JPanel headerPanel = new JPanel(new BorderLayout(10, 10));
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(new CompoundBorder(new LineBorder(UiUtils.COLOR_BORDER, 1), new EmptyBorder(15, 20, 15, 20)));

        JLabel title = new JLabel("My Bookings & Boarding Passes", IconUtils.getTicketIcon(22, UiUtils.COLOR_PRIMARY), SwingConstants.LEFT);
        title.setIconTextGap(10);
        title.setFont(UiUtils.FONT_TITLE);
        title.setForeground(UiUtils.COLOR_PRIMARY);

        JButton refreshBtn = UiUtils.createSecondaryButton("Refresh");
        refreshBtn.setIcon(IconUtils.getRefreshIcon(14, UiUtils.COLOR_PRIMARY));
        refreshBtn.setIconTextGap(6);
        refreshBtn.addActionListener(e -> refreshBookings());

        headerPanel.add(title, BorderLayout.WEST);
        headerPanel.add(refreshBtn, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Table
        String[] columns = {"PNR Reference", "Bus Name", "Route", "Departure Date & Time", "Seats", "Total Amount", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        bookingsTable = new JTable(tableModel);
        bookingsTable.setRowHeight(38);
        bookingsTable.setFont(UiUtils.FONT_BODY);
        bookingsTable.getTableHeader().setFont(UiUtils.FONT_HEADER);
        bookingsTable.getTableHeader().setBackground(new Color(241, 245, 249));

        // Center align
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(JLabel.CENTER);
        bookingsTable.getColumnModel().getColumn(0).setCellRenderer(center);
        bookingsTable.getColumnModel().getColumn(3).setCellRenderer(center);
        bookingsTable.getColumnModel().getColumn(4).setCellRenderer(center);
        bookingsTable.getColumnModel().getColumn(5).setCellRenderer(center);
        bookingsTable.getColumnModel().getColumn(6).setCellRenderer(new StatusCellRenderer());

        JScrollPane scrollPane = new JScrollPane(bookingsTable);
        scrollPane.setBorder(new LineBorder(UiUtils.COLOR_BORDER, 1));

        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setBackground(UiUtils.COLOR_BG);
        statusLabel.setFont(UiUtils.FONT_SUBTITLE);
        statusLabel.setForeground(UiUtils.COLOR_PRIMARY);
        centerPanel.add(statusLabel, BorderLayout.NORTH);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        // Bottom Actions Bar
        JPanel actionsBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        actionsBar.setBackground(Color.WHITE);
        actionsBar.setBorder(new CompoundBorder(new LineBorder(UiUtils.COLOR_BORDER, 1), new EmptyBorder(10, 15, 10, 15)));

        JButton viewTicketBtn = UiUtils.createPrimaryButton("View Ticket & QR Code");
        viewTicketBtn.setIcon(IconUtils.getTicketIcon(16, Color.WHITE));
        viewTicketBtn.setIconTextGap(8);

        JButton downloadPdfBtn = UiUtils.createSecondaryButton("Download PDF Ticket");
        downloadPdfBtn.setIcon(IconUtils.getDownloadIcon(16, UiUtils.COLOR_PRIMARY));
        downloadPdfBtn.setIconTextGap(8);

        JButton cancelBookingBtn = UiUtils.createDangerButton("Cancel Booking & Refund");
        cancelBookingBtn.setIcon(IconUtils.getCloseIcon(14, Color.WHITE));
        cancelBookingBtn.setIconTextGap(8);

        viewTicketBtn.addActionListener(e -> viewSelectedTicket());
        downloadPdfBtn.addActionListener(e -> downloadSelectedPdf());
        cancelBookingBtn.addActionListener(e -> cancelSelectedBooking());

        // Dynamically disable cancel button if selected booking is already cancelled
        bookingsTable.getSelectionModel().addListSelectionListener(e -> {
            int row = bookingsTable.getSelectedRow();
            if (row >= 0 && row < bookingList.size()) {
                boolean isCancelled = "CANCELLED".equalsIgnoreCase(bookingList.get(row).getStatus());
                cancelBookingBtn.setEnabled(!isCancelled);
                cancelBookingBtn.setToolTipText(isCancelled ? "This booking has already been cancelled" : "Cancel booking & process refund");
            } else {
                cancelBookingBtn.setEnabled(true);
            }
        });

        actionsBar.add(viewTicketBtn);
        actionsBar.add(downloadPdfBtn);
        actionsBar.add(cancelBookingBtn);

        centerPanel.add(actionsBar, BorderLayout.SOUTH);
        add(centerPanel, BorderLayout.CENTER);
    }

    public void refreshBookings() {
        if (!ApiClient.getInstance().isLoggedIn()) {
            statusLabel.setText("Please sign in to view your bookings.");
            tableModel.setRowCount(0);
            return;
        }

        statusLabel.setText("Loading your bookings...");
        SwingWorker<List<BookingModel>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<BookingModel> doInBackground() throws Exception {
                return ApiClient.getInstance().getMyBookings();
            }

            @Override
            protected void done() {
                try {
                    List<BookingModel> bookings = get();
                    bookingList.clear();
                    tableModel.setRowCount(0);

                    if (bookings != null && !bookings.isEmpty()) {
                        bookingList.addAll(bookings);
                        DateTimeFormatter df = DateTimeFormatter.ofPattern("dd-MMM-yyyy hh:mm a");

                        for (BookingModel b : bookings) {
                            tableModel.addRow(new Object[]{
                                    b.getBookingReference(),
                                    b.getBusName() + " (" + b.getBusNumber() + ")",
                                    b.getSourceCity() + " -> " + b.getDestinationCity(),
                                    b.getDepartureTime().format(df),
                                    String.join(", ", b.getSeatNumbers()),
                                    UiUtils.FORMAT_CURRENCY.format(b.getTotalAmount()),
                                    b.getStatus()
                            });
                        }
                        statusLabel.setText("Found " + bookings.size() + " booking(s)");
                    } else {
                        statusLabel.setText("You have no booking history yet.");
                    }
                } catch (Exception ex) {
                    statusLabel.setText("Failed to load bookings: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private BookingModel getSelectedBooking() {
        int row = bookingsTable.getSelectedRow();
        if (row < 0 || row >= bookingList.size()) {
            JOptionPane.showMessageDialog(this, "Please select a booking from the table.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return bookingList.get(row);
    }

    private void viewSelectedTicket() {
        BookingModel booking = getSelectedBooking();
        if (booking == null) return;

        BookingSuccessDialog dialog = new BookingSuccessDialog(parentFrame, booking.getBookingReference());
        dialog.setVisible(true);
    }

    private void downloadSelectedPdf() {
        BookingModel booking = getSelectedBooking();
        if (booking == null) return;

        if (booking.getTicketNumber() == null) {
            JOptionPane.showMessageDialog(this, "No ticket generated for this booking yet.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File("Ticket_" + booking.getBookingReference() + ".pdf"));
        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            try {
                ApiClient.getInstance().savePdfToFile(booking.getTicketNumber(), fileToSave);
                int opt = JOptionPane.showConfirmDialog(this,
                        "PDF Ticket saved successfully!\nWould you like to open it now?",
                        "Saved", JOptionPane.YES_NO_OPTION);
                if (opt == JOptionPane.YES_OPTION) {
                    Desktop.getDesktop().open(fileToSave);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error saving PDF: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void cancelSelectedBooking() {
        BookingModel booking = getSelectedBooking();
        if (booking == null) return;

        if ("CANCELLED".equalsIgnoreCase(booking.getStatus())) {
            JOptionPane.showMessageDialog(this, "This booking has already been cancelled.", "Already Cancelled", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String reason = JOptionPane.showInputDialog(this,
                "Enter reason for cancellation:\n(Note: Refund is calculated according to cancellation policy)",
                "Cancel Booking", JOptionPane.QUESTION_MESSAGE);

        if (reason == null || reason.trim().isEmpty()) {
            return;
        }

        SwingWorker<BookingModel, Void> worker = new SwingWorker<>() {
            @Override
            protected BookingModel doInBackground() throws Exception {
                return ApiClient.getInstance().cancelBooking(booking.getId(), reason.trim());
            }

            @Override
            protected void done() {
                try {
                    BookingModel cancelled = get();
                    JOptionPane.showMessageDialog(parentFrame,
                            "Booking " + cancelled.getBookingReference() + " cancelled successfully!\n" +
                            "Refund Amount Processed: " + UiUtils.FORMAT_CURRENCY.format(cancelled.getRefundAmount()),
                            "Cancellation Confirmed", JOptionPane.INFORMATION_MESSAGE);
                    refreshBookings();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(parentFrame, "Cancellation failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private static class StatusCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            label.setHorizontalAlignment(JLabel.CENTER);
            label.setFont(new Font("Segoe UI", Font.BOLD, 12));

            String status = value != null ? value.toString() : "";
            if ("CONFIRMED".equalsIgnoreCase(status)) {
                label.setForeground(UiUtils.COLOR_SUCCESS);
            } else if ("CANCELLED".equalsIgnoreCase(status)) {
                label.setForeground(UiUtils.COLOR_DANGER);
            } else {
                label.setForeground(UiUtils.COLOR_ACCENT);
            }
            return label;
        }
    }
}
