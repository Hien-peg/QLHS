/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sgu.qlhs.ui.components;

/**
 *
 * @author minho
 */
import javax.swing.*;
import java.awt.*;

public class RoundedPanel extends JPanel {
    private final int arc; private final Color bg; private final Color border;
    public RoundedPanel(int arc, Color bg, Color border){ this.arc=arc; this.bg=bg; this.border=border; setOpaque(false);}    
    @Override protected void paintComponent(Graphics g){
        var g2=(Graphics2D)g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(bg); g2.fillRoundRect(0,0,getWidth()-1,getHeight()-1, arc, arc);
        g2.setColor(border); g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1, arc, arc);
        g2.dispose();
        super.paintComponent(g);
    }
}

