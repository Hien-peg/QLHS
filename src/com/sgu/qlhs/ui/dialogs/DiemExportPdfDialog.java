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
import java.awt.*;
import com.sgu.qlhs.bus.LopBUS;
import com.sgu.qlhs.bus.DiemBUS;
import com.sgu.qlhs.dto.LopDTO;

public class DiemExportPdfDialog extends JDialog {
    private final JComboBox<String> cboLop = new JComboBox<>();
    private final JComboBox<String> cboMon = new JComboBox<>();
    private final JComboBox<String> cboHK = new JComboBox<>(new String[] { "HK1", "HK2" });
    private LopBUS lopBUS;
    private DiemBUS diemBUS;
    private java.util.List<LopDTO> lops = new java.util.ArrayList<>();

    public DiemExportPdfDialog(Window owner) {
        super(owner, "Xuất bảng điểm PDF", ModalityType.APPLICATION_MODAL);
        setMinimumSize(new Dimension(520, 220));
        setLocationRelativeTo(owner);
        build();
        loadMonData();
        initBuses();
        loadLopData();
        pack();
    }

    // subject -> maMon mapping removed in favor of MonBUS lookups

    private void initBuses() {
        lopBUS = new LopBUS();
        diemBUS = new DiemBUS();
    }

    private void loadLopData() {
        lops = lopBUS.getAllLop();
        cboLop.removeAllItems();
        cboLop.addItem("-- Chọn lớp --");
        for (LopDTO l : lops)
            cboLop.addItem(l.getTenLop());
    }

    private void build() {
        var root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(new EmptyBorder(16, 16, 16, 16));
        setContentPane(root);

        var form = new JPanel(new GridLayout(3, 2, 10, 10));
        form.add(new JLabel("Lớp:"));
        form.add(cboLop);
        form.add(new JLabel("Môn:"));
        form.add(cboMon);
        form.add(new JLabel("Học kỳ:"));
        form.add(cboHK);
        root.add(form, BorderLayout.CENTER);

        var btnPane = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        var btnClose = new JButton("Đóng");
        var btnExport = new JButton("Xuất PDF");
        btnExport.addActionListener(e -> {
            int sel = cboLop.getSelectedIndex();
            if (sel <= 0) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn lớp");
                return;
            }
            LopDTO lop = lops.get(sel - 1);
            int maLop = lop.getMaLop();
            int hocKy = cboHK.getSelectedIndex() + 1;
            int maNK = com.sgu.qlhs.bus.NienKhoaBUS.current();
            // map subject name -> maMon
            com.sgu.qlhs.bus.MonBUS monBus = new com.sgu.qlhs.bus.MonBUS();
            java.util.Map<String, Integer> monMap = new java.util.HashMap<>();
            for (var m : monBus.getAllMon())
                monMap.put(m.getTenMon(), m.getMaMon());
            // subject id not needed for this export path; we only filter by class/hk
            java.util.List<com.sgu.qlhs.dto.DiemDTO> rows = diemBUS.getDiemByLopHocKy(maLop, hocKy, maNK);

            var chooser = new JFileChooser();
            chooser.setSelectedFile(new java.io.File("bang-diem.pdf"));
            if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                // For now simulate export and show how many rows would be exported
                JOptionPane.showMessageDialog(this, "Sẽ xuất " + rows.size() + " dòng cho lớp " + lop.getTenLop()
                        + " vào \n" + chooser.getSelectedFile().getAbsolutePath());
                dispose();
            }
        });
        btnClose.addActionListener((java.awt.event.ActionEvent __) -> {
            if (__ == null) {
            }
            dispose();
        });
        btnPane.add(btnClose);
        btnPane.add(btnExport);
        root.add(btnPane, BorderLayout.SOUTH);
    }

    private void loadMonData() {
        cboMon.removeAllItems();
        try {
            com.sgu.qlhs.bus.MonBUS monBus = new com.sgu.qlhs.bus.MonBUS();
            for (var m : monBus.getAllMon())
                cboMon.addItem(m.getTenMon());
        } catch (Exception ex) {
            cboMon.addItem("Toán");
            cboMon.addItem("Văn");
        }
    }
}