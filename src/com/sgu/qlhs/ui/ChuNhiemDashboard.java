package com.sgu.qlhs.ui;

import com.sgu.qlhs.dto.*;
import com.sgu.qlhs.ui.panels.*;
import javax.swing.*;
import java.awt.*;

/**
 * Dashboard dành cho Giáo viên Chủ nhiệm
 * - Có 4 chức năng: Lịch dạy của tôi, TKB lớp chủ nhiệm, Điểm, Thống kê
 * - Có nút Đăng xuất
 * - Giữ nguyên style của MainDashboard
 */
public class ChuNhiemDashboard extends MainDashboard {

    private JButton btnLichDay, btnTkbLop, btnDiem, btnThongKe, btnLogout;
    private JButton btnHs;
    private final ChuNhiemDTO cn;
    private final NguoiDungDTO nd;

    public ChuNhiemDashboard(NguoiDungDTO nd, ChuNhiemDTO cn) {
        super(nd);
        this.cn = cn;
        this.nd = nd;
        setTitle("Chủ nhiệm - " + nd.getHoTen());
        buildChuNhiemUI();
    }

    // === THÊM HÀM MỚI ===
    /**
     * Cung cấp thông tin chủ nhiệm cho các panel con (như ThongKePanel).
     */
    public ChuNhiemDTO getChuNhiemInfo() {
        return this.cn;
    }
    // ======================

    private void buildChuNhiemUI() {
        // Xóa sidebar mặc định để thay sidebar riêng
        sidebar.removeAll();

        JLabel lblTitle = new JLabel("GIÁO VIÊN CHỦ NHIỆM", SwingConstants.LEFT);
        lblTitle.setFont(new Font("Segoe UI Semibold", Font.BOLD, 16));
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(lblTitle);
        sidebar.add(Box.createVerticalStrut(20));

        // ===== Các nút chức năng =====
        btnLichDay = createSidebarButton("Lịch dạy của tôi");
        btnHs = createSidebarButton("Học sinh");
        btnTkbLop = createSidebarButton("TKB lớp chủ nhiệm");
        btnDiem = createSidebarButton("Điểm lớp chủ nhiệm");
        btnThongKe = createSidebarButton("Thống kê lớp");
        btnLogout = createSidebarButton("Đăng xuất");

        sidebar.add(btnLichDay);
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(btnHs);
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(btnTkbLop);
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(btnDiem);
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(btnThongKe);
        sidebar.add(Box.createVerticalGlue());
        sidebar.add(btnLogout);

        // ==== Khu vực hiển thị nội dung chính ====
        cards = new CardLayout();
        centerCards = new JPanel(cards);
        centerCards.setBackground(PAGE_BG);

        // 1️⃣ Lịch dạy của tôi (TKBPanel lọc theo giáo viên)
        JPanel pnlLichDay = new TKBPanel("GiaoVien", nd.getId(), null);
        centerCards.add(pnlLichDay, "LICHDAY");

        // 1.5️⃣ Danh sách học sinh (dành cho Chủ nhiệm)
        JPanel pnlHs = new HocSinhPanel();
        centerCards.add(pnlHs, "HS");

        // 2️⃣ TKB lớp chủ nhiệm (TKBPanel lọc theo lớp)
        JPanel pnlTkbLop = new TKBPanel("ChuNhiem", nd.getId(), cn.getMaLop());
        centerCards.add(pnlTkbLop, "TKBLOP");

        // 3️⃣ Điểm lớp chủ nhiệm (dùng lại panel DiemPanel)
        JPanel pnlDiem = new DiemPanel();
        centerCards.add(pnlDiem, "DIEM");

        // 4️⃣ Thống kê lớp (dùng lại panel ThongKePanel)
        JPanel pnlThongKe = new ThongKePanel();
        centerCards.add(pnlThongKe, "THONGKE");

        add(centerCards, BorderLayout.CENTER);

        // ==== Sự kiện các nút ====
        btnLichDay.addActionListener(e -> cards.show(centerCards, "LICHDAY"));
        btnHs.addActionListener(e -> cards.show(centerCards, "HS"));
        btnTkbLop.addActionListener(e -> cards.show(centerCards, "TKBLOP"));
        btnDiem.addActionListener(e -> cards.show(centerCards, "DIEM"));
        btnThongKe.addActionListener(e -> cards.show(centerCards, "THONGKE"));
        btnLogout.addActionListener(e -> {
            this.dispose();
            DangNhapUI.moLaiDangNhap();
        });

        // Mặc định hiển thị TKB lớp chủ nhiệm
        cards.show(centerCards, "TKBLOP");
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