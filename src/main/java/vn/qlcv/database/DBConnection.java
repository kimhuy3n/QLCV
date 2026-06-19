package vn.qlcv.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DBConnection {
    private static final String URL = env("QLCV_DB_URL",
            "jdbc:mysql://localhost:3306/qlcv?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Ho_Chi_Minh");
    private static final String USER = env("QLCV_DB_USER", "root");
    private static final String PASSWORD = env("QLCV_DB_PASSWORD", "");

    private DBConnection() {}

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void testConnection() throws SQLException {
        try (Connection ignored = getConnection()) { }
    }

    public static void ensureSchema() throws SQLException {
        try (Connection c = getConnection();
             var st = c.createStatement()) {
            st.executeUpdate("""
                    ALTER TABLE tasks
                    MODIFY COLUMN status ENUM('PENDING', 'IN_PROGRESS', 'COMPLETED')
                    NOT NULL DEFAULT 'PENDING'
                    """);
        }
    }

    private static String env(String name, String fallback) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? fallback : value;
    }
}
