package com.sgu.qlhs.ui.panels;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;
import com.sgu.qlhs.bus.*;
import com.sgu.qlhs.dto.ThoiKhoaBieuDTO;
import com.sgu.qlhs.ui.dialogs.TKBDialog;

public class TKBPanel extends JPanel {

    private JTable tblTKB;
    private DefaultTableModel model;
    private JButton btnThem, btnSua, btnXoa;
    private JComboBox<String> cboLop, cboHocKy;
    private ThoiKhoaBieuBUS tkbBUS;
    private List<ThoiKhoaBieuDTO> currentTkbList;
    private ThoiKhoaBieuDTO selectedTKB = null;

    // ==== Biến phân quyền ====
    private String role;  // "Admin", "GiaoVien", "HocSinh", "ChuNhiem"
    private Integer maGV;
    private Integer maLop;
    private String namHoc = "2024-2025";

    // ==== Constructor cho Admin (mặc định) ====
    public TKBPanel() {
        this("Admin", null, null);
    }

    // ==== Constructor có phân quyền ====
    public TKBPanel(String role, Integer maGV, Integer maLop) {
        this.role = role;
        this.maGV = maGV;
        this.maLop = maLop;
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);

        tkbBUS = new ThoiKhoaBieuBUS();

        // ===== Title =====
        JLabel lblTitle = new JLabel("Thời khóa biểu", SwingConstants.LEFT);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(new Color(30, 60, 130));
        lblTitle.setBorder(new EmptyBorder(15, 20, 5, 10));
        add(lblTitle, BorderLayout.NORTH);

        // ===== Thanh công cụ =====
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        topPanel.setBackground(new Color(245, 248, 255));
        topPanel.setBorder(new EmptyBorder(8, 10, 8, 10));

        btnThem = button("Thêm");
        btnSua = button("Sửa");
        btnXoa = button("Xóa");

        topPanel.add(btnThem);
        topPanel.add(btnSua);
        topPanel.add(btnXoa);

        topPanel.add(Box.createHorizontalStrut(20));
        topPanel.add(new JLabel("Học kỳ:"));
        cboHocKy = new JComboBox<>(new String[]{"HK1", "HK2"});
        topPanel.add(cboHocKy);

        // Admin mới cần chọn lớp
        if (role.equals("Admin")) {
            topPanel.add(Box.createHorizontalStrut(10));
            topPanel.add(new JLabel("Lớp:"));
            cboLop = new JComboBox<>();
            loadDanhSachLop();
            topPanel.add(cboLop);
        }

        add(topPanel, BorderLayout.NORTH);

        // ===== Bảng hiển thị =====
        String[] columnNames = {"Tiết", "Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7"};
        model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        for (int i = 1; i <= 10; i++)
            model.addRow(new Object[]{"Tiết " + i, "", "", "", "", "", ""});

        tblTKB = new JTable(model);
        tblTKB.setRowHeight(70);
        tblTKB.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tblTKB.setShowGrid(true);
        tblTKB.setGridColor(new Color(210, 210, 210));
        tblTKB.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        tblTKB.setCellSelectionEnabled(true);
        tblTKB.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JTableHeader header = tblTKB.getTableHeader();
        header.setFont(new Font("Segoe UI Semibold", Font.BOLD, 15));
        header.setBackground(new Color(70, 120, 200));
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(header.getWidth(), 38));
        header.setReorderingAllowed(false);

        tblTKB.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                          boolean isSelected, boolean hasFocus,
                                                          int row, int column) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setVerticalAlignment(SwingConstants.CENTER);
                lbl.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 230)));

                if (column == 0) {
                    lbl.setBackground(new Color(240, 243, 255));
                    lbl.setFont(new Font("Segoe UI Semibold", Font.BOLD, 14));
                } else if (isSelected) {
                    lbl.setBackground(new Color(80, 140, 255));
                    lbl.setForeground(Color.WHITE);
                } else {
                    lbl.setBackground(row % 2 == 0 ? new Color(250, 252, 255) : Color.WHITE);
                    lbl.setForeground(Color.BLACK);
                }
                return lbl;
            }
        });

        JScrollPane scroll = new JScrollPane(tblTKB);
        scroll.setBorder(new EmptyBorder(15, 15, 15, 15));
        add(scroll, BorderLayout.CENTER);
        
     // ==== Lắng nghe click chuột chọn tiết học ====
        tblTKB.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int row = tblTKB.getSelectedRow();
                int col = tblTKB.getSelectedColumn();

                // Bỏ qua nếu click vào cột "Tiết"
                if (col == 0) {
                    selectedTKB = null;
                    return;
                }

                int tiet = row + 1;
                String thu = "Thứ " + (col + 1);

                selectedTKB = null;
                for (ThoiKhoaBieuDTO t : currentTkbList) {
                    if (t.getThu().equalsIgnoreCase(thu)
                            && t.getTietBD() <= tiet && tiet <= t.getTietKT()) {
                        selectedTKB = t;
                        break;
                    }
                }

                if (selectedTKB != null) {
                    System.out.println(">> Đã chọn tiết: " + selectedTKB.getTenMon() +
                            " - GV: " + selectedTKB.getTenGV() + " - " + selectedTKB.getThu() +
                            " (Tiết " + selectedTKB.getTietBD() + "-" + selectedTKB.getTietKT() + ")");
                } else {
                    System.out.println(">> Không tìm thấy tiết phù hợp (" + thu + ", Tiết " + tiet + ")");
                }
            }
        });


        // ===== Nút sự kiện =====
        btnThem.addActionListener(e -> openDialog(null));
        btnSua.addActionListener(e -> onEdit());
        btnXoa.addActionListener(e -> onDelete());
        cboHocKy.addActionListener(e -> reloadData());
        if (role.equals("Admin") && cboLop != null)
            cboLop.addActionListener(e -> onLopChanged());

        // ===== Phân quyền hiển thị =====
        applyRolePermissions();

        // ===== Tải dữ liệu ban đầu =====
        reloadData();
    }

    // ==== Tùy chỉnh quyền truy cập ====
    private void applyRolePermissions() {
        boolean isAdmin = role.equals("Admin");
        btnThem.setVisible(isAdmin);
        btnSua.setVisible(isAdmin);
        btnXoa.setVisible(isAdmin);
    }

    private JButton button(String text) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.setBackground(new Color(80, 130, 200));
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBorder(BorderFactory.createEmptyBorder(5, 12, 5, 12));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void loadDanhSachLop() {
        if (cboLop == null) return;
        cboLop.removeAllItems();
        LopBUS lopBUS = new LopBUS();
        for (var l : lopBUS.getAllLop()) cboLop.addItem(l.getTenLop());
    }

    // ======= Load dữ liệu thời khóa biểu =======
    private void reloadData() {
        clearAll();
        String hocKy = (String) cboHocKy.getSelectedItem();
        if (hocKy == null) return;

        Integer lopHienTai = maLop;
        if (role.equals("Admin") && cboLop != null && cboLop.getSelectedItem() != null) {
            // Khi admin chọn lớp, cập nhật mã lớp theo lựa chọn
            LopBUS lopBUS = new LopBUS();
            var dsLop = lopBUS.getAllLop();
            String tenLopChon = (String) cboLop.getSelectedItem();
            for (var l : dsLop)
                if (l.getTenLop().equals(tenLopChon)) lopHienTai = l.getMaLop();
        }

        currentTkbList = tkbBUS.getByRole(role, lopHienTai, maGV, hocKy, namHoc);
        System.out.println("=== DEBUG getByRole ===");
        System.out.println("Role: " + role);
        System.out.println("maLop: " + lopHienTai);
        System.out.println("maGV: " + maGV);
        System.out.println("hocKy: " + hocKy);
        System.out.println("namHoc: " + namHoc);
        System.out.println("========================");
        System.out.println("Số bản ghi lấy được: " + currentTkbList.size());

        // Vẽ dữ liệu lên bảng
        for (ThoiKhoaBieuDTO tkb : currentTkbList) {
            int thu = dayToNumber(tkb.getThu());
            for (int tiet = tkb.getTietBD(); tiet <= tkb.getTietKT(); tiet++) {
                setCell(tiet, thu,
                        "<html><center><b>" + tkb.getTenMon() + "</b><br>(" +
                                tkb.getTenPhong() + ")<br><i>GV: " + tkb.getTenGV() + "</i></center></html>");
            }
        }
    }

    private void clearAll() {
        for (int r = 0; r < model.getRowCount(); r++)
            for (int c = 1; c < model.getColumnCount(); c++)
                model.setValueAt("", r, c);
    }

    private int dayToNumber(String thu) {
        if (thu == null) return 0;
        thu = thu.replace("Thứ", "").trim();
        return switch (thu) {
            case "2" -> 2;
            case "3" -> 3;
            case "4" -> 4;
            case "5" -> 5;
            case "6" -> 6;
            case "7" -> 7;
            default -> 0;
        };
    }

    private void setCell(int tiet, int thu, String text) {
        if (tiet < 1 || tiet > 10 || thu < 2 || thu > 7) return;
        model.setValueAt(text, tiet - 1, thu - 1);
    }

    private void openDialog(ThoiKhoaBieuDTO selected) {
        JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(this);
        TKBDialog dlg = new TKBDialog(parent, selected, tkbBUS);
        dlg.setVisible(true);
        reloadData();
    }

    private void onEdit() {
        if (selectedTKB == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn tiết học để sửa!");
            return;
        }
        openDialog(selectedTKB);
    }

    private void onDelete() {
        if (selectedTKB == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn tiết học để xóa!");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn xóa tiết này không?", "Xác nhận",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (tkbBUS.delete(selectedTKB.getMaTKB())) {
                JOptionPane.showMessageDialog(this, "Đã xóa thành công!");
                reloadData();
            } else {
                JOptionPane.showMessageDialog(this, "Xóa thất bại!");
            }
        }
    }
    private void onLopChanged() {
        if (cboLop == null || cboLop.getSelectedItem() == null) return;
        String tenLopChon = (String) cboLop.getSelectedItem();

        LopBUS lopBUS = new LopBUS();
        var dsLop = lopBUS.getAllLop();
        for (var l : dsLop) {
            if (l.getTenLop().equals(tenLopChon)) {
                maLop = l.getMaLop(); // ⚡ Cập nhật lại mã lớp toàn cục
                break;
            }
        }

        System.out.println(">> Admin chọn lớp: " + tenLopChon + " (MaLop=" + maLop + ")");
        reloadData();
   
 
    }
    
}

