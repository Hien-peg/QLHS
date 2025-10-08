/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sgu.qlhs.ui.dialogs;

/**
 *
 * @author minho
 */
import com.sgu.qlhs.ui.components.BarChartCanvas;
import javax.swing.*; import javax.swing.border.EmptyBorder; import java.awt.*;

public class ThongKeDiemMonDialog extends JDialog {
    private final JComboBox<String> cboKhoi = new JComboBox<>(new String[]{"10","11","12"});
    private final JComboBox<String> cboHK   = new JComboBox<>(new String[]{"HK1","HK2"});

    public ThongKeDiemMonDialog(Window owner){
        super(owner, "Thống kê điểm TB theo môn", ModalityType.APPLICATION_MODAL);
        setMinimumSize(new Dimension(720, 460)); setLocationRelativeTo(owner);

        var root = new JPanel(new BorderLayout(12,12));
        root.setBorder(new EmptyBorder(16,16,16,16)); setContentPane(root);

        var top = new JPanel(new FlowLayout(FlowLayout.LEFT,10,0));
        top.add(new JLabel("Khối:")); top.add(cboKhoi);
        top.add(new JLabel("Học kỳ:")); top.add(cboHK);
        var btnLoad = new JButton("Tải"); top.add(btnLoad);
        root.add(top, BorderLayout.NORTH);

        // placeholder chart
        String[] mons = {"Toán","Văn","Anh","Lý","Hóa","Sinh"};
        int[]    tbs  = {75,78,80,72,74,76}; // x10 (điểm 7.5 => 75) để trực quan
        var chart = new BarChartCanvas("Điểm TB theo môn (x10)", mons, tbs);
        root.add(chart, BorderLayout.CENTER);

        btnLoad.addActionListener(e -> {
            // TODO: load dữ liệu thật theo khối & học kỳ, rồi repaint chart
            chart.repaint();
        });

        var south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        var close = new JButton("Đóng"); close.addActionListener(e -> dispose());
        south.add(close); root.add(south, BorderLayout.SOUTH);
        pack();
    }
}