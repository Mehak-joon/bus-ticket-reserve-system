package com.brs.client.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

/**
 * Modern, interactive Calendar Date Picker Dialog for BRS.
 * Allows passengers to pick any valid travel date with visual month navigation,
 * quick presets (Today, Tomorrow), and automatic past-date disabling.
 */
public class CalendarDatePickerDialog extends JDialog {

    private final Consumer<LocalDate> onDateSelected;
    private YearMonth currentYearMonth;
    private LocalDate selectedDate;
    private final LocalDate minDate;

    private JLabel monthYearLabel;
    private JPanel daysGridPanel;

    public CalendarDatePickerDialog(Window owner, LocalDate initialDate, Consumer<LocalDate> onDateSelected) {
        super(owner, "Select Travel Date", ModalityType.APPLICATION_MODAL);
        this.selectedDate = initialDate != null ? initialDate : LocalDate.now();
        this.minDate = LocalDate.now();
        this.currentYearMonth = YearMonth.from(this.selectedDate);
        this.onDateSelected = onDateSelected;

        initUi();
        buildCalendarDays();
    }

    private void initUi() {
        setSize(360, 420);
        setLocationRelativeTo(getOwner());
        setResizable(false);

        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setBackground(Color.WHITE);
        content.setBorder(new EmptyBorder(16, 18, 16, 18));

        // 1. Month/Year Header with Navigation
        JPanel headerPanel = new JPanel(new BorderLayout(8, 8));
        headerPanel.setBackground(Color.WHITE);

        JButton prevBtn = new JButton("◀");
        prevBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        prevBtn.setFocusPainted(false);
        prevBtn.setBackground(new Color(241, 245, 249));
        prevBtn.setBorder(new LineBorder(UiUtils.COLOR_BORDER, 1));
        prevBtn.setPreferredSize(new Dimension(38, 30));
        prevBtn.addActionListener(e -> {
            YearMonth prev = currentYearMonth.minusMonths(1);
            if (!prev.isBefore(YearMonth.from(minDate))) {
                currentYearMonth = prev;
                updateHeaderAndDays();
            }
        });

        JButton nextBtn = new JButton("▶");
        nextBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        nextBtn.setFocusPainted(false);
        nextBtn.setBackground(new Color(241, 245, 249));
        nextBtn.setBorder(new LineBorder(UiUtils.COLOR_BORDER, 1));
        nextBtn.setPreferredSize(new Dimension(38, 30));
        nextBtn.addActionListener(e -> {
            currentYearMonth = currentYearMonth.plusMonths(1);
            updateHeaderAndDays();
        });

        monthYearLabel = new JLabel("", SwingConstants.CENTER);
        monthYearLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        monthYearLabel.setForeground(UiUtils.COLOR_PRIMARY);

        headerPanel.add(prevBtn, BorderLayout.WEST);
        headerPanel.add(monthYearLabel, BorderLayout.CENTER);
        headerPanel.add(nextBtn, BorderLayout.EAST);

        content.add(headerPanel, BorderLayout.NORTH);

        // 2. Day-of-Week headers + Days grid
        JPanel calendarCenter = new JPanel(new BorderLayout(5, 5));
        calendarCenter.setBackground(Color.WHITE);

        JPanel dowPanel = new JPanel(new GridLayout(1, 7, 4, 4));
        dowPanel.setBackground(new Color(248, 250, 252));
        dowPanel.setBorder(new EmptyBorder(6, 0, 6, 0));
        String[] daysOfWeek = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        for (String dow : daysOfWeek) {
            JLabel lbl = new JLabel(dow, SwingConstants.CENTER);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
            lbl.setForeground(dow.equals("Sun") ? UiUtils.COLOR_DANGER : Color.GRAY);
            dowPanel.add(lbl);
        }
        calendarCenter.add(dowPanel, BorderLayout.NORTH);

        daysGridPanel = new JPanel(new GridLayout(6, 7, 4, 4));
        daysGridPanel.setBackground(Color.WHITE);
        calendarCenter.add(daysGridPanel, BorderLayout.CENTER);

        content.add(calendarCenter, BorderLayout.CENTER);

        // 3. Quick presets: Today, Tomorrow, Cancel
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
        footer.setBackground(Color.WHITE);

        JButton todayBtn = new JButton("Today");
        todayBtn.setFont(UiUtils.FONT_SMALL);
        todayBtn.setFocusPainted(false);
        todayBtn.addActionListener(e -> selectAndClose(LocalDate.now()));

        JButton tomorrowBtn = new JButton("Tomorrow");
        tomorrowBtn.setFont(UiUtils.FONT_SMALL);
        tomorrowBtn.setFocusPainted(false);
        tomorrowBtn.addActionListener(e -> selectAndClose(LocalDate.now().plusDays(1)));

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setFont(UiUtils.FONT_SMALL);
        cancelBtn.setFocusPainted(false);
        cancelBtn.addActionListener(e -> dispose());

        footer.add(todayBtn);
        footer.add(tomorrowBtn);
        footer.add(cancelBtn);

        content.add(footer, BorderLayout.SOUTH);

        setContentPane(content);
    }

    private void updateHeaderAndDays() {
        monthYearLabel.setText(currentYearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")));
        buildCalendarDays();
    }

    private void buildCalendarDays() {
        monthYearLabel.setText(currentYearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")));
        daysGridPanel.removeAll();

        LocalDate firstOfMonth = currentYearMonth.atDay(1);
        int dayOfWeekFirst = firstOfMonth.getDayOfWeek().getValue(); // 1 = Monday, 7 = Sunday
        int lengthOfMonth = currentYearMonth.lengthOfMonth();

        // Empty filler buttons before the 1st
        for (int i = 1; i < dayOfWeekFirst; i++) {
            JLabel empty = new JLabel("");
            daysGridPanel.add(empty);
        }

        // Days of the month
        for (int day = 1; day <= lengthOfMonth; day++) {
            LocalDate date = currentYearMonth.atDay(day);
            JButton dayBtn = new JButton(String.valueOf(day));
            dayBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            dayBtn.setMargin(new Insets(2, 2, 2, 2));
            dayBtn.setFocusPainted(false);

            boolean isPast = date.isBefore(minDate);
            boolean isSelected = date.equals(selectedDate);
            boolean isToday = date.equals(LocalDate.now());

            if (isPast) {
                dayBtn.setEnabled(false);
                dayBtn.setForeground(new Color(203, 213, 225));
                dayBtn.setBackground(new Color(248, 250, 252));
                dayBtn.setBorder(new LineBorder(new Color(241, 245, 249), 1));
            } else if (isSelected) {
                dayBtn.setBackground(UiUtils.COLOR_PRIMARY);
                dayBtn.setForeground(Color.WHITE);
                dayBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
                dayBtn.setBorder(new LineBorder(UiUtils.COLOR_PRIMARY, 1));
            } else if (isToday) {
                dayBtn.setBackground(new Color(224, 242, 254));
                dayBtn.setForeground(UiUtils.COLOR_PRIMARY);
                dayBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
                dayBtn.setBorder(new LineBorder(UiUtils.COLOR_PRIMARY, 1));
            } else {
                dayBtn.setBackground(Color.WHITE);
                dayBtn.setForeground(Color.DARK_GRAY);
                dayBtn.setBorder(new LineBorder(UiUtils.COLOR_BORDER, 1));
            }

            if (!isPast) {
                dayBtn.addActionListener(e -> selectAndClose(date));
            }

            daysGridPanel.add(dayBtn);
        }

        // Fill remaining cells in the 6x7 grid (42 cells total)
        int totalCells = (dayOfWeekFirst - 1) + lengthOfMonth;
        for (int i = totalCells; i < 42; i++) {
            JLabel empty = new JLabel("");
            daysGridPanel.add(empty);
        }

        daysGridPanel.revalidate();
        daysGridPanel.repaint();
    }

    private void selectAndClose(LocalDate date) {
        this.selectedDate = date;
        if (onDateSelected != null) {
            onDateSelected.accept(date);
        }
        dispose();
    }
}
