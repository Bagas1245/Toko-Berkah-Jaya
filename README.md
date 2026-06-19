# Toko-Berkah-Jaya
#Desktop App with NetBeans IDE

APLIKASI TOKO BERKAH JAYA

DEPENDENCY (otomatis di-download Maven):
------------
- MySQL Connector/J 8.0.33
- ZXing Core 3.5.1 (QR Code)
- ZXing Java SE 3.5.1 (QR Image)
- Apache POI 5.2.3 (Export Excel)
- Apache POI OOXML 5.2.3 (Export .xlsx)
- OpenPDF 1.3.30 (Export PDF)

CARA SETUP:

1. IMPORT DATABASE:
   - Buka phpMyAdmin atau MySQL CLI
   - Jalankan file: database_toko_berkah_jayaa.sql
   - Database 'toko_berkah_jayaa' akan dibuat otomatis

2. LETAKKAN FILE JAVA:
   Masukkan semua file .java ke dalam folder project NetBeans:
   src/main/java/com/mycompany/tokoberkahjayaa/

3. LETAKKAN POM.XML:
   Ganti file pom.xml di root project dengan yang baru

4. BUILD DENGAN MAVEN:
   - Klik kanan project -> Clean and Build
   - Atau jalankan: mvn clean install

5. JALANKAN APLIKASI:
   - Klik Run Project di NetBeans
   - Atau jalankan: mvn exec:java

AKUN LOGIN:

Username: admin      Password: admin123     (Level: Admin)
Username: kasir      Password: kasir123     (Level: Kasir)

FITUR APLIKASI:

1. LOGIN
   - Sistem autentikasi dengan tb_user
   - Password plaintext (untuk tugas praktikum)

2. MANAJEMEN BARANG (CRUD)
   - Tambah, edit, hapus data barang
   - Pilih kategori dari dropdown
   - Validasi input

3. MANAJEMEN CUSTOMER (CRUD)
   - Tambah, edit, hapus data customer
   - Validasi input

4. TRANSAKSI PENJUALAN
   - Pilih customer dan barang dari combo box
   - Harga otomatis terisi saat pilih barang
   - Keranjang belanja (bisa multiple barang)
   - Validasi stok otomatis
   - Pengurangan stok otomatis setelah simpan
   - Generate nomor faktur otomatis

5. LAPORAN PENJUALAN
   - Tampilkan riwayat transaksi
   - Filter: Semua / Hari Ini / Bulan Ini / Tahun Ini
   - Statistik total transaksi dan pendapatan
   - EXPORT ke PDF (menggunakan OpenPDF)
   - EXPORT ke Excel .xlsx (menggunakan Apache POI)

6. PEMBAYARAN QR CODE
   - Generate QR code untuk pembayaran
   - QR code berisi URL ke embedded web server
   - Scan QR dengan HP -> buka halaman web pembayaran
   - Input nominal -> klik Bayar
   - Status aplikasi desktop otomatis terupdate

============================================================
CARA MENGGUNAKAN PEMBAYARAN QR:
============================================================

1. Pastikan komputer dan HP terhubung ke WiFi yang SAMA
2. Klik "Generate QR Code"
3. Scan QR code dengan HP
4. Akan terbuka halaman web pembayaran di HP
5. Masukkan nominal pembayaran
6. Klik "BAYAR SEKARANG"
7. Status di aplikasi desktop akan otomatis berubah

============================================================
CATATAN:
============================================================
- Port server QR: 8765
- Pastikan port 8765 tidak diblokir firewall
- Aplikasi menggunakan Java 22 (sesuai pom.xml)
- Koneksi database: localhost:3306, root, (tanpa password)
- Jika MySQL menggunakan password, ubah di DatabaseConnection.java

