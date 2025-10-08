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

public class DiemNhapDialog extends JDialog {
    private final JComboBox<String> cboLop = new JComboBox<>(new String[]{"10A1","10A2","11A1","12A1"});
    private final JComboBox<String> cboMon = new JComboBox<>(new String[]{"Toán","Văn","Anh"});
    private final JComboBox<String> cboHK  = new JComboBox<>(new String[]{"HK1","HK2"});
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"Mã HS","Họ tên","Miệng","15p","Giữa kỳ","Cuối kỳ"}, 0) {
        @Override public boolean isCellEditable(int r,int c){ return c>=2; } // chỉ cho nhập điểm
        @Override public Class<?> getColumnClass(int c){ return c>=2? Double.class:String.class; }
    };

    public DiemNhapDialog(Window owner){
        super(owner, "Nhập điểm", ModalityType.APPLICATION_MODAL);
        setMinimumSize(new Dimension(860, 520));
        setLocationRelativeTo(owner);
        build(); pack();
    }

    private void build(){
        var root = new JPanel(new BorderLayout(12,12));
        root.setBorder(new EmptyBorder(16,16,16,16));
        setContentPane(root);

        var bar = new JPanel(new FlowLayout(FlowLayout.LEFT,10,0));
        bar.add(new JLabel("Lớp:")); bar.add(cboLop);
        bar.add(new JLabel("Môn:")); bar.add(cboMon);
        bar.add(new JLabel("Học kỳ:")); bar.add(cboHK);
        root.add(bar, BorderLayout.NORTH);

        var tbl = new JTable(model); tbl.setRowHeight(26);
        root.add(new JScrollPane(tbl), BorderLayout.CENTER);

        // demo dữ liệu
        model.addRow(new Object[]{1,"Nguyễn Văn A",null,null,null,null});
        model.addRow(new Object[]{2,"Trần Thị B",null,null,null,null});
        model.addRow(new Object[]{3,"Phạm Minh C",null,null,null,null});

        var btnPane = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        var btnClose = new JButton("Đóng");
        var btnSave  = new JButton("Lưu");
        btnSave.addActionListener(e -> { /* TODO: lưu vào DB/service */ dispose(); });
        btnClose.addActionListener(e -> dispose());
        btnPane.add(btnClose); btnPane.add(btnSave);
        root.add(btnPane, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(btnSave);
    }
}