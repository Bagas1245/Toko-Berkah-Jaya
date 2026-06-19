package com.mycompany.tokoberkahjayaa.data;

import com.mycompany.tokoberkahjayaa.model.Penjualan;
import com.mycompany.tokoberkahjayaa.model.DetailPenjualan;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PenjualanRepository {

    public int getTodayTransactionCount() {
        String sql = "SELECT COUNT(*) as total FROM tb_penjualan WHERE DATE(tgl_transaksi) = CURDATE()";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt("total");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int save(Penjualan penjualan) throws SQLException {
        String sql = "INSERT INTO tb_penjualan (no_faktur, tgl_transaksi, id_customer, total_bayar, id_user) VALUES (?, ?, ?, ?, ?)";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, penjualan.getNoFaktur());
        ps.setDate(2, Date.valueOf(penjualan.getTglTransaksi()));
        ps.setString(3, penjualan.getIdCustomer());
        ps.setDouble(4, penjualan.getTotalBayar());
        ps.setInt(5, penjualan.getIdUser());
        ps.executeUpdate();

        ResultSet rs = ps.getGeneratedKeys();
        int id = 0;
        if (rs.next()) id = rs.getInt(1);
        rs.close();
        ps.close();
        return id;
    }

    public boolean saveDetail(DetailPenjualan detail) throws SQLException {
        String sql = "INSERT INTO tb_detail_penjualan (id_jual, id_barang, harga_satuan, jumlah_beli, subtotal) VALUES (?, ?, ?, ?, ?)";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, detail.getIdJual());
        ps.setString(2, detail.getIdBarang());
        ps.setDouble(3, detail.getHargaSatuan());
        ps.setInt(4, detail.getJumlahBeli());
        ps.setDouble(5, detail.getSubtotal());
        boolean result = ps.executeUpdate() > 0;
        ps.close();
        return result;
    }

    public List<Penjualan> findAllWithFilter(String filter) {
        List<Penjualan> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT p.*, c.nama_customer FROM tb_penjualan p " +
            "JOIN tb_customer c ON p.id_customer = c.id_customer WHERE 1=1 "
        );

        switch (filter.toLowerCase()) {
            case "hariini":
                sql.append("AND DATE(p.tgl_transaksi) = CURDATE() ");
                break;
            case "bulanini":
                sql.append("AND MONTH(p.tgl_transaksi) = MONTH(CURDATE()) AND YEAR(p.tgl_transaksi) = YEAR(CURDATE()) ");
                break;
            case "tahunini":
                sql.append("AND YEAR(p.tgl_transaksi) = YEAR(CURDATE()) ");
                break;
        }
        sql.append("ORDER BY p.tgl_transaksi DESC");

        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql.toString())) {
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<DetailPenjualan> findDetailsByIdJual(int idJual) {
        List<DetailPenjualan> list = new ArrayList<>();
        String sql = "SELECT d.*, b.nama_barang FROM tb_detail_penjualan d " +
                     "JOIN tb_barang b ON d.id_barang = b.id_barang WHERE d.id_jual = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idJual);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapDetailResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public int countTransactions(String filter) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) as total FROM tb_penjualan WHERE 1=1 ");
        switch (filter.toLowerCase()) {
            case "hariini":
                sql.append("AND DATE(tgl_transaksi) = CURDATE() ");
                break;
            case "bulanini":
                sql.append("AND MONTH(tgl_transaksi) = MONTH(CURDATE()) AND YEAR(tgl_transaksi) = YEAR(CURDATE()) ");
                break;
            case "tahunini":
                sql.append("AND YEAR(tgl_transaksi) = YEAR(CURDATE()) ");
                break;
        }
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql.toString())) {
            if (rs.next()) return rs.getInt("total");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public double sumTotalPendapatan(String filter) {
        StringBuilder sql = new StringBuilder("SELECT SUM(total_bayar) as total FROM tb_penjualan WHERE 1=1 ");
        switch (filter.toLowerCase()) {
            case "hariini":
                sql.append("AND DATE(tgl_transaksi) = CURDATE() ");
                break;
            case "bulanini":
                sql.append("AND MONTH(tgl_transaksi) = MONTH(CURDATE()) AND YEAR(tgl_transaksi) = YEAR(CURDATE()) ");
                break;
            case "tahunini":
                sql.append("AND YEAR(tgl_transaksi) = YEAR(CURDATE()) ");
                break;
        }
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql.toString())) {
            if (rs.next()) return rs.getDouble("total");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private Penjualan mapResultSet(ResultSet rs) throws SQLException {
        Penjualan p = new Penjualan();
        p.setIdJual(rs.getInt("id_jual"));
        p.setNoFaktur(rs.getString("no_faktur"));
        p.setTglTransaksi(rs.getDate("tgl_transaksi").toLocalDate());
        p.setIdCustomer(rs.getString("id_customer"));
        p.setNamaCustomer(rs.getString("nama_customer"));
        p.setTotalBayar(rs.getDouble("total_bayar"));
        p.setIdUser(rs.getInt("id_user"));
        return p;
    }

    private DetailPenjualan mapDetailResultSet(ResultSet rs) throws SQLException {
        DetailPenjualan d = new DetailPenjualan();
        d.setIdDetail(rs.getInt("id_detail"));
        d.setIdJual(rs.getInt("id_jual"));
        d.setIdBarang(rs.getString("id_barang"));
        d.setNamaBarang(rs.getString("nama_barang"));
        d.setHargaSatuan(rs.getDouble("harga_satuan"));
        d.setJumlahBeli(rs.getInt("jumlah_beli"));
        d.setSubtotal(rs.getDouble("subtotal"));
        return d;
    }
}
