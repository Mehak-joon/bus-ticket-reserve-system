package com.brs.client.ui;

import com.brs.client.api.ApiClient;
import com.brs.client.model.UserSession;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LoginDialog extends JDialog {

    private final JTextField usernameField = new JTextField(20);
    private final JPasswordField passwordField = new JPasswordField(20);
    private final JLabel errorLabel = new JLabel();
    private boolean loginSuccessful = false;
    private final Runnable onLoginSuccess;

    public LoginDialog(Frame parent, Runnable onLoginSuccess) {
        super(parent, "Sign In to Bus Reservation System", true);
        this.onLoginSuccess = onLoginSuccess;
        initUi();
    }

    private void initUi() {
        setSize(400, 340);
        setLocationRelativeTo(getParent());
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(new EmptyBorder(25, 30, 25, 30));
        mainPanel.setBackground(Color.WHITE);

        // Header
        JPanel headerPanel = new JPanel(new GridLayout(2, 1, 4, 4));
        headerPanel.setBackground(Color.WHITE);
        JLabel titleLabel = new JLabel("Welcome Back", IconUtils.getBusIcon(22, UiUtils.COLOR_PRIMARY), SwingConstants.CENTER);
        titleLabel.setIconTextGap(8);
        titleLabel.setFont(UiUtils.FONT_TITLE);
        titleLabel.setForeground(UiUtils.COLOR_PRIMARY);

        JLabel subLabel = new JLabel("Please enter your credentials to continue", SwingConstants.CENTER);
        subLabel.setFont(UiUtils.FONT_SMALL);
        subLabel.setForeground(Color.GRAY);

        headerPanel.add(titleLabel);
        headerPanel.add(subLabel);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Form Fields with Vector Icons
        JPanel formPanel = new JPanel(new GridLayout(4, 1, 8, 8));
        formPanel.setBackground(Color.WHITE);

        JLabel uLabel = new JLabel("Username or Email:", IconUtils.getUserIcon(14, UiUtils.COLOR_PRIMARY), SwingConstants.LEFT);
        uLabel.setIconTextGap(6);
        uLabel.setFont(UiUtils.FONT_BODY);
        formPanel.add(uLabel);
        formPanel.add(usernameField);

        JLabel pLabel = new JLabel("Password:", IconUtils.getShieldLockIcon(14, UiUtils.COLOR_PRIMARY), SwingConstants.LEFT);
        pLabel.setIconTextGap(6);
        pLabel.setFont(UiUtils.FONT_BODY);
        formPanel.add(pLabel);
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

        JButton loginBtn = UiUtils.createPrimaryButton("Sign In");
        loginBtn.setIcon(IconUtils.getLoginIcon(14, Color.WHITE));
        loginBtn.setIconTextGap(6);

        JButton registerLinkBtn = UiUtils.createSecondaryButton("Create Account");
        registerLinkBtn.setIcon(IconUtils.getPlusIcon(12, UiUtils.COLOR_PRIMARY));
        registerLinkBtn.setIconTextGap(6);

        loginBtn.addActionListener(e -> performLogin());
        registerLinkBtn.addActionListener(e -> {
            dispose();
            new RegisterDialog((Frame) getParent()).setVisible(true);
        });

        // Press Enter to submit
        passwordField.addActionListener(e -> performLogin());
        usernameField.addActionListener(e -> performLogin());

        btnPanel.add(registerLinkBtn);
        btnPanel.add(loginBtn);
        footerPanel.add(btnPanel, BorderLayout.CENTER);

        mainPanel.add(footerPanel, BorderLayout.SOUTH);
        setContentPane(mainPanel);
    }

    private void performLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please enter both username and password.");
            return;
        }

        errorLabel.setText("Signing in...");
        SwingWorker<UserSession, Void> worker = new SwingWorker<>() {
            @Override
            protected UserSession doInBackground() throws Exception {
                return ApiClient.getInstance().login(username, password);
            }

            @Override
            protected void done() {
                try {
                    UserSession session = get();
                    loginSuccessful = true;
                    dispose();
                    if (onLoginSuccess != null) {
                        onLoginSuccess.run();
                    }
                    JOptionPane.showMessageDialog(getParent(),
                            "Welcome back, " + session.getUsername() + " (" + session.getRole() + ")!",
                            "Sign In Successful", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    errorLabel.setText(ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    public boolean isLoginSuccessful() {
        return loginSuccessful;
    }
}
