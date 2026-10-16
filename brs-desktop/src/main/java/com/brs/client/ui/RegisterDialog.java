package com.brs.client.ui;

import com.brs.client.api.ApiClient;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class RegisterDialog extends JDialog {

    private final JTextField usernameField = new JTextField(20);
    private final JTextField emailField = new JTextField(20);
    private final JTextField fullNameField = new JTextField(20);
    private final JTextField phoneField = new JTextField(20);
    private final JPasswordField passwordField = new JPasswordField(20);
    private final JLabel errorLabel = new JLabel();

    public RegisterDialog(Frame parent) {
        super(parent, "Create New Account - Bus Reservation System", true);
        initUi();
    }

    private void initUi() {
        setSize(440, 480);
        setLocationRelativeTo(getParent());
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(new EmptyBorder(25, 30, 25, 30));
        mainPanel.setBackground(Color.WHITE);

        // Header
        JPanel headerPanel = new JPanel(new GridLayout(2, 1, 4, 4));
        headerPanel.setBackground(Color.WHITE);
        JLabel titleLabel = new JLabel("Register New Account", SwingConstants.CENTER);
        titleLabel.setFont(UiUtils.FONT_TITLE);
        titleLabel.setForeground(UiUtils.COLOR_PRIMARY);

        JLabel subLabel = new JLabel("Fill in your details to book bus tickets easily", SwingConstants.CENTER);
        subLabel.setFont(UiUtils.FONT_SMALL);
        subLabel.setForeground(Color.GRAY);

        headerPanel.add(titleLabel);
        headerPanel.add(subLabel);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Form Fields
        JPanel formPanel = new JPanel(new GridLayout(10, 1, 4, 4));
        formPanel.setBackground(Color.WHITE);

        formPanel.add(new JLabel("Full Name:"));
        formPanel.add(fullNameField);

        formPanel.add(new JLabel("Username:"));
        formPanel.add(usernameField);

        formPanel.add(new JLabel("Email Address:"));
        formPanel.add(emailField);

        formPanel.add(new JLabel("Phone Number:"));
        formPanel.add(phoneField);

        formPanel.add(new JLabel("Password (min 6 characters):"));
        formPanel.add(passwordField);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // Footer Actions
        JPanel footerPanel = new JPanel(new BorderLayout(10, 10));
        footerPanel.setBackground(Color.WHITE);

        errorLabel.setForeground(UiUtils.COLOR_DANGER);
        errorLabel.setFont(UiUtils.FONT_SMALL);
        errorLabel.setHorizontalAlignment(SwingConstants.CENTER);
        footerPanel.add(errorLabel, BorderLayout.NORTH);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        btnPanel.setBackground(Color.WHITE);

        JButton registerBtn = UiUtils.createPrimaryButton("Register Account");
        JButton cancelBtn = UiUtils.createSecondaryButton("Cancel");

        registerBtn.addActionListener(e -> performRegister());
        cancelBtn.addActionListener(e -> dispose());

        btnPanel.add(cancelBtn);
        btnPanel.add(registerBtn);
        footerPanel.add(btnPanel, BorderLayout.CENTER);

        mainPanel.add(footerPanel, BorderLayout.SOUTH);
        setContentPane(mainPanel);
    }

    private void performRegister() {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String fullName = fullNameField.getText().trim();
        String phone = phoneField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || email.isEmpty() || fullName.isEmpty() || phone.isEmpty() || password.isEmpty()) {
            errorLabel.setText("All fields are required.");
            return;
        }

        errorLabel.setText("Creating account...");
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                ApiClient.getInstance().register(username, email, password, fullName, phone);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    JOptionPane.showMessageDialog(getParent(),
                            "Registration successful! You can now sign in.",
                            "Account Created", JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                    Frame parent = (Frame) getParent();
                    new LoginDialog(parent, () -> {
                        if (parent instanceof MainFrame) {
                            ((MainFrame) parent).updateAuthState();
                        }
                    }).setVisible(true);
                } catch (Exception ex) {
                    errorLabel.setText(ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage());
                }
            }
        };
        worker.execute();
    }
}
