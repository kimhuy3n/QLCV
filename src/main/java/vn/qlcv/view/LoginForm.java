package vn.qlcv.view;

import vn.qlcv.controller.AuthController;
import vn.qlcv.database.DBConnection;
import javax.swing.*;
import java.awt.*;

public class LoginForm extends JFrame {
    private final JTextField username=new JTextField(20);
    private final JPasswordField password=new JPasswordField(20);
    private final AuthController auth=new AuthController();

    public LoginForm(){
        super("QLCV - Đăng nhập"); setDefaultCloseOperation(EXIT_ON_CLOSE); setResizable(false);
        JPanel form=Ui.form(); form.add(new JLabel("Tên đăng nhập:"));form.add(username);form.add(new JLabel("Mật khẩu:"));form.add(password);
        JButton login=new JButton("Đăng nhập"); JButton register=new JButton("Đăng ký"); JPanel actions=new JPanel();actions.add(login);actions.add(register);
        add(new JLabel("ĐĂNG NHẬP",SwingConstants.CENTER),BorderLayout.NORTH);add(form);add(actions,BorderLayout.SOUTH);
        login.addActionListener(e->login());register.addActionListener(e->new RegisterForm(this,auth).setVisible(true));password.addActionListener(e->login());
        pack();setLocationRelativeTo(null);
    }
    private void login(){try{var user=auth.login(username.getText(),new String(password.getPassword()));if(user.isEmpty()){JOptionPane.showMessageDialog(this,"Sai tên đăng nhập hoặc mật khẩu.");return;}dispose();new DashboardForm(user.get()).setVisible(true);}catch(Exception e){Ui.error(this,e);}}
    public static void verifyDatabase(Component parent){
        try {
            DBConnection.testConnection();
            DBConnection.ensureSchema();
        } catch(Exception e){
            Ui.error(parent,new IllegalStateException("Không kết nối được MySQL hoặc chưa nâng schema tasks.status.\nHãy khởi động MySQL và import database/qlcv.sql.\n"+e.getMessage()));
        }
    }
}
