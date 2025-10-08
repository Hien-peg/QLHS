/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sgu.qlhs.ui.dialogs;

/**
 *
 * @author minho
 */
import javax.swing.*; import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel; import java.awt.*;
import java.io.*;

public class ThongKeLopSucChuaDialog extends JDialog {
    private final DefaultTableModel model =
        new DefaultTableModel(new Object[]{"Lớp","Phòng","Sĩ số","Sức chứa","% sử dụng"}, 0) {
            @Override public Class<?> getColumnClass(int c){ return switch(c){ case 2,3,4 -> Double.class; default -> String.class; }; }
            @Override public boolean isCellEditable(int r,int c){ return false; }
        };

    public ThongKeLopSucChuaDialog(Window owner){
        super(owner, "Lớp vs sức chứa phòng", ModalityType.APPLICATION_MODAL);
        setMinimumSize(new Dimension(760, 480)); setLocationRelativeTo(owner);

        var root = new JPanel(new BorderLayout(12,12));
        root.setBorder(new EmptyBorder(16,16,16,16)); setContentPane(root);

        var tbl = new JTable(model); tbl.setAutoCreateRowSorter(true);
        root.add(new JScrollPane(tbl), BorderLayout.CENTER);

        // TODO: thay bằng dữ liệu thật
        addRow("10A1","P101", 45, 45);
        addRow("10A2","P102", 48, 45);
        addRow("11A1","P201", 40, 45);
        addRow("12A1","P301", 44, 45);

        var south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        var btnExport = new JButton("Xuất CSV");
        var btnClose  = new JButton("Đóng");
        btnExport.addActionListener(e -> exportCsv());
        btnClose.addActionListener(e -> dispose());
        south.add(btnClose); south.add(btnExport);
        root.add(south, BorderLayout.SOUTH);
        pack();
    }

    private void addRow(String lop, String phong, int siSo, int sucChua){
        double used = sucChua==0? 0 : Math.round((siSo*1000.0/sucChua))/10.0; // % 1 số lẻ
        model.addRow(new Object[]{lop, phong, (double)siSo, (double)sucChua, used});
    }

    private void exportCsv(){
        var fc = new JFileChooser(); fc.setSelectedFile(new File("lop_vs_suc_chua.csv"));
        if (fc.showSaveDialog(this)==JFileChooser.APPROVE_OPTION){
            try (var w = new PrintWriter(fc.getSelectedFile(), java.nio.charset.StandardCharsets.UTF_8)){
                for (int c=0;c<model.getColumnCount();c++){
                    if(c>0) w.print(","); w.print(model.getColumnName(c));
                }
                w.println();
                for (int r=0;r<model.getRowCount();r++){
                    for (int c=0;c<model.getColumnCount();c++){
                        if(c>0) w.print(",");
                        w.print(String.valueOf(model.getValueAt(r,c)));
                    }
                    w.println();
                }
                JOptionPane.showMessageDialog(this, "Đã xuất CSV.");
            } catch (Exception ex){ JOptionPane.showMessageDialog(this, "Lỗi xuất: "+ex.getMessage()); }
        }
    }
}