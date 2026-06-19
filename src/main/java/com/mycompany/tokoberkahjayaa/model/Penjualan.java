package com.mycompany.tokoberkahjayaa.model;

import java.time.LocalDate;

public class Penjualan {
    private int idJual;
    private String noFaktur;
    private LocalDate tglTransaksi;
    private String idCustomer;
    private String namaCustomer;
    private double totalBayar;
    private int idUser;

    public Penjualan() {}

    public Penjualan(int idJual, String noFaktur, LocalDate tglTransaksi, 
                     String idCustomer, double totalBayar, int idUser) {
        this.idJual = idJual;
        this.noFaktur = noFaktur;
        this.tglTransaksi = tglTransaksi;
        this.idCustomer = idCustomer;
        this.totalBayar = totalBayar;
        this.idUser = idUser;
    }

    public int getIdJual() { return idJual; }
    public void setIdJual(int idJual) { this.idJual = idJual; }

    public String getNoFaktur() { return noFaktur; }
    public void setNoFaktur(String noFaktur) { this.noFaktur = noFaktur; }

    public LocalDate getTglTransaksi() { return tglTransaksi; }
    public void setTglTransaksi(LocalDate tglTransaksi) { this.tglTransaksi = tglTransaksi; }

    public String getIdCustomer() { return idCustomer; }
    public void setIdCustomer(String idCustomer) { this.idCustomer = idCustomer; }

    public String getNamaCustomer() { return namaCustomer; }
    public void setNamaCustomer(String namaCustomer) { this.namaCustomer = namaCustomer; }

    public double getTotalBayar() { return totalBayar; }
    public void setTotalBayar(double totalBayar) { this.totalBayar = totalBayar; }

    public int getIdUser() { return idUser; }
    public void setIdUser(int idUser) { this.idUser = idUser; }
}
