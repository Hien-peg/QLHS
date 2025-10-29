/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sgu.qlhs.ui.dialogs;

/**
 *
 * @author minho
 */
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import com.sgu.qlhs.bus.PhongBUS;
import com.sgu.qlhs.dto.PhongDTO;

public class PhongQuanLyDialog extends JDialog {
    private final JTextField txtSearch = new JTextField();
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[] { "Mã phòng", "Tên phòng", "Loại", "Sức chứa", "Vị trí" }, 0);

    public PhongQuanLyDialog(Window owner) {
        super(owner, "Quản lý phòng học", ModalityType.APPLICATION_MODAL);
        setMinimumSize(new Dimension(780, 480));
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
        PhongBUS phongBUS = new PhongBUS();
        reloadTable(phongBUS, model);

        btnAdd.addActionListener(e -> {
            String ten = JOptionPane.showInputDialog(this, "Tên phòng:");
            if (ten == null || ten.trim().isEmpty())
                return;
            String loai = JOptionPane.showInputDialog(this, "Loại phòng:", "Lý thuyết");
            String sucStr = JOptionPane.showInputDialog(this, "Sức chứa:", "30");
            int suc = 0;
            try {
                suc = Integer.parseInt(sucStr);
            } catch (Exception ex) {
            }
            String viTri = JOptionPane.showInputDialog(this, "Vị trí:", "Tầng 1");
            phongBUS.savePhong(ten.trim(), loai != null ? loai.trim() : "", suc, viTri != null ? viTri.trim() : "");
            reloadTable(phongBUS, model);
        });

        btnEdit.addActionListener(e -> {
            int r = tbl.getSelectedRow();
            if (r < 0)
                return;
            int modelRow = tbl.convertRowIndexToModel(r);
            Object idObj = model.getValueAt(modelRow, 0);
            if (idObj == null)
                return;
            int ma = Integer.parseInt(idObj.toString());
            PhongDTO p = phongBUS.getPhongByMa(ma);
            if (p == null)
                return;
            String ten = JOptionPane.showInputDialog(this, "Tên phòng:", p.getTenPhong());
            String loai = JOptionPane.showInputDialog(this, "Loại phòng:", p.getLoaiPhong());
            String sucStr = JOptionPane.showInputDialog(this, "Sức chứa:", String.valueOf(p.getSucChua()));
            int suc = p.getSucChua();
            try {
                suc = Integer.parseInt(sucStr);
            } catch (Exception ex) {
            }
            String viTri = JOptionPane.showInputDialog(this, "Vị trí:", p.getViTri());
            phongBUS.updatePhong(ma, ten != null ? ten.trim() : p.getTenPhong(),
                    loai != null ? loai.trim() : p.getLoaiPhong(), suc, viTri != null ? viTri.trim() : p.getViTri());
            reloadTable(phongBUS, model);
        });

        btnDel.addActionListener(e -> {
            int r = tbl.getSelectedRow();
            if (r < 0)
                return;
            int modelRow = tbl.convertRowIndexToModel(r);
            Object idObj = model.getValueAt(modelRow, 0);
            if (idObj == null)
                return;
            int ma = Integer.parseInt(idObj.toString());
            int conf = JOptionPane.showConfirmDialog(this, "Xóa phòng có mã " + ma + "?", "Xác nhận",
                    JOptionPane.YES_NO_OPTION);
            if (conf == JOptionPane.YES_OPTION) {
                phongBUS.deletePhong(ma);
                reloadTable(phongBUS, model);
            }
        });
    }

    private void reloadTable(PhongBUS phongBUS, DefaultTableModel model) {
        model.setRowCount(0);
        java.util.List<PhongDTO> list = phongBUS.getAllPhong();
        for (PhongDTO p : list) {
            model.addRow(
                    new Object[] { p.getMaPhong(), p.getTenPhong(), p.getLoaiPhong(), p.getSucChua(), p.getViTri() });
        }
    }
}