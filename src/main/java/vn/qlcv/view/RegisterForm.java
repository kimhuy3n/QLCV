package vn.qlcv.view;

import vn.qlcv.controller.AuthController;
import javax.swing.*;
import java.awt.*;
import java.sql.SQLIntegrityConstraintViolationException;

public class RegisterForm extends JDialog {
    private final JTextField username=new JTextField(),fullname=new JTextField(),email=new JTextField();
    private final JPasswordField password=new JPasswordField(),confirm=new JPasswordField();
    private final AuthController auth;
    RegisterForm(Frame owner,AuthController auth){super(owner,"Đăng ký",true);this.auth=auth;JPanel p=Ui.form();
        p.add(new JLabel("Tên đăng nhập:"));p.add(username);p.add(new JLabel("Họ tên:"));p.add(fullname);p.add(new JLabel("Email:"));p.add(email);p.add(new JLabel("Mật khẩu:"));p.add(password);p.add(new JLabel("Xác nhận:"));p.add(confirm);
        JButton save=new JButton("Tạo tài khoản");save.addActionListener(e->register());add(p);add(save,BorderLayout.SOUTH);pack();setLocationRelativeTo(owner);}
    private void register(){try{auth.register(username.getText(),new String(password.getPassword()),new String(confirm.getPassword()),fullname.getText(),email.getText());JOptionPane.showMessageDialog(this,"Đăng ký thành công.");dispose();}catch(SQLIntegrityConstraintViolationException e){Ui.error(this,new IllegalArgumentException("Tên đăng nhập hoặc email đã tồn tại."));}catch(Exception e){Ui.error(this,e);}}
}
