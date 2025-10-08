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

public class BarChartCanvas extends JComponent {
    private final String title; private final String[] cats; private final int[] values;
    public BarChartCanvas(String title, String[] categories, int[] values){
        this.title=title; this.cats=categories; this.values=values; setPreferredSize(new Dimension(600, 360)); setOpaque(false);
    }
    @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w=getWidth(), h=getHeight();
        g2.setColor(new Color(29,35,66)); g2.setFont(getFont().deriveFont(Font.BOLD, 18f)); g2.drawString(title, 12, 26);
        int left=60, right=20, top=50, bottom=40, pw = w-left-right, ph = h-top-bottom;
        g2.setColor(new Color(250,250,252)); g2.fillRoundRect(left, top, pw, ph, 12,12);
        g2.setColor(new Color(230,235,245)); g2.drawRoundRect(left, top, pw, ph, 12,12);
        int max = 0; for(int v: values) max=Math.max(max,v); max=Math.max(max,1);
        int n=values.length, barW=Math.max(20,(pw-40)/(n*2)), gap=barW, x=left+20;
        for(int i=0;i<n;i++){ int bh=(int)((values[i]*1.0/max)*(ph-30)), y=top+ph-bh-10;
            g2.setColor(new Color(180,205,255)); g2.fillRoundRect(x,y,barW,bh,8,8);
            g2.setColor(new Color(120,160,230)); g2.drawRoundRect(x,y,barW,bh,8,8);
            g2.setColor(Color.DARK_GRAY); var fm=g2.getFontMetrics(); String cat=cats[i];
            g2.drawString(cat, x+(barW-fm.stringWidth(cat))/2, top+ph+16);
            String val=String.valueOf(values[i]); g2.drawString(val, x+(barW-fm.stringWidth(val))/2, y-4);
            x+=barW+gap; }
        g2.dispose();
    }
}
