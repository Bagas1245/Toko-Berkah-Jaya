package com.mycompany.tokoberkahjayaa.data;

import com.mycompany.tokoberkahjayaa.model.Kategori;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class KategoriRepository {

    public List<Kategori> findAll() {
        List<Kategori> list = new ArrayList<>();
        String sql = "SELECT * FROM tb_kategori ORDER BY nama_kategori";
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

    public Kategori findByName(String nama) {
        String sql = "SELECT * FROM tb_kategori WHERE nama_kategori = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nama);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapResultSet(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int getIdByName(String nama) {
        Kategori k = findByName(nama);
        return k != null ? k.getIdKategori() : -1;
    }

    private Kategori mapResultSet(ResultSet rs) throws SQLException {
        return new Kategori(rs.getInt("id_kategori"), rs.getString("nama_kategori"));
    }
}
