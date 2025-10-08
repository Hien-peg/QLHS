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

public class MonQuanLyDialog extends JDialog {
    private final JTextField txtSearch = new JTextField();
    private final DefaultTableModel model =
            new DefaultTableModel(new Object[]{"Mã môn","Tên môn","Số tiết","Ghi chú"}, 0);

    public MonQuanLyDialog(Window owner){
        super(owner, "Quản lý môn học", ModalityType.APPLICATION_MODAL);
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

        model.addRow(new Object[]{101,"Toán",90,""});
        model.addRow(new Object[]{102,"Văn",90,""});
        model.addRow(new Object[]{103,"Anh",90,""});

        btnAdd.addActionListener(e -> model.addRow(new Object[]{null,"","", ""}));
        btnEdit.addActionListener(e -> {});
        btnDel.addActionListener(e -> {
            int r=tbl.getSelectedRow();
            if(r>=0) model.removeRow(tbl.convertRowIndexToModel(r));
        });
    }
}