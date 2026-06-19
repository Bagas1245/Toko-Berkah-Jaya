package com.mycompany.tokoberkahjayaa.ui;

import com.mycompany.tokoberkahjayaa.model.Penjualan;
import com.mycompany.tokoberkahjayaa.model.DetailPenjualan;
import com.mycompany.tokoberkahjayaa.process.LaporanService;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.awt.Color;
import java.awt.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Cell;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.io.*;
import java.sql.*;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

public class LaporanFrame extends JPanel {

    private JTable tableLaporan;
    private DefaultTableModel tableModel;
    private JComboBox<String> cmbFilter;
    private JLabel lblTotalPendapatan, lblTotalTransaksi;

    private final LaporanService laporanService;
    private final NumberFormat formatter;

    private final Color PRIMARY = new Color(79, 70, 229);
    private final Color SECONDARY = new Color(14, 165, 233);
    private final Color SUCCESS = new Color(16, 185, 129);
    private final Color DANGER = new Color(244, 63, 94);
    private final Color BG_COLOR = new Color(241, 245, 249);
    private final Color WHITE = Color.WHITE;

    public LaporanFrame() {
        this.laporanService = new LaporanService();
        this.formatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        setLayout(new BorderLayout(15, 15));
        setBackground(BG_COLOR);
        setBorder(new EmptyBorder(20, 20, 20, 20));
        initComponents();
        loadData("semua");
    }

    private void initComponents() {
        JLabel lblTitle = new JLabel("Laporan Penjualan");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(PRIMARY);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BG_COLOR);
        headerPanel.add(lblTitle, BorderLayout.WEST);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        filterPanel.setBackground(BG_COLOR);

        JLabel lblFilter = new JLabel("Filter:");
        lblFilter.setFont(new Font("Segoe UI", Font.BOLD, 12));
        filterPanel.add(lblFilter);

        cmbFilter = new JComboBox<>(new String[]{"Semua", "Hari Ini", "Bulan Ini", "Tahun Ini"});
        cmbFilter.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cmbFilter.setPreferredSize(new Dimension(150, 30));
        cmbFilter.addActionListener(e -> {
            String selected = cmbFilter.getSelectedItem().toString().toLowerCase().replace(" ", "");
            loadData(selected);
        });
        filterPanel.add(cmbFilter);

        headerPanel.add(filterPanel, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        JPanel statsPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        statsPanel.setBackground(BG_COLOR);
        statsPanel.setPreferredSize(new Dimension(0, 80));

        JPanel card1 = createStatCard("Total Transaksi", "0", SECONDARY);
        lblTotalTransaksi = (JLabel) card1.getComponent(1);
        statsPanel.add(card1);

        JPanel card2 = createStatCard("Total Pendapatan", "Rp0", SUCCESS);
        lblTotalPendapatan = (JLabel) card2.getComponent(1);
        statsPanel.add(card2);

        JPanel centerPanel = new JPanel(new BorderLayout(0, 15));
        centerPanel.setBackground(BG_COLOR);
        centerPanel.add(statsPanel, BorderLayout.NORTH);

        String[] columns = {"No. Faktur", "Tanggal", "Customer", "Barang", "Jumlah", "Harga Satuan", "Subtotal"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tableLaporan = new JTable(tableModel);
        tableLaporan.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tableLaporan.setRowHeight(32);
        tableLaporan.setGridColor(new Color(220, 220, 220));
        tableLaporan.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tableLaporan.getTableHeader().setBackground(PRIMARY);
        tableLaporan.getTableHeader().setForeground(new Color(0, 0, 0));
        tableLaporan.getTableHeader().setPreferredSize(new Dimension(0, 38));

        JScrollPane scrollPane = new JScrollPane(tableLaporan);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(203, 213, 225)));
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        btnPanel.setBackground(BG_COLOR);

        JButton btnExportPDF = createExportButton("Export PDF", DANGER);
        JButton btnExportExcel = createExportButton("Export Excel", SUCCESS);

        btnExportPDF.addActionListener(e -> exportPDF());
        btnExportExcel.addActionListener(e -> exportExcel());

        btnPanel.add(btnExportPDF);
        btnPanel.add(btnExportExcel);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private JPanel createStatCard(String title, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(WHITE);
        card.setBorder(new CompoundBorder(
                new MatteBorder(0, 5, 0, 0, color),
                new EmptyBorder(15, 20, 15, 20)));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblTitle.setForeground(new Color(127, 140, 141));
        card.add(lblTitle, BorderLayout.NORTH);

        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblValue.setForeground(color);
        card.add(lblValue, BorderLayout.CENTER);

        return card;
    }

    private JButton createExportButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setBorderPainted(false);
        btn.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(12, 25, 12, 25));
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

    private void loadData(String filter) {
        tableModel.setRowCount(0);

        List<DetailPenjualan> details = laporanService.getDetailPenjualan(0); // 0 = semua
        // Note: Untuk laporan dengan detail, perlu query khusus. Simplified untuk demo.

        int totalTransaksi = laporanService.getTotalTransaksi(filter);
        double totalPendapatan = laporanService.getTotalPendapatan(filter);

        lblTotalTransaksi.setText(String.valueOf(totalTransaksi));
        lblTotalPendapatan.setText(formatter.format(totalPendapatan));
    }

    private void exportPDF() {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("Laporan_Penjualan_" + LocalDate.now() + ".pdf"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File file = fc.getSelectedFile();
        if (!file.getName().endsWith(".pdf")) {
            file = new File(file.getAbsolutePath() + ".pdf");
        }

        Document document = new Document(PageSize.A4.rotate());
        try {
            PdfWriter.getInstance(document, new FileOutputStream(file));
            document.open();

            com.lowagie.text.Font titleFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 18, com.lowagie.text.Font.BOLD);
            Paragraph title = new Paragraph("LAPORAN PENJUALAN - TOKO BERKAH JAYA", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(10);
            document.add(title);

            Paragraph periode = new Paragraph("Periode: " + cmbFilter.getSelectedItem().toString()
                    + " | Tanggal Cetak: " + LocalDate.now());
            periode.setAlignment(Element.ALIGN_CENTER);
            periode.setSpacingAfter(20);
            document.add(periode);

            PdfPTable pdfTable = new PdfPTable(7);
            pdfTable.setWidthPercentage(100);
            pdfTable.setWidths(new float[]{2f, 1.5f, 2f, 2.5f, 1f, 1.5f, 1.5f});

            String[] headers = {"No. Faktur", "Tanggal", "Customer", "Barang", "Jumlah", "Harga", "Subtotal"};
            com.lowagie.text.Font headerFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 10, com.lowagie.text.Font.BOLD, Color.WHITE);
            com.lowagie.text.Font cellFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 9, com.lowagie.text.Font.NORMAL);

            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
                cell.setBackgroundColor(new Color(44, 62, 80));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(6);
                pdfTable.addCell(cell);
            }

            for (int i = 0; i < tableModel.getRowCount(); i++) {
                for (int j = 0; j < tableModel.getColumnCount(); j++) {
                    PdfPCell cell = new PdfPCell(new Phrase(tableModel.getValueAt(i, j).toString(), cellFont));
                    cell.setPadding(5);
                    pdfTable.addCell(cell);
                }
            }

            document.add(pdfTable);
            document.close();

            JOptionPane.showMessageDialog(this,
                    "Laporan berhasil diexport ke PDF!\n" + file.getAbsolutePath(),
                    "Sukses", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error export PDF: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportExcel() {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("Laporan_Penjualan_" + LocalDate.now() + ".xlsx"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File file = fc.getSelectedFile();
        if (!file.getName().endsWith(".xlsx")) {
            file = new File(file.getAbsolutePath() + ".xlsx");
        }

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Laporan Penjualan");

            CellStyle titleStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 16);
            titleStyle.setFont(titleFont);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);

            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("LAPORAN PENJUALAN - TOKO BERKAH JAYA");
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 6));

            Row periodeRow = sheet.createRow(1);
            periodeRow.createCell(0).setCellValue("Periode: " + cmbFilter.getSelectedItem().toString()
                    + " | Tanggal Cetak: " + LocalDate.now());
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(1, 1, 0, 6));

            Row headerRow = sheet.createRow(3);
            String[] headers = {"No. Faktur", "Tanggal", "Customer", "Barang", "Jumlah", "Harga", "Subtotal"};
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }

            for (int i = 0; i < tableModel.getRowCount(); i++) {
                Row row = sheet.createRow(i + 4);
                for (int j = 0; j < tableModel.getColumnCount(); j++) {
                    row.createCell(j).setCellValue(tableModel.getValueAt(i, j).toString());
                }
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            try (FileOutputStream fos = new FileOutputStream(file)) {
                workbook.write(fos);
            }

            JOptionPane.showMessageDialog(this,
                    "Laporan berhasil diexport ke Excel!\n" + file.getAbsolutePath(),
                    "Sukses", JOptionPane.INFORMATION_MESSAGE);

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error export Excel: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}