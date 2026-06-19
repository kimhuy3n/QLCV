package vn.qlcv.view;

import vn.qlcv.model.Category;
import vn.qlcv.model.Task;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

public class TaskForm extends JDialog {
    private static final class StatusItem {
        final String label;
        final String value;

        StatusItem(String label, String value) {
            this.label = label;
            this.value = value;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private final JTextField title = new JTextField();
    private final JTextField deadline = new JTextField();
    private final JTextArea description = new JTextArea(5, 25);
    private final JComboBox<StatusItem> status = new JComboBox<>(new StatusItem[]{
            new StatusItem("Chua lam", "PENDING"),
            new StatusItem("Dang lam", "IN_PROGRESS"),
            new StatusItem("Da lam", "COMPLETED")
    });
    private final JComboBox<Category> category = new JComboBox<>();
    private Task result;
    private final Task existing;
    private final int userId;

    TaskForm(Frame owner, Task task, List<Category> categories, int userId) {
        super(owner, task == null ? "Them cong viec" : "Sua cong viec", true);
        existing = task;
        this.userId = userId;
        categories.forEach(category::addItem);

        JPanel p = Ui.form();
        p.add(new JLabel("Ten cong viec:"));
        p.add(title);
        p.add(new JLabel("Mo ta:"));
        p.add(new JScrollPane(description));
        p.add(new JLabel("Trang thai:"));
        p.add(status);
        p.add(new JLabel("Deadline (yyyy-MM-dd):"));
        p.add(deadline);
        p.add(new JLabel("Danh muc:"));
        p.add(category);

        if (task != null) {
            title.setText(task.title());
            description.setText(task.description());
            deadline.setText(task.deadline().toString());
            selectStatus(task.status());
            for (int i = 0; i < category.getItemCount(); i++) {
                Category item = category.getItemAt(i);
                if (item != null && item.id() == (task.categoryId() == null ? -1 : task.categoryId())) {
                    category.setSelectedIndex(i);
                    break;
                }
            }
        } else {
            deadline.setText(LocalDate.now().plusDays(1).toString());
            status.setSelectedIndex(0);
        }

        JButton save = new JButton("Luu");
        save.addActionListener(e -> save());
        add(p);
        add(save, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(owner);
    }

    private void selectStatus(String value) {
        for (int i = 0; i < status.getItemCount(); i++) {
            if (status.getItemAt(i).value.equals(value)) {
                status.setSelectedIndex(i);
                return;
            }
        }
        status.setSelectedIndex(0);
    }

    private void save() {
        try {
            Category c = (Category) category.getSelectedItem();
            StatusItem selected = (StatusItem) status.getSelectedItem();
            result = new Task(
                    existing == null ? 0 : existing.id(),
                    title.getText().trim(),
                    description.getText().trim(),
                    existing == null ? LocalDate.now() : existing.createdDate(),
                    LocalDate.parse(deadline.getText().trim()),
                    selected == null ? "PENDING" : selected.value,
                    c == null ? null : c.id(),
                    c == null ? null : c.name(),
                    userId
            );
            dispose();
        } catch (DateTimeParseException e) {
            Ui.error(this, new IllegalArgumentException("Deadline phai theo dinh dang yyyy-MM-dd."));
        }
    }

    Task getResult() {
        return result;
    }
}
