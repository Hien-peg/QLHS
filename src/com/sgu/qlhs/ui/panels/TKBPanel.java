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
    private ThoiKhoaBieuDTO selectedTKB = null; // lưu tiết đang chọn để sửa

    public TKBPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);

        tkbBUS = new ThoiKhoaBieuBUS();

        // ===== Title =====
        JLabel lblTitle = new JLabel("Quản lý Thời khóa biểu", SwingConstants.LEFT);
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
        topPanel.add(new JLabel("Lớp:"));
        cboLop = new JComboBox<>();
        loadDanhSachLop();
        topPanel.add(cboLop);

        topPanel.add(new JLabel("Học kỳ:"));
        cboHocKy = new JComboBox<>(new String[]{"HK1", "HK2"});
        topPanel.add(cboHocKy);

        add(topPanel, BorderLayout.NORTH);

        // ===== Bảng hiển thị TKB =====
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

        JTableHeader header = tblTKB.getTableHeader();
        header.setFont(new Font("Segoe UI Semibold", Font.BOLD, 15));
        header.setBackground(new Color(70, 120, 200));
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(header.getWidth(), 38));
        header.setReorderingAllowed(false);

        // ✅ CHỈ CHỌN 1 Ô DUY NHẤT, HIỂN THỊ MÀU KHI CLICK
        tblTKB.setCellSelectionEnabled(true);                 
        tblTKB.setRowSelectionAllowed(true);      // bật để JTable hiển thị highlight
        tblTKB.setColumnSelectionAllowed(true);   // bật để highlight chính xác ô
        tblTKB.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); 

        tblTKB.setSelectionBackground(new Color(100, 160, 255)); // xanh dương nhạt
        tblTKB.setSelectionForeground(Color.WHITE);
        tblTKB.putClientProperty("Table.isFileList", Boolean.TRUE); 
        tblTKB.setFocusable(true);

        // ===== Renderer cho từng ô =====
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
                    lbl.setForeground(Color.BLACK);
                } else if (isSelected) {
                    lbl.setBackground(new Color(80, 140, 255)); // ô đang chọn
                    lbl.setForeground(Color.WHITE);
                    lbl.setFont(lbl.getFont().deriveFont(Font.BOLD));
                } else {
                    lbl.setBackground(row % 2 == 0 ? new Color(250, 252, 255) : Color.WHITE);
                    lbl.setForeground(Color.BLACK);
                }
                return lbl;
            }
        });

        // ===== Bắt sự kiện click chuột =====
        tblTKB.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int row = tblTKB.rowAtPoint(e.getPoint());
                int col = tblTKB.columnAtPoint(e.getPoint());
                if (row >= 0 && col > 0) {
                    tblTKB.setRowSelectionInterval(row, row);
                    tblTKB.setColumnSelectionInterval(col, col);

                    String thu = "Thứ " + (col + 1);
                    int tiet = row + 1;
                    selectedTKB = findByThuTiet(thu, tiet);

                    System.out.println("Click ô: " + thu + " - Tiết " + tiet +
                            (selectedTKB != null ? " | Môn: " + selectedTKB.getTenMon() : " | Trống"));
                }

                // Double-click để sửa
                if (e.getClickCount() == 2 && selectedTKB != null) {
                    openDialog(selectedTKB);
                }
            }
        });

        JScrollPane scroll = new JScrollPane(tblTKB);
        scroll.setBorder(new EmptyBorder(15, 15, 15, 15));
        add(scroll, BorderLayout.CENTER);

        // ===== Nút sự kiện =====
        btnThem.addActionListener(e -> openDialog(null));
        btnSua.addActionListener(e -> onEdit());
        btnXoa.addActionListener(e -> onDelete());

        cboLop.addActionListener(e -> reloadData());
        cboHocKy.addActionListener(e -> reloadData());

        reloadData();
    }

    // ====== Các hàm phụ ======
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
        cboLop.removeAllItems();
        LopBUS lopBUS = new LopBUS();
        for (var l : lopBUS.getAllLop()) cboLop.addItem(l.getTenLop());
    }

    private void reloadData() {
        clearAll();
        try {
            String tenLop = (String) cboLop.getSelectedItem();
            String hocKy = (String) cboHocKy.getSelectedItem();
            if (tenLop == null || hocKy == null) return;

            currentTkbList = tkbBUS.getAll();
            for (ThoiKhoaBieuDTO tkb : currentTkbList) {
                if (!hocKy.equals(tkb.getHocKy()) || !tenLop.equals(tkb.getTenLop())) continue;

                int thu = dayToNumber(tkb.getThu());
                for (int tiet = tkb.getTietBD(); tiet <= tkb.getTietKT(); tiet++) {
                    setCell(tiet, thu,
                            "<html><center><b>" + tkb.getTenMon() + "</b><br>(" +
                                    tkb.getTenPhong() + ")<br><i>GV: " + tkb.getTenGV() + "</i></center></html>");
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
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

    private ThoiKhoaBieuDTO findByThuTiet(String thu, int tiet) {
        if (currentTkbList == null) return null;
        for (ThoiKhoaBieuDTO tkb : currentTkbList) {
            if (tkb.getThu().equalsIgnoreCase(thu)
                    && tiet >= tkb.getTietBD() && tiet <= tkb.getTietKT()) {
                return tkb;
            }
        }
        return null;
    }

    private void openDialog(ThoiKhoaBieuDTO selected) {
        JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(this);
        TKBDialog dlg = new TKBDialog(parent, selected, tkbBUS);
        dlg.setVisible(true);
        reloadData();
    }

    private void onEdit() {
        if (selectedTKB == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn ô có tiết học để sửa!");
            return;
        }
        openDialog(selectedTKB);
    }

    private void onDelete() {
        if (selectedTKB == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn ô có tiết học để xóa!");
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
}
