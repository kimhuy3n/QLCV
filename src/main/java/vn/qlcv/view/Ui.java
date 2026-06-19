package vn.qlcv.view;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

final class Ui {
    private Ui() {}
    static void error(Component parent, Exception e) {
        String message = e instanceof SQLException
                ? "Lỗi cơ sở dữ liệu: " + e.getMessage()
                : e.getMessage();
        JOptionPane.showMessageDialog(parent, message, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }
    static JPanel form() { JPanel p=new JPanel(new GridLayout(0,2,8,8)); p.setBorder(BorderFactory.createEmptyBorder(16,16,16,16)); return p; }
}
