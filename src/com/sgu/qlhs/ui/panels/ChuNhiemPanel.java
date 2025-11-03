package com.sgu.qlhs.ui.panels;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import com.sgu.qlhs.bus.*;
import com.sgu.qlhs.dto.ChuNhiemDTO;
import com.sgu.qlhs.ui.dialogs.ChuNhiemDialog;

public class ChuNhiemPanel extends JPanel {

    private JTable tbl;
    private DefaultTableModel model;
    private JButton btnThem, btnSua, btnXoa;
    private ChuNhiemBUS cnBUS;
    private List<ChuNhiemDTO> dsCN;

    public ChuNhiemPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);

        cnBUS = new ChuNhiemBUS();

        JLabel lblTitle = new JLabel("Quản lý Giáo viên Chủ nhiệm", SwingConstants.LEFT);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setBorder(new EmptyBorder(15, 20, 5, 10));
        add(lblTitle, BorderLayout.NORTH);

        // Thanh công cụ
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        btnThem = button("Thêm");
        btnSua = button("Sửa");
        btnXoa = button("Xóa");
        top.add(btnThem); top.add(btnSua); top.add(btnXoa);
        add(top, BorderLayout.NORTH);

        // Bảng
        String[] cols = {"Mã CN", "Giáo viên", "Lớp", "Ngày nhận nhiệm", "Ngày kết thúc"};
        model = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r, int c) { return false; } };
        tbl = new JTable(model);
        tbl.setRowHeight(28);
        tbl.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        add(new JScrollPane(tbl), BorderLayout.CENTER);

        // Sự kiện nút
        btnThem.addActionListener(e -> openDialog(null));
        btnSua.addActionListener(e -> onEdit());
        btnXoa.addActionListener(e -> onDelete());

        reload();
    }

    private JButton button(String t) {
        JButton b = new JButton(t);
        b.setBackground(new Color(70, 130, 200));
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    private void reload() {
        model.setRowCount(0);
        dsCN = cnBUS.getAll();
        for (ChuNhiemDTO cn : dsCN) {
            model.addRow(new Object[]{
                    cn.getMaCN(),
                    cn.getTenGV(),
                    cn.getTenLop(),
                    cn.getNgayNhanNhiem(),
                    cn.getNgayKetThuc()
            });
        }
    }

    private void openDialog(ChuNhiemDTO dto) {
        JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(this);
        new ChuNhiemDialog(parent, cnBUS, dto).setVisible(true);
        reload();
    }

    private void onEdit() {
        int r = tbl.getSelectedRow();
        if (r == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn dòng cần sửa!");
            return;
        }
        int ma = (int) model.getValueAt(r, 0);
        ChuNhiemDTO dto = dsCN.stream().filter(x -> x.getMaCN() == ma).findFirst().orElse(null);
        openDialog(dto);
    }

    private void onDelete() {
        int r = tbl.getSelectedRow();
        if (r == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn dòng cần xóa!");
            return;
        }
        int ma = (int) model.getValueAt(r, 0);
        if (JOptionPane.showConfirmDialog(this, "Xóa giáo viên chủ nhiệm này?", "Xác nhận",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            if (cnBUS.delete(ma)) {
                JOptionPane.showMessageDialog(this, "Đã xóa!");
                reload();
            } else {
                JOptionPane.showMessageDialog(this, "Xóa thất bại!");
            }
        }
    }
}
