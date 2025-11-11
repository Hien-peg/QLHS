package com.sgu.qlhs.ui.dialogs;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Date;
import java.util.ArrayList;
import com.sgu.qlhs.bus.LopBUS;
import com.sgu.qlhs.bus.HocSinhBUS;
// import com.sgu.qlhs.database.HocSinhDAO; 

public class HocSinhAddDialog extends JDialog {
    private JTextField txtHoTen, txtSdt, txtEmail, txtDiaChi;
    private JComboBox<String> cboGioiTinh, cboLop;
    private JSpinner spNgaySinh;
    private JTextField txtPH1HoTen, txtPH1MQH, txtPH1Sdt, txtPH1Email;
    private JTextField txtPH2HoTen, txtPH2MQH, txtPH2Sdt, txtPH2Email;

    // private HocSinhDAO hocSinhDAO = new HocSinhDAO();
    private HocSinhBUS hocSinhBUS = new HocSinhBUS(); 
    private LopBUS lopBUS = new LopBUS();
    private java.util.List<Integer> lopIds = new ArrayList<>();

    private boolean addSuccessful = false;

    public HocSinhAddDialog(Window owner) {
        super(owner, "Thêm học sinh", ModalityType.APPLICATION_MODAL);
        setSize(700, 600);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(12, 12));
        buildForm();
    }

    public boolean isAddSuccessful() {
        return this.addSuccessful;
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
        cboGioiTinh = new JComboBox<>(new String[] { "Nam", "Nữ" });
        cboLop = new JComboBox<>();

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

        // ===== Thông tin phụ huynh 1 =====
        var pnlPH1 = new JPanel(new GridLayout(0, 2, 10, 10));
        pnlPH1.setBorder(BorderFactory.createTitledBorder("Phụ huynh 1"));

        txtPH1HoTen = new JTextField();
        txtPH1MQH = new JTextField();
        txtPH1Sdt = new JTextField();
        txtPH1Email = new JTextField();

        pnlPH1.add(new JLabel("Họ tên:"));
        pnlPH1.add(txtPH1HoTen);
        pnlPH1.add(new JLabel("Mối quan hệ:"));
        pnlPH1.add(txtPH1MQH);
        pnlPH1.add(new JLabel("SĐT:"));
        pnlPH1.add(txtPH1Sdt);
        pnlPH1.add(new JLabel("Email:"));
        pnlPH1.add(txtPH1Email);

        // ===== Thông tin phụ huynh 2 =====
        var pnlPH2 = new JPanel(new GridLayout(0, 2, 10, 10));
        pnlPH2.setBorder(BorderFactory.createTitledBorder("Phụ huynh 2"));

        txtPH2HoTen = new JTextField();
        txtPH2MQH = new JTextField();
        txtPH2Sdt = new JTextField();
        txtPH2Email = new JTextField();

        pnlPH2.add(new JLabel("Họ tên:"));
        pnlPH2.add(txtPH2HoTen);
        pnlPH2.add(new JLabel("Mối quan hệ:"));
        pnlPH2.add(txtPH2MQH);
        pnlPH2.add(new JLabel("SĐT:"));
        pnlPH2.add(txtPH2Sdt);
        pnlPH2.add(new JLabel("Email:"));
        pnlPH2.add(txtPH2Email);

        mainPanel.add(pnlHS);   
        mainPanel.add(pnlPH1);
        mainPanel.add(pnlPH2);

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

    private void themHocSinh() {
        String hoTen = txtHoTen.getText().trim();
        String soDienThoai = txtSdt.getText().trim();
        String email = txtEmail.getText().trim();
        String diaChi = txtDiaChi.getText().trim();
        String gioiTinh = cboGioiTinh.getSelectedItem().toString();
        String tenLop = cboLop.getSelectedItem().toString();
        Date ngaySinh = (Date) spNgaySinh.getValue();

        // Phụ huynh
        String ph1HoTen = txtPH1HoTen.getText().trim();
        String ph1MQH = txtPH1MQH.getText().trim();
        String ph1Sdt = txtPH1Sdt.getText().trim();
        String ph1Email = txtPH1Email.getText().trim();

        String ph2HoTen = txtPH2HoTen.getText().trim();
        String ph2MQH = txtPH2MQH.getText().trim();
        String ph2Sdt = txtPH2Sdt.getText().trim();
        String ph2Email = txtPH2Email.getText().trim();

        if (hoTen.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập họ tên học sinh!");
            return;
        }

        try {
            int maLop = getMaLopByTenLop(tenLop);

            boolean success = hocSinhBUS.saveHocSinh(
                    hoTen, ngaySinh, gioiTinh, diaChi, soDienThoai, email, maLop,
                    ph1HoTen, ph1MQH, ph1Sdt, ph1Email,
                    ph2HoTen, ph2MQH, ph2Sdt, ph2Email
            );

            if (success) {
                JOptionPane.showMessageDialog(this, "Đã thêm học sinh mới thành công!");
                
                this.addSuccessful = true;
                
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi khi thêm học sinh!");
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new HocSinhAddDialog(null).setVisible(true));
    }
}