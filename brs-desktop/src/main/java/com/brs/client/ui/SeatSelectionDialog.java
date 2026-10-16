package com.brs.client.ui;

import com.brs.client.api.ApiClient;
import com.brs.client.model.BookingModel;
import com.brs.client.model.PassengerModel;
import com.brs.client.model.ScheduleModel;
import com.brs.client.model.SeatModel;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.List;

public class SeatSelectionDialog extends JDialog {

    private final ScheduleModel schedule;
    private final Frame parentFrame;
    private final Set<String> selectedSeats = new TreeSet<>();
    private final Map<String, JButton> seatButtons = new HashMap<>();

    private JLabel timerLabel;
    private JLabel totalFareLabel;
    private JLabel selectedSeatsLabel;
    private DefaultTableModel passengerTableModel;
    private JTable passengerTable;
    private javax.swing.Timer countdownTimer;
    private int remainingSeconds = 600; // 10 minutes

    public SeatSelectionDialog(Frame parent, ScheduleModel schedule) {
        super(parent, "Select Seats - " + schedule.getBusName() + " (" + schedule.getBusNumber() + ")", true);
        this.parentFrame = parent;
        this.schedule = schedule;
        initUi();
    }

    private void initUi() {
        setSize(980, 680);
        setLocationRelativeTo(parentFrame);

        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(new EmptyBorder(15, 20, 15, 20));
        mainPanel.setBackground(UiUtils.COLOR_BG);

        // 1. Top Header: Trip & Bus details + Timer
        JPanel topHeader = new JPanel(new BorderLayout(10, 10));
        topHeader.setBackground(Color.WHITE);
        topHeader.setBorder(new CompoundBorder(new LineBorder(UiUtils.COLOR_BORDER, 1), new EmptyBorder(12, 16, 12, 16)));

        JPanel tripInfo = new JPanel(new GridLayout(2, 1, 4, 4));
        tripInfo.setBackground(Color.WHITE);

        JLabel routeLabel = new JLabel(schedule.getSourceCity() + " -> " + schedule.getDestinationCity() + " | " + schedule.getBusName() + " (" + schedule.getBusType() + ")");
        routeLabel.setFont(UiUtils.FONT_SUBTITLE);
        routeLabel.setForeground(UiUtils.COLOR_PRIMARY);

        JLabel timeLabel = new JLabel("Departure: " + schedule.getDepartureTime().format(UiUtils.FORMAT_DATETIME) + "   |   Base Fare: " + UiUtils.FORMAT_CURRENCY.format(schedule.getBaseFare()));
        timeLabel.setFont(UiUtils.FONT_BODY);
        timeLabel.setForeground(Color.DARK_GRAY);

        tripInfo.add(routeLabel);
        tripInfo.add(timeLabel);
        topHeader.add(tripInfo, BorderLayout.WEST);

        // Lock Timer Display
        JPanel timerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        timerPanel.setBackground(Color.WHITE);
        timerLabel = new JLabel("Lock Window: 10:00", IconUtils.getClockIcon(16, UiUtils.COLOR_ACCENT), SwingConstants.LEFT);
        timerLabel.setIconTextGap(6);
        timerLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        timerLabel.setForeground(UiUtils.COLOR_ACCENT);
        timerPanel.add(timerLabel);
        topHeader.add(timerPanel, BorderLayout.EAST);

        mainPanel.add(topHeader, BorderLayout.NORTH);

        // 2. Center Panel: Split into Left (Bus Seat Layout) and Right (Passenger details & Checkout)
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.55);
        splitPane.setDividerSize(6);

        // Left: Bus Graphic & Seats
        JPanel busContainer = createBusSeatLayout();
        splitPane.setLeftComponent(busContainer);

        // Right: Passenger Details & Summary
        JPanel checkoutContainer = createPassengerAndSummaryPanel();
        splitPane.setRightComponent(checkoutContainer);

        mainPanel.add(splitPane, BorderLayout.CENTER);

        setContentPane(mainPanel);
        startCountdownTimer();
    }

    private JPanel createBusSeatLayout() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new CompoundBorder(new LineBorder(UiUtils.COLOR_BORDER, 1), new EmptyBorder(15, 15, 15, 15)));

        // Bus Front / Legend
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);

        JLabel busFront = new JLabel("FRONT / DRIVER CABIN", IconUtils.getSteeringWheelIcon(18, Color.DARK_GRAY), SwingConstants.CENTER);
        busFront.setIconTextGap(8);
        busFront.setFont(UiUtils.FONT_HEADER);
        busFront.setForeground(Color.GRAY);
        busFront.setOpaque(true);
        busFront.setBackground(new Color(240, 244, 248));
        busFront.setBorder(new EmptyBorder(6, 12, 6, 12));
        header.add(busFront, BorderLayout.NORTH);

        // Legend
        JPanel legendPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 8));
        legendPanel.setBackground(Color.WHITE);
        legendPanel.add(createLegendItem("Available", new Color(220, 252, 231), UiUtils.COLOR_SUCCESS));
        legendPanel.add(createLegendItem("Selected", UiUtils.COLOR_SECONDARY, Color.WHITE));
        legendPanel.add(createLegendItem("Locked", new Color(254, 243, 199), UiUtils.COLOR_ACCENT));
        legendPanel.add(createLegendItem("Booked", new Color(254, 226, 226), UiUtils.COLOR_DANGER));
        header.add(legendPanel, BorderLayout.SOUTH);

        panel.add(header, BorderLayout.NORTH);

        // Seats Grid inside a scroll pane
        JPanel seatsGrid = new JPanel(new GridLayout(0, 5, 8, 8)); // 2 seats + Aisle + 2 seats
        seatsGrid.setBackground(Color.WHITE);

        List<SeatModel> seats = schedule.getSeats();
        if (seats != null && !seats.isEmpty()) {
            for (int i = 0; i < seats.size(); i++) {
                SeatModel seat = seats.get(i);
                JButton seatBtn = new JButton(seat.getSeatNumber());
                seatBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
                seatBtn.setFocusPainted(false);
                seatBtn.setPreferredSize(new Dimension(55, 45));

                if (seat.isBooked()) {
                    seatBtn.setBackground(new Color(254, 226, 226));
                    seatBtn.setForeground(UiUtils.COLOR_DANGER);
                    seatBtn.setEnabled(false);
                    seatBtn.setToolTipText("Booked");
                } else if (seat.isLocked()) {
                    seatBtn.setBackground(new Color(254, 243, 199));
                    seatBtn.setForeground(UiUtils.COLOR_ACCENT);
                    seatBtn.setEnabled(false);
                    seatBtn.setToolTipText("Temporarily locked by another user");
                } else {
                    seatBtn.setBackground(new Color(220, 252, 231));
                    seatBtn.setForeground(UiUtils.COLOR_SUCCESS);
                    seatBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
                    seatBtn.setToolTipText("Available - INR " + seat.getFare());
                    seatBtn.addActionListener(e -> toggleSeatSelection(seat.getSeatNumber(), seatBtn));
                }

                seatButtons.put(seat.getSeatNumber(), seatBtn);
                seatsGrid.add(seatBtn);

                // Add empty aisle column after every 2 seats
                if ((i + 1) % 4 == 2) {
                    JLabel aisle = new JLabel();
                    aisle.setPreferredSize(new Dimension(20, 45));
                    seatsGrid.add(aisle);
                }
            }
        }

        JScrollPane scrollPane = new JScrollPane(seatsGrid);
        scrollPane.setBorder(null);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createLegendItem(String label, Color bg, Color fg) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        p.setBackground(Color.WHITE);
        JLabel box = new JLabel(IconUtils.getSeatIcon(14, fg));
        box.setOpaque(true);
        box.setBackground(bg);
        box.setBorder(new CompoundBorder(new LineBorder(UiUtils.COLOR_BORDER, 1), new EmptyBorder(2, 3, 2, 3)));
        JLabel text = new JLabel(label);
        text.setFont(UiUtils.FONT_SMALL);
        p.add(box);
        p.add(text);
        return p;
    }

    private JPanel createPassengerAndSummaryPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new CompoundBorder(new LineBorder(UiUtils.COLOR_BORDER, 1), new EmptyBorder(15, 15, 15, 15)));

        // Passenger Table Header
        JLabel pTitle = new JLabel("Passenger Details for Selected Seats");
        pTitle.setFont(UiUtils.FONT_HEADER);
        pTitle.setForeground(UiUtils.COLOR_PRIMARY);
        panel.add(pTitle, BorderLayout.NORTH);

        // Passenger Details Editable Table
        String[] columns = {"Seat", "Full Name", "Age", "Gender"};
        passengerTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return col != 0; // Seat number is fixed
            }
        };

        passengerTable = new JTable(passengerTableModel);
        passengerTable.setRowHeight(28);
        passengerTable.setFont(UiUtils.FONT_BODY);

        JComboBox<String> genderCombo = new JComboBox<>(new String[]{"MALE", "FEMALE", "OTHER"});
        passengerTable.getColumnModel().getColumn(3).setCellEditor(new DefaultCellEditor(genderCombo));

        JScrollPane pScroll = new JScrollPane(passengerTable);
        panel.add(pScroll, BorderLayout.CENTER);

        // Bottom Summary & Proceed Button
        JPanel summaryPanel = new JPanel(new GridLayout(4, 1, 6, 6));
        summaryPanel.setBackground(new Color(248, 250, 252));
        summaryPanel.setBorder(new CompoundBorder(new LineBorder(UiUtils.COLOR_BORDER, 1), new EmptyBorder(12, 12, 12, 12)));

        selectedSeatsLabel = new JLabel("Selected Seats: None");
        selectedSeatsLabel.setFont(UiUtils.FONT_BODY);

        totalFareLabel = new JLabel("Total Fare: INR 0.00");
        totalFareLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        totalFareLabel.setForeground(UiUtils.COLOR_SUCCESS);

        JButton proceedBtn = UiUtils.createPrimaryButton("Lock Seats & Proceed to Payment");
        proceedBtn.setIcon(IconUtils.getShieldLockIcon(16, Color.WHITE));
        proceedBtn.setIconTextGap(8);
        proceedBtn.addActionListener(e -> proceedToLockAndPayment());

        summaryPanel.add(selectedSeatsLabel);
        summaryPanel.add(totalFareLabel);
        summaryPanel.add(proceedBtn);

        panel.add(summaryPanel, BorderLayout.SOUTH);
        return panel;
    }

    private void toggleSeatSelection(String seatNumber, JButton seatBtn) {
        if (selectedSeats.contains(seatNumber)) {
            selectedSeats.remove(seatNumber);
            seatBtn.setBackground(new Color(220, 252, 231));
            seatBtn.setForeground(UiUtils.COLOR_SUCCESS);

            // Remove from passenger table
            for (int r = 0; r < passengerTableModel.getRowCount(); r++) {
                if (passengerTableModel.getValueAt(r, 0).equals(seatNumber)) {
                    passengerTableModel.removeRow(r);
                    break;
                }
            }
        } else {
            if (selectedSeats.size() >= 6) {
                JOptionPane.showMessageDialog(this, "You can select a maximum of 6 seats per booking.", "Limit Reached", JOptionPane.WARNING_MESSAGE);
                return;
            }
            selectedSeats.add(seatNumber);
            seatBtn.setBackground(UiUtils.COLOR_SECONDARY);
            seatBtn.setForeground(Color.WHITE);

            // Add row to passenger table
            passengerTableModel.addRow(new Object[]{seatNumber, "Passenger " + selectedSeats.size(), 25, "MALE"});
        }

        updateSummary();
    }

    private void updateSummary() {
        if (selectedSeats.isEmpty()) {
            selectedSeatsLabel.setText("Selected Seats: None");
            totalFareLabel.setText("Total Fare: INR 0.00");
        } else {
            selectedSeatsLabel.setText("Selected Seats (" + selectedSeats.size() + "): " + String.join(", ", selectedSeats));
            BigDecimal total = schedule.getBaseFare().multiply(new BigDecimal(selectedSeats.size()));
            totalFareLabel.setText("Total Fare: " + UiUtils.FORMAT_CURRENCY.format(total));
        }
    }

    private void proceedToLockAndPayment() {
        if (!ApiClient.getInstance().isLoggedIn()) {
            int opt = JOptionPane.showConfirmDialog(this,
                    "You need to be signed in to lock seats and proceed with booking.\nWould you like to sign in now?",
                    "Sign In Required", JOptionPane.YES_NO_OPTION);
            if (opt == JOptionPane.YES_OPTION) {
                new LoginDialog(parentFrame, () -> {
                    if (parentFrame instanceof MainFrame) {
                        ((MainFrame) parentFrame).updateAuthState();
                    }
                    proceedToLockAndPayment();
                }).setVisible(true);
            }
            return;
        }

        if (selectedSeats.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select at least one seat.", "No Seats Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Validate passenger details
        List<PassengerModel> passengers = new ArrayList<>();
        for (int r = 0; r < passengerTableModel.getRowCount(); r++) {
            String seatNo = (String) passengerTableModel.getValueAt(r, 0);
            String name = String.valueOf(passengerTableModel.getValueAt(r, 1)).trim();
            int age;
            try {
                age = Integer.parseInt(String.valueOf(passengerTableModel.getValueAt(r, 2)).trim());
                if (age < 1 || age > 120) throw new NumberFormatException();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Please enter a valid age for seat " + seatNo, "Invalid Input", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String gender = String.valueOf(passengerTableModel.getValueAt(r, 3)).trim().toUpperCase();

            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter name for seat " + seatNo, "Missing Name", JOptionPane.ERROR_MESSAGE);
                return;
            }

            passengers.add(PassengerModel.builder()
                    .seatNumber(seatNo)
                    .fullName(name)
                    .age(age)
                    .gender(gender)
                    .build());
        }

        // 1. Lock seats on backend
        try {
            ApiClient.getInstance().lockSeats(schedule.getId(), new ArrayList<>(selectedSeats));

            // 2. Create pending booking
            BookingModel booking = ApiClient.getInstance().createBooking(schedule.getId(), passengers);

            // 3. Open Payment Dialog with remaining lock time
            int currentRemaining = remainingSeconds;
            dispose();
            PaymentDialog paymentDialog = new PaymentDialog(parentFrame, booking, currentRemaining);
            paymentDialog.setVisible(true);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Could not lock seats: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void startCountdownTimer() {
        if (countdownTimer != null) countdownTimer.stop();
        remainingSeconds = 600;

        countdownTimer = new javax.swing.Timer(1000, e -> {
            remainingSeconds--;
            int mins = Math.max(0, remainingSeconds / 60);
            int secs = Math.max(0, remainingSeconds % 60);
            timerLabel.setText(String.format("Lock Window: %02d:%02d", mins, secs));

            if (remainingSeconds <= 60) {
                timerLabel.setForeground(UiUtils.COLOR_DANGER);
            } else {
                timerLabel.setForeground(UiUtils.COLOR_ACCENT);
            }

            if (remainingSeconds <= 0) {
                countdownTimer.stop();
                JOptionPane.showMessageDialog(this, "Seat locking window expired! Please reselect seats.", "Lock Expired", JOptionPane.WARNING_MESSAGE);
                dispose();
            }
        });
        countdownTimer.start();
    }

    @Override
    public void dispose() {
        if (countdownTimer != null) countdownTimer.stop();
        super.dispose();
    }
}
