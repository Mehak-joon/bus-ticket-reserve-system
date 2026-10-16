package com.brs.client.ui;

import com.brs.client.api.ApiClient;
import com.brs.client.model.AdminStatsModel;
import com.brs.client.model.BookingModel;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class AdminDashboardPanel extends JPanel {

    private final Frame parentFrame;
    private JLabel totalRevenueLabel = new JLabel("INR 0.00");
    private JLabel totalBookingsLabel = new JLabel("0");
    private JLabel totalBusesLabel = new JLabel("0");
    private JLabel totalRoutesLabel = new JLabel("0");

    private DefaultTableModel busTableModel;
    private DefaultTableModel routeTableModel;
    private DefaultTableModel scheduleTableModel;
    private DefaultTableModel allBookingsTableModel;

    public AdminDashboardPanel(Frame parent) {
        this.parentFrame = parent;
        setLayout(new BorderLayout(15, 15));
        setBackground(UiUtils.COLOR_BG);
        setBorder(new EmptyBorder(15, 20, 15, 20));

        // Top Header
        JPanel headerPanel = new JPanel(new BorderLayout(10, 10));
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(new CompoundBorder(new LineBorder(UiUtils.COLOR_BORDER, 1), new EmptyBorder(15, 20, 15, 20)));

        JLabel title = new JLabel("Admin Control Panel & Revenue Reports", IconUtils.getSettingsIcon(22, UiUtils.COLOR_PRIMARY), SwingConstants.LEFT);
        title.setIconTextGap(10);
        title.setFont(UiUtils.FONT_TITLE);
        title.setForeground(UiUtils.COLOR_PRIMARY);

        JButton refreshBtn = UiUtils.createSecondaryButton("Refresh All Data");
        refreshBtn.setIcon(IconUtils.getRefreshIcon(14, UiUtils.COLOR_PRIMARY));
        refreshBtn.setIconTextGap(6);
        refreshBtn.addActionListener(e -> refreshAllAdminData());

        headerPanel.add(title, BorderLayout.WEST);
        headerPanel.add(refreshBtn, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Center: Tabbed Pane for Overview, Buses, Routes, Schedules, All Bookings
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UiUtils.FONT_HEADER);

        tabbedPane.addTab("Overview & Revenue", IconUtils.getRevenueIcon(16, UiUtils.COLOR_PRIMARY), createOverviewTab());
        tabbedPane.addTab("Manage Buses", IconUtils.getBusIcon(16, UiUtils.COLOR_PRIMARY), createBusesTab());
        tabbedPane.addTab("Manage Routes", IconUtils.getTicketIcon(16, UiUtils.COLOR_PRIMARY), createRoutesTab());
        tabbedPane.addTab("Manage Schedules", IconUtils.getClockIcon(16, UiUtils.COLOR_PRIMARY), createSchedulesTab());
        tabbedPane.addTab("All Bookings", IconUtils.getDocumentIcon(16, UiUtils.COLOR_PRIMARY), createAllBookingsTab());

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createOverviewTab() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBackground(UiUtils.COLOR_BG);
        panel.setBorder(new EmptyBorder(20, 10, 20, 10));

        // Stats Cards Grid
        JPanel cardsGrid = new JPanel(new GridLayout(1, 4, 15, 15));
        cardsGrid.setBackground(UiUtils.COLOR_BG);

        cardsGrid.add(createStatCard("Total System Revenue", totalRevenueLabel, UiUtils.COLOR_SUCCESS, IconUtils.getRevenueIcon(20, UiUtils.COLOR_SUCCESS)));
        cardsGrid.add(createStatCard("Total Bookings", totalBookingsLabel, UiUtils.COLOR_PRIMARY, IconUtils.getDocumentIcon(20, UiUtils.COLOR_PRIMARY)));
        cardsGrid.add(createStatCard("Active Fleet Buses", totalBusesLabel, UiUtils.COLOR_SECONDARY, IconUtils.getBusIcon(20, UiUtils.COLOR_SECONDARY)));
        cardsGrid.add(createStatCard("Active Routes", totalRoutesLabel, UiUtils.COLOR_ACCENT, IconUtils.getTicketIcon(20, UiUtils.COLOR_ACCENT)));

        panel.add(cardsGrid, BorderLayout.NORTH);

        // Info Banner
        JPanel infoCard = UiUtils.createCardPanel();
        infoCard.setLayout(new BorderLayout(10, 10));
        JLabel infoTitle = new JLabel("System Health & Status");
        infoTitle.setFont(UiUtils.FONT_SUBTITLE);
        infoTitle.setForeground(UiUtils.COLOR_PRIMARY);

        JTextArea infoText = new JTextArea(
                "• All transactions and seat locks are synchronized in real-time with the Spring Boot backend.\n" +
                "• Expired temporary seat locks (10-minute timeout) are cleaned automatically by the backend scheduler.\n" +
                "• Use the tabs above to add new luxury or standard buses, configure inter-city routes, and publish new trip schedules.\n" +
                "• Razorpay test mode payment verification and electronic QR ticketing are operating in college demo mode."
        );
        infoText.setEditable(false);
        infoText.setFont(UiUtils.FONT_BODY);
        infoText.setBackground(Color.WHITE);
        infoCard.add(infoTitle, BorderLayout.NORTH);
        infoCard.add(infoText, BorderLayout.CENTER);

        panel.add(infoCard, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createStatCard(String title, JLabel valueLabel, Color accentColor, Icon icon) {
        JPanel card = UiUtils.createCardPanel();
        card.setLayout(new BorderLayout(8, 8));

        JLabel titleLbl = new JLabel(title, icon, SwingConstants.LEFT);
        titleLbl.setIconTextGap(8);
        titleLbl.setFont(UiUtils.FONT_SMALL);
        titleLbl.setForeground(Color.GRAY);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valueLabel.setForeground(accentColor);

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    private JPanel createBusesTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(UiUtils.COLOR_BG);
        panel.setBorder(new EmptyBorder(10, 5, 10, 5));

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topBar.setBackground(UiUtils.COLOR_BG);
        JButton addBusBtn = UiUtils.createPrimaryButton("Add New Bus");
        addBusBtn.setIcon(IconUtils.getPlusIcon(14, Color.WHITE));
        addBusBtn.setIconTextGap(6);

        JButton deleteBusBtn = UiUtils.createDangerButton("Delete Selected Bus");
        deleteBusBtn.setIcon(IconUtils.getTrashIcon(14, Color.WHITE));
        deleteBusBtn.setIconTextGap(6);

        topBar.add(addBusBtn);
        topBar.add(deleteBusBtn);
        panel.add(topBar, BorderLayout.NORTH);

        String[] cols = {"ID", "Bus Number", "Bus Name", "Bus Type", "Total Seats", "Amenities"};
        busTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable table = new JTable(busTableModel);
        table.setRowHeight(32);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        addBusBtn.addActionListener(e -> showAddBusDialog());
        deleteBusBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                Long id = Long.valueOf(String.valueOf(busTableModel.getValueAt(row, 0)));
                deleteBus(id);
            } else {
                JOptionPane.showMessageDialog(this, "Select a bus to delete.");
            }
        });

        return panel;
    }

    private JPanel createRoutesTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(UiUtils.COLOR_BG);
        panel.setBorder(new EmptyBorder(10, 5, 10, 5));

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topBar.setBackground(UiUtils.COLOR_BG);
        JButton addRouteBtn = UiUtils.createPrimaryButton("Add New Route");
        addRouteBtn.setIcon(IconUtils.getPlusIcon(14, Color.WHITE));
        addRouteBtn.setIconTextGap(6);

        JButton deleteRouteBtn = UiUtils.createDangerButton("Delete Selected Route");
        deleteRouteBtn.setIcon(IconUtils.getTrashIcon(14, Color.WHITE));
        deleteRouteBtn.setIconTextGap(6);

        topBar.add(addRouteBtn);
        topBar.add(deleteRouteBtn);
        panel.add(topBar, BorderLayout.NORTH);

        String[] cols = {"ID", "Source City", "Destination City", "Distance (KM)", "Estimated Duration (Hours)"};
        routeTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable table = new JTable(routeTableModel);
        table.setRowHeight(32);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        addRouteBtn.addActionListener(e -> showAddRouteDialog());
        deleteRouteBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                Long id = Long.valueOf(String.valueOf(routeTableModel.getValueAt(row, 0)));
                deleteRoute(id);
            } else {
                JOptionPane.showMessageDialog(this, "Select a route to delete.");
            }
        });

        return panel;
    }

    private JPanel createSchedulesTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(UiUtils.COLOR_BG);
        panel.setBorder(new EmptyBorder(10, 5, 10, 5));

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topBar.setBackground(UiUtils.COLOR_BG);
        JButton addScheduleBtn = UiUtils.createPrimaryButton("Add Trip Schedule");
        addScheduleBtn.setIcon(IconUtils.getPlusIcon(14, Color.WHITE));
        addScheduleBtn.setIconTextGap(6);

        JButton deleteScheduleBtn = UiUtils.createDangerButton("Delete Selected Schedule");
        deleteScheduleBtn.setIcon(IconUtils.getTrashIcon(14, Color.WHITE));
        deleteScheduleBtn.setIconTextGap(6);

        topBar.add(addScheduleBtn);
        topBar.add(deleteScheduleBtn);
        panel.add(topBar, BorderLayout.NORTH);

        String[] cols = {"Schedule ID", "Bus Name", "Route", "Departure Time", "Arrival Time", "Base Fare", "Status"};
        scheduleTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable table = new JTable(scheduleTableModel);
        table.setRowHeight(32);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        addScheduleBtn.addActionListener(e -> showAddScheduleDialog());
        deleteScheduleBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                Long id = Long.valueOf(String.valueOf(scheduleTableModel.getValueAt(row, 0)));
                deleteSchedule(id);
            } else {
                JOptionPane.showMessageDialog(this, "Select a schedule to delete.");
            }
        });

        return panel;
    }

    private JPanel createAllBookingsTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(UiUtils.COLOR_BG);
        panel.setBorder(new EmptyBorder(10, 5, 10, 5));

        String[] cols = {"PNR", "Customer / Route", "Bus", "Departure", "Seats", "Total Paid", "Status"};
        allBookingsTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable table = new JTable(allBookingsTableModel);
        table.setRowHeight(32);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        return panel;
    }

    public void refreshAllAdminData() {
        if (!ApiClient.getInstance().isAdmin()) return;

        // Load stats
        SwingWorker<AdminStatsModel, Void> statsWorker = new SwingWorker<>() {
            @Override
            protected AdminStatsModel doInBackground() throws Exception {
                return ApiClient.getInstance().getAdminStats();
            }

            @Override
            protected void done() {
                try {
                    AdminStatsModel stats = get();
                    totalRevenueLabel.setText(UiUtils.FORMAT_CURRENCY.format(stats.getTotalRevenue()));
                    totalBookingsLabel.setText(stats.getConfirmedBookings() + " / " + stats.getTotalBookings());
                    totalBusesLabel.setText(String.valueOf(stats.getTotalBuses()));
                    totalRoutesLabel.setText(String.valueOf(stats.getTotalRoutes()));
                } catch (Exception ignored) {}
            }
        };
        statsWorker.execute();

        // Load Buses
        SwingWorker<List<Map<String, Object>>, Void> busWorker = new SwingWorker<>() {
            @Override
            protected List<Map<String, Object>> doInBackground() throws Exception {
                return ApiClient.getInstance().getAllBuses();
            }
            @Override
            protected void done() {
                try {
                    List<Map<String, Object>> buses = get();
                    busTableModel.setRowCount(0);
                    for (Map<String, Object> b : buses) {
                        busTableModel.addRow(new Object[]{b.get("id"), b.get("busNumber"), b.get("busName"), b.get("busType"), b.get("totalSeats"), b.get("amenities")});
                    }
                } catch (Exception ignored) {}
            }
        };
        busWorker.execute();

        // Load Routes
        SwingWorker<List<Map<String, Object>>, Void> routeWorker = new SwingWorker<>() {
            @Override
            protected List<Map<String, Object>> doInBackground() throws Exception {
                return ApiClient.getInstance().getAllRoutes();
            }
            @Override
            protected void done() {
                try {
                    List<Map<String, Object>> routes = get();
                    routeTableModel.setRowCount(0);
                    for (Map<String, Object> r : routes) {
                        routeTableModel.addRow(new Object[]{r.get("id"), r.get("sourceCity"), r.get("destinationCity"), r.get("distanceKm"), r.get("estimatedDurationHours")});
                    }
                } catch (Exception ignored) {}
            }
        };
        routeWorker.execute();

        // Load Schedules
        SwingWorker<List<Map<String, Object>>, Void> scheduleWorker = new SwingWorker<>() {
            @Override
            protected List<Map<String, Object>> doInBackground() throws Exception {
                return ApiClient.getInstance().getAllSchedules();
            }
            @Override
            protected void done() {
                try {
                    List<Map<String, Object>> schedules = get();
                    scheduleTableModel.setRowCount(0);
                    for (Map<String, Object> s : schedules) {
                        Map<String, Object> bus = (Map<String, Object>) s.get("bus");
                        Map<String, Object> route = (Map<String, Object>) s.get("route");
                        String busDesc = bus != null ? bus.get("busName") + " (" + bus.get("busNumber") + ")" : "-";
                        String routeDesc = route != null ? route.get("sourceCity") + " -> " + route.get("destinationCity") : "-";

                        scheduleTableModel.addRow(new Object[]{
                                s.get("id"), busDesc, routeDesc, s.get("departureTime"), s.get("arrivalTime"),
                                UiUtils.FORMAT_CURRENCY.format(new BigDecimal(String.valueOf(s.get("baseFare")))), s.get("status")
                        });
                    }
                } catch (Exception ignored) {}
            }
        };
        scheduleWorker.execute();

        // Load All Bookings
        SwingWorker<List<BookingModel>, Void> bookingsWorker = new SwingWorker<>() {
            @Override
            protected List<BookingModel> doInBackground() throws Exception {
                return ApiClient.getInstance().getAllBookings();
            }
            @Override
            protected void done() {
                try {
                    List<BookingModel> bookings = get();
                    allBookingsTableModel.setRowCount(0);
                    DateTimeFormatter df = DateTimeFormatter.ofPattern("dd-MMM hh:mm a");
                    for (BookingModel b : bookings) {
                        allBookingsTableModel.addRow(new Object[]{
                                b.getBookingReference(),
                                b.getSourceCity() + " -> " + b.getDestinationCity(),
                                b.getBusName(),
                                b.getDepartureTime().format(df),
                                String.join(", ", b.getSeatNumbers()),
                                UiUtils.FORMAT_CURRENCY.format(b.getTotalAmount()),
                                b.getStatus()
                        });
                    }
                } catch (Exception ignored) {}
            }
        };
        bookingsWorker.execute();
    }

    private void showAddBusDialog() {
        JTextField numF = new JTextField();
        JTextField nameF = new JTextField();
        JComboBox<String> typeF = new JComboBox<>(new String[]{"VOLVO_MULTI_AXLE", "AC_SLEEPER", "AC_SEATER", "SEATER"});
        JTextField seatsF = new JTextField("40");
        JTextField amenF = new JTextField("AC, Charging Port, WiFi, Water Bottle");

        Object[] fields = {
                "Bus Registration Number:", numF,
                "Bus Operator / Name:", nameF,
                "Bus Type:", typeF,
                "Total Seats:", seatsF,
                "Amenities:", amenF
        };

        int opt = JOptionPane.showConfirmDialog(this, fields, "Add New Bus to Fleet", JOptionPane.OK_CANCEL_OPTION);
        if (opt == JOptionPane.OK_OPTION) {
            try {
                int seats = Integer.parseInt(seatsF.getText().trim());
                ApiClient.getInstance().createBus(numF.getText().trim(), nameF.getText().trim(),
                        (String) typeF.getSelectedItem(), seats, amenF.getText().trim());
                JOptionPane.showMessageDialog(this, "Bus added successfully!");
                refreshAllAdminData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error adding bus: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteBus(Long id) {
        int opt = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete bus ID " + id + "?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (opt == JOptionPane.YES_OPTION) {
            try {
                ApiClient.getInstance().deleteBus(id);
                refreshAllAdminData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error deleting bus: " + ex.getMessage());
            }
        }
    }

    private void showAddRouteDialog() {
        JTextField srcF = new JTextField();
        JTextField dstF = new JTextField();
        JTextField kmF = new JTextField("250.00");
        JTextField durF = new JTextField("5.00");

        Object[] fields = {
                "Source City:", srcF,
                "Destination City:", dstF,
                "Distance (KM):", kmF,
                "Estimated Duration (Hours):", durF
        };

        int opt = JOptionPane.showConfirmDialog(this, fields, "Add New Inter-City Route", JOptionPane.OK_CANCEL_OPTION);
        if (opt == JOptionPane.OK_OPTION) {
            try {
                BigDecimal km = new BigDecimal(kmF.getText().trim());
                BigDecimal dur = new BigDecimal(durF.getText().trim());
                ApiClient.getInstance().createRoute(srcF.getText().trim(), dstF.getText().trim(), km, dur);
                JOptionPane.showMessageDialog(this, "Route created successfully!");
                refreshAllAdminData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error adding route: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteRoute(Long id) {
        int opt = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete route ID " + id + "?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (opt == JOptionPane.YES_OPTION) {
            try {
                ApiClient.getInstance().deleteRoute(id);
                refreshAllAdminData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error deleting route: " + ex.getMessage());
            }
        }
    }

    private void showAddScheduleDialog() {
        JTextField busIdF = new JTextField("1");
        JTextField routeIdF = new JTextField("1");
        JTextField fareF = new JTextField("750.00");

        LocalDateTime dep = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0);
        LocalDateTime arr = dep.plusHours(6);
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

        JTextField depF = new JTextField(dep.format(df));
        JTextField arrF = new JTextField(arr.format(df));

        Object[] fields = {
                "Bus ID (from Buses table):", busIdF,
                "Route ID (from Routes table):", routeIdF,
                "Departure Time (YYYY-MM-DDTHH:MM:SS):", depF,
                "Arrival Time (YYYY-MM-DDTHH:MM:SS):", arrF,
                "Base Fare (INR):", fareF
        };

        int opt = JOptionPane.showConfirmDialog(this, fields, "Publish Trip Schedule (Auto-generates seats)", JOptionPane.OK_CANCEL_OPTION);
        if (opt == JOptionPane.OK_OPTION) {
            try {
                Long busId = Long.parseLong(busIdF.getText().trim());
                Long routeId = Long.parseLong(routeIdF.getText().trim());
                BigDecimal fare = new BigDecimal(fareF.getText().trim());
                ApiClient.getInstance().createSchedule(busId, routeId, depF.getText().trim(), arrF.getText().trim(), fare);
                JOptionPane.showMessageDialog(this, "Schedule published successfully and all seats configured!");
                refreshAllAdminData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error publishing schedule: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteSchedule(Long id) {
        int opt = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete schedule ID " + id + "?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (opt == JOptionPane.YES_OPTION) {
            try {
                ApiClient.getInstance().deleteSchedule(id);
                refreshAllAdminData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error deleting schedule: " + ex.getMessage());
            }
        }
    }
}
