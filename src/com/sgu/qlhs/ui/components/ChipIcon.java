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
import java.awt.BasicStroke;

public class ChipIcon extends JComponent {
    private final int size; private final Color bg; private final Color fg; private final IconType type;
    public ChipIcon(int size, Color bg, Color fg, IconType type){ this.size=size; this.bg=bg; this.fg=fg; this.type=type; setPreferredSize(new Dimension(size,size)); }
    @Override protected void paintComponent(Graphics g){
        var g2=(Graphics2D)g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int s=size; int arc=12; g2.setColor(bg); g2.fillRoundRect(0,0,s,s,arc,arc); g2.setColor(fg);
        switch(type){
            case ADD -> { g2.fillRoundRect(s/2-1, s/5, 2, s*3/5, 2,2); g2.fillRoundRect(s/5, s/2-1, s*3/5, 2, 2,2); }
            case EDIT -> { g2.setStroke(new BasicStroke(2f)); g2.drawLine(s/4, s*3/4, s*3/4, s/4); g2.drawLine(s/3, s*2/3, s*2/3, s/3); }
            case DELETE -> { g2.setStroke(new BasicStroke(2f)); g2.drawLine(s/4, s/4, s*3/4, s*3/4); g2.drawLine(s*3/4, s/4, s/4, s*3/4); }
            case HOME -> { int[] x={s/5, s/2, s*4/5}; int[] y={s/2, s/5, s/2}; g2.drawPolygon(x,y,3); g2.drawRect(s/3, s/2, s/3, s/3); }
            case ABC -> { g2.setFont(getFont().deriveFont(Font.BOLD, s/2f)); g2.drawString("ABC", 3, s*2/3); }
            case ROOM -> { g2.drawRect(s/4, s/4, s/2, s/2); g2.fillRect(s/2-2, s/2, 4, s/4); }
            case CLIPBOARD -> { g2.drawRect(s/4, s/4, s/2, s/2); g2.drawLine(s/3, s/2, s*2/3, s/2); g2.drawLine(s/3, s*2/3, s*2/3, s*2/3); }
            case TABLE -> { g2.drawRect(s/5, s/5, s*3/5, s*3/5); g2.drawLine(s/5, s/2, s*4/5, s/2); g2.drawLine(s/2, s/5, s/2, s*4/5); }
            case PDF -> { g2.setFont(getFont().deriveFont(Font.BOLD, s/2f)); g2.drawString("PDF", 3, s*2/3); }
            case CHART -> { g2.fillRect(s/5, s*2/3, s/7, s/6); g2.fillRect(s/2- s/14, s/2, s/7, s/3); g2.fillRect(s*2/3, s/3, s/7, s/2); }
            case BARCHART -> { g2.drawLine(s/5, s*4/5, s*4/5, s*4/5); g2.fillRect(s/4, s*3/5, s/10, s/5); g2.fillRect(s/2- s/12, s/2, s/10, s*3/10); g2.fillRect(s*3/4- s/12, s/3, s/10, s*1/2); }
            case MATRIX -> { g2.drawRect(s/5, s/5, s*3/5, s*3/5); g2.drawLine(s/5, s/2, s*4/5, s/2); g2.drawLine(s/2, s/5, s/2, s*4/5); }
        }
        g2.dispose();
    }
}
