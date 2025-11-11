package com.sgu.qlhs.ui;

import com.sgu.qlhs.bus.*;
import com.sgu.qlhs.dto.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class DangNhapUI extends JFrame {

    private JTextField txtUser;
    private JPasswordField txtPass;
    private JButton btnLogin;

    private static final Color PRIMARY = new Color(33, 84, 170);
    private static final Color BG = new Color(246, 248, 251);
    private static final Color FIELD_BG = Color.WHITE;
    private static final Color FIELD_BORDER = new Color(210, 215, 230);

    public DangNhapUI() {
        setTitle("Đăng nhập hệ thống");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(420, 320);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG);
        setContentPane(root);

        JLabel lblTitle = new JLabel("QUẢN LÝ HỌC SINH", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(PRIMARY);
        lblTitle.setBorder(new EmptyBorder(25, 0, 5, 0));
        root.add(lblTitle, BorderLayout.NORTH);

        JPanel form = new JPanel();
        form.setBackground(BG);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(new EmptyBorder(20, 60, 20, 60));
        root.add(form, BorderLayout.CENTER);

        JLabel lblUser = new JLabel("Tên đăng nhập:");
        lblUser.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblUser.setForeground(Color.DARK_GRAY);
        form.add(lblUser);

        txtUser = new JTextField();
        txtUser.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtUser.setBackground(FIELD_BG);
        txtUser.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(FIELD_BORDER),
                new EmptyBorder(6, 8, 6, 8)));
        form.add(txtUser);
        form.add(Box.createVerticalStrut(12));

        JLabel lblPass = new JLabel("Mật khẩu:");
        lblPass.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblPass.setForeground(Color.DARK_GRAY);
        form.add(lblPass);

        txtPass = new JPasswordField();
        txtPass.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtPass.setBackground(FIELD_BG);
        txtPass.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(FIELD_BORDER),
                new EmptyBorder(6, 8, 6, 8)));
        form.add(txtPass);
        form.add(Box.createVerticalStrut(20));

        btnLogin = new JButton("Đăng nhập");
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setBackground(PRIMARY);
        btnLogin.setFocusPainted(false);
        btnLogin.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btnLogin.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLogin.setAlignmentX(Component.CENTER_ALIGNMENT);

        btnLogin.addChangeListener(e -> {
            if (btnLogin.getModel().isRollover())
                btnLogin.setBackground(new Color(25, 70, 145));
            else
                btnLogin.setBackground(PRIMARY);
        });

        form.add(btnLogin);
        form.add(Box.createVerticalStrut(10));

        btnLogin.addActionListener(e -> xuLyDangNhap());
    }

    private void xuLyDangNhap() {
        try {
            String user = txtUser.getText().trim();
            String pass = new String(txtPass.getPassword());

            if (user.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu!",
                        "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
                return;
            }

            NguoiDungBUS bus = new NguoiDungBUS();
            NguoiDungDTO nd = bus.dangNhap(user, pass);

            if (nd != null) {
                moGiaoDienTheoVaiTro(nd);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Sai tên đăng nhập hoặc mật khẩu!",
                        "Lỗi đăng nhập", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Lỗi hệ thống: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void moGiaoDienTheoVaiTro(NguoiDungDTO nd) {
        SwingUtilities.invokeLater(() -> {
            try {
                String vaiTro = nd.getVaiTro();
                System.out.println(">> Vai trò đăng nhập: " + vaiTro + " | Tài khoản: " + nd.getTenDangNhap());

                switch (vaiTro) {
                    case "quan_tri_vien" -> {
                        System.out.println(">> Mở giao diện Quản trị viên");
                        new MainDashboard(nd).setVisible(true);

                    }

                    case "giao_vien" -> {
                        System.out.println(">> Kiểm tra giáo viên có là GVCN không...");

                        // Kiểm tra xem MaGV này có trong bảng ChuNhiem không
                        ChuNhiemBUS cnBus = new ChuNhiemBUS();
                        ChuNhiemDTO cn = cnBus.getChuNhiemByGV(nd.getId());

                        if (cn != null) {
                            // Nếu CÓ chủ nhiệm -> Mở ChuNhiemDashboard
                            System.out.println(">> Là GVCN của lớp " + cn.getMaLop());
                            new ChuNhiemDashboard(nd, cn).setVisible(true);
                        } else {
                            // Nếu KHÔNG chủ nhiệm -> Mở GiaoVienDashboard
                            System.out.println(">> Là giáo viên bộ môn");
                            new GiaoVienDashboard(nd).setVisible(true);
                        }
                    }

                    case "hoc_sinh" -> {
                        System.out.println(">> Mở giao diện Học sinh");

                        // Khởi tạo BUS để lấy thông tin học sinh
                        HocSinhBUS hsBUS = new HocSinhBUS();
                        HocSinhDTO hs = hsBUS.getByMaHS(nd.getId()); // Lấy theo MaHS, không dùng MaTK

                        if (hs != null) {
                            System.out.println("🟢 Học sinh đăng nhập: " + hs.getHoTen());
                            System.out.println("   MaHS: " + hs.getMaHS() + " | MaLop: " + hs.getMaLop());

                            // Mở giao diện dashboard học sinh
                            new HocSinhDashboard(nd, hs.getMaLop()).setVisible(true);
                        } else {
                            System.err.println("⚠ Không tìm thấy thông tin học sinh với MaHS = " + nd.getId());
                            JOptionPane.showMessageDialog(this,
                                    "Không tìm thấy thông tin học sinh!",
                                    "Lỗi đăng nhập", JOptionPane.ERROR_MESSAGE);
                        }
                    }

                    default -> {
                        JOptionPane.showMessageDialog(this,
                                "Vai trò không hợp lệ: " + vaiTro,
                                "Lỗi phân quyền", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }

                this.dispose();

            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this,
                        "Lỗi khi mở giao diện: " + e.getMessage(),
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    public static void moLaiDangNhap() {
        SwingUtilities.invokeLater(() -> new DangNhapUI().setVisible(true));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            } catch (Exception ignored) {
            }
            new DangNhapUI().setVisible(true);
        });
    }
}