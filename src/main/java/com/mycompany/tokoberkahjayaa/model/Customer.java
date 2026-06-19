package com.mycompany.tokoberkahjayaa.model;

public class Customer {
    private String idCustomer;
    private String namaCustomer;
    private String alamat;
    private String telepon;

    public Customer() {}

    public Customer(String idCustomer, String namaCustomer, String alamat, String telepon) {
        this.idCustomer = idCustomer;
        this.namaCustomer = namaCustomer;
        this.alamat = alamat;
        this.telepon = telepon;
    }

    public String getIdCustomer() { return idCustomer; }
    public void setIdCustomer(String idCustomer) { this.idCustomer = idCustomer; }

    public String getNamaCustomer() { return namaCustomer; }
    public void setNamaCustomer(String namaCustomer) { this.namaCustomer = namaCustomer; }

    public String getAlamat() { return alamat; }
    public void setAlamat(String alamat) { this.alamat = alamat; }

    public String getTelepon() { return telepon; }
    public void setTelepon(String telepon) { this.telepon = telepon; }

    @Override
    public String toString() {
        return idCustomer + " - " + namaCustomer;
    }
}
