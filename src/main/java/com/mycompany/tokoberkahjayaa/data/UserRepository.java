package com.mycompany.tokoberkahjayaa.data;

import com.mycompany.tokoberkahjayaa.model.User;
import java.sql.*;

public class UserRepository {

    public User findByUsernameAndPassword(String username, String password) {
        String sql = "SELECT * FROM tb_user WHERE username = ? AND password = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private User mapResultSet(ResultSet rs) throws SQLException {
        User user = new User();
        user.setIdUser(rs.getInt("id_user"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setNamaLengkap(rs.getString("nama_lengkap"));
        user.setLevel(rs.getInt("level"));
        return user;
    }
}
