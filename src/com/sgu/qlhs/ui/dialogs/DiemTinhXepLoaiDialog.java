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

public class DiemTinhXepLoaiDialog extends JDialog {
    private final JComboBox<String> cboLop = new JComboBox<>(new String[] { "10A1", "10A2", "11A1", "12A1" });
    private final JComboBox<String> cboMon = new JComboBox<>(
            new String[] { "Toán", "Văn", "Anh", "Lý", "Hóa", "Sinh" });
    private final JComboBox<String> cboHK = new JComboBox<>(new String[] { "HK1", "HK2" });
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[] { "Mã HS", "Họ tên", "Miệng", "15p", "Giữa kỳ", "Cuối kỳ", "TB" }, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return c >= 2 && c <= 5;
        } // cho sửa điểm nguồn

        @Override
        public Class<?> getColumnClass(int c) {
            return (c >= 2 && c <= 6) ? Double.class : String.class;
        }
    };

    public DiemTinhXepLoaiDialog(Window owner) {
        super(owner, "Tính điểm TB từng môn", ModalityType.APPLICATION_MODAL);
        setMinimumSize(new Dimension(900, 540));
        setLocationRelativeTo(owner);
        build();
        pack();
    }

    private void build() {
        var root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(new EmptyBorder(16, 16, 16, 16));
        setContentPane(root);

        var bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        bar.add(new JLabel("Lớp:"));
        bar.add(cboLop);
        bar.add(new JLabel("Môn:"));
        bar.add(cboMon);
        bar.add(new JLabel("Học kỳ:"));
        bar.add(cboHK);
        var btnCalc = new JButton("Tính");
        bar.add(btnCalc);
        root.add(bar, BorderLayout.NORTH);

        var tbl = new JTable(model);
        tbl.setRowHeight(26);
        root.add(new JScrollPane(tbl), BorderLayout.CENTER);

        // demo
        model.addRow(new Object[] { 1, "Nguyễn Văn A", 8.5, 7.0, 8.0, 8.5, null });
        model.addRow(new Object[] { 2, "Trần Thị B", 7.5, 8.0, 8.0, 8.5, null });

        btnCalc.addActionListener(e -> {
            for (int r = 0; r < model.getRowCount(); r++) {
                double mieng = val(model.getValueAt(r, 2));
                double p15 = val(model.getValueAt(r, 3));
                double gk = val(model.getValueAt(r, 4));
                double ck = val(model.getValueAt(r, 5));
                double tb = round1(mieng * 0.1 + p15 * 0.2 + gk * 0.3 + ck * 0.4);
                model.setValueAt(tb, r, 6);
                // model.setValueAt(xepLoai(tb), r, 7);
            }
        });

        var btnPane = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        var btnClose = new JButton("Đóng");
        var btnSave = new JButton("Lưu kết quả");
        btnSave.addActionListener(e -> {
            /* TODO: ghi kết quả */ dispose();
        });
        btnClose.addActionListener(e -> dispose());
        btnPane.add(btnClose);
        btnPane.add(btnSave);
        root.add(btnPane, BorderLayout.SOUTH);
    }

    private static double val(Object o) {
        return o == null ? 0.0 : ((Number) o).doubleValue();
    }

    private static double round1(double x) {
        return Math.round(x * 10.0) / 10.0;
    }
    // private static String xepLoai(double tb){
    // if(tb>=8.0) return "Giỏi";
    // if(tb>=6.5) return "Khá";
    // if(tb>=5.0) return "Trung bình";
    // return "Yếu";
    // }
}