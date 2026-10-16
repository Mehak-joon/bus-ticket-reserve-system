package com.brs.client.ui;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class UiUtils {

    public static final Color COLOR_PRIMARY = new Color(26, 54, 93);      // Deep Navy
    public static final Color COLOR_SECONDARY = new Color(43, 108, 176);  // Ocean Blue
    public static final Color COLOR_ACCENT = new Color(221, 107, 32);     // Amber Orange
    public static final Color COLOR_SUCCESS = new Color(47, 133, 90);     // Forest Green
    public static final Color COLOR_DANGER = new Color(197, 48, 48);      // Deep Red
    public static final Color COLOR_BG = new Color(247, 250, 252);        // Light Slate
    public static final Color COLOR_CARD = Color.WHITE;
    public static final Color COLOR_BORDER = new Color(226, 232, 240);

    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_BUTTON = new Font("Segoe UI", Font.BOLD, 13);

    public static final DateTimeFormatter FORMAT_DATETIME = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");
    public static final NumberFormat FORMAT_CURRENCY = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

    public static JButton createPrimaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(FONT_BUTTON);
        button.setBackground(COLOR_PRIMARY);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(new CompoundBorder(
                new LineBorder(COLOR_PRIMARY, 1, true),
                new EmptyBorder(8, 16, 8, 16)
        ));
        return button;
    }

    public static JButton createSecondaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(FONT_BUTTON);
        button.setBackground(Color.WHITE);
        button.setForeground(COLOR_PRIMARY);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(new CompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(8, 16, 8, 16)
        ));
        return button;
    }

    public static JButton createDangerButton(String text) {
        JButton button = new JButton(text);
        button.setFont(FONT_BUTTON);
        button.setBackground(COLOR_DANGER);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(new CompoundBorder(
                new LineBorder(COLOR_DANGER, 1, true),
                new EmptyBorder(6, 12, 6, 12)
        ));
        return button;
    }

    public static JPanel createCardPanel() {
        JPanel card = new JPanel();
        card.setBackground(COLOR_CARD);
        card.setBorder(new CompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(16, 16, 16, 16)
        ));
        return card;
    }

    public static JLabel createBadge(String text, Color bg, Color fg) {
        JLabel badge = new JLabel(text, SwingConstants.CENTER);
        badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        badge.setOpaque(true);
        badge.setBackground(bg);
        badge.setForeground(fg);
        badge.setBorder(new EmptyBorder(4, 8, 4, 8));
        return badge;
    }
}
