package com.mycompany.tokoberkahjayaa.ui;

import com.mycompany.tokoberkahjayaa.data.DatabaseConnection;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Map;

public class DashboardFrame extends JFrame {

    private String namaUser;
    private int levelUser;
    private JPanel contentPanel;
    private CardLayout cardLayout;
    
    // Menyimpan referensi tombol berdasarkan nama panelnya
    private Map<String, JButton> menuButtons = new HashMap<>();
    private JButton activeButton = null;

    private final Color PRIMARY = new Color(30, 58, 138); // Biru Gelap Sidebar
    private final Color ACTIVE_COLOR = new Color(14, 116, 144); // Warna Cyan/Biru Cerah saat Aktif
    private final Color SIDEBAR_HOVER = new Color(37, 99, 235); // Biru Terang Hover
    private final Color WHITE = Color.WHITE;
    private final Color TEXT_INACTIVE = new Color(203, 213, 225);
    private final Color BG_COLOR = new Color(241, 245, 249);

    public DashboardFrame(String nama, int level) {
        this.namaUser = nama;
        this.levelUser = level;
        setTitle("Toko Berkah Jaya - Dashboard");
        setSize(1200, 720);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(1000, 600));
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BorderLayout());
        sidebar.setBackground(PRIMARY);
        sidebar.setPreferredSize(new Dimension(240, 0));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(15, 23, 42));
        headerPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        headerPanel.setPreferredSize(new Dimension(0, 110));

        JLabel lblStore = new JLabel("TOKO BERKAH JAYA");
        lblStore.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblStore.setForeground(WHITE);
        headerPanel.add(lblStore, BorderLayout.NORTH);

        JLabel lblUser = new JLabel("User: " + namaUser);
        lblUser.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblUser.setForeground(new Color(149, 165, 166));
        headerPanel.add(lblUser, BorderLayout.SOUTH);

        sidebar.add(headerPanel, BorderLayout.NORTH);

        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setBackground(PRIMARY);
        menuPanel.setBorder(new EmptyBorder(10, 0, 10, 0));

        if (levelUser == 1) {
            addMenuItem(menuPanel, "barang", "Data Barang", e -> showPanel("barang"));
            addMenuItem(menuPanel, "customer", "Data Customer", e -> showPanel("customer"));
            addMenuItem(menuPanel, "transaksi", "Transaksi Penjualan", e -> showPanel("transaksi"));
            addMenuItem(menuPanel, "laporan", "Laporan Penjualan", e -> showPanel("laporan"));
        } else {
            addMenuItem(menuPanel, "transaksi", "Transaksi Penjualan", e -> showPanel("transaksi"));
        }

        JScrollPane menuScroll = new JScrollPane(menuPanel);
        menuScroll.setBorder(null);
        menuScroll.setBackground(PRIMARY);
        menuScroll.getViewport().setBackground(PRIMARY);
        sidebar.add(menuScroll, BorderLayout.CENTER);

        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBackground(new Color(15, 23, 42));
        footerPanel.setBorder(new EmptyBorder(15, 20, 15, 20));

        JButton btnLogout = new JButton("Logout");
        btnLogout.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnLogout.setForeground(WHITE);
        btnLogout.setBackground(new Color(239, 68, 68));
        btnLogout.setOpaque(true);
        btnLogout.setContentAreaFilled(true);
        btnLogout.setBorderPainted(false);
        btnLogout.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        btnLogout.setFocusPainted(false);
        btnLogout.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnLogout.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btnLogout.setBackground(new Color(220, 38, 38));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btnLogout.setBackground(new Color(239, 68, 68));
            }
        });

        btnLogout.addActionListener(e -> logout());
        footerPanel.add(btnLogout, BorderLayout.CENTER);

        sidebar.add(footerPanel, BorderLayout.SOUTH);
        add(sidebar, BorderLayout.WEST);

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(BG_COLOR);

        contentPanel.add(new BarangFrame(), "barang");
        contentPanel.add(new CustomerFrame(), "customer");
        contentPanel.add(new TransaksiFrame(), "transaksi");
        contentPanel.add(new LaporanFrame(), "laporan");

        add(contentPanel, BorderLayout.CENTER);
        showPanel("transaksi");
    }

    private void addMenuItem(JPanel panel, String key, String text, ActionListener action) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btn.setForeground(TEXT_INACTIVE);
        btn.setBackground(PRIMARY);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(14, 25, 14, 20));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (btn != activeButton) {
                    btn.setBackground(SIDEBAR_HOVER);
                    btn.setForeground(WHITE);
                }
            }
            @Override
            public void mouseExited(MouseEvent e) {
                if (btn != activeButton) {
                    btn.setBackground(PRIMARY);
                    btn.setForeground(TEXT_INACTIVE);
                }
            }
        });

        btn.addActionListener(action);
        panel.add(btn);
        menuButtons.put(key, btn); // Simpan referensi ke map
    }

    private void showPanel(String name) {
        cardLayout.show(contentPanel, name);
        
        // Reset warna tombol sebelumnya jika ada
        if (activeButton != null) {
            activeButton.setBackground(PRIMARY);
            activeButton.setForeground(TEXT_INACTIVE);
        }
        
        activeButton = menuButtons.get(name);
        if (activeButton != null) {
            activeButton.setBackground(ACTIVE_COLOR);
            activeButton.setForeground(WHITE);
        }
        
        Component currentComp = null;
        for (Component comp : contentPanel.getComponents()) {
            if (comp.isVisible()) {
                currentComp = comp;
                break;
            }
        }
        if (currentComp instanceof BarangFrame) {
            ((BarangFrame) currentComp).bersihkanForm();
        } else if (currentComp instanceof CustomerFrame) {
            ((CustomerFrame) currentComp).bersihkanForm();
        }
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Yakin ingin logout?",
                "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            DatabaseConnection.closeConnection();
            this.dispose();
            new LoginFrame().setVisible(true);
        }
    }
}