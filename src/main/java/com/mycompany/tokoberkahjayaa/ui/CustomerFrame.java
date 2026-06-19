package com.mycompany.tokoberkahjayaa.ui;

import com.mycompany.tokoberkahjayaa.model.Customer;
import com.mycompany.tokoberkahjayaa.process.CustomerService;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

public class CustomerFrame extends JPanel {

    private JTable tableCustomer;
    private DefaultTableModel tableModel;
    private JTextField txtIdCustomer, txtNamaCustomer, txtTelepon;
    private JTextArea txtAlamat;
    private JButton btnSimpan, btnUpdate, btnHapus, btnBersihkan;

    private final CustomerService customerService;

    private final Color PRIMARY = new Color(79, 70, 229);
    private final Color SECONDARY = new Color(14, 165, 233);
    private final Color SUCCESS = new Color(16, 185, 129);
    private final Color DANGER = new Color(244, 63, 94);
    private final Color BG_COLOR = new Color(241, 245, 249);
    private final Color WHITE = Color.WHITE;

    public CustomerFrame() {
        this.customerService = new CustomerService();
        setLayout(new BorderLayout(15, 15));
        setBackground(BG_COLOR);
        setBorder(new EmptyBorder(20, 20, 20, 20));
        initComponents();
        loadData();
    }

    private void initComponents() {
        JLabel lblTitle = new JLabel("Manajemen Data Customer");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(PRIMARY);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BG_COLOR);
        headerPanel.add(lblTitle, BorderLayout.WEST);
        add(headerPanel, BorderLayout.NORTH);

        String[] columns = {"ID Customer", "Nama Customer", "Alamat", "No. Telepon"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tableCustomer = new JTable(tableModel);
        tableCustomer.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tableCustomer.setRowHeight(32);
        tableCustomer.setSelectionBackground(new Color(186, 230, 253));
        tableCustomer.setSelectionForeground(new Color(8, 47, 73));
        tableCustomer.setGridColor(new Color(220, 220, 220));
        tableCustomer.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tableCustomer.getTableHeader().setBackground(PRIMARY);
        tableCustomer.getTableHeader().setForeground(new Color(0, 0, 0));
        tableCustomer.getTableHeader().setPreferredSize(new Dimension(0, 38));

        tableCustomer.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = tableCustomer.getSelectedRow();
                if (row >= 0) {
                    txtIdCustomer.setText(tableModel.getValueAt(row, 0).toString());
                    txtNamaCustomer.setText(tableModel.getValueAt(row, 1).toString());
                    txtAlamat.setText(tableModel.getValueAt(row, 2).toString());
                    txtTelepon.setText(tableModel.getValueAt(row, 3).toString());
                    txtIdCustomer.setEnabled(false);
                    btnSimpan.setEnabled(false);
                    btnUpdate.setEnabled(true);
                    btnHapus.setEnabled(true);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(tableCustomer);
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

        JLabel lblFormTitle = new JLabel("Form Customer");
        lblFormTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblFormTitle.setForeground(PRIMARY);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 5, 15, 5);
        formPanel.add(lblFormTitle, gbc);

        gbc.gridwidth = 1;
        gbc.insets = new Insets(6, 5, 6, 5);

        addFormRow(formPanel, gbc, 1, "ID Customer:", txtIdCustomer = new JTextField(15));
        addFormRow(formPanel, gbc, 2, "Nama Customer:", txtNamaCustomer = new JTextField(15));

        JLabel lblAlamat = new JLabel("Alamat:");
        lblAlamat.setFont(new Font("Segoe UI", Font.BOLD, 12));
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(lblAlamat, gbc);

        txtAlamat = new JTextArea(3, 15);
        txtAlamat.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtAlamat.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(165, 180, 252)),
                new EmptyBorder(8, 10, 8, 10)));
        txtAlamat.setLineWrap(true);
        txtAlamat.setWrapStyleWord(true);
        JScrollPane alamatScroll = new JScrollPane(txtAlamat);
        alamatScroll.setBorder(null);
        gbc.gridx = 1;
        formPanel.add(alamatScroll, gbc);

        addFormRow(formPanel, gbc, 4, "No. Telepon:", txtTelepon = new JTextField(15));

        JPanel btnPanel = new JPanel(new GridLayout(2, 2, 8, 8));
        btnPanel.setBackground(WHITE);
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        gbc.insets = new Insets(15, 5, 5, 5);
        formPanel.add(btnPanel, gbc);

        btnSimpan = createButton("Simpan", SUCCESS);
        btnUpdate = createButton("Update", new Color(243, 156, 18));
        btnHapus = createButton("Hapus", DANGER);
        btnBersihkan = createButton("Bersihkan", new Color(149, 165, 166));

        btnSimpan.addActionListener(e -> simpanCustomer());
        btnUpdate.addActionListener(e -> updateCustomer());
        btnHapus.addActionListener(e -> hapusCustomer());
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

    private void loadData() {
        tableModel.setRowCount(0);
        List<Customer> list = customerService.getAllCustomer();
        for (Customer c : list) {
            Object[] row = {
                c.getIdCustomer(),
                c.getNamaCustomer(),
                c.getAlamat(),
                c.getTelepon()
            };
            tableModel.addRow(row);
        }
    }

    private void simpanCustomer() {
        if (!validasiInput()) return;

        Customer customer = new Customer();
        customer.setIdCustomer(txtIdCustomer.getText().trim());
        customer.setNamaCustomer(txtNamaCustomer.getText().trim());
        customer.setAlamat(txtAlamat.getText().trim());
        customer.setTelepon(txtTelepon.getText().trim());

        if (customerService.simpanCustomer(customer)) {
            JOptionPane.showMessageDialog(this, "Data customer berhasil disimpan!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
            bersihkanForm();
            loadData();
        } else {
            JOptionPane.showMessageDialog(this, "Gagal menyimpan! ID mungkin sudah ada.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateCustomer() {
        if (!validasiInput()) return;

        Customer customer = new Customer();
        customer.setIdCustomer(txtIdCustomer.getText().trim());
        customer.setNamaCustomer(txtNamaCustomer.getText().trim());
        customer.setAlamat(txtAlamat.getText().trim());
        customer.setTelepon(txtTelepon.getText().trim());

        if (customerService.updateCustomer(customer)) {
            JOptionPane.showMessageDialog(this, "Data customer berhasil diupdate!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
            bersihkanForm();
            loadData();
        } else {
            JOptionPane.showMessageDialog(this, "Gagal mengupdate data!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void hapusCustomer() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Yakin ingin menghapus customer ini?",
                "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        if (customerService.hapusCustomer(txtIdCustomer.getText().trim())) {
            JOptionPane.showMessageDialog(this, "Data customer berhasil dihapus!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
            bersihkanForm();
            loadData();
        } else {
            JOptionPane.showMessageDialog(this, "Gagal menghapus data!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean validasiInput() {
        if (txtIdCustomer.getText().trim().isEmpty() || txtNamaCustomer.getText().trim().isEmpty()
                || txtAlamat.getText().trim().isEmpty() || txtTelepon.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Semua field harus diisi!", "Validasi", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    public void bersihkanForm() {
        txtNamaCustomer.setText("");
        txtAlamat.setText("");
        txtTelepon.setText("");
        txtIdCustomer.setText(customerService.generateNewId());
        txtIdCustomer.setEnabled(false);

        btnSimpan.setEnabled(true);
        btnUpdate.setEnabled(false);
        btnHapus.setEnabled(false);
        tableCustomer.clearSelection();
    }
}