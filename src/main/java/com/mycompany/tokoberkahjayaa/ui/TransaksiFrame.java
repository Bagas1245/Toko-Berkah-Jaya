package com.mycompany.tokoberkahjayaa.ui;

import com.mycompany.tokoberkahjayaa.model.Barang;
import com.mycompany.tokoberkahjayaa.model.Customer;
import com.mycompany.tokoberkahjayaa.model.Penjualan;
import com.mycompany.tokoberkahjayaa.model.DetailPenjualan;
import com.mycompany.tokoberkahjayaa.process.BarangService;
import com.mycompany.tokoberkahjayaa.process.CustomerService;
import com.mycompany.tokoberkahjayaa.process.TransaksiService;

// Import untuk QR Code dan Server
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.text.NumberFormat;
import java.util.Locale;

public class TransaksiFrame extends JPanel {

    private JComboBox<String> cmbCustomer, cmbBarang;
    private JTextField txtHarga, txtJumlah, txtSubtotal, txtTotalBayar, txtNoFaktur;
    private JTable tableDetail;
    private DefaultTableModel detailModel;
    private ArrayList<DetailPenjualan> detailList = new ArrayList<>();

    // Menyimpan data asli untuk keperluan filter pencarian ketik
    private List<Customer> originalCustomerList = new ArrayList<>();
    private List<Barang> originalBarangList = new ArrayList<>();

    // Komponen QR Code terintegrasi
    private JLabel lblQRCode;
    private JLabel lblStatusBayar;
    private HttpServer server;
    private int serverPort = 8765;
    private String baseUrl;
    private volatile boolean pembayaranSelesai = false;
    private volatile String currentNoFaktur = "";
    private volatile double currentTotal = 0;
    private Thread pollThread;

    private final BarangService barangService;
    private final CustomerService customerService;
    private final TransaksiService transaksiService;
    private final NumberFormat formatter;

    private final Color PRIMARY = new Color(30, 58, 138);
    private final Color SECONDARY = new Color(37, 99, 235);
    private final Color BG_COLOR = new Color(241, 245, 249);
    private final Color WHITE = Color.WHITE;

    public TransaksiFrame() {
        this.barangService = new BarangService();
        this.customerService = new CustomerService();
        this.transaksiService = new TransaksiService();
        this.formatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        
        setLayout(new BorderLayout(15, 15));
        setBackground(BG_COLOR);
        setBorder(new EmptyBorder(20, 20, 20, 20));
        
        startServer(); // Mulai server QR otomatis saat frame dibuka
        initComponents();
        generateNoFaktur();
        loadComboCustomer();
        loadComboBarang();
        setupAutoSuggestSearch();
    }

    private void initComponents() {
        JLabel lblTitle = new JLabel("Transaksi Penjualan");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(PRIMARY);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BG_COLOR);
        headerPanel.add(lblTitle, BorderLayout.WEST);
        add(headerPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout(0, 15));
        centerPanel.setBackground(BG_COLOR);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(WHITE);
        formPanel.setBorder(new CompoundBorder(
                new LineBorder(new Color(191, 219, 254), 2),
                new EmptyBorder(18, 22, 18, 22)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 6, 5, 6);
        gbc.weightx = 1;

        addFormRow(formPanel, gbc, 0, 0, "No. Faktur:", txtNoFaktur = new JTextField(15));
        txtNoFaktur.setEnabled(false);
        txtNoFaktur.setDisabledTextColor(Color.BLACK);

        JLabel lblTgl = new JLabel("Tanggal: " + LocalDate.now());
        lblTgl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        gbc.gridx = 2; gbc.gridy = 0; gbc.gridwidth = 2;
        formPanel.add(lblTgl, gbc);
        gbc.gridwidth = 1;

        JLabel lblCust = new JLabel("Customer:");
        lblCust.setFont(new Font("Segoe UI", Font.BOLD, 12));
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(lblCust, gbc);

        cmbCustomer = new JComboBox<>();
        cmbCustomer.setEditable(true); // <--- Diaktifkan agar bisa diketik langsung
        cmbCustomer.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        gbc.gridx = 1; gbc.gridy = 1; gbc.gridwidth = 3;
        formPanel.add(cmbCustomer, gbc);
        gbc.gridwidth = 1;

        JLabel lblBarang = new JLabel("Barang:");
        lblBarang.setFont(new Font("Segoe UI", Font.BOLD, 12));
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(lblBarang, gbc);

        cmbBarang = new JComboBox<>();
        cmbBarang.setEditable(true); // <--- Diaktifkan agar bisa diketik langsung
        cmbBarang.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cmbBarang.addActionListener(e -> updateHargaOtomatis());
        gbc.gridx = 1; gbc.gridy = 2; gbc.gridwidth = 3;
        formPanel.add(cmbBarang, gbc);
        gbc.gridwidth = 1;

        JLabel lblHarga = new JLabel("Harga:");
        lblHarga.setFont(new Font("Segoe UI", Font.BOLD, 12));
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(lblHarga, gbc);

        txtHarga = new JTextField(10);
        txtHarga.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtHarga.setEnabled(false);
        txtHarga.setDisabledTextColor(Color.BLACK);
        gbc.gridx = 1; gbc.gridy = 3;
        formPanel.add(txtHarga, gbc);

        JLabel lblJumlah = new JLabel("Jumlah:");
        lblJumlah.setFont(new Font("Segoe UI", Font.BOLD, 12));
        gbc.gridx = 2; gbc.gridy = 3;
        formPanel.add(lblJumlah, gbc);

        txtJumlah = new JTextField(10);
        txtJumlah.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtJumlah.addActionListener(e -> hitungSubtotal());
        txtJumlah.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent e) { hitungSubtotal(); }
        });
        gbc.gridx = 3; gbc.gridy = 3;
        formPanel.add(txtJumlah, gbc);

        JLabel lblSub = new JLabel("Subtotal:");
        lblSub.setFont(new Font("Segoe UI", Font.BOLD, 12));
        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(lblSub, gbc);

        txtSubtotal = new JTextField(10);
        txtSubtotal.setFont(new Font("Segoe UI", Font.BOLD, 13));
        txtSubtotal.setEnabled(false);
        txtSubtotal.setDisabledTextColor(new Color(39, 174, 96));
        gbc.gridx = 1; gbc.gridy = 4;
        formPanel.add(txtSubtotal, gbc);

        JButton btnTambah = createButton("+ Tambah", new Color(34, 197, 94));
        btnTambah.addActionListener(e -> tambahKeKeranjang());
        gbc.gridx = 2; gbc.gridy = 4; gbc.gridwidth = 2;
        formPanel.add(btnTambah, gbc);

        centerPanel.add(formPanel, BorderLayout.NORTH);

        String[] columns = {"ID Barang", "Nama Barang", "Harga", "Jumlah", "Subtotal"};
        detailModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tableDetail = new JTable(detailModel);
        tableDetail.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tableDetail.setRowHeight(30);
        
        tableDetail.setForeground(Color.BLACK);
        tableDetail.setGridColor(new Color(220, 220, 220));
        tableDetail.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tableDetail.getTableHeader().setBackground(PRIMARY);
        tableDetail.getTableHeader().setForeground(new Color(0, 0, 0));
        
        JScrollPane scrollDetail = new JScrollPane(tableDetail);
        scrollDetail.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        scrollDetail.setPreferredSize(new Dimension(0, 200));
        centerPanel.add(scrollDetail, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        JPanel qrPanel = new JPanel(new BorderLayout(10, 10));
        qrPanel.setBackground(WHITE);
        qrPanel.setBorder(new CompoundBorder(
                new LineBorder(new Color(191, 219, 254), 2),
                new EmptyBorder(20, 20, 20, 20)));
        qrPanel.setPreferredSize(new Dimension(260, 0));

        JLabel lblTitleQR = new JLabel("Scan untuk Bayar", SwingConstants.CENTER);
        lblTitleQR.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitleQR.setForeground(PRIMARY);
        qrPanel.add(lblTitleQR, BorderLayout.NORTH);

        lblQRCode = new JLabel("Keranjang Kosong", SwingConstants.CENTER);
        lblQRCode.setPreferredSize(new Dimension(200, 200));
        lblQRCode.setOpaque(true);
        lblQRCode.setBackground(new Color(248, 248, 248));
        lblQRCode.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        qrPanel.add(lblQRCode, BorderLayout.CENTER);

        lblStatusBayar = new JLabel("BELUM DIBAYAR", SwingConstants.CENTER);
        lblStatusBayar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblStatusBayar.setForeground(new Color(231, 76, 60));
        qrPanel.add(lblStatusBayar, BorderLayout.SOUTH);

        add(qrPanel, BorderLayout.EAST);

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.setBackground(BG_COLOR);

        JPanel totalPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        totalPanel.setBackground(BG_COLOR);

        JLabel lblTotal = new JLabel("TOTAL BAYAR: ");
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTotal.setForeground(PRIMARY);
        totalPanel.add(lblTotal);

        txtTotalBayar = new JTextField(15);
        txtTotalBayar.setFont(new Font("Segoe UI", Font.BOLD, 20));
        txtTotalBayar.setForeground(new Color(39, 174, 96));
        txtTotalBayar.setBackground(WHITE);
        txtTotalBayar.setHorizontalAlignment(JTextField.RIGHT);
        txtTotalBayar.setText("Rp0");
        txtTotalBayar.setEnabled(false);
        txtTotalBayar.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220, 220, 220)),
                new EmptyBorder(10, 15, 10, 15)));
        totalPanel.add(txtTotalBayar);

        southPanel.add(totalPanel, BorderLayout.NORTH);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        btnPanel.setBackground(BG_COLOR);

        JButton btnHapusItem = createButton("Hapus Item", new Color(239, 68, 68));
        JButton btnBersihkan = createButton("Bersihkan", new Color(245, 158, 11));
        JButton btnSimpan = createButton("Simpan Transaksi", new Color(37, 99, 235));
        btnSimpan.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSimpan.setPreferredSize(new Dimension(180, 45));

        btnHapusItem.addActionListener(e -> hapusItem());
        btnBersihkan.addActionListener(e -> bersihkanTransaksi());
        btnSimpan.addActionListener(e -> simpanTransaksi());

        btnPanel.add(btnHapusItem);
        btnPanel.add(btnBersihkan);
        btnPanel.add(btnSimpan);

        southPanel.add(btnPanel, BorderLayout.SOUTH);
        add(southPanel, BorderLayout.SOUTH);
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, int x, int y, String label, JTextField field) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        gbc.gridx = x; gbc.gridy = y;
        panel.add(lbl, gbc);

        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(165, 180, 252)),
                new EmptyBorder(8, 10, 8, 10)));
        gbc.gridx = x + 1;
        panel.add(field, gbc);
    }

    private JButton createButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setBorderPainted(false);
        btn.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(bg.brighter());
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(bg);
            }
        });
        return btn;
    }

    private void generateNoFaktur() {
        txtNoFaktur.setText(transaksiService.generateNoFaktur());
    }

    private void loadComboCustomer() {
        cmbCustomer.removeAllItems();
        originalCustomerList = customerService.getAllCustomer();
        for (Customer c : originalCustomerList) {
            cmbCustomer.addItem(c.toString());
        }
        cmbCustomer.setSelectedIndex(-1);
    }

    private void loadComboBarang() {
        cmbBarang.removeAllItems();
        originalBarangList = barangService.getBarangAvailable();
        for (Barang b : originalBarangList) {
            cmbBarang.addItem(b.toString());
        }
        cmbBarang.setSelectedIndex(-1);
    }

    // =========================================================================
    // IMPLEMENTASI ENGINE AUTO-SUGGEST DENGAN LOGIKA FILTER KETIKAN USER
    // =========================================================================
    private void setupAutoSuggestSearch() {
        // 1. Setup Auto-Suggest untuk JComboBox Customer
        JTextField txtCustEditor = (JTextField) cmbCustomer.getEditor().getEditorComponent();
        txtCustEditor.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_ENTER) {
                    return; // Lewatkan jika pengguna sedang navigasi menggunakan tombol panah atau enter
                }
                String expr = txtCustEditor.getText();
                filterCustomer(expr);
            }
        });

        // 2. Setup Auto-Suggest untuk JComboBox Barang
        JTextField txtBarangEditor = (JTextField) cmbBarang.getEditor().getEditorComponent();
        txtBarangEditor.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_ENTER) {
                    return;
                }
                String expr = txtBarangEditor.getText();
                filterBarang(expr);
            }
        });
    }

    private void filterCustomer(String text) {
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        for (Customer c : originalCustomerList) {
            if (c.toString().toLowerCase().contains(text.toLowerCase())) {
                model.addElement(c.toString());
            }
        }
        cmbCustomer.setModel(model);
        cmbCustomer.getEditor().setItem(text);
        if (model.getSize() > 0) {
            cmbCustomer.showPopup();
        } else {
            cmbCustomer.hidePopup();
        }
    }

    private void filterBarang(String text) {
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        for (Barang b : originalBarangList) {
            if (b.toString().toLowerCase().contains(text.toLowerCase())) {
                model.addElement(b.toString());
            }
        }
        cmbBarang.setModel(model);
        cmbBarang.getEditor().setItem(text);
        if (model.getSize() > 0) {
            cmbBarang.showPopup();
        } else {
            cmbBarang.hidePopup();
        }
    }

    private void updateHargaOtomatis() {
        if (cmbBarang.getSelectedItem() == null) return;
        String selected = cmbBarang.getSelectedItem().toString();
        if (!selected.contains(" - ")) return; // Cegah error jika teks input belum lengkap sesuai format toString()
        
        String idBarang = selected.split(" - ")[0];
        Barang barang = barangService.getBarangById(idBarang);
        if (barang != null) {
            txtHarga.setText(String.valueOf(barang.getHargaJual()));
        }
        txtJumlah.setText("");
        txtSubtotal.setText("");
    }

    private void hitungSubtotal() {
        try {
            double harga = Double.parseDouble(txtHarga.getText().trim());
            int jumlah = Integer.parseInt(txtJumlah.getText().trim());
            double subtotal = harga * jumlah;
            txtSubtotal.setText(String.valueOf(subtotal));
        } catch (NumberFormatException e) {
            txtSubtotal.setText("");
        }
    }

    private void tambahKeKeranjang() {
        if (cmbBarang.getSelectedItem() == null || txtJumlah.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Pilih barang dan masukkan jumlah!", "Validasi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String selected = cmbBarang.getSelectedItem().toString();
        if (!selected.contains(" - ")) {
            JOptionPane.showMessageDialog(this, "Pilih barang valid dari list saran!", "Validasi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String idBarang = selected.split(" - ")[0];
        String namaBarang = selected.split(" - ")[1].split(" \\(")[0];
        double harga = Double.parseDouble(txtHarga.getText().trim());
        int jumlah = Integer.parseInt(txtJumlah.getText().trim());

        if (jumlah <= 0) {
            JOptionPane.showMessageDialog(this, "Jumlah harus lebih dari 0!", "Validasi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int sudahDipesan = 0;
        for (DetailPenjualan d : detailList) {
            if (d.getIdBarang().equals(idBarang)) {
                sudahDipesan += d.getJumlahBeli();
            }
        }

        if (!barangService.cekStokCukup(idBarang, jumlah, sudahDipesan)) {
            int stok = barangService.getStok(idBarang);
            JOptionPane.showMessageDialog(this,
                    "Stok tidak mencukupi!\nStok tersedia: " + stok + "\nSudah dipesan: " + sudahDipesan,
                    "Stok Habis", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double subtotal = harga * jumlah;
        DetailPenjualan detail = new DetailPenjualan();
        detail.setIdBarang(idBarang);
        detail.setNamaBarang(namaBarang);
        detail.setHargaSatuan(harga);
        detail.setJumlahBeli(jumlah);
        detail.setSubtotal(subtotal);
        detailList.add(detail);
        
        refreshTableDetail();
        
        // Kembalikan form barang ke kosong & load ulang list agar pencarian bersih
        loadComboBarang();
        txtHarga.setText("");
        txtJumlah.setText("");
        txtSubtotal.setText("");
    }

    private void refreshTableDetail() {
        detailModel.setRowCount(0);
        for (DetailPenjualan d : detailList) {
            detailModel.addRow(new Object[]{
                d.getIdBarang(), d.getNamaBarang(), d.getHargaSatuan(), d.getJumlahBeli(), d.getSubtotal()
            });
        }
        double total = transaksiService.hitungTotal(detailList);
        txtTotalBayar.setText(formatter.format(total));
        
        // Perbarui QR otomatis setelah tabel berubah
        updateQRCodeOtomatis();
    }

    private void hapusItem() {
        int row = tableDetail.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Pilih item yang akan dihapus!", "Validasi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        detailList.remove(row);
        refreshTableDetail();
    }

    private void bersihkanTransaksi() {
        detailList.clear();
        refreshTableDetail();
        txtTotalBayar.setText("Rp0");
        generateNoFaktur();
        loadComboCustomer();
        loadComboBarang();
        txtHarga.setText("");
        txtJumlah.setText("");
        txtSubtotal.setText("");
    }

    private void simpanTransaksi() {
        if (detailList.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tambahkan barang ke keranjang!", "Validasi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (cmbCustomer.getSelectedItem() == null || !cmbCustomer.getSelectedItem().toString().contains(" - ")) {
            JOptionPane.showMessageDialog(this, "Pilih customer valid dari list saran!", "Validasi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (!pembayaranSelesai) {
             int confirm = JOptionPane.showConfirmDialog(this, "Pembayaran belum dikonfirmasi di halaman web.\nTetap simpan transaksi?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
             if (confirm != JOptionPane.YES_OPTION) {
                 return;
             }
        }

        String noFaktur = txtNoFaktur.getText().trim();
        String idCustomer = cmbCustomer.getSelectedItem().toString().split(" - ")[0];
        double total = transaksiService.hitungTotal(detailList);

        Penjualan penjualan = new Penjualan();
        penjualan.setNoFaktur(noFaktur);
        penjualan.setTglTransaksi(LocalDate.now());
        penjualan.setIdCustomer(idCustomer);
        penjualan.setTotalBayar(total);
        penjualan.setIdUser(1);

        if (!transaksiService.validasiTransaksi(penjualan, detailList)) {
            JOptionPane.showMessageDialog(this, "Data transaksi tidak valid!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (transaksiService.simpanTransaksi(penjualan, detailList)) {
            JOptionPane.showMessageDialog(this,
                    "Transaksi berhasil disimpan!\nNo. Faktur: " + noFaktur + "\nTotal: " + formatter.format(total),
                    "Sukses", JOptionPane.INFORMATION_MESSAGE);
            bersihkanTransaksi();
        } else {
            JOptionPane.showMessageDialog(this, "Gagal menyimpan transaksi!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ==========================================
    // LOGIK QR CODE & HTTP SERVER TERINTEGRASI
    // ==========================================
    
    private void startServer() {
        try {
            server = HttpServer.create(new InetSocketAddress(serverPort), 0);
            server.createContext("/bayar", new BayarHandler());
            server.createContext("/proses", new ProsesHandler());
            server.createContext("/sukses", new SuksesHandler());
            server.setExecutor(null);
            server.start();

            String ip = getLocalIpAddress();
            baseUrl = "http://" + ip + ":" + serverPort;
        } catch (IOException e) {
            System.err.println("Server QR mungkin sudah berjalan: " + e.getMessage());
        }
    }

    private String getLocalIpAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            return "localhost";
        }
    }

    private void updateQRCodeOtomatis() {
        double total = transaksiService.hitungTotal(detailList);
        if (total <= 0) {
            lblQRCode.setIcon(null);
            lblQRCode.setText("Keranjang Kosong");
            lblStatusBayar.setText("BELUM DIBAYAR");
            lblStatusBayar.setForeground(new Color(231, 76, 60));
            pembayaranSelesai = false;
            return;
        }

        currentNoFaktur = txtNoFaktur.getText().trim();
        currentTotal = total;
        pembayaranSelesai = false;

        String formatTotal = String.format("%.0f", total);
        String qrUrl = baseUrl + "/bayar?faktur=" + currentNoFaktur + "&total=" + formatTotal;

        try {
            BitMatrix matrix = new com.google.zxing.qrcode.QRCodeWriter().encode(
                    qrUrl, BarcodeFormat.QR_CODE, 200, 200);
            BufferedImage image = MatrixToImageWriter.toBufferedImage(matrix);
            lblQRCode.setText("");
            lblQRCode.setIcon(new ImageIcon(image));
            
            lblStatusBayar.setText("MENUNGGU PEMBAYARAN...");
            lblStatusBayar.setForeground(new Color(243, 156, 18));

            startPollingStatus();

        } catch (WriterException e) {
            lblQRCode.setText("Gagal Generate QR");
            e.printStackTrace();
        }
    }

    private void startPollingStatus() {
        if (pollThread != null && pollThread.isAlive()) {
            return;
        }
        pollThread = new Thread(() -> {
            int attempts = 0;
            while (attempts < 120 && !pembayaranSelesai) {
                try {
                    Thread.sleep(2000);
                    if (pembayaranSelesai) {
                        SwingUtilities.invokeLater(() -> {
                            lblStatusBayar.setText("PEMBAYARAN BERHASIL!");
                            lblStatusBayar.setForeground(new Color(39, 174, 96));
                        });
                        break;
                    }
                    attempts++;
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        pollThread.setDaemon(true);
        pollThread.start();
    }

    class BayarHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();
            String faktur = getQueryParam(query, "faktur");
            String total = getQueryParam(query, "total");

            String html = buildPaymentPage(faktur, total);
            byte[] response = html.getBytes(StandardCharsets.UTF_8);

            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, response.length);
            OutputStream os = exchange.getResponseBody();
            os.write(response);
            os.close();
        }
    }

    class ProsesHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            InputStream is = exchange.getRequestBody();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = is.read(buffer)) != -1) {
                bos.write(buffer, 0, len);
            }
            String body = bos.toString(StandardCharsets.UTF_8);

            String faktur = getQueryParam(body, "faktur");
            String nominal = getQueryParam(body, "nominal");

            currentNoFaktur = faktur;
            try {
                currentTotal = Double.parseDouble(nominal);
            } catch (NumberFormatException e) {
                currentTotal = 0;
            }
            pembayaranSelesai = true;

            String response = buildSuccessPage(faktur, nominal);
            byte[] respBytes = response.getBytes(StandardCharsets.UTF_8);

            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, respBytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(respBytes);
            os.close();
        }
    }

    class SuksesHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String html = "<!DOCTYPE html><html><head><meta charset='UTF-8'>"
                    + "<title>Pembayaran Berhasil</title>"
                    + "<style>body{font-family:Arial;text-align:center;padding:50px;color:#27ae60;}"
                    + ".box{border:2px solid #27ae60;padding:30px;border-radius:10px;display:inline-block;}"
                    + "h1{color:#27ae60;}</style></head>"
                    + "<body><div class='box'><h1> Pembayaran Berhasil!</h1>"
                    + "<p>Terima kasih telah melakukan pembayaran.</p>"
                    + "<p>Silakan kembali ke aplikasi desktop untuk melihat status.</p></div></body></html>";
            byte[] response = html.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, response.length);
            OutputStream os = exchange.getResponseBody();
            os.write(response);
            os.close();
        }
    }

    private String buildPaymentPage(String faktur, String total) {
        return "<!DOCTYPE html>"
                + "<html><head><meta charset='UTF-8'><meta name='viewport' content='width=device-width, initial-scale=1.0'>"
                + "<title>Pembayaran Toko Berkah Jaya</title>"
                + "<style>"
                + "*{box-sizing:border-box;margin:0;padding:0;font-family:'Segoe UI',Arial,sans-serif;}"
                + "body{background:#ecf0f1;display:flex;justify-content:center;align-items:center;min-height:100vh;padding:20px;}"
                + ".container{background:#fff;border-radius:12px;box-shadow:0 4px 20px rgba(0,0,0,0.1);width:100%;max-width:400px;padding:30px;}"
                + ".header{text-align:center;margin-bottom:25px;}"
                + ".header h1{color:#2c3e50;font-size:22px;margin-bottom:5px;}"
                + ".header p{color:#7f8c8d;font-size:14px;}"
                + ".info-box{background:#f8f9fa;border-radius:8px;padding:15px;margin-bottom:20px;}"
                + ".info-row{display:flex;justify-content:space-between;margin:8px 0;font-size:14px;}"
                + ".info-label{color:#7f8c8d;}"
                + ".info-value{color:#2c3e50;font-weight:bold;}"
                + "input[type='number']{width:100%;padding:14px;border:2px solid #ddd;border-radius:8px;font-size:16px;margin-bottom:15px;transition:border-color 0.3s;}"
                + "input[type='number']:focus{outline:none;border-color:#3498db;}"
                + "button{width:100%;padding:14px;background:#3498db;color:#fff;border:none;border-radius:8px;font-size:16px;font-weight:bold;cursor:pointer;transition:background 0.3s;}"
                + "button:hover{background:#2980b9;}"
                + ".footer{text-align:center;margin-top:20px;color:#95a5a6;font-size:12px;}"
                + "</style></head>"
                + "<body>"
                + "<div class='container'>"
                + "<div class='header'><h1>Toko Berkah Jaya</h1><p>Halaman Pembayaran</p></div>"
                + "<div class='info-box'>"
                + "<div class='info-row'><span class='info-label'>No. Faktur:</span><span class='info-value'>" + faktur + "</span></div>"
                + "<div class='info-row'><span class='info-label'>Total Tagihan:</span><span class='info-value'>Rp " + total + "</span></div>"
                + "</div>"
                + "<form method='POST' action='/proses' onsubmit='return validateForm()'>"
                + "<input type='hidden' name='faktur' value='" + faktur + "'>"
                + "<label style='display:block;margin-bottom:8px;color:#2c3e50;font-size:14px;font-weight:bold;'>Nominal Pembayaran (Rp):</label>"
                + "<input type='number' name='nominal' id='nominal' placeholder='Masukkan nominal pembayaran' required min='1'>"
                + "<button type='submit'>BAYAR SEKARANG</button>"
                + "</form>"
                + "<div class='footer'>Powered by Toko Berkah Jaya</div>"
                + "</div>"
                + "<script>"
                + "function validateForm(){var n=document.getElementById('nominal').value;if(n<=0){alert('Nominal harus lebih dari 0!');return false;}return true;}"
                + "</script>"
                + "</body></html>";
    }

    private String buildSuccessPage(String faktur, String nominal) {
        return "<!DOCTYPE html>"
                + "<html><head><meta charset='UTF-8'><meta name='viewport' content='width=device-width, initial-scale=1.0'>"
                + "<title>Pembayaran Berhasil</title>"
                + "<style>"
                + "*{box-sizing:border-box;margin:0;padding:0;font-family:'Segoe UI',Arial,sans-serif;}"
                + "body{background:#ecf0f1;display:flex;justify-content:center;align-items:center;min-height:100vh;padding:20px;}"
                + ".container{background:#fff;border-radius:12px;box-shadow:0 4px 20px rgba(0,0,0,0.1);width:100%;max-width:400px;padding:30px;text-align:center;}"
                + ".success-icon{font-size:60px;color:#27ae60;margin-bottom:15px;}"
                + "h1{color:#27ae60;font-size:24px;margin-bottom:10px;}"
                + ".info-box{background:#f8f9fa;border-radius:8px;padding:15px;margin:20px 0;text-align:left;}"
                + ".info-row{display:flex;justify-content:space-between;margin:8px 0;font-size:14px;}"
                + ".info-label{color:#7f8c8d;}"
                + ".info-value{color:#2c3e50;font-weight:bold;}"
                + ".footer{color:#95a5a6;font-size:12px;margin-top:20px;}"
                + "</style></head>"
                + "<body>"
                + "<div class='container'>"
                + "<div class='success-icon'>OK</div>"
                + "<h1>Pembayaran Berhasil!</h1>"
                + "<p style='color:#7f8c8d;margin-bottom:20px;'>Terima kasih telah melakukan pembayaran.</p>"
                + "<div class='info-box'>"
                + "<div class='info-row'><span class='info-label'>No. Faktur:</span><span class='info-value'>" + faktur + "</span></div>"
                + "<div class='info-row'><span class='info-label'>Nominal:</span><span class='info-value'>Rp " + nominal + "</span></div>"
                + "<div class='info-row'><span class='info-label'>Status:</span><span style='color:#27ae60;font-weight:bold;'>LUNAS</span></div>"
                + "</div>"
                + "<div class='footer'>Silakan kembali ke aplikasi desktop.<br>Status akan otomatis terupdate.</div>"
                + "</div></body></html>";
    }

    private String getQueryParam(String query, String key) {
        if (query == null || query.isEmpty()) return "";
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            if (idx > 0 && pair.substring(0, idx).equals(key)) {
                return java.net.URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8);
            }
        }
        return "";
    }
}