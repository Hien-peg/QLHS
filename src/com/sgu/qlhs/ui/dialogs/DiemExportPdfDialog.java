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
    private final JComboBox<String> cboMon = new JComboBox<>(
            new String[] { "Toán", "Văn", "Anh", "Lý", "Hóa", "Sinh" });
    private final JComboBox<String> cboHK = new JComboBox<>(new String[] { "HK1", "HK2" });
    private LopBUS lopBUS;
    private DiemBUS diemBUS;
    private java.util.List<LopDTO> lops = new java.util.ArrayList<>();

    public DiemExportPdfDialog(Window owner) {
        super(owner, "Xuất bảng điểm PDF", ModalityType.APPLICATION_MODAL);
        setMinimumSize(new Dimension(520, 220));
        setLocationRelativeTo(owner);
        build();
        initBuses();
        loadLopData();
        pack();
    }

    private int mapMon(String tenMon) {
        if (tenMon == null)
            return 1;
        switch (tenMon) {
            case "Toán":
                return 1;
            case "Văn":
                return 2;
            case "Anh":
                return 3;
            case "Lý":
                return 4;
            case "Hóa":
                return 5;
            case "Sinh":
                return 6;
            default:
                return 1;
        }
    }

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
            int maNK = 1;
            int maMon = mapMon((String) cboMon.getSelectedItem());
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
        btnClose.addActionListener(e -> dispose());
        btnPane.add(btnClose);
        btnPane.add(btnExport);
        root.add(btnPane, BorderLayout.SOUTH);
    }
}