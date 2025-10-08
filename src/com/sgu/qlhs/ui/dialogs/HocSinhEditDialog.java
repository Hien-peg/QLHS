package com.sgu.qlhs.ui.dialogs;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class HocSinhEditDialog extends JDialog {
    private JTextField txtMa, txtHoTen, txtSdt, txtEmail;
    private JComboBox<String> cboGioiTinh, cboLop;
    private JSpinner spNgaySinh;

    // Phụ huynh 1
    private JTextField txtPh1HoTen, txtPh1Sdt, txtPh1Email;
    private JComboBox<String> cboPh1MoiQuanHe;

    // Phụ huynh 2
    private JTextField txtPh2HoTen, txtPh2Sdt, txtPh2Email;
    private JComboBox<String> cboPh2MoiQuanHe;

    private JButton btnLoad;

    public HocSinhEditDialog(Window owner) {
        super(owner, "Sửa học sinh", ModalityType.APPLICATION_MODAL);
        setSize(600, 720);
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
        txtEmail = new JTextField();
        cboGioiTinh = new JComboBox<>(new String[]{"Nam", "Nữ", "Khác"});
        cboLop = new JComboBox<>(new String[]{"10A1", "10A2", "11A1", "12A1"});

        spNgaySinh = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor editor = new JSpinner.DateEditor(spNgaySinh, "dd/MM/yyyy");
        spNgaySinh.setEditor(editor);

        pnlHS.add(new JLabel("Mã HS:"));
        JPanel pnlMa = new JPanel(new BorderLayout(5, 0));
        pnlMa.add(txtMa, BorderLayout.CENTER);
        pnlMa.add(btnLoad, BorderLayout.EAST);
        pnlHS.add(pnlMa);

        pnlHS.add(new JLabel("Họ tên:")); pnlHS.add(txtHoTen);
        pnlHS.add(new JLabel("Ngày sinh:")); pnlHS.add(spNgaySinh);
        pnlHS.add(new JLabel("Giới tính:")); pnlHS.add(cboGioiTinh);
        pnlHS.add(new JLabel("Lớp:")); pnlHS.add(cboLop);
        pnlHS.add(new JLabel("SĐT:")); pnlHS.add(txtSdt);
        pnlHS.add(new JLabel("Email:")); pnlHS.add(txtEmail);

        // ===== Phụ huynh 1 =====
        var pnlPh1 = new JPanel(new GridLayout(0, 2, 10, 10));
        pnlPh1.setBorder(BorderFactory.createTitledBorder("Phụ huynh 1"));

        txtPh1HoTen = new JTextField();
        txtPh1Sdt = new JTextField();
        txtPh1Email = new JTextField();
        cboPh1MoiQuanHe = new JComboBox<>(new String[]{"Cha", "Mẹ", "Anh/Chị", "Giám hộ", "Khác"});

        pnlPh1.add(new JLabel("Họ tên:")); pnlPh1.add(txtPh1HoTen);
        pnlPh1.add(new JLabel("Mối quan hệ:")); pnlPh1.add(cboPh1MoiQuanHe);
        pnlPh1.add(new JLabel("SĐT:")); pnlPh1.add(txtPh1Sdt);
        pnlPh1.add(new JLabel("Email:")); pnlPh1.add(txtPh1Email);

        // ===== Phụ huynh 2 =====
        var pnlPh2 = new JPanel(new GridLayout(0, 2, 10, 10));
        pnlPh2.setBorder(BorderFactory.createTitledBorder("Phụ huynh 2"));

        txtPh2HoTen = new JTextField();
        txtPh2Sdt = new JTextField();
        txtPh2Email = new JTextField();
        cboPh2MoiQuanHe = new JComboBox<>(new String[]{"Cha", "Mẹ", "Anh/Chị", "Giám hộ", "Khác"});

        pnlPh2.add(new JLabel("Họ tên:")); pnlPh2.add(txtPh2HoTen);
        pnlPh2.add(new JLabel("Mối quan hệ:")); pnlPh2.add(cboPh2MoiQuanHe);
        pnlPh2.add(new JLabel("SĐT:")); pnlPh2.add(txtPh2Sdt);
        pnlPh2.add(new JLabel("Email:")); pnlPh2.add(txtPh2Email);

        mainPanel.add(pnlHS);
        mainPanel.add(Box.createVerticalStrut(12));
        mainPanel.add(pnlPh1);
        mainPanel.add(Box.createVerticalStrut(12));
        mainPanel.add(pnlPh2);

        add(new JScrollPane(mainPanel), BorderLayout.CENTER);

        // ===== Nút =====
        var btnPane = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        var btnCancel = new JButton("Hủy");
        var btnSave = new JButton("Lưu thay đổi");

        btnCancel.addActionListener(e -> dispose());
        btnSave.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Cập nhật thông tin học sinh thành công!");
            dispose();
        });

        btnPane.add(btnCancel);
        btnPane.add(btnSave);
        add(btnPane, BorderLayout.SOUTH);

        // ===== Sự kiện tải thông tin =====
        btnLoad.addActionListener(e -> {
            String maHS = txtMa.getText().trim();
            if (maHS.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập mã học sinh!");
                return;
            }

            Map<String, Object> data = getHocSinhData(maHS);
            if (data == null) {
                JOptionPane.showMessageDialog(this, "Không tìm thấy học sinh có mã: " + maHS);
                return;
            }

            txtHoTen.setText((String) data.get("hoTen"));
            txtSdt.setText((String) data.get("sdt"));
            txtEmail.setText((String) data.get("email"));
            cboGioiTinh.setSelectedItem(data.get("gioiTinh"));
            cboLop.setSelectedItem(data.get("lop"));
            spNgaySinh.setValue(data.get("ngaySinh"));

            // Phụ huynh 1
            txtPh1HoTen.setText((String) data.get("ph1HoTen"));
            txtPh1Sdt.setText((String) data.get("ph1Sdt"));
            txtPh1Email.setText((String) data.get("ph1Email"));
            cboPh1MoiQuanHe.setSelectedItem(data.get("ph1MoiQuanHe"));

            // Phụ huynh 2
            txtPh2HoTen.setText((String) data.get("ph2HoTen"));
            txtPh2Sdt.setText((String) data.get("ph2Sdt"));
            txtPh2Email.setText((String) data.get("ph2Email"));
            cboPh2MoiQuanHe.setSelectedItem(data.get("ph2MoiQuanHe"));
        });
    }

    // ======= Fake data (giả lập từ DB) =======
    private Map<String, Object> getHocSinhData(String maHS) {
        Map<String, Map<String, Object>> fakeDB = new HashMap<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        try {
            // HS001
            Map<String, Object> hs1 = new HashMap<>();
            hs1.put("hoTen", "Nguyễn Văn A");
            hs1.put("ngaySinh", sdf.parse("15/03/2007"));
            hs1.put("gioiTinh", "Nam");
            hs1.put("lop", "10A1");
            hs1.put("sdt", "0123456789");
            hs1.put("email", "nguyenvana@example.com");
            hs1.put("ph1HoTen", "Nguyễn Văn B");
            hs1.put("ph1MoiQuanHe", "Cha");
            hs1.put("ph1Sdt", "0987654321");
            hs1.put("ph1Email", "ph1@example.com");
            hs1.put("ph2HoTen", "Trần Thị C");
            hs1.put("ph2MoiQuanHe", "Mẹ");
            hs1.put("ph2Sdt", "0978123456");
            hs1.put("ph2Email", "ph2@example.com");
            fakeDB.put("HS001", hs1);

            // HS002
            Map<String, Object> hs2 = new HashMap<>();
            hs2.put("hoTen", "Lê Thị D");
            hs2.put("ngaySinh", sdf.parse("02/06/2008"));
            hs2.put("gioiTinh", "Nữ");
            hs2.put("lop", "10A2");
            hs2.put("sdt", "0912345678");
            hs2.put("email", "led@example.com");
            hs2.put("ph1HoTen", "Lê Văn E");
            hs2.put("ph1MoiQuanHe", "Cha");
            hs2.put("ph1Sdt", "0909988776");
            hs2.put("ph1Email", "lecha@example.com");
            hs2.put("ph2HoTen", "Nguyễn Thị F");
            hs2.put("ph2MoiQuanHe", "Mẹ");
            hs2.put("ph2Sdt", "0933221100");
            hs2.put("ph2Email", "leme@example.com");
            fakeDB.put("HS002", hs2);

            return fakeDB.get(maHS);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // ===== Main test nhanh =====
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new HocSinhEditDialog(null).setVisible(true));
    }
}
