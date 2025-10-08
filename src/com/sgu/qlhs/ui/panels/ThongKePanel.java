/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sgu.qlhs.ui.panels;

/**
 *
 * @author minho
 */
import com.sgu.qlhs.ui.components.*;
import javax.swing.*; import javax.swing.border.EmptyBorder; import java.awt.*;
import static com.sgu.qlhs.ui.MainDashboard.*;

public class ThongKePanel extends JPanel {
    public ThongKePanel(){ setLayout(new BorderLayout()); setOpaque(false);
        var outer = new RoundedPanel(18, CARD_BG, CARD_BORDER); outer.setLayout(new BorderLayout());
        var lbl = new JLabel("Thống kê"); lbl.setBorder(new EmptyBorder(12,16,8,16)); lbl.setFont(lbl.getFont().deriveFont(Font.BOLD,18f));
        outer.add(lbl, BorderLayout.NORTH);
        String[] cats = {"Nam","Nữ","Khác"}; int[] values = {58, 42, 3};
        var chart = new BarChartCanvas("Tỉ lệ giới tính học sinh", cats, values);
        outer.add(chart, BorderLayout.CENTER);
        add(outer, BorderLayout.CENTER); }
}
