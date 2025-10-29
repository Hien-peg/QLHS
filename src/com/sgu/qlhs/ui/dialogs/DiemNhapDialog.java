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
import com.sgu.qlhs.bus.DiemBUS;
import com.sgu.qlhs.bus.LopBUS;
import com.sgu.qlhs.bus.HocSinhBUS;
import com.sgu.qlhs.bus.MonBUS;
import com.sgu.qlhs.bus.NienKhoaBUS;
import com.sgu.qlhs.dto.HocSinhDTO;
import com.sgu.qlhs.dto.LopDTO;

public class DiemNhapDialog extends JDialog {
    private final DiemBUS diemBUS = new DiemBUS();
    private final LopBUS lopBUS = new LopBUS();
    private final HocSinhBUS hocSinhBUS = new HocSinhBUS();
    private final JComboBox<String> cboLop = new JComboBox<>();
    private final JComboBox<String> cboMon = new JComboBox<>();
    private final JComboBox<String> cboHK = new JComboBox<>(new String[] { "HK1", "HK2" });
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[] { "Mã HS", "Họ tên", "Miệng", "15p", "Giữa kỳ", "Cuối kỳ" }, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return c >= 2;
        } // chỉ cho nhập điểm

        @Override
        public Class<?> getColumnClass(int c) {
            return c >= 2 ? Double.class : String.class;
        }
    };

    public DiemNhapDialog(Window owner) {
        super(owner, "Nhập điểm", ModalityType.APPLICATION_MODAL);
        setMinimumSize(new Dimension(860, 520));
        setLocationRelativeTo(owner);
        build();
        loadMonData();
        loadLopData();
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
        root.add(bar, BorderLayout.NORTH);

        var tbl = new JTable(model);
        tbl.setRowHeight(26);
        root.add(new JScrollPane(tbl), BorderLayout.CENTER);

        // initial demo or empty until a class is selected
        // model rows will be loaded when user selects a class

        var btnPane = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        var btnClose = new JButton("Đóng");
        var btnSave = new JButton("Lưu");
        btnSave.addActionListener((java.awt.event.ActionEvent __) -> {
            if (__ == null) {
            }
            // map selected môn -> maMon using MonBUS
            MonBUS monBUS = new MonBUS();
            java.util.Map<String, Integer> monMap = new java.util.HashMap<>();
            for (var m : monBUS.getAllMon())
                monMap.put(m.getTenMon(), m.getMaMon());
            Integer maMonObj = monMap.get(cboMon.getSelectedItem().toString());
            int maMon = maMonObj == null ? 1 : maMonObj;
            int hocKy = cboHK.getSelectedIndex() + 1; // HK1 -> 1, HK2 -> 2
            int maNK = NienKhoaBUS.current();

            for (int r = 0; r < model.getRowCount(); r++) {
                Object idObj = model.getValueAt(r, 0);
                if (idObj == null)
                    continue;
                int maHS;
                try {
                    maHS = Integer.parseInt(idObj.toString());
                } catch (NumberFormatException ex) {
                    // skip rows where first column isn't an integer id
                    continue;
                }

                double mieng = valueToDouble(model.getValueAt(r, 2));
                double p15 = valueToDouble(model.getValueAt(r, 3));
                double gk = valueToDouble(model.getValueAt(r, 4));
                double ck = valueToDouble(model.getValueAt(r, 5));

                // Call BUS to save the record (delegates to DAO internally)
                diemBUS.saveOrUpdateDiem(maHS, maMon, hocKy, maNK, mieng, p15, gk, ck);
            }

            JOptionPane.showMessageDialog(this, "Lưu điểm xong");
            dispose();
        });
        btnClose.addActionListener((java.awt.event.ActionEvent __) -> {
            if (__ == null) {
            }
            dispose();
        });
        btnPane.add(btnClose);
        btnPane.add(btnSave);
        root.add(btnPane, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(btnSave);
    }

    private void loadMonData() {
        cboMon.removeAllItems();
        try {
            MonBUS monBUS = new MonBUS();
            for (var m : monBUS.getAllMon()) {
                cboMon.addItem(m.getTenMon());
            }
        } catch (Exception ex) {
            // fallback: keep empty or default list
            cboMon.addItem("Toán");
            cboMon.addItem("Văn");
        }
    }

    private void loadLopData() {
        cboLop.removeAllItems();
        cboLop.addItem("-- Chọn lớp --");
        java.util.List<LopDTO> lops = lopBUS.getAllLop();
        for (LopDTO l : lops) {
            cboLop.addItem(l.getTenLop());
        }

        cboLop.addActionListener((java.awt.event.ActionEvent __) -> {
            if (__ == null) {
            }
            int idx = cboLop.getSelectedIndex();
            if (idx <= 0) {
                model.setRowCount(0);
                return;
            }
            LopDTO selected = lops.get(idx - 1);
            loadStudentsForLop(selected.getMaLop());
        });
    }

    private void loadStudentsForLop(int maLop) {
        model.setRowCount(0);
        java.util.List<HocSinhDTO> students = hocSinhBUS.getHocSinhByMaLop(maLop);
        for (HocSinhDTO hs : students) {
            model.addRow(new Object[] { hs.getMaHS(), hs.getHoTen(), null, null, null, null });
        }
    }

    // subject mapping removed; MonBUS is used dynamically where needed

    private double valueToDouble(Object o) {
        if (o == null)
            return 0.0;
        if (o instanceof Number)
            return ((Number) o).doubleValue();
        try {
            return Double.parseDouble(o.toString());
        } catch (Exception ex) {
            return 0.0;
        }
    }
}