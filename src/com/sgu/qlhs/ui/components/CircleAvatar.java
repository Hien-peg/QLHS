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

public class CircleAvatar extends JComponent {
    private final int r; private final Color stroke;
    public CircleAvatar(int r, Color stroke){ this.r=r; this.stroke=stroke; setPreferredSize(new Dimension(r*2+8, r*2+8)); }
    @Override protected void paintComponent(Graphics g){
        var g2=(Graphics2D)g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int d=r*2; int x=4, y=4;
        g2.setColor(new Color(255,255,255,40)); g2.fillOval(x, y, d, d);
        g2.setColor(stroke); g2.drawOval(x, y, d, d);
        g2.setColor(Color.WHITE); g2.setFont(getFont().deriveFont(Font.BOLD, r));
        String s="👤"; FontMetrics fm=g2.getFontMetrics();
        int tx=x+(d-fm.stringWidth(s))/2; int ty=y+(d+fm.getAscent()-fm.getDescent())/2;
        g2.drawString(s, tx, ty);
        g2.dispose();
    }
}

