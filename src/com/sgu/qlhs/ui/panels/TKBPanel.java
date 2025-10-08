package com.sgu.qlhs.ui.panels;

import com.sgu.qlhs.ui.components.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Random;
import static com.sgu.qlhs.ui.MainDashboard.*;

public class TKBPanel extends JPanel {

    private static final String[] MON_HOC = {
        "Toán", "Văn", "Anh", "Lý", "Hóa", "Sinh", "Sử", "Địa",
        "GDCD", "Tin học", "Công nghệ", "Thể dục", "Âm nhạc", "Mỹ thuật", "SHCN"
    };

    private JTable table;
    private static final int SO_TIET = 8;
    private static final int SO_THU = 5;

    public TKBPanel() {
        setLayout(new BorderLayout());
        setOpaque(false);

        // ==== PANEL NGOÀI ====
        var outer = new RoundedPanel(18, CARD_BG, CARD_BORDER);
        outer.setLayout(new BorderLayout());

        // ==== TIÊU ĐỀ ====
        var lblTitle = new JLabel("Thời khóa biểu");
        lblTitle.setBorder(new EmptyBorder(12, 16, 8, 16));
        lblTitle.setFont(lblTitle.getFont().deriveFont(Font.BOLD, 18f));

        // ==== THANH CHỌN LỚP & NÚT ====
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        topPanel.setOpaque(false);
        topPanel.setBorder(new EmptyBorder(5, 16, 10, 16));

        JLabel lblLop = new JLabel("Chọn lớp:");
        JComboBox<String> cbLop = new JComboBox<>(new String[]{
            "10A1", "10A2", "11A1", "12A1"
        });
        cbLop.setPreferredSize(new Dimension(100, 28));

        JButton btnTai = new JButton("Tải");
        btnTai.setFocusPainted(false);
        btnTai.setBackground(new Color(66, 133, 244));
        btnTai.setForeground(Color.WHITE);
        btnTai.setFont(btnTai.getFont().deriveFont(Font.BOLD, 13f));
        btnTai.setBorder(BorderFactory.createEmptyBorder(5, 12, 5, 12));
        btnTai.setCursor(new Cursor(Cursor.HAND_CURSOR));

        topPanel.add(lblLop);
        topPanel.add(cbLop);
        topPanel.add(btnTai);

        // ==== BẢNG THỜI KHÓA BIỂU ====
        String[] columns = {"Tiết", "Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6"};
        Object[][] data = taoThoiKhoaBieuNgauNhien(SO_TIET, SO_THU);

        table = new JTable(data, columns);
        table.setRowHeight(28);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.setGridColor(new Color(230, 230, 230));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 16, 16, 16));

        // ==== HEADER GỒM TIÊU ĐỀ + THANH CHỌN ====
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.add(lblTitle, BorderLayout.NORTH);
        headerPanel.add(topPanel, BorderLayout.CENTER);

        outer.add(headerPanel, BorderLayout.NORTH);
        outer.add(scrollPane, BorderLayout.CENTER);

        add(outer, BorderLayout.CENTER);

        // ==== SỰ KIỆN NÚT "TẢI" ====
        btnTai.addActionListener(e -> {
            Object[][] newData = taoThoiKhoaBieuNgauNhien(SO_TIET, SO_THU);
            for (int i = 0; i < SO_TIET; i++) {
                for (int j = 0; j <= SO_THU; j++) {
                    table.setValueAt(newData[i][j], i, j);
                }
            }
        });
    }

    // ===== HÀM TẠO DỮ LIỆU NGẪU NHIÊN =====
    private Object[][] taoThoiKhoaBieuNgauNhien(int tietCount, int thuCount) {
        Object[][] data = new Object[tietCount][thuCount + 1];
        Random rd = new Random();

        for (int i = 0; i < tietCount; i++) {
            data[i][0] = "Tiết " + (i + 1);
            for (int j = 1; j <= thuCount; j++) {
                if (i < 5) {
                    // Tiết 1-5: luôn có môn học
                    data[i][j] = MON_HOC[rd.nextInt(MON_HOC.length)];
                } else {
                    // Tiết 6-8: có thể trống hoặc có môn (30% trống)
                    if (rd.nextDouble() < 0.3) {
                        data[i][j] = "";
                    } else {
                        data[i][j] = MON_HOC[rd.nextInt(MON_HOC.length)];
                    }
                }
            }
        }
        return data;
    }
}
