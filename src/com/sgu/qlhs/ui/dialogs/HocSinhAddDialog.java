package com.sgu.qlhs.ui.dialogs;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import com.sgu.qlhs.bus.HocSinhBUS;
import com.sgu.qlhs.bus.LopBUS;
import java.text.SimpleDateFormat;

public class HocSinhAddDialog extends JDialog {
    private JTextField txtMa, txtHoTen, txtSdt, txtEmail;
    private JComboBox<String> cboGioiTinh, cboLop;
    private JSpinner spNgaySinh;
    private final HocSinhBUS hocSinhBUS = new HocSinhBUS();
    private final LopBUS lopBUS = new LopBUS();

    // Phụ huynh 1
    private JTextField txtPh1HoTen, txtPh1Sdt, txtPh1Email;
    private JComboBox<String> cboPh1MoiQuanHe;

    // Phụ huynh 2
    private JTextField txtPh2HoTen, txtPh2Sdt, txtPh2Email;
    private JComboBox<String> cboPh2MoiQuanHe;

    public HocSinhAddDialog(Window owner) {
        super(owner, "Thêm học sinh", ModalityType.APPLICATION_MODAL);
        setSize(600, 700);
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
        txtHoTen = new JTextField();
        txtSdt = new JTextField();
        txtEmail = new JTextField();
        cboGioiTinh = new JComboBox<>(new String[] { "Nam", "Nữ", "Khác" });
        cboLop = new JComboBox<>(new String[] { "10A1", "10A2", "11A1", "12A1" });

        spNgaySinh = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor editor = new JSpinner.DateEditor(spNgaySinh, "dd/MM/yyyy");
        spNgaySinh.setEditor(editor);

        pnlHS.add(new JLabel("Mã HS:"));
        pnlHS.add(txtMa);
        pnlHS.add(new JLabel("Họ tên:"));
        pnlHS.add(txtHoTen);
        pnlHS.add(new JLabel("Ngày sinh:"));
        pnlHS.add(spNgaySinh);
        pnlHS.add(new JLabel("Giới tính:"));
        pnlHS.add(cboGioiTinh);
        pnlHS.add(new JLabel("Lớp:"));
        pnlHS.add(cboLop);
        pnlHS.add(new JLabel("SĐT:"));
        pnlHS.add(txtSdt);
        pnlHS.add(new JLabel("Email:"));
        pnlHS.add(txtEmail);

        // ===== Phụ huynh 1 =====
        var pnlPh1 = new JPanel(new GridLayout(0, 2, 10, 10));
        pnlPh1.setBorder(BorderFactory.createTitledBorder("Phụ huynh 1"));

        txtPh1HoTen = new JTextField();
        txtPh1Sdt = new JTextField();
        txtPh1Email = new JTextField();
        cboPh1MoiQuanHe = new JComboBox<>(new String[] { "Cha", "Mẹ", "Anh/Chị", "Giám hộ", "Khác" });

        pnlPh1.add(new JLabel("Họ tên:"));
        pnlPh1.add(txtPh1HoTen);
        pnlPh1.add(new JLabel("Mối quan hệ:"));
        pnlPh1.add(cboPh1MoiQuanHe);
        pnlPh1.add(new JLabel("SĐT:"));
        pnlPh1.add(txtPh1Sdt);
        pnlPh1.add(new JLabel("Email:"));
        pnlPh1.add(txtPh1Email);

        // ===== Phụ huynh 2 =====
        var pnlPh2 = new JPanel(new GridLayout(0, 2, 10, 10));
        pnlPh2.setBorder(BorderFactory.createTitledBorder("Phụ huynh 2"));

        txtPh2HoTen = new JTextField();
        txtPh2Sdt = new JTextField();
        txtPh2Email = new JTextField();
        cboPh2MoiQuanHe = new JComboBox<>(new String[] { "Cha", "Mẹ", "Anh/Chị", "Giám hộ", "Khác" });

        pnlPh2.add(new JLabel("Họ tên:"));
        pnlPh2.add(txtPh2HoTen);
        pnlPh2.add(new JLabel("Mối quan hệ:"));
        pnlPh2.add(cboPh2MoiQuanHe);
        pnlPh2.add(new JLabel("SĐT:"));
        pnlPh2.add(txtPh2Sdt);
        pnlPh2.add(new JLabel("Email:"));
        pnlPh2.add(txtPh2Email);

        // ===== Thêm tất cả vào panel chính =====
        mainPanel.add(pnlHS);
        mainPanel.add(Box.createVerticalStrut(12));
        mainPanel.add(pnlPh1);
        mainPanel.add(Box.createVerticalStrut(12));
        mainPanel.add(pnlPh2);

        add(new JScrollPane(mainPanel), BorderLayout.CENTER);

        // ===== Nút =====
        var btnPane = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        var btnCancel = new JButton("Hủy");
        var btnSave = new JButton("Thêm");
        btnCancel.addActionListener(e -> dispose());
        btnSave.addActionListener(e -> {
            String hoTen = txtHoTen.getText().trim();
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            String ngaySinh = sdf.format((java.util.Date) spNgaySinh.getValue());
            String gioiTinh = (String) cboGioiTinh.getSelectedItem();
            String tenLop = (String) cboLop.getSelectedItem();
            int maLop = mapTenLopToMaLop(tenLop);
            String sdt = txtSdt.getText().trim();
            String email = txtEmail.getText().trim();

            hocSinhBUS.saveHocSinh(hoTen, ngaySinh, gioiTinh, maLop, sdt, email);
            JOptionPane.showMessageDialog(this, "Đã lưu học sinh vào cơ sở dữ liệu (qua BUS)");
            dispose();
        });
        btnPane.add(btnCancel);
        btnPane.add(btnSave);
        add(btnPane, BorderLayout.SOUTH);
    }

    private int mapTenLopToMaLop(String tenLop) {
        if (tenLop == null)
            return 0;
        for (var l : lopBUS.getAllLop()) {
            if (tenLop.equals(l.getTenLop()))
                return l.getMaLop();
        }
        return 0; // fallback
    }
}
