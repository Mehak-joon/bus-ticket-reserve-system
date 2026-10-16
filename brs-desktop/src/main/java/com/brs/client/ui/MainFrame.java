package com.brs.client.ui;

import com.brs.client.api.ApiClient;
import com.brs.client.model.UserSession;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

public class MainFrame extends JFrame {

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentCards = new JPanel(cardLayout);

    private BusSearchPanel busSearchPanel;
    private MyBookingsPanel myBookingsPanel;
    private AdminDashboardPanel adminDashboardPanel;

    private JLabel userStatusLabel;
    private JButton authButton;
    private JButton navSearchBtn;
    private JButton navBookingsBtn;
    private JButton navAdminBtn;

    public MainFrame() {
        super("Bus Reservation System - Desktop Client");
        initUi();
    }

    private void initUi() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 780);
        setMinimumSize(new Dimension(1000, 650));
        setLocationRelativeTo(null);
        setIconImage(IconUtils.getAppImage(64));

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UiUtils.COLOR_BG);

        // 1. Navigation Header Bar
        JPanel navBar = createNavigationBar();
        root.add(navBar, BorderLayout.NORTH);

        // 2. Center Card Panels
        busSearchPanel = new BusSearchPanel(this);
        myBookingsPanel = new MyBookingsPanel(this);
        adminDashboardPanel = new AdminDashboardPanel(this);

        contentCards.add(busSearchPanel, "SEARCH");
        contentCards.add(myBookingsPanel, "BOOKINGS");
        contentCards.add(adminDashboardPanel, "ADMIN");

        root.add(contentCards, BorderLayout.CENTER);
        setContentPane(root);

        updateAuthState();
    }

    private JPanel createNavigationBar() {
        JPanel navBar = new JPanel(new BorderLayout(15, 0));
        navBar.setBackground(UiUtils.COLOR_PRIMARY);
        navBar.setBorder(new EmptyBorder(12, 24, 12, 24));

        // Brand / Logo
        JPanel brandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        brandPanel.setOpaque(false);

        JLabel logoIcon = new JLabel(IconUtils.getBusIcon(26, new Color(254, 240, 138)));

        JLabel brandTitle = new JLabel("BRS EXPRESS");
        brandTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        brandTitle.setForeground(Color.WHITE);

        brandPanel.add(logoIcon);
        brandPanel.add(brandTitle);
        navBar.add(brandPanel, BorderLayout.WEST);

        // Center Navigation Tabs
        JPanel tabsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        tabsPanel.setOpaque(false);

        navSearchBtn = createNavButton("Search Buses", IconUtils.getSearchIcon(16, Color.WHITE), true);
        navBookingsBtn = createNavButton("My Bookings", IconUtils.getTicketIcon(16, Color.WHITE), false);
        navAdminBtn = createNavButton("Admin Dashboard", IconUtils.getSettingsIcon(16, Color.WHITE), false);

        navSearchBtn.addActionListener(e -> {
            setActiveNav(navSearchBtn);
            cardLayout.show(contentCards, "SEARCH");
        });

        navBookingsBtn.addActionListener(e -> {
            if (!ApiClient.getInstance().isLoggedIn()) {
                openLoginDialog(() -> {
                    setActiveNav(navBookingsBtn);
                    cardLayout.show(contentCards, "BOOKINGS");
                    myBookingsPanel.refreshBookings();
                });
            } else {
                setActiveNav(navBookingsBtn);
                cardLayout.show(contentCards, "BOOKINGS");
                myBookingsPanel.refreshBookings();
            }
        });

        navAdminBtn.addActionListener(e -> {
            if (!ApiClient.getInstance().isLoggedIn() || !ApiClient.getInstance().isAdmin()) {
                int opt = JOptionPane.showConfirmDialog(this,
                        "Admin privileges required. Would you like to sign in as Administrator?",
                        "Admin Authentication", JOptionPane.YES_NO_OPTION);
                if (opt == JOptionPane.YES_OPTION) {
                    openLoginDialog(() -> {
                        if (ApiClient.getInstance().isAdmin()) {
                            setActiveNav(navAdminBtn);
                            cardLayout.show(contentCards, "ADMIN");
                            adminDashboardPanel.refreshAllAdminData();
                        } else {
                            JOptionPane.showMessageDialog(this, "Your account does not have Admin privileges.", "Access Denied", JOptionPane.WARNING_MESSAGE);
                        }
                    });
                }
            } else {
                setActiveNav(navAdminBtn);
                cardLayout.show(contentCards, "ADMIN");
                adminDashboardPanel.refreshAllAdminData();
            }
        });

        tabsPanel.add(navSearchBtn);
        tabsPanel.add(navBookingsBtn);
        tabsPanel.add(navAdminBtn);
        navBar.add(tabsPanel, BorderLayout.CENTER);

        // Right User Account Panel
        JPanel accountPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        accountPanel.setOpaque(false);

        userStatusLabel = new JLabel("Guest");
        userStatusLabel.setFont(UiUtils.FONT_BODY);
        userStatusLabel.setForeground(new Color(226, 232, 240));

        authButton = new JButton("Sign In");
        authButton.setFont(UiUtils.FONT_BUTTON);
        authButton.setBackground(UiUtils.COLOR_SECONDARY);
        authButton.setForeground(Color.WHITE);
        authButton.setFocusPainted(false);
        authButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        authButton.setBorder(new CompoundBorder(new LineBorder(UiUtils.COLOR_SECONDARY, 1, true), new EmptyBorder(6, 14, 6, 14)));

        authButton.addActionListener(e -> {
            if (ApiClient.getInstance().isLoggedIn()) {
                int opt = JOptionPane.showConfirmDialog(this, "Are you sure you want to sign out?", "Sign Out", JOptionPane.YES_NO_OPTION);
                if (opt == JOptionPane.YES_OPTION) {
                    ApiClient.getInstance().logout();
                    updateAuthState();
                    setActiveNav(navSearchBtn);
                    cardLayout.show(contentCards, "SEARCH");
                }
            } else {
                openLoginDialog(this::updateAuthState);
            }
        });

        accountPanel.add(userStatusLabel);
        accountPanel.add(authButton);
        navBar.add(accountPanel, BorderLayout.EAST);

        return navBar;
    }

    private JButton createNavButton(String title, Icon icon, boolean active) {
        JButton btn = new JButton(title, icon);
        btn.setIconTextGap(8);
        btn.setFont(UiUtils.FONT_BUTTON);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        setNavButtonActive(btn, active);
        return btn;
    }

    private void setNavButtonActive(JButton btn, boolean active) {
        if (active) {
            btn.setBackground(UiUtils.COLOR_SECONDARY);
            btn.setForeground(Color.WHITE);
            btn.setBorder(new CompoundBorder(new LineBorder(UiUtils.COLOR_SECONDARY, 1, true), new EmptyBorder(8, 16, 8, 16)));
        } else {
            btn.setBackground(UiUtils.COLOR_PRIMARY);
            btn.setForeground(new Color(203, 213, 225));
            btn.setBorder(new CompoundBorder(new LineBorder(UiUtils.COLOR_PRIMARY, 1, true), new EmptyBorder(8, 16, 8, 16)));
        }
    }

    private void setActiveNav(JButton activeBtn) {
        setNavButtonActive(navSearchBtn, activeBtn == navSearchBtn);
        setNavButtonActive(navBookingsBtn, activeBtn == navBookingsBtn);
        setNavButtonActive(navAdminBtn, activeBtn == navAdminBtn);
    }

    private void openLoginDialog(Runnable onDone) {
        LoginDialog dialog = new LoginDialog(this, () -> {
            updateAuthState();
            if (onDone != null) onDone.run();
        });
        dialog.setVisible(true);
    }

    public void updateAuthState() {
        if (ApiClient.getInstance().isLoggedIn()) {
            UserSession session = ApiClient.getInstance().getCurrentSession();
            userStatusLabel.setIcon(IconUtils.getUserIcon(16, new Color(226, 232, 240)));
            userStatusLabel.setText(session.getUsername() + " (" + (session.isAdmin() ? "Admin" : "Passenger") + ")");
            userStatusLabel.setIconTextGap(6);
            authButton.setText("Sign Out");
            authButton.setIcon(IconUtils.getLogoutIcon(14, Color.WHITE));
            authButton.setIconTextGap(6);
            authButton.setBackground(UiUtils.COLOR_DANGER);
            navAdminBtn.setVisible(true);
        } else {
            userStatusLabel.setIcon(IconUtils.getUserIcon(16, new Color(226, 232, 240)));
            userStatusLabel.setText("Guest");
            userStatusLabel.setIconTextGap(6);
            authButton.setText("Sign In");
            authButton.setIcon(IconUtils.getLoginIcon(14, Color.WHITE));
            authButton.setIconTextGap(6);
            authButton.setBackground(UiUtils.COLOR_SECONDARY);
        }
    }
}
