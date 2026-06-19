package com.mycompany.tokoberkahjayaa.ui;

import com.mycompany.tokoberkahjayaa.model.Barang;
import com.mycompany.tokoberkahjayaa.model.Kategori;
import com.mycompany.tokoberkahjayaa.process.BarangService;
import com.mycompany.tokoberkahjayaa.data.KategoriRepository;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

public class BarangFrame extends JPanel {

    private JTable tableBarang;
    private DefaultTableModel tableModel;
    private JTextField txtIdBarang, txtNamaBarang, txtSatuan, txtHargaJual, txtStok;
    private JComboBox<String> cmbKategori;
    private JButton btnSimpan, btnUpdate, btnHapus, btnBersihkan;

    private final BarangService barangService;
    private final KategoriRepository kategoriRepo;

    private final Color PRIMARY = new Color(79, 70, 229);
    private final Color SECONDARY = new Color(14, 165, 233);
    private final Color SUCCESS = new Color(16, 185, 129);
    private final Color DANGER = new Color(244, 63, 94);
    private final Color BG_COLOR = new Color(241, 245, 249);
    private final Color WHITE = Color.WHITE;

    public BarangFrame() {
        this.barangService = new BarangService();
        this.kategoriRepo = new KategoriRepository();
        setLayout(new BorderLayout(15, 15));
        setBackground(BG_COLOR);
        setBorder(new EmptyBorder(20, 20, 20, 20));
        initComponents();
        loadData();
    }

    private void initComponents() {
        JLabel lblTitle = new JLabel("Manajemen Data Barang");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(PRIMARY);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BG_COLOR);
        headerPanel.add(lblTitle, BorderLayout.WEST);
        add(headerPanel, BorderLayout.NORTH);

        String[] columns = {"ID Barang", "Kategori", "Nama Barang", "Satuan", "Harga Jual", "Stok"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tableBarang = new JTable(tableModel);
        tableBarang.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tableBarang.setRowHeight(32);
        tableBarang.setSelectionBackground(new Color(186, 230, 253));
        tableBarang.setSelectionForeground(new Color(8, 47, 73));
        tableBarang.setGridColor(new Color(51, 153, 255));
        tableBarang.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tableBarang.getTableHeader().setBackground(PRIMARY);
        tableBarang.getTableHeader().setForeground(new Color(0, 0, 0));
        tableBarang.getTableHeader().setPreferredSize(new Dimension(0, 38));

        tableBarang.getColumnModel().getColumn(0).setPreferredWidth(100);
        tableBarang.getColumnModel().getColumn(1).setPreferredWidth(100);
        tableBarang.getColumnModel().getColumn(2).setPreferredWidth(200);
        tableBarang.getColumnModel().getColumn(3).setPreferredWidth(80);
        tableBarang.getColumnModel().getColumn(4).setPreferredWidth(120);
        tableBarang.getColumnModel().getColumn(5).setPreferredWidth(60);

        tableBarang.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = tableBarang.getSelectedRow();
                if (row >= 0) {
                    txtIdBarang.setText(tableModel.getValueAt(row, 0).toString());
                    cmbKategori.setSelectedItem(tableModel.getValueAt(row, 1).toString());
                    txtNamaBarang.setText(tableModel.getValueAt(row, 2).toString());
                    txtSatuan.setText(tableModel.getValueAt(row, 3).toString());
                    txtHargaJual.setText(tableModel.getValueAt(row, 4).toString());
                    txtStok.setText(tableModel.getValueAt(row, 5).toString());
                    txtIdBarang.setEnabled(false);
                    btnSimpan.setEnabled(false);
                    btnUpdate.setEnabled(true);
                    btnHapus.setEnabled(true);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(tableBarang);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(203, 213, 225)));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(WHITE);
        formPanel.setBorder(new CompoundBorder(
                new LineBorder(new Color(199, 210, 254), 2),
                new EmptyBorder(20, 25, 20, 25)));
        formPanel.setPreferredSize(new Dimension(380, 0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 5, 6, 5);
        gbc.weightx = 1;

        JLabel lblFormTitle = new JLabel("Form Barang");
        lblFormTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblFormTitle.setForeground(PRIMARY);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 5, 15, 5);
        formPanel.add(lblFormTitle, gbc);

        gbc.gridwidth = 1;
        gbc.insets = new Insets(6, 5, 6, 5);

        addFormRow(formPanel, gbc, 1, "ID Barang:", txtIdBarang = new JTextField(15));

        JLabel lblKategori = new JLabel("Kategori:");
        lblKategori.setFont(new Font("Segoe UI", Font.BOLD, 12));
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(lblKategori, gbc);

        cmbKategori = new JComboBox<>();
        cmbKategori.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cmbKategori.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(165, 180, 252)),
                new EmptyBorder(8, 10, 8, 10)));
        loadKategoriCombo();
        gbc.gridx = 1; gbc.gridy = 2;
        formPanel.add(cmbKategori, gbc);

        addFormRow(formPanel, gbc, 3, "Nama Barang:", txtNamaBarang = new JTextField(15));
        addFormRow(formPanel, gbc, 4, "Satuan:", txtSatuan = new JTextField(15));
        addFormRow(formPanel, gbc, 5, "Harga Jual:", txtHargaJual = new JTextField(15));
        addFormRow(formPanel, gbc, 6, "Stok:", txtStok = new JTextField(15));

        JPanel btnPanel = new JPanel(new GridLayout(2, 2, 8, 8));
        btnPanel.setBackground(WHITE);
        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 2;
        gbc.insets = new Insets(15, 5, 5, 5);
        formPanel.add(btnPanel, gbc);

        btnSimpan = createButton("Simpan", SUCCESS);
        btnUpdate = createButton("Update", new Color(243, 156, 18));
        btnHapus = createButton("Hapus", DANGER);
        btnBersihkan = createButton("Bersihkan", new Color(149, 165, 166));

        btnSimpan.addActionListener(e -> simpanBarang());
        btnUpdate.addActionListener(e -> updateBarang());
        btnHapus.addActionListener(e -> hapusBarang());
        btnBersihkan.addActionListener(e -> bersihkanForm());

        btnPanel.add(btnSimpan);
        btnPanel.add(btnUpdate);
        btnPanel.add(btnHapus);
        btnPanel.add(btnBersihkan);

        btnUpdate.setEnabled(false);
        btnHapus.setEnabled(false);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollPane, formPanel);
        splitPane.setResizeWeight(0.65);
        splitPane.setDividerSize(5);
        splitPane.setBorder(null);
        add(splitPane, BorderLayout.CENTER);
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row, String label, JTextField field) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(lbl, gbc);

        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(165, 180, 252)),
                new EmptyBorder(8, 10, 8, 10)));
        gbc.gridx = 1;
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
        btn.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
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

    private void loadKategoriCombo() {
        cmbKategori.removeAllItems();
        List<Kategori> list = kategoriRepo.findAll();
        for (Kategori k : list) {
            cmbKategori.addItem(k.getNamaKategori());
        }
    }

    private void loadData() {
        tableModel.setRowCount(0);
        List<Barang> list = barangService.getAllBarang();
        for (Barang b : list) {
            Object[] row = {
                b.getIdBarang(),
                b.getNamaKategori(),
                b.getNamaBarang(),
                b.getSatuan(),
                b.getHargaJual(),
                b.getStok()
            };
            tableModel.addRow(row);
        }
    }

    private void simpanBarang() {
        if (!validasiInput()) return;

        Barang barang = new Barang();
        barang.setIdBarang(txtIdBarang.getText().trim());
        barang.setNamaBarang(txtNamaBarang.getText().trim());
        barang.setSatuan(txtSatuan.getText().trim());
        barang.setHargaJual(Double.parseDouble(txtHargaJual.getText().trim()));
        barang.setStok(Integer.parseInt(txtStok.getText().trim()));

        String kategori = cmbKategori.getSelectedItem().toString();

        if (barangService.simpanBarang(barang, kategori)) {
            JOptionPane.showMessageDialog(this, "Data barang berhasil disimpan!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
            bersihkanForm();
            loadData();
        } else {
            JOptionPane.showMessageDialog(this, "Gagal menyimpan data! ID mungkin sudah ada.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateBarang() {
        if (!validasiInput()) return;

        Barang barang = new Barang();
        barang.setIdBarang(txtIdBarang.getText().trim());
        barang.setNamaBarang(txtNamaBarang.getText().trim());
        barang.setSatuan(txtSatuan.getText().trim());
        barang.setHargaJual(Double.parseDouble(txtHargaJual.getText().trim()));
        barang.setStok(Integer.parseInt(txtStok.getText().trim()));

        String kategori = cmbKategori.getSelectedItem().toString();

        if (barangService.updateBarang(barang, kategori)) {
            JOptionPane.showMessageDialog(this, "Data barang berhasil diupdate!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
            bersihkanForm();
            loadData();
        } else {
            JOptionPane.showMessageDialog(this, "Gagal mengupdate data!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void hapusBarang() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Yakin ingin menghapus barang ini?",
                "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        if (barangService.hapusBarang(txtIdBarang.getText().trim())) {
            JOptionPane.showMessageDialog(this, "Data barang berhasil dihapus!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
            bersihkanForm();
            loadData();
        } else {
            JOptionPane.showMessageDialog(this, "Gagal menghapus data!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean validasiInput() {
        if (txtIdBarang.getText().trim().isEmpty() || txtNamaBarang.getText().trim().isEmpty()
                || txtSatuan.getText().trim().isEmpty() || txtHargaJual.getText().trim().isEmpty()
                || txtStok.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Semua field harus diisi!", "Validasi", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        try {
            Double.parseDouble(txtHargaJual.getText().trim());
            Integer.parseInt(txtStok.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Harga Jual dan Stok harus berupa angka!", "Validasi", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    public void bersihkanForm() {
        txtNamaBarang.setText("");
        txtSatuan.setText("");
        txtHargaJual.setText("");
        txtStok.setText("");
        txtIdBarang.setText(barangService.generateNewId());
        txtIdBarang.setEnabled(false); 

        btnSimpan.setEnabled(true);
        btnUpdate.setEnabled(false);
        btnHapus.setEnabled(false);
        tableBarang.clearSelection();
    }
}