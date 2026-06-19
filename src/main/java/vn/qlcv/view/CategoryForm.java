package vn.qlcv.view;

import vn.qlcv.controller.TaskController;
import vn.qlcv.model.Category;
import javax.swing.*;
import java.awt.*;

public class CategoryForm extends JDialog {
    private final DefaultListModel<Category> model=new DefaultListModel<>();private final JList<Category> list=new JList<>(model);
    private final TaskController controller;private final int userId;private boolean changed;
    CategoryForm(Frame owner,TaskController controller,int userId){super(owner,"Quản lý danh mục",true);this.controller=controller;this.userId=userId;
        JButton add=new JButton("Thêm"),edit=new JButton("Sửa"),delete=new JButton("Xóa"),close=new JButton("Đóng");JPanel buttons=new JPanel();buttons.add(add);buttons.add(edit);buttons.add(delete);buttons.add(close);
        add(new JScrollPane(list));add(buttons,BorderLayout.SOUTH);add.addActionListener(e->add());edit.addActionListener(e->edit());delete.addActionListener(e->delete());close.addActionListener(e->dispose());setSize(450,350);setLocationRelativeTo(owner);reload();}
    private void reload(){try{model.clear();controller.categories(userId).forEach(model::addElement);}catch(Exception e){Ui.error(this,e);}}
    private void add(){String name=JOptionPane.showInputDialog(this,"Tên danh mục:");if(name!=null)run(()->controller.addCategory(name,userId));}
    private void edit(){Category c=list.getSelectedValue();if(c==null)return;String name=JOptionPane.showInputDialog(this,"Tên mới:",c.name());if(name!=null)run(()->controller.updateCategory(c.id(),name,userId));}
    private void delete(){Category c=list.getSelectedValue();if(c==null)return;if(JOptionPane.showConfirmDialog(this,"Xóa danh mục này? Công việc sẽ không còn danh mục.","Xác nhận",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION)run(()->controller.deleteCategory(c.id(),userId));}
    private void run(SqlAction a){try{a.run();changed=true;reload();}catch(Exception e){Ui.error(this,e);}}
    boolean isChanged(){return changed;} private interface SqlAction{void run()throws Exception;}
}
