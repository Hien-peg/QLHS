package com.sgu.qlhs.ui.dialogs;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import com.sgu.qlhs.bus.GiaoVienBUS;
import com.sgu.qlhs.dto.GiaoVienDTO;

public class GiaoVienQuanLyDialog extends JDialog {
    private final JTextField txtSearch = new JTextField();
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[] { "Mã GV", "Họ tên", "Chuyên môn", "SĐT", "Email" }, 0);

    public GiaoVienQuanLyDialog(Window owner) {
        super(owner, "Quản lý giáo viên", ModalityType.APPLICATION_MODAL);
        setMinimumSize(new Dimension(720, 480));
        setLocationRelativeTo(owner);
        build();
        pack();
    }

    private void build() {
        var root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(new EmptyBorder(16, 16, 16, 16));
        setContentPane(root);

        var bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        txtSearch.setColumns(18);
        txtSearch.setBorder(BorderFactory.createTitledBorder("Tìm kiếm"));
        JButton btnAdd = new JButton("Thêm"), btnEdit = new JButton("Sửa"), btnDel = new JButton("Xóa");
        bar.add(txtSearch);
        bar.add(btnAdd);
        bar.add(btnEdit);
        bar.add(btnDel);
        root.add(bar, BorderLayout.NORTH);

        var tbl = new JTable(model);
        tbl.setAutoCreateRowSorter(true);
        root.add(new JScrollPane(tbl), BorderLayout.CENTER);

        // load real data via BUS
        GiaoVienBUS gvBUS = new GiaoVienBUS();
        reloadTable(gvBUS, model);

        btnAdd.addActionListener(e -> {
            e.getSource();
            String hoTen = JOptionPane.showInputDialog(this, "Họ tên:");
            if (hoTen == null || hoTen.trim().isEmpty())
                return;
            String sdt = JOptionPane.showInputDialog(this, "Số điện thoại:", "");
            String email = JOptionPane.showInputDialog(this, "Email:", "");
            // DAO expects ngaySinh and gioiTinh; pass empty for now
            gvBUS.saveGiaoVien(hoTen.trim(), "", "", sdt != null ? sdt.trim() : "", email != null ? email.trim() : "");
            reloadTable(gvBUS, model);
        });

        btnEdit.addActionListener(e -> {
            e.getSource();
            int r = tbl.getSelectedRow();
            if (r < 0)
                return;
            int modelRow = tbl.convertRowIndexToModel(r);
            Object idObj = model.getValueAt(modelRow, 0);
            if (idObj == null)
                return;
            int ma = Integer.parseInt(idObj.toString());
            GiaoVienDTO g = gvBUS.getGiaoVienByMa(ma);
            if (g == null)
                return;
            String hoTen = JOptionPane.showInputDialog(this, "Họ tên:", g.getHoTen());
            String sdt = JOptionPane.showInputDialog(this, "Số điện thoại:", g.getSoDienThoai());
            String email = JOptionPane.showInputDialog(this, "Email:", g.getEmail());
            gvBUS.updateGiaoVien(ma, hoTen != null ? hoTen.trim() : g.getHoTen(), "", "",
                    sdt != null ? sdt.trim() : g.getSoDienThoai(),
                    email != null ? email.trim() : g.getEmail());
            reloadTable(gvBUS, model);
        });

        btnDel.addActionListener(e -> {
            e.getSource();
            int r = tbl.getSelectedRow();
            if (r < 0)
                return;
            int modelRow = tbl.convertRowIndexToModel(r);
            Object idObj = model.getValueAt(modelRow, 0);
            if (idObj == null)
                return;
            int ma = Integer.parseInt(idObj.toString());
            int conf = JOptionPane.showConfirmDialog(this, "Xóa giáo viên có mã " + ma + "?", "Xác nhận",
                    JOptionPane.YES_NO_OPTION);
            if (conf == JOptionPane.YES_OPTION) {
                gvBUS.deleteGiaoVien(ma);
                reloadTable(gvBUS, model);
            }
        });
    }

    private void reloadTable(GiaoVienBUS gvBUS, DefaultTableModel model) {
        model.setRowCount(0);
        java.util.List<GiaoVienDTO> list = gvBUS.getAllGiaoVien();
        for (GiaoVienDTO g : list) {
            model.addRow(
                    new Object[] { g.getMaGV(), g.getHoTen(), g.getChuyenMon(), g.getSoDienThoai(), g.getEmail() });
        }
    }
}
