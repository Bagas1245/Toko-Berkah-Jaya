package com.mycompany.tokoberkahjayaa.model;

public class DetailPenjualan {
    private int idDetail;
    private int idJual;
    private String idBarang;
    private String namaBarang;
    private double hargaSatuan;
    private int jumlahBeli;
    private double subtotal;

    public DetailPenjualan() {}

    public DetailPenjualan(int idDetail, int idJual, String idBarang, 
                           double hargaSatuan, int jumlahBeli, double subtotal) {
        this.idDetail = idDetail;
        this.idJual = idJual;
        this.idBarang = idBarang;
        this.hargaSatuan = hargaSatuan;
        this.jumlahBeli = jumlahBeli;
        this.subtotal = subtotal;
    }

    public int getIdDetail() { return idDetail; }
    public void setIdDetail(int idDetail) { this.idDetail = idDetail; }

    public int getIdJual() { return idJual; }
    public void setIdJual(int idJual) { this.idJual = idJual; }

    public String getIdBarang() { return idBarang; }
    public void setIdBarang(String idBarang) { this.idBarang = idBarang; }

    public String getNamaBarang() { return namaBarang; }
    public void setNamaBarang(String namaBarang) { this.namaBarang = namaBarang; }

    public double getHargaSatuan() { return hargaSatuan; }
    public void setHargaSatuan(double hargaSatuan) { this.hargaSatuan = hargaSatuan; }

    public int getJumlahBeli() { return jumlahBeli; }
    public void setJumlahBeli(int jumlahBeli) { this.jumlahBeli = jumlahBeli; }

    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }
}
