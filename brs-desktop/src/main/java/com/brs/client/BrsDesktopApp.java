package com.brs.client;

import com.brs.client.ui.MainFrame;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.awt.*;

public class BrsDesktopApp {

    public static void main(String[] args) {
        // Set modern FlatLaf Look and Feel
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
            // Custom UI Polish
            UIManager.put("Button.arc", 8);
            UIManager.put("Component.arc", 8);
            UIManager.put("TextComponent.arc", 8);
            UIManager.put("ScrollBar.showButtons", true);
            UIManager.put("TabbedPane.showTabSeparators", true);
        } catch (Exception e) {
            System.err.println("Failed to initialize FlatLaf look and feel: " + e.getMessage());
        }

        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
