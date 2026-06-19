package vn.qlcv.controller;

import vn.qlcv.dao.UserDAO;
import vn.qlcv.model.User;
import java.sql.SQLException;
import java.util.Optional;
import java.util.regex.Pattern;

public class AuthController {
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private final UserDAO dao = new UserDAO();

    public Optional<User> login(String username, String password) throws SQLException {
        if (username.isBlank() || password.isBlank()) throw new IllegalArgumentException("Vui lòng nhập tên đăng nhập và mật khẩu.");
        return dao.login(username, password);
    }

    public User register(String username, String password, String confirm, String fullname, String email) throws SQLException {
        if (username.trim().length() < 3) throw new IllegalArgumentException("Tên đăng nhập phải có ít nhất 3 ký tự.");
        if (password.length() < 6) throw new IllegalArgumentException("Mật khẩu phải có ít nhất 6 ký tự.");
        if (!password.equals(confirm)) throw new IllegalArgumentException("Mật khẩu xác nhận không khớp.");
        if (fullname.isBlank()) throw new IllegalArgumentException("Vui lòng nhập họ tên.");
        if (!EMAIL.matcher(email.trim()).matches()) throw new IllegalArgumentException("Email không hợp lệ.");
        return dao.register(username, password, fullname, email);
    }

    public boolean changePassword(int userId, String oldPassword, String newPassword, String confirm) throws SQLException {
        if (newPassword.length() < 6) throw new IllegalArgumentException("Mật khẩu mới phải có ít nhất 6 ký tự.");
        if (!newPassword.equals(confirm)) throw new IllegalArgumentException("Mật khẩu xác nhận không khớp.");
        return dao.changePassword(userId, oldPassword, newPassword);
    }
}
