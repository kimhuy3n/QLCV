package vn.qlcv.dao;

import vn.qlcv.database.DBConnection;
import vn.qlcv.model.User;
import vn.qlcv.security.PasswordHasher;

import java.sql.*;
import java.util.Optional;

public class UserDAO {
    public User register(String username, String password, String fullname, String email) throws SQLException {
        String sql = "INSERT INTO users(username,password,fullname,email) VALUES(?,?,?,?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, username.trim());
            ps.setString(2, PasswordHasher.hash(password));
            ps.setString(3, fullname.trim());
            ps.setString(4, email.trim());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                int id = rs.getInt(1);
                createDefaultCategories(c, id);
                return new User(id, username.trim(), fullname.trim(), email.trim());
            }
        }
    }

    public Optional<User> login(String username, String password) throws SQLException {
        String sql = "SELECT id,username,password,fullname,email FROM users WHERE username=?";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && PasswordHasher.verify(password, rs.getString("password"))) {
                    return Optional.of(new User(rs.getInt("id"), rs.getString("username"),
                            rs.getString("fullname"), rs.getString("email")));
                }
                return Optional.empty();
            }
        }
    }

    public boolean changePassword(int userId, String oldPassword, String newPassword) throws SQLException {
        String read = "SELECT password FROM users WHERE id=?";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(read)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next() || !PasswordHasher.verify(oldPassword, rs.getString(1))) return false;
            }
            try (PreparedStatement update = c.prepareStatement("UPDATE users SET password=? WHERE id=?")) {
                update.setString(1, PasswordHasher.hash(newPassword));
                update.setInt(2, userId);
                return update.executeUpdate() == 1;
            }
        }
    }

    private void createDefaultCategories(Connection c, int userId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO categories(category_name,user_id) VALUES(?,?)")) {
            for (String name : new String[]{"Học tập", "Công việc", "Cá nhân", "Khác"}) {
                ps.setString(1, name); ps.setInt(2, userId); ps.addBatch();
            }
            ps.executeBatch();
        }
    }
}
