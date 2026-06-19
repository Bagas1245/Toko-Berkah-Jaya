package com.mycompany.tokoberkahjayaa.process;

import com.mycompany.tokoberkahjayaa.model.Barang;
import com.mycompany.tokoberkahjayaa.data.BarangRepository;
import com.mycompany.tokoberkahjayaa.data.KategoriRepository;
import java.util.List;

public class BarangService {

    private final BarangRepository barangRepo;
    private final KategoriRepository kategoriRepo;

    public BarangService() {
        this.barangRepo = new BarangRepository();
        this.kategoriRepo = new KategoriRepository();
    }

    public List<Barang> getAllBarang() {
        return barangRepo.findAll();
    }

    public List<Barang> getBarangAvailable() {
        return barangRepo.findByStokAvailable();
    }

    public Barang getBarangById(String id) {
        return barangRepo.findById(id);
    }

    public boolean simpanBarang(Barang barang, String namaKategori) {
        if (!validasiBarang(barang)) return false;

        int idKategori = kategoriRepo.getIdByName(namaKategori);
        if (idKategori == -1) return false;

        barang.setIdKategori(idKategori);
        return barangRepo.save(barang);
    }

    public boolean updateBarang(Barang barang, String namaKategori) {
        if (!validasiBarang(barang)) return false;

        int idKategori = kategoriRepo.getIdByName(namaKategori);
        if (idKategori == -1) return false;

        barang.setIdKategori(idKategori);
        return barangRepo.update(barang);
    }

    public boolean hapusBarang(String idBarang) {
        return barangRepo.delete(idBarang);
    }

    public boolean validasiBarang(Barang barang) {
        if (barang == null) return false;
        if (barang.getIdBarang() == null || barang.getIdBarang().trim().isEmpty()) return false;
        if (barang.getNamaBarang() == null || barang.getNamaBarang().trim().isEmpty()) return false;
        if (barang.getSatuan() == null || barang.getSatuan().trim().isEmpty()) return false;
        if (barang.getHargaJual() <= 0) return false;
        if (barang.getStok() < 0) return false;
        return true;
    }

    public int getStok(String idBarang) {
        return barangRepo.getStok(idBarang);
    }

    public boolean cekStokCukup(String idBarang, int jumlahDibeli, int sudahDipesan) {
        int stok = getStok(idBarang);
        return (jumlahDibeli + sudahDipesan) <= stok;
    }

    public boolean kurangiStok(String idBarang, int qty) {
        return barangRepo.updateStok(idBarang, qty);
    }
    
    public String generateNewId() {
        String lastId = barangRepo.getLastId();
        if (lastId == null || lastId.trim().isEmpty()) {
            return "BRG01";
        }

        String numberPart = lastId.replaceAll("[^0-9]", "");
        String letterPart = lastId.replaceAll("[0-9]", "");

        if (numberPart.isEmpty()) {
            return lastId + "1";
        }

        int nextNumber = Integer.parseInt(numberPart) + 1;
        String format = "%0" + numberPart.length() + "d";
        return letterPart + String.format(format, nextNumber);
    }
}
