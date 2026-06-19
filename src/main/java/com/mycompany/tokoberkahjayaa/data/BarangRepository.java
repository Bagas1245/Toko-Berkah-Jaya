package com.mycompany.tokoberkahjayaa.data;

import com.mycompany.tokoberkahjayaa.model.Barang;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BarangRepository {

    public List<Barang> findAll() {
        List<Barang> list = new ArrayList<>();
        String sql = "SELECT b.*, k.nama_kategori FROM tb_barang b " +
                     "JOIN tb_kategori k ON b.id_kategori = k.id_kategori ORDER BY b.id_barang";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Barang findById(String idBarang) {
        String sql = "SELECT b.*, k.nama_kategori FROM tb_barang b " +
                     "JOIN tb_kategori k ON b.id_kategori = k.id_kategori WHERE b.id_barang = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, idBarang);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapResultSet(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Barang> findByStokAvailable() {
        List<Barang> list = new ArrayList<>();
        String sql = "SELECT b.*, k.nama_kategori FROM tb_barang b " +
                     "JOIN tb_kategori k ON b.id_kategori = k.id_kategori WHERE b.stok > 0 ORDER BY b.nama_barang";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean save(Barang barang) {
        String sql = "INSERT INTO tb_barang (id_barang, id_kategori, nama_barang, satuan, harga_jual, stok) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, barang.getIdBarang());
            ps.setInt(2, barang.getIdKategori());
            ps.setString(3, barang.getNamaBarang());
            ps.setString(4, barang.getSatuan());
            ps.setDouble(5, barang.getHargaJual());
            ps.setInt(6, barang.getStok());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean update(Barang barang) {
        String sql = "UPDATE tb_barang SET id_kategori=?, nama_barang=?, satuan=?, harga_jual=?, stok=? WHERE id_barang=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, barang.getIdKategori());
            ps.setString(2, barang.getNamaBarang());
            ps.setString(3, barang.getSatuan());
            ps.setDouble(4, barang.getHargaJual());
            ps.setInt(5, barang.getStok());
            ps.setString(6, barang.getIdBarang());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(String idBarang) {
        String sql = "DELETE FROM tb_barang WHERE id_barang = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, idBarang);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateStok(String idBarang, int qty) {
        String sql = "UPDATE tb_barang SET stok = stok - ? WHERE id_barang = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, qty);
            ps.setString(2, idBarang);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public int getStok(String idBarang) {
        String sql = "SELECT stok FROM tb_barang WHERE id_barang = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, idBarang);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("stok");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    public String getLastId() {
        String sql = "SELECT id_barang FROM tb_barang ORDER BY id_barang DESC LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getString("id_barang");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Barang mapResultSet(ResultSet rs) throws SQLException {
        Barang b = new Barang();
        b.setIdBarang(rs.getString("id_barang"));
        b.setIdKategori(rs.getInt("id_kategori"));
        b.setNamaKategori(rs.getString("nama_kategori"));
        b.setNamaBarang(rs.getString("nama_barang"));
        b.setSatuan(rs.getString("satuan"));
        b.setHargaJual(rs.getDouble("harga_jual"));
        b.setStok(rs.getInt("stok"));
        return b;
    }
}
