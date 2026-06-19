package com.mycompany.tokoberkahjayaa.ui;

import com.mycompany.tokoberkahjayaa.model.User;
import com.mycompany.tokoberkahjayaa.process.AuthService;
import com.mycompany.tokoberkahjayaa.data.DatabaseConnection;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

public class LoginFrame extends JFrame {

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private final AuthService authService;

    private final Color PRIMARY = new Color(30, 58, 138);
    private final Color SECONDARY = new Color(37, 99, 235);
    private final Color BG_COLOR = new Color(224, 242, 254);
    private final Color WHITE = Color.WHITE;
    private final Color TEXT_DARK = new Color(30, 41, 59);

    public LoginFrame() {
        this.authService = new AuthService();
        setTitle("Toko Berkah Jaya - Login");
        setSize(450, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(BG_COLOR);
        initComponents();
    }

    private void initComponents() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 20, 10, 20);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JPanel loginPanel = new JPanel(new GridBagLayout());
        loginPanel.setBackground(WHITE);
        loginPanel.setPreferredSize(new Dimension(380, 460));
        loginPanel.setBorder(new CompoundBorder(
                new LineBorder(new Color(191, 219, 254), 2),
                new EmptyBorder(30, 30, 30, 30)));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 10, 8, 10);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;

        JLabel lblIcon = new JLabel("TOKO", SwingConstants.CENTER);
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        c.gridx = 0; c.gridy = 0; c.gridwidth = 2;
        loginPanel.add(lblIcon, c);

        JLabel lblTitle = new JLabel("TOKO BERKAH JAYA", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(PRIMARY);
        c.gridy = 1;
        loginPanel.add(lblTitle, c);

        JLabel lblSubtitle = new JLabel("Sistem Manajemen Penjualan", SwingConstants.CENTER);
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSubtitle.setForeground(new Color(127, 140, 141));
        c.gridy = 2;
        c.insets = new Insets(2, 10, 25, 10);
        loginPanel.add(lblSubtitle, c);

        c.insets = new Insets(8, 10, 8, 10);

        JLabel lblUser = new JLabel("Username");
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblUser.setForeground(TEXT_DARK);
        c.gridy = 3; c.gridwidth = 2;
        loginPanel.add(lblUser, c);

        txtUsername = new JTextField(20);
        txtUsername.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtUsername.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(147, 197, 253)),
                new EmptyBorder(10, 12, 10, 12)));
        c.gridy = 4;
        loginPanel.add(txtUsername, c);

        JLabel lblPass = new JLabel("Password");
        lblPass.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblPass.setForeground(TEXT_DARK);
        c.gridy = 5; c.insets = new Insets(15, 10, 8, 10);
        loginPanel.add(lblPass, c);

        c.insets = new Insets(8, 10, 8, 10);
        txtPassword = new JPasswordField(20);
        txtPassword.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtPassword.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(147, 197, 253)),
                new EmptyBorder(10, 12, 10, 12)));
        c.gridy = 6;
        loginPanel.add(txtPassword, c);

        JButton btnLogin = new JButton("LOGIN");
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLogin.setForeground(WHITE);
        btnLogin.setBackground(SECONDARY);
        btnLogin.setOpaque(true);
        btnLogin.setContentAreaFilled(true);
        btnLogin.setBorderPainted(false);
        btnLogin.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        btnLogin.setFocusPainted(false);
        btnLogin.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnLogin.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnLogin.setBackground(new Color(29, 78, 216));
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnLogin.setBackground(SECONDARY);
            }
        });
        c.gridy = 7; c.insets = new Insets(25, 10, 10, 10);
        loginPanel.add(btnLogin, c);

        JLabel lblFooter = new JLabel("(c) 2025 Toko Berkah Jaya", SwingConstants.CENTER);
        lblFooter.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblFooter.setForeground(new Color(149, 165, 166));
        c.gridy = 8; c.insets = new Insets(15, 10, 5, 10);
        loginPanel.add(lblFooter, c);

        btnLogin.addActionListener(e -> doLogin());
        txtPassword.addActionListener(e -> doLogin());

        gbc.gridx = 0; gbc.gridy = 0;
        add(loginPanel, gbc);
    }

    private void doLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (!authService.validateInput(username, password)) {
            JOptionPane.showMessageDialog(this,
                    "Username dan Password harus diisi!",
                    "Validasi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        User user = authService.login(username, password);
        if (user != null) {
            JOptionPane.showMessageDialog(this,
                    "Selamat datang, " + user.getNamaLengkap() + "!",
                    "Login Berhasil", JOptionPane.INFORMATION_MESSAGE);
            this.dispose();
            new DashboardFrame(user.getNamaLengkap(), user.getLevel()).setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Username atau Password salah!",
                    "Login Gagal", JOptionPane.ERROR_MESSAGE);
            txtPassword.setText("");
        }
    }
}