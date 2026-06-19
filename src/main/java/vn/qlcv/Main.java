package vn.qlcv;

import vn.qlcv.view.LoginForm;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
            catch (Exception ignored) { }
            LoginForm form = new LoginForm();
            form.setVisible(true);
            LoginForm.verifyDatabase(form);
        });
    }
}
