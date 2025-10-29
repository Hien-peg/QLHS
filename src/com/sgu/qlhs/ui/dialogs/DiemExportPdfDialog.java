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

public class DiemExportPdfDialog extends JDialog {
    private final JComboBox<String> cboLop = new JComboBox<>(new String[] { "10A1", "10A2", "11A1", "12A1" });
    private final JComboBox<String> cboMon = new JComboBox<>(
            new String[] { "Toán", "Văn", "Anh", "Lý", "Hóa", "Sinh" });
    private final JComboBox<String> cboHK = new JComboBox<>(new String[] { "HK1", "HK2" });

    public DiemExportPdfDialog(Window owner) {
        super(owner, "Xuất bảng điểm PDF", ModalityType.APPLICATION_MODAL);
        setMinimumSize(new Dimension(520, 220));
        setLocationRelativeTo(owner);
        build();
        pack();
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
            var chooser = new JFileChooser();
            chooser.setSelectedFile(new java.io.File("bang-diem.pdf"));
            if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                // TODO: sinh PDF (iText/OpenPDF) theo lớp/môn/học kỳ đã chọn
                JOptionPane.showMessageDialog(this, "Đã giả lập xuất: " + chooser.getSelectedFile().getAbsolutePath());
                dispose();
            }
        });
        btnClose.addActionListener(e -> dispose());
        btnPane.add(btnClose);
        btnPane.add(btnExport);
        root.add(btnPane, BorderLayout.SOUTH);
    }
}