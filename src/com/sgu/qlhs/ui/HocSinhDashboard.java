package com.sgu.qlhs.ui;

import com.sgu.qlhs.dto.NguoiDungDTO;
import com.sgu.qlhs.ui.panels.*;

import javax.swing.*;
import java.awt.*;

/**
 * Dashboard dành cho Học sinh
 * - Kế thừa MainDashboard
 * - Nhận MaLop trực tiếp từ lúc đăng nhập
 * - Hiển thị Thời khóa biểu, Điểm, Thống kê
 */
public class HocSinhDashboard extends MainDashboard {

    private JButton btnTkb, btnDiem, btnThongKe, btnLogout;
    private final NguoiDungDTO nd;
    private final int maLopHS;

    // ✅ Constructor nhận MaLop từ DangNhapUI
    public HocSinhDashboard(NguoiDungDTO nd, int maLopHS) {
        super(nd);
        this.nd = nd;
        this.maLopHS = maLopHS;
        setTitle("Học sinh - " + nd.getHoTen());
        buildHocSinhUI();
    }

    private void buildHocSinhUI() {
        // Xóa sidebar mặc định của admin
        sidebar.removeAll();

        JLabel lblTitle = new JLabel("HỌC SINH", SwingConstants.LEFT);
        lblTitle.setFont(new Font("Segoe UI Semibold", Font.BOLD, 16));
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(lblTitle);
        sidebar.add(Box.createVerticalStrut(20));

        // ===== Các nút sidebar =====
        btnTkb = createSidebarButton("Thời khóa biểu");
        btnDiem = createSidebarButton("Điểm");
        btnThongKe = createSidebarButton("Thống kê");
        btnLogout = createSidebarButton("Đăng xuất");

        sidebar.add(btnTkb);
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(btnDiem);
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(btnThongKe);
        sidebar.add(Box.createVerticalGlue());
        sidebar.add(btnLogout);

        // ===== Khu vực hiển thị chính =====
        cards = new CardLayout();
        centerCards = new JPanel(cards);
        centerCards.setBackground(PAGE_BG);

        // ✅ Hiển thị TKB đúng lớp học sinh
        JPanel pnlTkb = new TKBPanel("HocSinh", null, maLopHS);
        centerCards.add(pnlTkb, "TKB");

        // Điểm học sinh
        JPanel pnlDiem = new DiemPanel();
        centerCards.add(pnlDiem, "DIEM");

        // Thống kê
        JPanel pnlThongKe = new ThongKePanel();
        centerCards.add(pnlThongKe, "THONGKE");

        add(centerCards, BorderLayout.CENTER);

        // ===== Gán sự kiện các nút =====
        btnTkb.addActionListener(e -> cards.show(centerCards, "TKB"));
        btnDiem.addActionListener(e -> cards.show(centerCards, "DIEM"));
        btnThongKe.addActionListener(e -> cards.show(centerCards, "THONGKE"));
        btnLogout.addActionListener(e -> {
            this.dispose();
            DangNhapUI.moLaiDangNhap();
        });

        // Mặc định hiển thị Thời khóa biểu
        cards.show(centerCards, "TKB");
    }

    private JButton createSidebarButton(String text) {
        JButton btn = new JButton(text);
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btn.setBackground(new Color(45, 85, 150));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 8));

        btn.addChangeListener(e -> {
            if (btn.getModel().isRollover())
                btn.setBackground(new Color(60, 110, 180));
            else
                btn.setBackground(new Color(45, 85, 150));
        });
        return btn;
    }
}
