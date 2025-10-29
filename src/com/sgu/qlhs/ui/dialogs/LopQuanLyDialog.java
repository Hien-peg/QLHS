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
import com.sgu.qlhs.bus.LopBUS;
import com.sgu.qlhs.dto.LopDTO;

public class LopQuanLyDialog extends JDialog {
    private final JTextField txtSearch = new JTextField();
    private final DefaultTableModel model = new DefaultTableModel(new Object[] { "Mã lớp", "Tên lớp", "Khối", "Phòng" },
            0);

    public LopQuanLyDialog(Window owner) {
        super(owner, "Quản lý lớp", ModalityType.APPLICATION_MODAL);
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
        LopBUS lopBUS = new LopBUS();
        reloadTable(lopBUS, model);

        btnAdd.addActionListener(e -> {
            String ten = JOptionPane.showInputDialog(this, "Tên lớp:");
            if (ten == null || ten.trim().isEmpty())
                return;
            String khoiStr = JOptionPane.showInputDialog(this, "Khối (ví dụ 10):", "10");
            int khoi = 10;
            try {
                khoi = Integer.parseInt(khoiStr);
            } catch (Exception ex) {
            }
            String maPhongStr = JOptionPane.showInputDialog(this, "Mã phòng (số):", "1");
            int maPhong = 1;
            try {
                maPhong = Integer.parseInt(maPhongStr);
            } catch (Exception ex) {
            }
            lopBUS.saveLop(ten.trim(), khoi, maPhong);
            reloadTable(lopBUS, model);
        });

        btnEdit.addActionListener(e -> {
            int r = tbl.getSelectedRow();
            if (r < 0)
                return;
            int modelRow = tbl.convertRowIndexToModel(r);
            Object idObj = model.getValueAt(modelRow, 0);
            if (idObj == null)
                return;
            int maLop = Integer.parseInt(idObj.toString());
            LopDTO l = lopBUS.getLopByMa(maLop);
            if (l == null)
                return;
            String ten = JOptionPane.showInputDialog(this, "Tên lớp:", l.getTenLop());
            String khoiStr = JOptionPane.showInputDialog(this, "Khối:", String.valueOf(l.getKhoi()));
            int khoi = l.getKhoi();
            try {
                khoi = Integer.parseInt(khoiStr);
            } catch (Exception ex) {
            }
            String maPhongStr = JOptionPane.showInputDialog(this, "Mã phòng:", "1");
            int maPhong = 1;
            try {
                maPhong = Integer.parseInt(maPhongStr);
            } catch (Exception ex) {
            }
            lopBUS.updateLop(maLop, ten != null ? ten.trim() : l.getTenLop(), khoi, maPhong);
            reloadTable(lopBUS, model);
        });

        btnDel.addActionListener(e -> {
            int r = tbl.getSelectedRow();
            if (r < 0)
                return;
            int modelRow = tbl.convertRowIndexToModel(r);
            Object idObj = model.getValueAt(modelRow, 0);
            if (idObj == null)
                return;
            int maLop = Integer.parseInt(idObj.toString());
            int conf = JOptionPane.showConfirmDialog(this, "Xóa lớp có mã " + maLop + "?", "Xác nhận",
                    JOptionPane.YES_NO_OPTION);
            if (conf == JOptionPane.YES_OPTION) {
                lopBUS.deleteLop(maLop);
                reloadTable(lopBUS, model);
            }
        });
    }

    private void reloadTable(LopBUS lopBUS, DefaultTableModel model) {
        model.setRowCount(0);
        java.util.List<LopDTO> list = lopBUS.getAllLop();
        for (LopDTO l : list) {
            model.addRow(new Object[] { l.getMaLop(), l.getTenLop(), l.getKhoi(), l.getTenPhong() });
        }
    }
}