package com.brs.client.ui;

import com.brs.client.api.ApiClient;
import com.brs.client.model.BookingModel;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.Map;
import java.util.UUID;

public class PaymentDialog extends JDialog {

    private final Frame parentFrame;
    private final BookingModel booking;
    private String orderId = "";
    private JLabel statusLabel;
    private JLabel timerLabel;
    private JButton payButton;
    private int remainingSeconds;
    private javax.swing.Timer countdownTimer;

    public PaymentDialog(Frame parent, BookingModel booking) {
        this(parent, booking, 600);
    }

    public PaymentDialog(Frame parent, BookingModel booking, int initialSeconds) {
        super(parent, "Razorpay Secure Checkout (Test Mode)", true);
        this.parentFrame = parent;
        this.booking = booking;
        this.remainingSeconds = initialSeconds > 0 ? initialSeconds : 600;
        initUi();
        loadRazorpayOrder();
        startPaymentTimer();
    }

    private void initUi() {
        setSize(500, 530);
        setLocationRelativeTo(parentFrame);
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(new EmptyBorder(20, 25, 20, 25));
        mainPanel.setBackground(Color.WHITE);

        // Razorpay Header Banner with Timer & Test badge
        JPanel bannerPanel = new JPanel(new BorderLayout(10, 0));
        bannerPanel.setBackground(new Color(11, 35, 65));
        bannerPanel.setBorder(new EmptyBorder(12, 15, 12, 15));

        JLabel razorpayLogo = new JLabel("Razorpay Checkout", IconUtils.getRevenueIcon(18, Color.WHITE), SwingConstants.LEFT);
        razorpayLogo.setIconTextGap(8);
        razorpayLogo.setFont(new Font("Segoe UI", Font.BOLD, 17));
        razorpayLogo.setForeground(Color.WHITE);

        JPanel bannerRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        bannerRight.setOpaque(false);

        timerLabel = new JLabel("Hold: " + formatTime(remainingSeconds), IconUtils.getClockIcon(14, new Color(254, 240, 138)), SwingConstants.LEFT);
        timerLabel.setIconTextGap(6);
        timerLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        timerLabel.setForeground(new Color(254, 240, 138));

        JLabel testBadge = UiUtils.createBadge("TEST MODE", new Color(254, 243, 199), new Color(180, 83, 9));
        bannerRight.add(timerLabel);
        bannerRight.add(testBadge);

        bannerPanel.add(razorpayLogo, BorderLayout.WEST);
        bannerPanel.add(bannerRight, BorderLayout.EAST);
        mainPanel.add(bannerPanel, BorderLayout.NORTH);

        // Booking & Payment Info
        JPanel contentPanel = new JPanel(new GridLayout(6, 1, 6, 6));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(new CompoundBorder(new LineBorder(UiUtils.COLOR_BORDER, 1), new EmptyBorder(15, 15, 15, 15)));

        JLabel pnrLabel = new JLabel("Booking Reference: " + booking.getBookingReference());
        pnrLabel.setFont(UiUtils.FONT_HEADER);
        pnrLabel.setForeground(UiUtils.COLOR_PRIMARY);

        JLabel busLabel = new JLabel("Bus: " + booking.getBusName() + " (" + booking.getBusNumber() + ")");
        busLabel.setFont(UiUtils.FONT_BODY);

        JLabel routeLabel = new JLabel("Route: " + booking.getSourceCity() + " -> " + booking.getDestinationCity());
        routeLabel.setFont(UiUtils.FONT_BODY);

        JLabel seatsLabel = new JLabel("Seats: " + String.join(", ", booking.getSeatNumbers()));
        seatsLabel.setFont(UiUtils.FONT_BODY);

        JLabel amountLabel = new JLabel("Amount Payable: " + UiUtils.FORMAT_CURRENCY.format(booking.getTotalAmount()));
        amountLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        amountLabel.setForeground(UiUtils.COLOR_SUCCESS);

        statusLabel = new JLabel("Connecting to Razorpay gateway...");
        statusLabel.setFont(UiUtils.FONT_SMALL);
        statusLabel.setForeground(Color.GRAY);

        contentPanel.add(pnrLabel);
        contentPanel.add(busLabel);
        contentPanel.add(routeLabel);
        contentPanel.add(seatsLabel);
        contentPanel.add(amountLabel);
        contentPanel.add(statusLabel);

        mainPanel.add(contentPanel, BorderLayout.CENTER);

        // Action Buttons with Icons
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        footer.setBackground(Color.WHITE);

        JButton cancelBtn = UiUtils.createSecondaryButton("Cancel");
        cancelBtn.setIcon(IconUtils.getCloseIcon(14, UiUtils.COLOR_PRIMARY));
        cancelBtn.setIconTextGap(6);

        payButton = UiUtils.createPrimaryButton("Pay " + UiUtils.FORMAT_CURRENCY.format(booking.getTotalAmount()));
        payButton.setIcon(IconUtils.getShieldLockIcon(16, Color.WHITE));
        payButton.setIconTextGap(8);
        payButton.setEnabled(false);

        cancelBtn.addActionListener(e -> dispose());
        payButton.addActionListener(e -> processPayment());

        footer.add(cancelBtn);
        footer.add(payButton);
        mainPanel.add(footer, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private void startPaymentTimer() {
        if (countdownTimer != null) countdownTimer.stop();
        countdownTimer = new javax.swing.Timer(1000, e -> {
            remainingSeconds--;
            timerLabel.setText("Hold: " + formatTime(remainingSeconds));
            if (remainingSeconds <= 60) {
                timerLabel.setForeground(new Color(254, 202, 202));
            }
            if (remainingSeconds <= 0) {
                countdownTimer.stop();
                payButton.setEnabled(false);
                JOptionPane.showMessageDialog(this,
                        "Payment window expired! Your temporary seat hold has been released.\nPlease select seats again.",
                        "Hold Window Expired", JOptionPane.WARNING_MESSAGE);
                dispose();
            }
        });
        countdownTimer.start();
    }

    private String formatTime(int totalSeconds) {
        int m = Math.max(0, totalSeconds / 60);
        int s = Math.max(0, totalSeconds % 60);
        return String.format("%02d:%02d", m, s);
    }

    @Override
    public void dispose() {
        if (countdownTimer != null) countdownTimer.stop();
        super.dispose();
    }

    private void loadRazorpayOrder() {
        SwingWorker<Map<String, Object>, Void> worker = new SwingWorker<>() {
            @Override
            protected Map<String, Object> doInBackground() throws Exception {
                return ApiClient.getInstance().createRazorpayOrder(booking.getId());
            }

            @Override
            protected void done() {
                try {
                    Map<String, Object> resp = get();
                    orderId = String.valueOf(resp.get("orderId"));
                    statusLabel.setText("Razorpay Order ID: " + orderId);
                    statusLabel.setForeground(UiUtils.COLOR_SECONDARY);
                    payButton.setEnabled(true);
                } catch (Exception ex) {
                    statusLabel.setText("Failed to create Razorpay order: " + ex.getMessage());
                    statusLabel.setForeground(UiUtils.COLOR_DANGER);
                }
            }
        };
        worker.execute();
    }

    private void processPayment() {
        payButton.setEnabled(false);
        statusLabel.setText("Verifying transaction with Razorpay...");
        statusLabel.setForeground(UiUtils.COLOR_ACCENT);

        String paymentId = "pay_test_" + UUID.randomUUID().toString().substring(0, 10);
        String mockSignature = "sig_test_" + UUID.randomUUID().toString().substring(0, 10);

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                ApiClient.getInstance().verifyPayment(booking.getId(), orderId, paymentId, mockSignature);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    dispose();
                    // Open booking success dialog with ticket & QR code
                    BookingSuccessDialog successDialog = new BookingSuccessDialog(parentFrame, booking.getBookingReference());
                    successDialog.setVisible(true);
                } catch (Exception ex) {
                    payButton.setEnabled(true);
                    statusLabel.setText("Payment failed: " + ex.getMessage());
                    statusLabel.setForeground(UiUtils.COLOR_DANGER);
                }
            }
        };
        worker.execute();
    }
}
