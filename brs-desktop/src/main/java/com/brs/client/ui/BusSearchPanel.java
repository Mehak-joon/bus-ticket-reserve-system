package com.brs.client.ui;

import com.brs.client.api.ApiClient;
import com.brs.client.model.ScheduleModel;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class BusSearchPanel extends JPanel {

    private final Frame parentFrame;
    private final JComboBox<String> sourceCombo = new JComboBox<>();
    private final JComboBox<String> destCombo = new JComboBox<>();
    private LocalDate selectedTravelDate = LocalDate.now().plusDays(1); // Default to tomorrow
    private final JTextField dateField = new JTextField();
    private JButton searchBtn;
    private final DefaultTableModel tableModel;
    private final JTable resultsTable;
    private final JLabel statusLabel = new JLabel("Search for available buses between popular cities");
    private final List<ScheduleModel> currentResults = new ArrayList<>();

    public BusSearchPanel(Frame parent) {
        this.parentFrame = parent;
        setLayout(new BorderLayout(15, 15));
        setBackground(UiUtils.COLOR_BG);
        setBorder(new EmptyBorder(20, 25, 20, 25));

        // 1. Top Search Filter Card
        JPanel searchCard = createSearchFilterCard();
        add(searchCard, BorderLayout.NORTH);

        // 2. Results Table Card with From (Departure) and To (Arrival) columns
        String[] columns = {"Bus Name / Number", "From (Departure)", "To (Arrival)", "Type", "Departure", "Arrival", "Duration", "Available Seats", "Fare", "Action"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return col == 9; // Only action button column
            }
        };

        resultsTable = new JTable(tableModel);
        resultsTable.setRowHeight(42);
        resultsTable.setFont(UiUtils.FONT_BODY);
        resultsTable.getTableHeader().setFont(UiUtils.FONT_HEADER);
        resultsTable.getTableHeader().setBackground(new Color(241, 245, 249));

        // Custom Renderer & Editor for "Select Seats" button
        resultsTable.getColumnModel().getColumn(9).setCellRenderer(new ButtonRenderer());
        resultsTable.getColumnModel().getColumn(9).setCellEditor(new ButtonEditor(new JCheckBox()));

        // Center align columns
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 1; i <= 8; i++) {
            resultsTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        JScrollPane scrollPane = new JScrollPane(resultsTable);
        scrollPane.setBorder(new LineBorder(UiUtils.COLOR_BORDER, 1));

        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setBackground(UiUtils.COLOR_BG);

        statusLabel.setFont(UiUtils.FONT_SUBTITLE);
        statusLabel.setForeground(UiUtils.COLOR_PRIMARY);
        centerPanel.add(statusLabel, BorderLayout.NORTH);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        loadCities();
        performSearch(); // Initial search for sample routes
    }

    private JPanel createSearchFilterCard() {
        JPanel card = new JPanel(new BorderLayout(12, 12));
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(new LineBorder(UiUtils.COLOR_BORDER, 1), new EmptyBorder(16, 20, 16, 20)));

        JLabel title = new JLabel("Search & Book Bus Tickets", IconUtils.getSearchIcon(22, UiUtils.COLOR_PRIMARY), SwingConstants.LEFT);
        title.setIconTextGap(10);
        title.setFont(UiUtils.FONT_TITLE);
        title.setForeground(UiUtils.COLOR_PRIMARY);
        card.add(title, BorderLayout.NORTH);

        JPanel filters = new JPanel(new GridLayout(1, 4, 15, 0));
        filters.setBackground(Color.WHITE);

        // Source City
        JPanel srcPanel = new JPanel(new BorderLayout(4, 4));
        srcPanel.setBackground(Color.WHITE);
        JLabel srcLabel = new JLabel("From (Source City):", IconUtils.getLocationPinIcon(14, UiUtils.COLOR_PRIMARY), SwingConstants.LEFT);
        srcLabel.setIconTextGap(6);
        srcPanel.add(srcLabel, BorderLayout.NORTH);
        srcPanel.add(sourceCombo, BorderLayout.CENTER);

        // Destination City
        JPanel dstPanel = new JPanel(new BorderLayout(4, 4));
        dstPanel.setBackground(Color.WHITE);
        JLabel dstLabel = new JLabel("To (Destination City):", IconUtils.getDestinationPinIcon(14, UiUtils.COLOR_ACCENT), SwingConstants.LEFT);
        dstLabel.setIconTextGap(6);
        dstPanel.add(dstLabel, BorderLayout.NORTH);
        dstPanel.add(destCombo, BorderLayout.CENTER);

        // Instant validation listener on city selection
        sourceCombo.addItemListener(e -> onCitySelectionChanged());
        destCombo.addItemListener(e -> onCitySelectionChanged());

        // Travel Date - Interactive Calendar Picker
        JPanel datePanel = new JPanel(new BorderLayout(4, 4));
        datePanel.setBackground(Color.WHITE);
        JLabel dateLabel = new JLabel("Travel Date:", IconUtils.getCalendarIcon(14, UiUtils.COLOR_PRIMARY), SwingConstants.LEFT);
        dateLabel.setIconTextGap(6);
        datePanel.add(dateLabel, BorderLayout.NORTH);

        JPanel dateInputPanel = new JPanel(new BorderLayout(4, 0));
        dateInputPanel.setBackground(Color.WHITE);

        dateField.setEditable(false);
        dateField.setBackground(Color.WHITE);
        dateField.setFont(UiUtils.FONT_BODY);
        dateField.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        updateDateField();

        JButton calendarBtn = UiUtils.createSecondaryButton("");
        calendarBtn.setIcon(IconUtils.getCalendarIcon(16, UiUtils.COLOR_PRIMARY));
        calendarBtn.setToolTipText("Open Calendar Date Picker");
        calendarBtn.setPreferredSize(new Dimension(38, 32));

        Runnable openCalendar = () -> {
            CalendarDatePickerDialog dialog = new CalendarDatePickerDialog(parentFrame, selectedTravelDate, date -> {
                selectedTravelDate = date;
                updateDateField();
                performSearch();
            });
            dialog.setVisible(true);
        };

        calendarBtn.addActionListener(e -> openCalendar.run());
        dateField.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                openCalendar.run();
            }
        });

        dateInputPanel.add(dateField, BorderLayout.CENTER);
        dateInputPanel.add(calendarBtn, BorderLayout.EAST);
        datePanel.add(dateInputPanel, BorderLayout.CENTER);

        // Search Button
        JPanel btnPanel = new JPanel(new BorderLayout(4, 4));
        btnPanel.setBackground(Color.WHITE);
        btnPanel.add(new JLabel(" "), BorderLayout.NORTH);
        searchBtn = UiUtils.createPrimaryButton("Search Buses");
        searchBtn.setIcon(IconUtils.getSearchIcon(16, Color.WHITE));
        searchBtn.setIconTextGap(8);
        searchBtn.addActionListener(e -> performSearch());
        btnPanel.add(searchBtn, BorderLayout.CENTER);

        filters.add(srcPanel);
        filters.add(dstPanel);
        filters.add(datePanel);
        filters.add(btnPanel);

        card.add(filters, BorderLayout.CENTER);
        return card;
    }

    private void onCitySelectionChanged() {
        String src = (String) sourceCombo.getSelectedItem();
        String dst = (String) destCombo.getSelectedItem();
        boolean isAllSrc = src == null || "All Cities".equalsIgnoreCase(src);
        boolean isAllDst = dst == null || "All Cities".equalsIgnoreCase(dst);

        if (!isAllSrc && !isAllDst && src.trim().equalsIgnoreCase(dst.trim())) {
            statusLabel.setForeground(UiUtils.COLOR_DANGER);
            statusLabel.setText("⚠ Invalid Route: Source and Destination cannot be the same city (" + src + ").");
            tableModel.setRowCount(0);
            currentResults.clear();
            if (searchBtn != null) searchBtn.setEnabled(false);
        } else {
            if (searchBtn != null) searchBtn.setEnabled(true);
            statusLabel.setForeground(UiUtils.COLOR_PRIMARY);
            statusLabel.setText("Select cities and date, then click 'Search Buses'");
        }
    }

    private void updateDateField() {
        LocalDate today = LocalDate.now();
        DateTimeFormatter df = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy");
        String prefix = selectedTravelDate.equals(today) ? "Today - " :
                (selectedTravelDate.equals(today.plusDays(1)) ? "Tomorrow - " : "");
        dateField.setText(prefix + selectedTravelDate.format(df));
    }

    private void loadCities() {
        sourceCombo.addItem("All Cities");
        sourceCombo.addItem("Delhi");
        sourceCombo.addItem("Jaipur");
        sourceCombo.addItem("Mumbai");
        sourceCombo.addItem("Pune");
        sourceCombo.addItem("Bangalore");
        sourceCombo.addItem("Hyderabad");

        destCombo.addItem("All Cities");
        destCombo.addItem("Jaipur");
        destCombo.addItem("Delhi");
        destCombo.addItem("Pune");
        destCombo.addItem("Mumbai");
        destCombo.addItem("Hyderabad");
        destCombo.addItem("Bangalore");

        // Fetch dynamic distinct cities from backend in background
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            List<String> sources;
            List<String> dests;

            @Override
            protected Void doInBackground() throws Exception {
                sources = ApiClient.getInstance().getSourceCities();
                dests = ApiClient.getInstance().getDestinationCities();
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    if (sources != null && !sources.isEmpty()) {
                        for (String s : sources) {
                            if (!containsItem(sourceCombo, s)) sourceCombo.addItem(s);
                        }
                    }
                    if (dests != null && !dests.isEmpty()) {
                        for (String d : dests) {
                            if (!containsItem(destCombo, d)) destCombo.addItem(d);
                        }
                    }
                } catch (Exception ignored) {
                }
            }
        };
        worker.execute();
    }

    private boolean containsItem(JComboBox<String> combo, String item) {
        for (int i = 0; i < combo.getItemCount(); i++) {
            if (item.equalsIgnoreCase(combo.getItemAt(i))) return true;
        }
        return false;
    }

    public void performSearch() {
        String src = (String) sourceCombo.getSelectedItem();
        String dst = (String) destCombo.getSelectedItem();
        boolean isAllSrc = src == null || "All Cities".equalsIgnoreCase(src);
        boolean isAllDst = dst == null || "All Cities".equalsIgnoreCase(dst);

        // Validation: Source and Destination cannot be the same city
        if (!isAllSrc && !isAllDst && src.trim().equalsIgnoreCase(dst.trim())) {
            statusLabel.setForeground(UiUtils.COLOR_DANGER);
            statusLabel.setText("⚠ Invalid Route: Source and Destination cities cannot be the same!");
            tableModel.setRowCount(0);
            currentResults.clear();
            JOptionPane.showMessageDialog(this,
                    "Source city and Destination city cannot be the same.\nPlease select different cities.",
                    "Invalid Route Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        final String finalSrc = isAllSrc ? null : src;
        final String finalDst = isAllDst ? null : dst;
        final LocalDate travelDate = selectedTravelDate != null ? selectedTravelDate : LocalDate.now().plusDays(1);

        statusLabel.setForeground(UiUtils.COLOR_PRIMARY);
        statusLabel.setText("Searching available buses...");

        SwingWorker<List<ScheduleModel>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<ScheduleModel> doInBackground() throws Exception {
                return ApiClient.getInstance().searchSchedules(finalSrc, finalDst, travelDate);
            }

            @Override
            protected void done() {
                try {
                    List<ScheduleModel> schedules = get();
                    currentResults.clear();
                    tableModel.setRowCount(0);

                    if (schedules != null && !schedules.isEmpty()) {
                        currentResults.addAll(schedules);
                        for (ScheduleModel s : schedules) {
                            tableModel.addRow(new Object[]{
                                    s.getBusName() + "\n(" + s.getBusNumber() + ")",
                                    s.getSourceCity() != null ? s.getSourceCity() : "Delhi",
                                    s.getDestinationCity() != null ? s.getDestinationCity() : "Jaipur",
                                    s.getBusType(),
                                    s.getDepartureTime().format(DateTimeFormatter.ofPattern("hh:mm a")),
                                    s.getArrivalTime().format(DateTimeFormatter.ofPattern("hh:mm a")),
                                    s.getEstimatedDurationHours() + " hrs",
                                    s.getAvailableSeats() + " / " + s.getTotalSeats(),
                                    UiUtils.FORMAT_CURRENCY.format(s.getBaseFare()),
                                    "Select Seats"
                            });
                        }
                        statusLabel.setForeground(UiUtils.COLOR_PRIMARY);
                        statusLabel.setText("Found " + schedules.size() + " bus service(s) for the selected date");
                    } else {
                        statusLabel.setText("No buses found for " + (isAllSrc ? "all routes" : (src + " to " + dst)) + " on " + travelDate + ". Try another date or route.");
                    }
                } catch (Exception ex) {
                    String msg = ex.getMessage();
                    if (msg != null && (msg.contains("ConnectException") || msg.contains("Failed to connect"))) {
                        statusLabel.setText("Connecting to backend service... (Make sure Spring Boot is running on port 8080)");
                    } else {
                        statusLabel.setText("Notice: " + (msg != null ? msg : "No connection to server"));
                    }
                }
            }
        };
        worker.execute();
    }

    // Button Renderer for Table
    private static class ButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
            setFont(UiUtils.FONT_BUTTON);
            setBackground(UiUtils.COLOR_PRIMARY);
            setForeground(Color.WHITE);
            setIcon(IconUtils.getSeatIcon(14, Color.WHITE));
            setIconTextGap(6);
            setFocusPainted(false);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "Select Seats" : value.toString());
            return this;
        }
    }

    // Button Editor for Table
    private class ButtonEditor extends DefaultCellEditor {
        private final JButton button;
        private int selectedRow;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.setFont(UiUtils.FONT_BUTTON);
            button.setBackground(UiUtils.COLOR_PRIMARY);
            button.setForeground(Color.WHITE);
            button.setIcon(IconUtils.getSeatIcon(14, Color.WHITE));
            button.setIconTextGap(6);
            button.setFocusPainted(false);
            button.addActionListener(e -> fireEditingStopped());
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            this.selectedRow = row;
            button.setText("Select Seats");
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (selectedRow >= 0 && selectedRow < currentResults.size()) {
                ScheduleModel selected = currentResults.get(selectedRow);
                openSeatSelection(selected);
            }
            return "Select Seats";
        }
    }

    private void openSeatSelection(ScheduleModel schedule) {
        SwingWorker<ScheduleModel, Void> worker = new SwingWorker<>() {
            @Override
            protected ScheduleModel doInBackground() throws Exception {
                return ApiClient.getInstance().getScheduleSeats(schedule.getId());
            }

            @Override
            protected void done() {
                try {
                    ScheduleModel detailed = get();
                    SeatSelectionDialog dialog = new SeatSelectionDialog(parentFrame, detailed);
                    dialog.setVisible(true);
                    performSearch(); // Refresh available seats after booking/locking
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(parentFrame, "Could not load seat map: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
}
