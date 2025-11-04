package com.sgu.qlhs.ui;

import com.sgu.qlhs.dto.NguoiDungDTO;
import com.sgu.qlhs.ui.panels.*;
import javax.swing.*;
import java.awt.*;

/**
 * Dashboard dành cho Giáo viên bộ môn
 * - Kế thừa MainDashboard
 * - Giữ 4 chức năng: Học sinh, Thời khóa biểu, Điểm, Thống kê
 * - Có nút Đăng xuất
 */
public class GiaoVienDashboard extends MainDashboard {

    private JButton btnHs, btnTkb, btnDiem, btnTk, btnLogout;
    private final NguoiDungDTO nd;

    public GiaoVienDashboard(NguoiDungDTO nd) {
        super(nd);
        this.nd = nd;
        setTitle("Giáo viên - " + nd.getHoTen());
        buildGiaoVienUI();
    }

    private void buildGiaoVienUI() {
        // Xóa sidebar mặc định của Admin
        sidebar.removeAll();

        JLabel lblTitle = new JLabel("GIÁO VIÊN BỘ MÔN", SwingConstants.LEFT);
        lblTitle.setFont(new Font("Segoe UI Semibold", Font.BOLD, 16));
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(lblTitle);
        sidebar.add(Box.createVerticalStrut(20));

        // ===== Nút Sidebar =====
        btnHs = createSidebarButton("Học sinh");
        btnTkb = createSidebarButton("Thời khóa biểu");
        btnDiem = createSidebarButton("Điểm");
        btnTk = createSidebarButton("Thống kê");
        btnLogout = createSidebarButton("Đăng xuất");

        sidebar.add(btnHs);
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(btnTkb);
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(btnDiem);
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(btnTk);
        sidebar.add(Box.createVerticalGlue());
        sidebar.add(btnLogout);

        // ===== Khu vực hiển thị chính =====
        cards = new CardLayout();
        centerCards = new JPanel(cards);
        centerCards.setBackground(PAGE_BG);

        // 1️⃣ Danh sách học sinh (panel cũ)
        JPanel pnlHs = new HocSinhPanel();
        centerCards.add(pnlHs, "HS");

        // 2️⃣ Thời khóa biểu của giáo viên (role: GiaoVien)
        JPanel pnlTkb = new TKBPanel("GiaoVien", nd.getId(), null);
        centerCards.add(pnlTkb, "TKB");

        // 3️⃣ Điểm học sinh (panel sẵn có)
        JPanel pnlDiem = new DiemPanel();
        centerCards.add(pnlDiem, "DIEM");

        // 4️⃣ Thống kê
        JPanel pnlTk = new ThongKePanel();
        centerCards.add(pnlTk, "TK");

        add(centerCards, BorderLayout.CENTER);

        // ===== Sự kiện các nút =====
        btnHs.addActionListener(e -> cards.show(centerCards, "HS"));
        btnTkb.addActionListener(e -> cards.show(centerCards, "TKB"));
        btnDiem.addActionListener(e -> cards.show(centerCards, "DIEM"));
        btnTk.addActionListener(e -> cards.show(centerCards, "TK"));
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
