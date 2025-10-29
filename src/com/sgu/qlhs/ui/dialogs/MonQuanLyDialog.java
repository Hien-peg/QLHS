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
import com.sgu.qlhs.bus.MonBUS;
import com.sgu.qlhs.dto.MonHocDTO;

public class MonQuanLyDialog extends JDialog {
    private final JTextField txtSearch = new JTextField();
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[] { "Mã môn", "Tên môn", "Số tiết", "Ghi chú" }, 0);

    public MonQuanLyDialog(Window owner) {
        super(owner, "Quản lý môn học", ModalityType.APPLICATION_MODAL);
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
        MonBUS monBUS = new MonBUS();
        reloadTable(monBUS, model);

        btnAdd.addActionListener(e -> {
            String ten = JOptionPane.showInputDialog(this, "Tên môn:");
            if (ten == null || ten.trim().isEmpty())
                return;
            String soStr = JOptionPane.showInputDialog(this, "Số tiết:", "45");
            int sotiet = 0;
            try {
                sotiet = Integer.parseInt(soStr);
            } catch (Exception ex) {
            }
            String ghi = JOptionPane.showInputDialog(this, "Ghi chú:", "");
            monBUS.saveMon(ten.trim(), sotiet, ghi != null ? ghi.trim() : "");
            reloadTable(monBUS, model);
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
            MonHocDTO m = monBUS.getMonByMa(ma);
            if (m == null)
                return;
            String ten = JOptionPane.showInputDialog(this, "Tên môn:", m.getTenMon());
            String soStr = JOptionPane.showInputDialog(this, "Số tiết:", "45");
            int sotiet = 0;
            try {
                sotiet = Integer.parseInt(soStr);
            } catch (Exception ex) {
            }
            String ghi = JOptionPane.showInputDialog(this, "Ghi chú:", "");
            monBUS.updateMon(ma, ten != null ? ten.trim() : m.getTenMon(), sotiet, ghi != null ? ghi.trim() : "");
            reloadTable(monBUS, model);
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
            int conf = JOptionPane.showConfirmDialog(this, "Xóa môn có mã " + ma + "?", "Xác nhận",
                    JOptionPane.YES_NO_OPTION);
            if (conf == JOptionPane.YES_OPTION) {
                monBUS.deleteMon(ma);
                reloadTable(monBUS, model);
            }
        });
    }

    private void reloadTable(MonBUS monBUS, DefaultTableModel model) {
        model.setRowCount(0);
        java.util.List<MonHocDTO> list = monBUS.getAllMon();
        for (MonHocDTO m : list) {
            model.addRow(new Object[] { m.getMaMon(), m.getTenMon(), m.getSoTiet(),
                    m.getGhiChu() != null ? m.getGhiChu() : "" });
        }
    }
}