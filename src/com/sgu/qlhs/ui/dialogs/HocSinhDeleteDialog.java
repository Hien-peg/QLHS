package com.sgu.qlhs.ui.dialogs;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import com.sgu.qlhs.bus.HocSinhBUS;
import com.sgu.qlhs.database.HocSinhDAO;
import com.sgu.qlhs.dto.HocSinhDTO;
import com.sgu.qlhs.DatabaseConnection;

public class HocSinhDeleteDialog extends JDialog {
    private JTextField txtMaHS;
    private JLabel lblHoTen, lblLop, lblKetQua;
    private JButton btnTim, btnDelete, btnCancel;

    private HocSinhDAO hocSinhDAO = new HocSinhDAO();

    public HocSinhDeleteDialog(Window owner) {
        super(owner, "Xóa học sinh", ModalityType.APPLICATION_MODAL);
        setSize(420, 300);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        buildForm();
    }

    private void buildForm() {
        JPanel pnlCenter = new JPanel(new GridLayout(0, 2, 10, 10));
        pnlCenter.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Ô nhập mã HS
        txtMaHS = new JTextField();
        btnTim = new JButton("Tìm");

        pnlCenter.add(new JLabel("Nhập mã học sinh:"));
        pnlCenter.add(txtMaHS);

        pnlCenter.add(new JLabel(""));
        pnlCenter.add(btnTim);

        // Thông tin sau khi tìm thấy
        pnlCenter.add(new JLabel("Họ tên:"));
        lblHoTen = new JLabel("-");
        pnlCenter.add(lblHoTen);

        pnlCenter.add(new JLabel("Lớp:"));
        lblLop = new JLabel("-");
        pnlCenter.add(lblLop);

        // Kết quả tìm kiếm
        lblKetQua = new JLabel(" ", SwingConstants.CENTER);
        lblKetQua.setForeground(Color.BLUE);

        add(pnlCenter, BorderLayout.CENTER);
        add(lblKetQua, BorderLayout.NORTH);

        // ===== Nút hành động =====
        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnCancel = new JButton("Hủy");
        btnDelete = new JButton("Xóa");
        btnDelete.setEnabled(false);

        pnlButtons.add(btnCancel);
        pnlButtons.add(btnDelete);
        add(pnlButtons, BorderLayout.SOUTH);

        // ===== Sự kiện =====
        btnCancel.addActionListener(e -> dispose());

        // 🔍 Nút TÌM — tìm học sinh theo mã (từ database)
        btnTim.addActionListener(e -> timHocSinh());

        // ❌ Nút XÓA — xóa học sinh khỏi database
        btnDelete.addActionListener(e -> xoaHocSinh());
    }

    private void timHocSinh() {
        String maText = txtMaHS.getText().trim();
        if (maText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập mã học sinh!");
            return;
        }

        try {
            int maHS = Integer.parseInt(maText);

            // Truy vấn trực tiếp để lấy thông tin
            String sql = "SELECT hs.HoTen, l.TenLop FROM HocSinh hs JOIN Lop l ON hs.MaLop = l.MaLop WHERE hs.MaHS = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                    PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, maHS);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        lblHoTen.setText(rs.getString("HoTen"));
                        lblLop.setText(rs.getString("TenLop"));
                        lblKetQua.setText("Đã tìm thấy học sinh!");
                        lblKetQua.setForeground(new Color(0, 128, 0));
                        btnDelete.setEnabled(true);
                    } else {
                        lblHoTen.setText("-");
                        lblLop.setText("-");
                        lblKetQua.setText("Không tìm thấy học sinh có mã " + maHS);
                        lblKetQua.setForeground(Color.RED);
                        btnDelete.setEnabled(false);
                    }
                }
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Mã học sinh phải là số!");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi truy vấn học sinh: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void xoaHocSinh() {
        String maText = txtMaHS.getText().trim();
        String ten = lblHoTen.getText();
        if (maText.isEmpty() || ten.equals("-")) {
            JOptionPane.showMessageDialog(this, "Chưa có học sinh để xóa!");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn xóa học sinh:\n" + ten + " (Mã: " + maText + ")?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            int maHS = Integer.parseInt(maText);
            boolean success = hocSinhDAO.deleteHocSinh(maHS);
            if (success) {
                JOptionPane.showMessageDialog(this, "✅ Đã xóa học sinh thành công!");
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "❌ Xóa thất bại! Học sinh có thể không tồn tại.");
            }
        }
    }

    // Để test riêng
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new HocSinhDeleteDialog(null).setVisible(true));
    }
}
