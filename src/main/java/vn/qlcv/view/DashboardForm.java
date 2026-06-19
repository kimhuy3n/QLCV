package vn.qlcv.view;

import vn.qlcv.controller.AuthController;
import vn.qlcv.controller.TaskController;
import vn.qlcv.model.Category;
import vn.qlcv.model.Task;
import vn.qlcv.model.TaskStatistics;
import vn.qlcv.model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class DashboardForm extends JFrame {
    private static final Color NAVY = new Color(15, 23, 42);
    private static final Color BLUE = new Color(37, 99, 235);
    private static final Color GREEN = new Color(22, 163, 74);
    private static final Color AMBER = new Color(217, 119, 6);
    private static final Color RED = new Color(220, 38, 38);
    private static final Color SURFACE = new Color(248, 250, 252);
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final User user;
    private final TaskController controller = new TaskController();
    private final JLabel totalValue = statValue(), completedValue = statValue();
    private final JLabel pendingValue = statValue(), overdueValue = statValue();
    private final JLabel welcome = new JLabel();
    private final JTextField keyword = new JTextField(18);
    private final JComboBox<String> status = new JComboBox<>(new String[]{
            "Tất cả trạng thái", "Chưa làm", "Đang làm", "Đã làm", "Quá hạn"
    });
    private final JComboBox<String> category = new JComboBox<>();
    private final JComboBox<String> rowStatus = new JComboBox<>(new String[]{
            "Chưa làm", "Đang làm", "Đã làm"
    });
    private final JButton applyStatus = new JButton("Đổi trạng thái");
    private final JProgressBar progress = new JProgressBar(0, 100);
    private final DonutChart chart = new DonutChart();
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"ID", "Công việc", "Danh mục", "Ngày tạo", "Deadline", "Trạng thái"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable table = new JTable(tableModel);
    private List<Category> categories = new ArrayList<>();
    private List<Task> rows = new ArrayList<>();

    public DashboardForm(User user) {
        super("QLCV - Quản lý công việc cá nhân");
        this.user = user;
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1100, 700));
        setSize(1250, 760);
        setContentPane(buildPage());
        configureTable();
        bindEvents();
        refreshCategories();
        refresh();
        setLocationRelativeTo(null);
    }

    private JPanel buildPage() {
        JPanel page = new JPanel(new BorderLayout());
        page.setBackground(SURFACE);
        page.add(buildSidebar(), BorderLayout.WEST);

        JPanel content = new JPanel(new BorderLayout(0, 18));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(20, 24, 20, 24));
        content.add(buildHeader(), BorderLayout.NORTH);
        content.add(buildMainContent(), BorderLayout.CENTER);
        page.add(content, BorderLayout.CENTER);
        return page;
    }

    private JPanel buildSidebar() {
        JPanel side = new JPanel();
        side.setBackground(NAVY);
        side.setPreferredSize(new Dimension(205, 0));
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setBorder(new EmptyBorder(24, 16, 20, 16));

        JLabel logo = new JLabel("✓  QLCV");
        logo.setForeground(Color.WHITE);
        logo.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);
        side.add(logo);
        JLabel caption = new JLabel("Làm việc thông minh hơn");
        caption.setForeground(new Color(148, 163, 184));
        caption.setBorder(new EmptyBorder(4, 0, 28, 0));
        caption.setAlignmentX(Component.LEFT_ALIGNMENT);
        side.add(caption);

        JButton add = sideButton("＋  Thêm công việc", BLUE);
        JButton edit = sideButton("✎  Sửa công việc", NAVY);
        JButton done = sideButton("✓  Hoàn thành", NAVY);
        JButton reopen = sideButton("↻  Khôi phục", NAVY);
        JButton delete = sideButton("×  Xóa công việc", NAVY);
        JButton cats = sideButton("▦  Danh mục", NAVY);
        for (JButton button : new JButton[]{add, edit, done, reopen, delete, cats}) {
            side.add(button); side.add(Box.createVerticalStrut(7));
        }
        side.add(Box.createVerticalGlue());
        JButton password = sideButton("⚿  Đổi mật khẩu", NAVY);
        JButton logout = sideButton("⇥  Đăng xuất", NAVY);
        side.add(password); side.add(Box.createVerticalStrut(7)); side.add(logout);

        add.addActionListener(e -> editTask(null));
        edit.addActionListener(e -> { Task t = selected(); if (t != null) editTask(t); });
        done.addActionListener(e -> complete());
        reopen.addActionListener(e -> reopen());
        delete.addActionListener(e -> delete());
        cats.addActionListener(e -> editCategories());
        password.addActionListener(e -> changePassword());
        logout.addActionListener(e -> { dispose(); new LoginForm().setVisible(true); });
        return side;
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        welcome.setText("Xin chào, " + user.fullname());
        welcome.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        welcome.setForeground(NAVY);
        header.add(welcome, BorderLayout.WEST);
        JLabel today = new JLabel("Hôm nay: " + DATE.format(LocalDate.now()));
        today.setForeground(new Color(100, 116, 139));
        header.add(today, BorderLayout.EAST);
        return header;
    }

    private JPanel buildMainContent() {
        JPanel main = new JPanel(new BorderLayout(0, 16));
        main.setOpaque(false);

        JPanel upper = new JPanel(new BorderLayout(16, 0));
        upper.setOpaque(false);
        JPanel left = new JPanel(new BorderLayout(0, 14));
        left.setOpaque(false);
        JPanel cards = new JPanel(new GridLayout(1, 4, 12, 0));
        cards.setOpaque(false);
        cards.add(statCard("TỔNG CÔNG VIỆC", totalValue, BLUE));
        cards.add(statCard("HOÀN THÀNH", completedValue, GREEN));
        cards.add(statCard("CHƯA HOÀN THÀNH", pendingValue, AMBER));
        cards.add(statCard("QUÁ HẠN", overdueValue, RED));
        left.add(cards, BorderLayout.NORTH);
        left.add(buildFilters(), BorderLayout.CENTER);
        upper.add(left, BorderLayout.CENTER);
        upper.add(buildChartCard(), BorderLayout.EAST);
        main.add(upper, BorderLayout.NORTH);
        main.add(buildTableCard(), BorderLayout.CENTER);
        return main;
    }

    private JPanel buildFilters() {
        JPanel panel = cardPanel(new BorderLayout(10, 10));
        JLabel title = new JLabel("Tìm kiếm và bộ lọc");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 15f));
        panel.add(title, BorderLayout.NORTH);
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        controls.setOpaque(false);
        keyword.putClientProperty("JTextField.placeholderText", "Nhập tên công việc...");
        controls.add(new JLabel("Từ khóa:")); controls.add(keyword);
        controls.add(status); controls.add(category);
        JButton refresh = new JButton("Làm mới");
        controls.add(refresh);
        refresh.addActionListener(e -> clearFilters());
        panel.add(controls, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildChartCard() {
        JPanel panel = cardPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(245, 205));
        JLabel title = new JLabel("TIẾN ĐỘ");
        title.setForeground(new Color(100, 116, 139));
        title.setFont(title.getFont().deriveFont(Font.BOLD));
        panel.add(title, BorderLayout.NORTH);
        chart.setPreferredSize(new Dimension(190, 125));
        panel.add(chart, BorderLayout.CENTER);
        progress.setStringPainted(true);
        progress.setForeground(GREEN);
        progress.setBackground(new Color(226, 232, 240));
        panel.add(progress, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildTableCard() {
        JPanel panel = cardPanel(new BorderLayout(0, 10));
        JLabel title = new JLabel("DANH SÁCH CÔNG VIỆC");
        title.setForeground(new Color(100, 116, 139));
        title.setFont(title.getFont().deriveFont(Font.BOLD));
        JPanel top = new JPanel(new BorderLayout(0, 8));
        top.setOpaque(false);
        top.add(title, BorderLayout.NORTH);
        top.add(buildTableActions(), BorderLayout.CENTER);
        panel.add(top, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildTableActions() {
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actions.setOpaque(false);
        actions.add(new JLabel("Trạng thái dòng đã chọn:"));
        actions.add(rowStatus);
        applyStatus.setEnabled(false);
        applyStatus.addActionListener(e -> applyRowStatus());
        actions.add(applyStatus);
        return actions;
    }

    private void configureTable() {
        table.setRowHeight(38);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(226, 232, 240));
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setPreferredSize(new Dimension(0, 38));
        table.setAutoCreateRowSorter(true);
        table.getColumnModel().getColumn(0).setMaxWidth(55);
        table.getColumnModel().getColumn(1).setPreferredWidth(260);
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object value, boolean selected,
                                                                       boolean focus, int row, int column) {
                Component c = super.getTableCellRendererComponent(t, value, selected, focus, row, column);
                if (!selected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                    c.setForeground(column == 5 ? statusColor(String.valueOf(value)) : NAVY);
                }
                setBorder(new EmptyBorder(0, 8, 0, 8));
                return c;
            }
        });
        table.getSelectionModel().addListSelectionListener(this::syncRowStatus);
    }

    private void bindEvents() {
        keyword.addActionListener(e -> refresh());
        status.addActionListener(e -> refresh());
        category.addActionListener(e -> refresh());
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) { Task t = selected(); if (t != null) editTask(t); }
            }
        });
        getRootPane().registerKeyboardAction(e -> editTask(null),
                KeyStroke.getKeyStroke("control N"), JComponent.WHEN_IN_FOCUSED_WINDOW);
        getRootPane().registerKeyboardAction(e -> refresh(),
                KeyStroke.getKeyStroke("F5"), JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private void refreshCategories() {
        try {
            Integer selected = selectedCategoryId();
            categories = controller.categories(user.id());
            category.removeAllItems();
            category.addItem("Tất cả danh mục");
            for (Category c : categories) category.addItem(c.name());
            if (selected != null) for (int i = 0; i < categories.size(); i++)
                if (categories.get(i).id() == selected) category.setSelectedIndex(i + 1);
        } catch (Exception e) { Ui.error(this, e); }
    }

    private Integer selectedCategoryId() {
        int i = category.getSelectedIndex();
        return i <= 0 || i - 1 >= categories.size() ? null : categories.get(i - 1).id();
    }

    private String selectedStatus() {
        return switch (status.getSelectedIndex()) {
            case 1 -> "PENDING";
            case 2 -> "IN_PROGRESS";
            case 3 -> "COMPLETED";
            case 4 -> "OVERDUE";
            default -> null;
        };
    }

    private void refresh() {
        try {
            rows = controller.search(user.id(), keyword.getText(), selectedStatus(), selectedCategoryId());
            tableModel.setRowCount(0);
            for (Task t : rows) tableModel.addRow(new Object[]{
                    t.id(), t.title(), t.categoryName() == null ? "—" : t.categoryName(),
                    DATE.format(t.createdDate()), DATE.format(t.deadline()), statusText(t)
            });
            TaskStatistics s = controller.statistics(user.id());
            totalValue.setText(String.valueOf(s.total()));
            completedValue.setText(String.valueOf(s.completed()));
            pendingValue.setText(String.valueOf(s.pending()));
            overdueValue.setText(String.valueOf(s.overdue()));
            int percent = (int) Math.round(s.completionRate());
            progress.setValue(percent);
            progress.setString(percent + "% hoàn thành");
            chart.setStatistics(s);
        } catch (Exception e) { Ui.error(this, e); }
    }

    private String statusText(Task t) {
        if (t.completed()) return "Hoàn thành";
        if (t.deadline().isBefore(LocalDate.now())) return "Quá hạn";
        if ("IN_PROGRESS".equals(t.status())) return "Đang làm";
        return "Chưa làm";
    }

    private Task selected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Hãy chọn một công việc trong bảng.");
            return null;
        }
        return rows.get(table.convertRowIndexToModel(row));
    }

    private void editTask(Task current) {
        TaskForm form = new TaskForm(this, current, categories, user.id());
        form.setVisible(true);
        if (form.getResult() != null) try {
            controller.save(form.getResult()); refresh();
        } catch (Exception e) { Ui.error(this, e); }
    }

    private void delete() {
        Task t = selected();
        if (t != null && JOptionPane.showConfirmDialog(this, "Xóa công việc '" + t.title() + "'?",
                "Xác nhận", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) try {
            controller.delete(t.id(), user.id()); refresh();
        } catch (Exception e) { Ui.error(this, e); }
    }

    private void complete() {
        Task t = selected();
        if (t == null || t.completed()) return;
        try { controller.complete(t.id(), user.id()); refresh(); }
        catch (Exception e) { Ui.error(this, e); }
    }

    private void reopen() {
        Task t = selected();
        if (t == null || !t.completed()) return;
        try { controller.reopen(t.id(), user.id()); refresh(); }
        catch (Exception e) { Ui.error(this, e); }
    }

    private void applyRowStatus() {
        Task t = selectedSilently();
        if (t == null) return;
        try {
            controller.changeStatus(t.id(), user.id(), selectedRowStatusCode());
            refresh();
        } catch (Exception e) { Ui.error(this, e); }
    }

    private void syncRowStatus(ListSelectionEvent event) {
        if (event.getValueIsAdjusting()) return;
        Task t = selectedSilently();
        applyStatus.setEnabled(t != null);
        if (t != null) rowStatus.setSelectedIndex(rowStatusIndex(t.status()));
    }

    private Task selectedSilently() {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        int modelRow = table.convertRowIndexToModel(row);
        return modelRow >= 0 && modelRow < rows.size() ? rows.get(modelRow) : null;
    }

    private String selectedRowStatusCode() {
        return switch (rowStatus.getSelectedIndex()) {
            case 1 -> "IN_PROGRESS";
            case 2 -> "COMPLETED";
            default -> "PENDING";
        };
    }

    private int rowStatusIndex(String value) {
        return switch (value) {
            case "IN_PROGRESS" -> 1;
            case "COMPLETED" -> 2;
            default -> 0;
        };
    }

    private void editCategories() {
        CategoryForm form = new CategoryForm(this, controller, user.id());
        form.setVisible(true);
        if (form.isChanged()) { refreshCategories(); refresh(); }
    }

    private void clearFilters() {
        keyword.setText(""); status.setSelectedIndex(0); category.setSelectedIndex(0); refresh();
    }

    private void changePassword() {
        JPasswordField oldP = new JPasswordField(), newP = new JPasswordField(), confirm = new JPasswordField();
        JPanel p = Ui.form();
        p.add(new JLabel("Mật khẩu hiện tại:")); p.add(oldP);
        p.add(new JLabel("Mật khẩu mới:")); p.add(newP);
        p.add(new JLabel("Xác nhận:")); p.add(confirm);
        if (JOptionPane.showConfirmDialog(this, p, "Đổi mật khẩu", JOptionPane.OK_CANCEL_OPTION)
                == JOptionPane.OK_OPTION) try {
            boolean ok = new AuthController().changePassword(user.id(), new String(oldP.getPassword()),
                    new String(newP.getPassword()), new String(confirm.getPassword()));
            JOptionPane.showMessageDialog(this, ok ? "Đổi mật khẩu thành công." : "Mật khẩu hiện tại không đúng.");
        } catch (Exception e) { Ui.error(this, e); }
    }

    private static JPanel cardPanel(LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)), new EmptyBorder(14, 16, 14, 16)));
        return panel;
    }

    private static JPanel statCard(String label, JLabel value, Color accent) {
        JPanel panel = cardPanel(new BorderLayout());
        JLabel name = new JLabel(label);
        name.setForeground(new Color(100, 116, 139));
        name.setFont(name.getFont().deriveFont(Font.BOLD, 11f));
        panel.add(name, BorderLayout.NORTH);
        value.setForeground(accent);
        panel.add(value, BorderLayout.CENTER);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, accent), panel.getBorder()));
        return panel;
    }

    private static JLabel statValue() {
        JLabel value = new JLabel("0");
        value.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 30));
        return value;
    }

    private static JButton sideButton(String text, Color background) {
        JButton button = new JButton(text);
        Color normal = background.equals(NAVY) ? new Color(30, 41, 59) : background;
        Color hover = background.equals(NAVY) ? new Color(51, 65, 85) : new Color(29, 78, 216);
        button.setUI(new BasicButtonUI());
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        button.setPreferredSize(new Dimension(173, 44));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(true);
        button.setForeground(Color.WHITE);
        button.setBackground(normal);
        button.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(background.equals(NAVY)
                        ? new Color(71, 85, 105) : new Color(96, 165, 250)),
                new EmptyBorder(11, 13, 11, 13)));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { button.setBackground(hover); }
            @Override public void mouseExited(MouseEvent e) { button.setBackground(normal); }
        });
        return button;
    }

    private static Color statusColor(String status) {
        return switch (status) {
            case "Hoàn thành" -> GREEN;
            case "Quá hạn" -> RED;
            case "Đang làm" -> AMBER;
            default -> AMBER;
        };
    }

    private static final class DonutChart extends JPanel {
        private TaskStatistics statistics = new TaskStatistics(0, 0, 0, 0);
        DonutChart() { setOpaque(false); }
        void setStatistics(TaskStatistics statistics) { this.statistics = statistics; repaint(); }

        @Override protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int size = Math.min(getWidth(), getHeight()) - 18;
            int x = (getWidth() - size) / 2, y = (getHeight() - size) / 2;
            g.setStroke(new BasicStroke(15, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
            g.setColor(new Color(226, 232, 240));
            g.drawArc(x, y, size, size, 0, 360);
            if (statistics.total() > 0) {
                int completedArc = (int) Math.round(statistics.completed() * 360.0 / statistics.total());
                g.setColor(GREEN); g.drawArc(x, y, size, size, 90, -completedArc);
                g.setColor(AMBER); g.drawArc(x, y, size, size, 90 - completedArc, -(360 - completedArc));
            }
            String value = String.format("%.0f%%", statistics.completionRate());
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
            g.setColor(NAVY);
            FontMetrics fm = g.getFontMetrics();
            g.drawString(value, (getWidth() - fm.stringWidth(value)) / 2,
                    (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
            g.dispose();
        }
    }
}
