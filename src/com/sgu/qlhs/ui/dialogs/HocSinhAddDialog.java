package com.sgu.qlhs.ui.dialogs;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import com.sgu.qlhs.bus.HocSinhBUS;
import com.sgu.qlhs.bus.LopBUS;
import com.sgu.qlhs.database.HocSinhDAO;

import java.text.SimpleDateFormat;
import java.util.Date;

public class HocSinhAddDialog extends JDialog {
    private JTextField txtHoTen, txtSdt, txtEmail, txtDiaChi;
    private JComboBox<String> cboGioiTinh, cboLop;
    private JSpinner spNgaySinh;

    // DAO
    private HocSinhDAO hocSinhDAO = new HocSinhDAO();
    // LopBUS to load classes dynamically
    private LopBUS lopBUS = new LopBUS();
    private java.util.List<Integer> lopIds = new java.util.ArrayList<>();

    public HocSinhAddDialog(Window owner) {
        super(owner, "Thêm học sinh", ModalityType.APPLICATION_MODAL);
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

        txtHoTen = new JTextField();
        txtSdt = new JTextField();
        txtEmail = new JTextField();
        txtDiaChi = new JTextField();

        cboGioiTinh = new JComboBox<>(new String[] { "Nam", "Nữ", "Khác" });
        cboLop = new JComboBox<>();

        // load lop list from LopBUS
        try {
            var lops = lopBUS.getAllLop();
            for (var l : lops) {
                cboLop.addItem(l.getTenLop());
                lopIds.add(l.getMaLop());
            }
        } catch (Exception ex) {
            // fallback to defaults if loading fails
            cboLop.addItem("10A1");
            cboLop.addItem("10A2");
            cboLop.addItem("11A1");
            cboLop.addItem("12A1");
        }

        spNgaySinh = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor editor = new JSpinner.DateEditor(spNgaySinh, "dd/MM/yyyy");
        spNgaySinh.setEditor(editor);

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
        var btnCancel = new JButton("Hủy");
        var btnSave = new JButton("Thêm");

        btnCancel.addActionListener(e -> dispose());
        btnSave.addActionListener(e -> themHocSinh());

        btnPane.add(btnCancel);
        btnPane.add(btnSave);
        add(btnPane, BorderLayout.SOUTH);
    }

    // Hàm xử lý thêm học sinh
    private void themHocSinh() {
        String hoTen = txtHoTen.getText().trim();
        String soDienThoai = txtSdt.getText().trim();
        String email = txtEmail.getText().trim();
        String diaChi = txtDiaChi.getText().trim();
        String gioiTinh = cboGioiTinh.getSelectedItem().toString();
        String tenLop = cboLop.getSelectedItem().toString();
        Date ngaySinh = (Date) spNgaySinh.getValue();

        if (hoTen.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập họ tên học sinh!");
            return;
        }

        try {
            // 🔹 Xác định mã lớp từ tên lớp
            int maLop = getMaLopByTenLop(tenLop);

            boolean success = hocSinhDAO.addHocSinh(
                    hoTen,
                    ngaySinh,
                    gioiTinh,
                    diaChi,
                    soDienThoai,
                    email,
                    maLop);

            if (success) {
                JOptionPane.showMessageDialog(this, "✅ Đã thêm học sinh mới thành công!");
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "❌ Lỗi khi thêm học sinh!");
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // 🔍 Lấy mã lớp từ tên lớp (dùng danh sách nạp từ DB)
    private int getMaLopByTenLop(String tenLop) throws Exception {
        // try to find in loaded list
        for (int i = 0; i < cboLop.getItemCount(); i++) {
            String t = cboLop.getItemAt(i);
            if (t != null && t.equals(tenLop)) {
                if (i < lopIds.size())
                    return lopIds.get(i);
            }
        }
        // fallback: search via LopBUS
        var lops = lopBUS.getAllLop();
        for (var l : lops) {
            if (l.getTenLop().equals(tenLop))
                return l.getMaLop();
        }
        throw new Exception("Không tìm thấy mã lớp cho " + tenLop);
    }

    // Test riêng
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new HocSinhAddDialog(null).setVisible(true));
    }
}
