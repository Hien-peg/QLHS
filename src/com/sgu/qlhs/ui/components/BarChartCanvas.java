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
    private final String title;
    private final String[] cats;
    private final int[] values;

    public BarChartCanvas(String title, String[] categories, int[] values) {
        this.title = title;
        this.cats = categories;
        this.values = values;
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

        // THAY ĐỔI: Tăng lề dưới (bottom) để có chỗ cho chữ xoay
        int left = 60, right = 20, top = 50, bottom = 100, pw = w - left - right, ph = h - top - bottom;

        g2.setColor(new Color(250, 250, 252));
        g2.fillRoundRect(left, top, pw, ph, 12, 12);
        g2.setColor(new Color(230, 235, 245));
        g2.drawRoundRect(left, top, pw, ph, 12, 12);
        int max = 0;
        for (int v : values)
            max = Math.max(max, v);
        max = Math.max(max, 1);

        // THAY ĐỔI: Tính toán lại barW và gap cho nhiều cột hơn
        int n = values.length, barW, gap;
        if (n <= 6) {
            barW = Math.max(20, (pw - 40) / (n * 2));
            gap = barW;
        } else {
            gap = 15; // Cố định gap
            barW = Math.max(15, (pw - 40 - (gap * (n - 1))) / n);
        }
        int x = left + 20;
        // ========================================================

        for (int i = 0; i < n; i++) {
            int bh = (int) ((values[i] * 1.0 / max) * (ph - 30)), y = top + ph - bh - 10;
            g2.setColor(new Color(180, 205, 255));
            g2.fillRoundRect(x, y, barW, bh, 8, 8);
            g2.setColor(new Color(120, 160, 230));
            g2.drawRoundRect(x, y, barW, bh, 8, 8);
            g2.setColor(Color.DARK_GRAY);
            var fm = g2.getFontMetrics();
            String cat = cats[i];

            // THAY ĐỔI: Xoay chữ 90 độ
            Graphics2D g2r = (Graphics2D) g2.create(); // Tạo bản sao của g2
            try {
                // Di chuyển tới vị trí (center-bottom của cột)
                int tx = x + barW / 2;
                int ty = top + ph + 8; // Dịch xuống 8px
                g2r.translate(tx, ty);
                g2r.rotate(Math.PI / 2); // Xoay 90 độ (PI/2 radians)

                // Vẽ chữ tại tọa độ (0, 0) của g2r
                // Dùng FontMetrics để căn trái (chữ sẽ đi "lên" trên)
                g2r.drawString(cat, 0, -fm.getDescent());
            } finally {
                g2r.dispose(); // Hủy bản sao để khôi phục trạng thái (không xoay)
            }
            // Dòng cũ: g2.drawString(cat, x+(barW-fm.stringWidth(cat))/2, top+ph+16);

            String val = String.valueOf(values[i]);
            g2.drawString(val, x + (barW - fm.stringWidth(val)) / 2, y - 4);
            x += barW + gap;
        }
        g2.dispose();
    }
}
