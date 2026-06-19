package com.mycompany.tokoberkahjayaa.process;

import com.mycompany.tokoberkahjayaa.model.Penjualan;
import com.mycompany.tokoberkahjayaa.model.DetailPenjualan;
import com.mycompany.tokoberkahjayaa.data.PenjualanRepository;
import java.util.List;

public class LaporanService {

    private final PenjualanRepository penjualanRepo;

    public LaporanService() {
        this.penjualanRepo = new PenjualanRepository();
    }

    public List<Penjualan> getLaporan(String filter) {
        return penjualanRepo.findAllWithFilter(filter);
    }

    public List<DetailPenjualan> getDetailPenjualan(int idJual) {
        return penjualanRepo.findDetailsByIdJual(idJual);
    }

    public int getTotalTransaksi(String filter) {
        return penjualanRepo.countTransactions(filter);
    }

    public double getTotalPendapatan(String filter) {
        return penjualanRepo.sumTotalPendapatan(filter);
    }

    public String[] getAvailableFilters() {
        return new String[]{"semua", "hariini", "bulanini", "tahunini"};
    }
}
