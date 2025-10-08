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

public class LopQuanLyDialog extends JDialog {
    private final JTextField txtSearch = new JTextField();
    private final DefaultTableModel model =
            new DefaultTableModel(new Object[]{"Mã lớp","Tên lớp","Khối","Phòng"}, 0);

    public LopQuanLyDialog(Window owner){
        super(owner, "Quản lý lớp", ModalityType.APPLICATION_MODAL);
        setMinimumSize(new Dimension(720, 480));
        setLocationRelativeTo(owner);
        build(); pack();
    }

    private void build(){
        var root = new JPanel(new BorderLayout(12,12));
        root.setBorder(new EmptyBorder(16,16,16,16));
        setContentPane(root);

        var bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        txtSearch.setColumns(18);
        txtSearch.setBorder(BorderFactory.createTitledBorder("Tìm kiếm"));
        JButton btnAdd = new JButton("Thêm"), btnEdit = new JButton("Sửa"), btnDel = new JButton("Xóa");
        bar.add(txtSearch); bar.add(btnAdd); bar.add(btnEdit); bar.add(btnDel);
        root.add(bar, BorderLayout.NORTH);

        var tbl = new JTable(model); tbl.setAutoCreateRowSorter(true);
        root.add(new JScrollPane(tbl), BorderLayout.CENTER);

        // seed demo
        model.addRow(new Object[]{1,"10A1",10,"P101"});
        model.addRow(new Object[]{2,"10A2",10,"P102"});
        model.addRow(new Object[]{3,"11A1",11,"P201"});
        model.addRow(new Object[]{4,"12A1",12,"P301"});

        // actions (TODO: nối dữ liệu thật)
        btnAdd.addActionListener(e -> model.addRow(new Object[]{null,"","", ""}));
        btnEdit.addActionListener(e -> {/* mở form chi tiết nếu cần */});
        btnDel.addActionListener(e -> {
            int r=tbl.getSelectedRow();
            if(r>=0) model.removeRow(tbl.convertRowIndexToModel(r));
        });
    }
}