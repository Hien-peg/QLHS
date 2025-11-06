package com.sgu.qlhs.ui.components;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.text.DecimalFormat;

public class PieChartCanvas extends JComponent {
    private final String title;
    private final double[] values;
    private final String[] labels;
    private final Color[] colors;

    private static final Color[] DEFAULT_COLORS = {
            new Color(69, 179, 157), // Giỏi (Xanh)
            new Color(241, 196, 15), // Khá (Vàng)
            new Color(230, 126, 34), // Trung bình (Cam)
            new Color(231, 76, 60),  // Yếu (Đỏ)
            new Color(155, 89, 182)  // Khác (Tím)
    };

    public PieChartCanvas(String title, double[] values, String[] labels) {
        this.title = title;
        this.values = values;
        this.labels = labels;
        this.colors = DEFAULT_COLORS;
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth(), h = getHeight();

        // Vẽ tiêu đề
        g2.setColor(new Color(29, 35, 66));
        g2.setFont(getFont().deriveFont(Font.BOLD, 18f));
        FontMetrics fmTitle = g2.getFontMetrics();
        g2.drawString(title, (w - fmTitle.stringWidth(title)) / 2, 26);

        // Tính toán tổng
        double total = 0;
        for (double v : values) {
            total += v;
        }

        if (total == 0) {
            g2.setColor(Color.GRAY);
            g2.setFont(getFont().deriveFont(Font.PLAIN, 16f));
            String msg = "Không có dữ liệu";
            g2.drawString(msg, (w - g2.getFontMetrics().stringWidth(msg)) / 2, h / 2);
            g2.dispose();
            return;
        }

        // Vùng vẽ biểu đồ
        int chartSize = Math.min(w, h) - 150;
        int chartX = 50;
        int chartY = (h - chartSize) / 2 + 20;

        // Vùng vẽ chú thích
        int legendX = chartX + chartSize + 30;
        int legendY = chartY + 20;
        int legendBoxSize = 20;

        g2.setFont(getFont().deriveFont(Font.PLAIN, 13f));
        FontMetrics fm = g2.getFontMetrics();
        DecimalFormat df = new DecimalFormat("#.#%");

        double currentAngle = 90.0;
        for (int i = 0; i < values.length; i++) {
            double arcAngle = (values[i] / total) * 360.0;
            g2.setColor(colors[i % colors.length]);

            // Vẽ cung tròn
            Arc2D.Double arc = new Arc2D.Double(chartX, chartY, chartSize, chartSize, currentAngle, -arcAngle, Arc2D.PIE);
            g2.fill(arc);

            // Vẽ chú thích
            g2.fillRoundRect(legendX, legendY + (i * 30), legendBoxSize, legendBoxSize, 5, 5);
            g2.setColor(Color.BLACK);
            String percent = df.format(values[i] / total);
            
            // SỬA LỖI: Bỏ (int) values[i] vì labels[i] đã chứa thông tin đó
            String label = String.format("%s - %s", labels[i], percent);
            
            g2.drawString(label, legendX + legendBoxSize + 10, legendY + (i * 30) + fm.getAscent());

            currentAngle -= arcAngle;
        }
        g2.dispose();
    }
}