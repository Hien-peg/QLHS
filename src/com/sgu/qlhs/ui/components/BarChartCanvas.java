package com.sgu.qlhs.ui.components;

import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat; 

public class BarChartCanvas extends JComponent {
    private final String title;
    private final String[] cats;
    private final double[] values;
    private final DecimalFormat df = new DecimalFormat("#.#");

    public BarChartCanvas(String title, String[] categories, double[] values) {
        this.title = title;
        this.cats = categories;
        this.values = values;
        setPreferredSize(new Dimension(600, 360));
        setOpaque(false);
    }
    
    public BarChartCanvas(String title, String[] categories, int[] intValues) {
        this.title = title;
        this.cats = categories;
        this.values = new double[intValues.length];
        for (int i = 0; i < intValues.length; i++) {
            this.values[i] = (double) intValues[i];
        }
        setPreferredSize(new Dimension(600, 360));
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth(), h = getHeight();
        g2.setColor(new Color(29, 35, 66));
        g2.setFont(getFont().deriveFont(Font.BOLD, 18f));
        g2.drawString(title, 12, 26);

        int left = 60, right = 20, top = 50, bottom = 100, pw = w - left - right, ph = h - top - bottom;

        g2.setColor(new Color(250, 250, 252));
        g2.fillRoundRect(left, top, pw, ph, 12, 12);
        g2.setColor(new Color(230, 235, 245));
        g2.drawRoundRect(left, top, pw, ph, 12, 12);
        
        double max = 0.0;
        for (double v : values)
            max = Math.max(max, v);
        max = Math.max(max, 1.0); 

        int n = values.length, barW, gap;
        if (n <= 6) {
            barW = Math.max(20, (pw - 40) / (n * 2));
            gap = barW;
        } else {
            gap = 15; 
            barW = Math.max(15, (pw - 40 - (gap * (n - 1))) / n);
        }
        int x = left + 20;

        for (int i = 0; i < n; i++) {
            int bh = (int) ((values[i] / max) * (ph - 30));
            int y = top + ph - bh - 10;

            g2.setColor(new Color(180, 205, 255));
            g2.fillRoundRect(x, y, barW, bh, 8, 8);
            g2.setColor(new Color(120, 160, 230));
            g2.drawRoundRect(x, y, barW, bh, 8, 8);
            g2.setColor(Color.DARK_GRAY);
            var fm = g2.getFontMetrics();
            String cat = cats[i];

            Graphics2D g2r = (Graphics2D) g2.create();
            try {
                int tx = x + barW / 2;
                int ty = top + ph + 8; 
                g2r.translate(tx, ty);
                g2r.rotate(Math.PI / 2); 
                g2r.drawString(cat, 0, -fm.getDescent());
            } finally {
                g2r.dispose(); 
            }
            
            String val;
            if (values[i] == (long) values[i]) {
                val = String.valueOf((long) values[i]);
            } else {
                val = df.format(values[i]);
            }

            g2.drawString(val, x + (barW - fm.stringWidth(val)) / 2, y - 4);
            x += barW + gap;
        }
        g2.dispose();
    }
}