/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sgu.qlhs.ui.panels;

/**
 *
 * @author minho
 */
import com.sgu.qlhs.ui.components.RoundedPanel;
import com.sgu.qlhs.ui.model.LopTableModel;
import com.sgu.qlhs.ui.model.PhongTableModel;
import javax.swing.*; import javax.swing.border.EmptyBorder; import java.awt.*;
import static com.sgu.qlhs.ui.MainDashboard.*;

public class LopPhongPanel extends JPanel {
    public LopPhongPanel(){ setLayout(new BorderLayout()); setOpaque(false);
        var outer = new RoundedPanel(18, CARD_BG, CARD_BORDER); outer.setLayout(new BorderLayout());
        var lbl = new JLabel("Lớp / Phòng"); lbl.setBorder(new EmptyBorder(12,16,8,16)); lbl.setFont(lbl.getFont().deriveFont(Font.BOLD,18f));
        outer.add(lbl, BorderLayout.NORTH);
        var tabs = new JTabbedPane(); tabs.setOpaque(false);
        var lopPanel = new JPanel(new BorderLayout()); lopPanel.setOpaque(false);
        lopPanel.add(new JScrollPane(new JTable(new LopTableModel())), BorderLayout.CENTER);
        var phongPanel = new JPanel(new BorderLayout()); phongPanel.setOpaque(false);
        phongPanel.add(new JScrollPane(new JTable(new PhongTableModel())), BorderLayout.CENTER);
        tabs.addTab("Lớp", lopPanel); tabs.addTab("Phòng", phongPanel);
        outer.add(tabs, BorderLayout.CENTER);
        add(outer, BorderLayout.CENTER); }
}
