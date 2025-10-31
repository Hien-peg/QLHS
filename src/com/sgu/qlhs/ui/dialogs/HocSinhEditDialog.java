package com.sgu.qlhs.ui.dialogs;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import com.sgu.qlhs.bus.HocSinhBUS;
import com.sgu.qlhs.bus.LopBUS;
import com.sgu.qlhs.database.HocSinhDAO;
import com.sgu.qlhs.DatabaseConnection;

public class HocSinhEditDialog extends JDialog {
    private JTextField txtMa, txtHoTen, txtSdt, txtEmail, txtDiaChi;
    private JComboBox<String> cboGioiTinh, cboLop;
    private JSpinner spNgaySinh;
    private JButton btnLoad, btnSave, btnCancel;

    private HocSinhDAO hocSinhDAO = new HocSinhDAO();
    private LopBUS lopBUS = new LopBUS();
    private java.util.List<Integer> lopIds = new java.util.ArrayList<>();

    public HocSinhEditDialog(Window owner) {
        super(owner, "Sửa thông tin học sinh", ModalityType.APPLICATION_MODAL);
        setSize(600, 500);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(12, 12));
        buildForm();
    }

    private void buildForm() {
        var mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(16, 16, 16, 16));

        // ===== Thông tin học sinh =====
        var pnlHS = new JPanel(new GridLayout(0, 2, 10, 10));
        pnlHS.setBorder(BorderFactory.createTitledBorder("Thông tin học sinh"));

        txtMa = new JTextField();
        btnLoad = new JButton("Tải thông tin");

        txtHoTen = new JTextField();
        txtSdt = new JTextField();
        txtDiaChi = new JTextField();
        txtEmail = new JTextField();
        cboGioiTinh = new JComboBox<>(new String[] { "Nam", "Nữ", "Khác" });
        cboLop = new JComboBox<>();

        // load lop list
        try {
            var lops = lopBUS.getAllLop();
            for (var l : lops) {
                cboLop.addItem(l.getTenLop());
                lopIds.add(l.getMaLop());
            }
        } catch (Exception ex) {
            cboLop.addItem("10A1");
            cboLop.addItem("10A2");
            cboLop.addItem("11A1");
            cboLop.addItem("12A1");
        }

        spNgaySinh = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor editor = new JSpinner.DateEditor(spNgaySinh, "dd/MM/yyyy");
        spNgaySinh.setEditor(editor);

        pnlHS.add(new JLabel("Mã HS:"));
        JPanel pnlMa = new JPanel(new BorderLayout(5, 0));
        pnlMa.add(txtMa, BorderLayout.CENTER);
        pnlMa.add(btnLoad, BorderLayout.EAST);
        pnlHS.add(pnlMa);

        pnlHS.add(new JLabel("Họ tên:"));
        pnlHS.add(txtHoTen);
        pnlHS.add(new JLabel("Ngày sinh:"));
        pnlHS.add(spNgaySinh);
        pnlHS.add(new JLabel("Giới tính:"));
        pnlHS.add(cboGioiTinh);
        pnlHS.add(new JLabel("Lớp:"));
        pnlHS.add(cboLop);
        pnlHS.add(new JLabel("Địa chỉ:"));
        pnlHS.add(txtDiaChi);
        pnlHS.add(new JLabel("SĐT:"));
        pnlHS.add(txtSdt);
        pnlHS.add(new JLabel("Email:"));
        pnlHS.add(txtEmail);

        mainPanel.add(pnlHS);
        add(new JScrollPane(mainPanel), BorderLayout.CENTER);

        // ===== Nút =====
        var btnPane = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnCancel = new JButton("Hủy");
        btnSave = new JButton("Lưu thay đổi");

        btnCancel.addActionListener(e -> dispose());
        btnSave.addActionListener(e -> capNhatHocSinh());
        btnSave.setEnabled(false); // Chỉ bật sau khi tải thông tin

        btnPane.add(btnCancel);
        btnPane.add(btnSave);
        add(btnPane, BorderLayout.SOUTH);

        // ===== Sự kiện tải thông tin =====
        btnLoad.addActionListener(e -> taiThongTinHocSinh());
    }

    // Hàm tải thông tin học sinh từ DB
    private void taiThongTinHocSinh() {
        String maText = txtMa.getText().trim();
        if (maText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập mã học sinh!");
            return;
        }

        try {
            int maHS = Integer.parseInt(maText);
            String sql = "SELECT hs.*, l.TenLop FROM HocSinh hs JOIN Lop l ON hs.MaLop = l.MaLop WHERE hs.MaHS = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                    PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, maHS);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        txtHoTen.setText(rs.getString("HoTen"));
                        spNgaySinh.setValue(rs.getDate("NgaySinh"));
                        cboGioiTinh.setSelectedItem(rs.getString("GioiTinh"));
                        cboLop.setSelectedItem(rs.getString("TenLop"));
                        txtDiaChi.setText(rs.getString("DiaChi"));
                        txtSdt.setText(rs.getString("SoDienThoai"));
                        txtEmail.setText(rs.getString("Email"));

                        btnSave.setEnabled(true);
                    } else {
                        JOptionPane.showMessageDialog(this, "Không tìm thấy học sinh có mã: " + maHS);
                        btnSave.setEnabled(false);
                    }
                }
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Mã học sinh phải là số!");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi tải dữ liệu: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Hàm cập nhật học sinh
    private void capNhatHocSinh() {
        try {
            int maHS = Integer.parseInt(txtMa.getText().trim());
            String hoTen = txtHoTen.getText().trim();
            Date ngaySinh = (Date) spNgaySinh.getValue();
            String gioiTinh = cboGioiTinh.getSelectedItem().toString();
            String diaChi = txtDiaChi.getText().trim();
            String sdt = txtSdt.getText().trim();
            String email = txtEmail.getText().trim();
            String tenLop = cboLop.getSelectedItem().toString();
            int maLop = getMaLopByTenLop(tenLop);

            if (hoTen.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập họ tên học sinh!");
                return;
            }

            boolean success = hocSinhDAO.updateHocSinh(
                    maHS, hoTen, ngaySinh, gioiTinh, diaChi, sdt, email, maLop);

            if (success) {
                JOptionPane.showMessageDialog(this, "✅ Đã cập nhật thông tin học sinh!");
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "❌ Cập nhật thất bại!");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Chuyển tên lớp → mã lớp (dựa vào dữ liệu mẫu)
    private int getMaLopByTenLop(String tenLop) throws Exception {
        for (int i = 0; i < cboLop.getItemCount(); i++) {
            String t = cboLop.getItemAt(i);
            if (t != null && t.equals(tenLop)) {
                if (i < lopIds.size())
                    return lopIds.get(i);
            }
        }
        var lops = lopBUS.getAllLop();
        for (var l : lops) {
            if (l.getTenLop().equals(tenLop))
                return l.getMaLop();
        }
        throw new Exception("Không tìm thấy mã lớp cho " + tenLop);
    }

    // Test riêng
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new HocSinhEditDialog(null).setVisible(true));
    }
}
