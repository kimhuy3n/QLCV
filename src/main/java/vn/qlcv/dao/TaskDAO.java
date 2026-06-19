package vn.qlcv.dao;

import vn.qlcv.database.DBConnection;
import vn.qlcv.model.Task;
import vn.qlcv.model.TaskStatistics;
import java.sql.*;
import java.util.*;

public class TaskDAO {
    public List<Task> search(int userId, String keyword, String status, Integer categoryId) throws SQLException {
        StringBuilder sql = new StringBuilder("""
            SELECT t.id,t.title,t.description,t.created_date,t.deadline,t.status,t.category_id,
                   c.category_name,t.user_id FROM tasks t LEFT JOIN categories c ON c.id=t.category_id
            WHERE t.user_id=? AND t.title LIKE ?""");
        List<Object> params = new ArrayList<>(List.of(userId, "%" + keyword.trim() + "%"));
        if ("OVERDUE".equals(status)) {
            sql.append(" AND t.status<>'COMPLETED' AND t.deadline<CURRENT_DATE");
        } else if (status != null) {
            sql.append(" AND t.status=?"); params.add(status);
        }
        if (categoryId != null) { sql.append(" AND t.category_id=?"); params.add(categoryId); }
        sql.append(" ORDER BY t.status DESC,t.deadline,t.id DESC");
        List<Task> result = new ArrayList<>();
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql.toString())) {
            for (int i=0;i<params.size();i++) ps.setObject(i+1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) result.add(map(rs)); }
        }
        return result;
    }

    public void insert(Task t) throws SQLException {
        String sql = "INSERT INTO tasks(title,description,created_date,deadline,status,category_id,user_id) VALUES(?,?,CURRENT_DATE,?,?,?,?)";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, t, false);
            ps.executeUpdate();
        }
    }

    public void update(Task t) throws SQLException {
        String sql = "UPDATE tasks SET title=?,description=?,deadline=?,status=?,category_id=? WHERE id=? AND user_id=?";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, t, true);
            ps.executeUpdate();
        }
    }

    private void bind(PreparedStatement ps, Task t, boolean update) throws SQLException {
        ps.setString(1, t.title());
        ps.setString(2, t.description());
        ps.setDate(3, java.sql.Date.valueOf(t.deadline()));
        ps.setString(4, t.status() == null ? "PENDING" : t.status());
        if (t.categoryId() == null) ps.setNull(5, Types.INTEGER); else ps.setInt(5, t.categoryId());
        if (update) {
            ps.setInt(6, t.id());
            ps.setInt(7, t.userId());
        } else {
            ps.setInt(6, t.userId());
        }
    }
    public void delete(int id,int userId) throws SQLException { change("DELETE FROM tasks WHERE id=? AND user_id=?",id,userId); }
    public void markCompleted(int id,int userId) throws SQLException { change("UPDATE tasks SET status='COMPLETED' WHERE id=? AND user_id=?",id,userId); }
    public void markPending(int id,int userId) throws SQLException { change("UPDATE tasks SET status='PENDING' WHERE id=? AND user_id=?",id,userId); }
    public void changeStatus(int id, int userId, String status) throws SQLException { change("UPDATE tasks SET status=? WHERE id=? AND user_id=?", status, id, userId); }
    private void change(String sql,int id,int userId) throws SQLException { try(Connection c=DBConnection.getConnection();PreparedStatement ps=c.prepareStatement(sql)){ps.setInt(1,id);ps.setInt(2,userId);ps.executeUpdate();} }
    private void change(String sql, String status, int id, int userId) throws SQLException { try(Connection c=DBConnection.getConnection();PreparedStatement ps=c.prepareStatement(sql)){ps.setString(1,status);ps.setInt(2,id);ps.setInt(3,userId);ps.executeUpdate();} }

    public TaskStatistics statistics(int userId) throws SQLException {
        String sql="""
                SELECT COUNT(*) total,
                       SUM(status='COMPLETED') completed,
                       SUM(status<>'COMPLETED') pending,
                       SUM(status<>'COMPLETED' AND deadline<CURRENT_DATE) overdue
                FROM tasks WHERE user_id=?
                """;
        try(Connection c=DBConnection.getConnection();PreparedStatement ps=c.prepareStatement(sql)){ps.setInt(1,userId);try(ResultSet rs=ps.executeQuery()){rs.next();return new TaskStatistics(rs.getInt(1),rs.getInt(2),rs.getInt(3),rs.getInt(4));}}
    }
    private Task map(ResultSet rs) throws SQLException {
        int category = rs.getInt("category_id");
        Integer categoryId = rs.wasNull() ? null : category;
        return new Task(rs.getInt("id"), rs.getString("title"), rs.getString("description"),
                rs.getDate("created_date").toLocalDate(), rs.getDate("deadline").toLocalDate(),
                rs.getString("status"), categoryId, rs.getString("category_name"), rs.getInt("user_id"));
    }
}
