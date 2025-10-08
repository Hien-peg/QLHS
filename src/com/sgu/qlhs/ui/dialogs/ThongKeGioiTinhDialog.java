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

public class ThongKeGioiTinhDialog extends JDialog {
    public ThongKeGioiTinhDialog(Window owner){
        super(owner, "Thống kê giới tính", ModalityType.APPLICATION_MODAL);
        setMinimumSize(new Dimension(640, 420)); setLocationRelativeTo(owner);
        var root = new JPanel(new BorderLayout(12,12));
        root.setBorder(new EmptyBorder(16,16,16,16)); setContentPane(root);

        // TODO: thay bằng dữ liệu thật
        String[] cats = {"Nam","Nữ","Khác"}; int[] vals = {58,42,3};
        root.add(new BarChartCanvas("Tỉ lệ giới tính học sinh", cats, vals), BorderLayout.CENTER);

        var south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        var close = new JButton("Đóng"); close.addActionListener(e -> dispose());
        south.add(close); root.add(south, BorderLayout.SOUTH);
        pack();
    }
}
