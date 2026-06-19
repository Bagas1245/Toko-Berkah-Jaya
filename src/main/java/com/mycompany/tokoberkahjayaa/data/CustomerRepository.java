package com.mycompany.tokoberkahjayaa.data;

import com.mycompany.tokoberkahjayaa.model.Customer;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerRepository {

    public List<Customer> findAll() {
        List<Customer> list = new ArrayList<>();
        String sql = "SELECT * FROM tb_customer ORDER BY id_customer";
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

    public Customer findById(String idCustomer) {
        String sql = "SELECT * FROM tb_customer WHERE id_customer = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, idCustomer);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapResultSet(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean save(Customer customer) {
        String sql = "INSERT INTO tb_customer (id_customer, nama_customer, alamat, telepon) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, customer.getIdCustomer());
            ps.setString(2, customer.getNamaCustomer());
            ps.setString(3, customer.getAlamat());
            ps.setString(4, customer.getTelepon());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean update(Customer customer) {
        String sql = "UPDATE tb_customer SET nama_customer=?, alamat=?, telepon=? WHERE id_customer=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, customer.getNamaCustomer());
            ps.setString(2, customer.getAlamat());
            ps.setString(3, customer.getTelepon());
            ps.setString(4, customer.getIdCustomer());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(String idCustomer) {
        String sql = "DELETE FROM tb_customer WHERE id_customer = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, idCustomer);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public String getLastId() {
        String sql = "SELECT id_customer FROM tb_customer ORDER BY id_customer DESC LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getString("id_customer");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Customer mapResultSet(ResultSet rs) throws SQLException {
        return new Customer(
            rs.getString("id_customer"),
            rs.getString("nama_customer"),
            rs.getString("alamat"),
            rs.getString("telepon")
        );
    }
}
