package com.mycompany.tokoberkahjayaa.process;

import com.mycompany.tokoberkahjayaa.model.Penjualan;
import com.mycompany.tokoberkahjayaa.model.DetailPenjualan;
import com.mycompany.tokoberkahjayaa.model.Barang;
import com.mycompany.tokoberkahjayaa.data.PenjualanRepository;
import com.mycompany.tokoberkahjayaa.data.BarangRepository;
import com.mycompany.tokoberkahjayaa.data.DatabaseConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class TransaksiService {

    private final PenjualanRepository penjualanRepo;
    private final BarangRepository barangRepo;

    public TransaksiService() {
        this.penjualanRepo = new PenjualanRepository();
        this.barangRepo = new BarangRepository();
    }

    public String generateNoFaktur() {
        int count = penjualanRepo.getTodayTransactionCount();
        return String.format("FK-%s-%04d", 
            LocalDate.now().toString().replace("-", ""), count + 1);
    }

    public boolean simpanTransaksi(Penjualan penjualan, List<DetailPenjualan> details) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            if (conn == null) return false;

            conn.setAutoCommit(false);

            // 1. Simpan penjualan
            int idJual = penjualanRepo.save(penjualan);
            if (idJual == 0) {
                conn.rollback();
                return false;
            }

            // 2. Simpan detail dan update stok
            for (DetailPenjualan detail : details) {
                detail.setIdJual(idJual);
                if (!penjualanRepo.saveDetail(detail)) {
                    conn.rollback();
                    return false;
                }
                if (!barangRepo.updateStok(detail.getIdBarang(), detail.getJumlahBeli())) {
                    conn.rollback();
                    return false;
                }
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (conn != null) conn.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean validasiTransaksi(Penjualan penjualan, List<DetailPenjualan> details) {
        if (penjualan == null || details == null || details.isEmpty()) return false;
        if (penjualan.getIdCustomer() == null || penjualan.getIdCustomer().trim().isEmpty()) return false;
        if (penjualan.getTotalBayar() <= 0) return false;

        for (DetailPenjualan d : details) {
            if (d.getIdBarang() == null || d.getJumlahBeli() <= 0 || d.getSubtotal() <= 0) {
                return false;
            }
        }
        return true;
    }

    public double hitungTotal(List<DetailPenjualan> details) {
        double total = 0;
        for (DetailPenjualan d : details) {
            total += d.getSubtotal();
        }
        return total;
    }
}
