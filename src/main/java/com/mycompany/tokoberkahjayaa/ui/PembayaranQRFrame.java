package com.mycompany.tokoberkahjayaa.ui;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class PembayaranQRFrame extends JPanel {

    private JLabel lblQRCode;
    private JTextField txtNoFaktur, txtTotal, txtStatus;
    private JButton btnGenerate, btnReset;
    private JLabel lblInfo;

    private HttpServer server;
    private int serverPort = 8765;
    private String baseUrl;
    private boolean serverRunning = false;

    private volatile String currentNoFaktur = "";
    private volatile double currentTotal = 0;
    private volatile boolean pembayaranSelesai = false;

    private final Color PRIMARY = new Color(44, 62, 80);
    private final Color SECONDARY = new Color(52, 152, 219);
    private final Color SUCCESS = new Color(39, 174, 96);
    private final Color DANGER = new Color(231, 76, 60);
    private final Color BG_COLOR = new Color(236, 240, 241);
    private final Color WHITE = Color.WHITE;

    public PembayaranQRFrame() {
        setLayout(new BorderLayout(15, 15));
        setBackground(BG_COLOR);
        setBorder(new EmptyBorder(20, 20, 20, 20));
        initComponents();
        startServer();
    }

    private void initComponents() {
        JLabel lblTitle = new JLabel("Pembayaran via QR Code");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(PRIMARY);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BG_COLOR);
        headerPanel.add(lblTitle, BorderLayout.WEST);
        add(headerPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        centerPanel.setBackground(BG_COLOR);

        JPanel qrPanel = new JPanel(new BorderLayout());
        qrPanel.setBackground(WHITE);
        qrPanel.setBorder(new CompoundBorder(
                new LineBorder(new Color(220, 220, 220), 1),
                new EmptyBorder(25, 25, 25, 25)));

        lblQRCode = new JLabel("QR Code akan muncul di sini", SwingConstants.CENTER);
        lblQRCode.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblQRCode.setForeground(new Color(149, 165, 166));
        lblQRCode.setPreferredSize(new Dimension(300, 300));
        lblQRCode.setOpaque(true);
        lblQRCode.setBackground(new Color(248, 248, 248));
        lblQRCode.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        qrPanel.add(lblQRCode, BorderLayout.CENTER);

        JLabel lblScan = new JLabel("Scan QR Code dengan HP Anda", SwingConstants.CENTER);
        lblScan.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblScan.setForeground(PRIMARY);
        lblScan.setBorder(new EmptyBorder(15, 0, 0, 0));
        qrPanel.add(lblScan, BorderLayout.SOUTH);

        centerPanel.add(qrPanel);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(WHITE);
        formPanel.setBorder(new CompoundBorder(
                new LineBorder(new Color(220, 220, 220), 1),
                new EmptyBorder(25, 25, 25, 25)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 5, 8, 5);
        gbc.weightx = 1;

        JLabel lblFormTitle = new JLabel("Pilih Transaksi");
        lblFormTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblFormTitle.setForeground(PRIMARY);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 5, 15, 5);
        formPanel.add(lblFormTitle, gbc);

        gbc.gridwidth = 1;
        gbc.insets = new Insets(8, 5, 8, 5);

        JLabel lblFaktur = new JLabel("No. Faktur:");
        lblFaktur.setFont(new Font("Segoe UI", Font.BOLD, 12));
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(lblFaktur, gbc);

        txtNoFaktur = new JTextField(20);
        txtNoFaktur.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtNoFaktur.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(200, 200, 200)),
                new EmptyBorder(10, 12, 10, 12)));
        gbc.gridx = 1; gbc.gridy = 1;
        formPanel.add(txtNoFaktur, gbc);

        JLabel lblTot = new JLabel("Total (Rp):");
        lblTot.setFont(new Font("Segoe UI", Font.BOLD, 12));
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(lblTot, gbc);

        txtTotal = new JTextField(20);
        txtTotal.setFont(new Font("Segoe UI", Font.BOLD, 14));
        txtTotal.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(200, 200, 200)),
                new EmptyBorder(10, 12, 10, 12)));
        gbc.gridx = 1; gbc.gridy = 2;
        formPanel.add(txtTotal, gbc);

        btnGenerate = new JButton("Generate QR Code");
        btnGenerate.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnGenerate.setForeground(WHITE);
        btnGenerate.setBackground(SECONDARY);
        btnGenerate.setFocusPainted(false);
        btnGenerate.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
        btnGenerate.setCursor(new Cursor(Cursor.HAND_CURSOR));
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 5, 10, 5);
        formPanel.add(btnGenerate, gbc);

        JLabel lblStat = new JLabel("Status Pembayaran:");
        lblStat.setFont(new Font("Segoe UI", Font.BOLD, 12));
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        gbc.insets = new Insets(15, 5, 5, 5);
        formPanel.add(lblStat, gbc);

        txtStatus = new JTextField("BELUM DIBAYAR");
        txtStatus.setFont(new Font("Segoe UI", Font.BOLD, 16));
        txtStatus.setForeground(DANGER);
        txtStatus.setHorizontalAlignment(JTextField.CENTER);
        txtStatus.setEditable(false);
        txtStatus.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(DANGER),
                new EmptyBorder(10, 15, 10, 15)));
        gbc.gridy = 5;
        formPanel.add(txtStatus, gbc);

        lblInfo = new JLabel("<html><center>Server berjalan di: -<br>Scan QR code untuk membayar</center></html>", SwingConstants.CENTER);
        lblInfo.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblInfo.setForeground(new Color(127, 140, 141));
        gbc.gridy = 6;
        gbc.insets = new Insets(10, 5, 5, 5);
        formPanel.add(lblInfo, gbc);

        btnReset = new JButton("Reset");
        btnReset.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnReset.setForeground(WHITE);
        btnReset.setBackground(new Color(149, 165, 166));
        btnReset.setFocusPainted(false);
        btnReset.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btnReset.setCursor(new Cursor(Cursor.HAND_CURSOR));
        gbc.gridy = 7;
        formPanel.add(btnReset, gbc);

        btnGenerate.addActionListener(e -> generateQR());
        btnReset.addActionListener(e -> resetForm());

        centerPanel.add(formPanel);
        add(centerPanel, BorderLayout.CENTER);

        JTextArea txtInfo = new JTextArea(
                "CARA BAYAR:"
                + "1. Masukkan No. Faktur dan Total Bayar "
                + "2. Klik 'Generate QR Code' "
                + "3. Scan QR code dengan kamera HP"
                + "4. Masukkan nominal pembayaran di halaman web"
                + "5. Klik 'Bayar' - status akan otomatis terupdate"
                + "Catatan: Pastikan HP dan komputer terhubung ke jaringan WiFi yang sama."
        );
        txtInfo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtInfo.setBackground(BG_COLOR);
        txtInfo.setForeground(PRIMARY);
        txtInfo.setEditable(false);
        txtInfo.setBorder(new EmptyBorder(10, 5, 10, 5));
        add(txtInfo, BorderLayout.SOUTH);
    }

    private void startServer() {
        try {
            server = HttpServer.create(new InetSocketAddress(serverPort), 0);
            server.createContext("/bayar", new BayarHandler());
            server.createContext("/proses", new ProsesHandler());
            server.createContext("/sukses", new SuksesHandler());
            server.setExecutor(null);
            server.start();
            serverRunning = true;

            String ip = getLocalIpAddress();
            baseUrl = "http://" + ip + ":" + serverPort;
            lblInfo.setText("<html><center>Server aktif: " + baseUrl + "<br>Scan QR code untuk membayar</center></html>");

        } catch (IOException e) {
            lblInfo.setText("Gagal start server: " + e.getMessage());
        }
    }

    private String getLocalIpAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            return "localhost";
        }
    }

    private void generateQR() {
        String noFaktur = txtNoFaktur.getText().trim();
        String total = txtTotal.getText().trim();

        if (noFaktur.isEmpty() || total.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No. Faktur dan Total harus diisi!", "Validasi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Double.parseDouble(total);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Total harus berupa angka!", "Validasi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        currentNoFaktur = noFaktur;
        currentTotal = Double.parseDouble(total);
        pembayaranSelesai = false;

        String qrUrl = baseUrl + "/bayar?faktur=" + noFaktur + "&total=" + total;

        try {
            BitMatrix matrix = new com.google.zxing.qrcode.QRCodeWriter().encode(
                    qrUrl, BarcodeFormat.QR_CODE, 300, 300);
            BufferedImage image = MatrixToImageWriter.toBufferedImage(matrix);
            lblQRCode.setText("");
            lblQRCode.setIcon(new ImageIcon(image));
            lblQRCode.setBackground(WHITE);

            txtStatus.setText("MENUNGGU PEMBAYARAN...");
            txtStatus.setForeground(new Color(243, 156, 18));
            txtStatus.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(new Color(243, 156, 18)),
                    new EmptyBorder(10, 15, 10, 15)));

            startPollingStatus();

        } catch (WriterException e) {
            JOptionPane.showMessageDialog(this, "Error generate QR: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void startPollingStatus() {
        Thread pollThread = new Thread(() -> {
            int attempts = 0;
            while (attempts < 120 && !pembayaranSelesai) {
                try {
                    Thread.sleep(2000);
                    if (pembayaranSelesai) {
                        SwingUtilities.invokeLater(() -> {
                            txtStatus.setText("PEMBAYARAN BERHASIL!");
                            txtStatus.setForeground(SUCCESS);
                            txtStatus.setBorder(BorderFactory.createCompoundBorder(
                                    new LineBorder(SUCCESS),
                                    new EmptyBorder(10, 15, 10, 15)));
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

    private void resetForm() {
        txtNoFaktur.setText("");
        txtTotal.setText("");
        txtStatus.setText("BELUM DIBAYAR");
        txtStatus.setForeground(DANGER);
        txtStatus.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(DANGER),
                new EmptyBorder(10, 15, 10, 15)));
        lblQRCode.setIcon(null);
        lblQRCode.setText("QR Code akan muncul di sini");
        lblQRCode.setBackground(new Color(248, 248, 248));
        pembayaranSelesai = false;
        currentNoFaktur = "";
        currentTotal = 0;
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

    public void stopServer() {
        if (server != null) {
            server.stop(0);
            serverRunning = false;
        }
    }
}
