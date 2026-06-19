package vn.qlcv.dao;

import vn.qlcv.database.DBConnection;
import vn.qlcv.model.Category;
import java.sql.*;
import java.util.*;

public class CategoryDAO {
    public List<Category> findAll(int userId) throws SQLException {
        List<Category> result = new ArrayList<>();
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(
                "SELECT id,category_name,user_id FROM categories WHERE user_id=? ORDER BY category_name")) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(new Category(rs.getInt(1), rs.getString(2), rs.getInt(3)));
            }
        }
        return result;
    }

    public void insert(String name, int userId) throws SQLException { execute("INSERT INTO categories(category_name,user_id) VALUES(?,?)", name, userId, 0); }
    public void update(int id, String name, int userId) throws SQLException { execute("UPDATE categories SET category_name=? WHERE user_id=? AND id=?", name, userId, id); }
    public void delete(int id, int userId) throws SQLException {
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement("DELETE FROM categories WHERE id=? AND user_id=?")) {
            ps.setInt(1, id); ps.setInt(2, userId); ps.executeUpdate();
        }
    }
    private void execute(String sql, String name, int userId, int id) throws SQLException {
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, name); ps.setInt(2, userId); if (id > 0) ps.setInt(3, id); ps.executeUpdate();
        }
    }
}
