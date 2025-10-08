package com.sgu.qlhs.ui.dialogs;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class HocSinhDeleteDialog extends JDialog {
    private JTextField txtMaHS;
    private JLabel lblHoTen, lblLop, lblKetQua;
    private JButton btnTim, btnDelete, btnCancel;

    // Giả lập "database" (bạn thay bằng truy vấn thật sau)
    private Map<String, String[]> fakeData = new HashMap<>();

    public HocSinhDeleteDialog(Window owner) {
        super(owner, "Xóa học sinh", ModalityType.APPLICATION_MODAL);
        setSize(420, 300);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        // ===== Dữ liệu mẫu =====
        fakeData.put("HS001", new String[]{"Nguyễn Văn A", "10A1"});
        fakeData.put("HS002", new String[]{"Trần Thị B", "11A2"});
        fakeData.put("HS003", new String[]{"Lê Văn C", "12A1"});

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
        btnDelete.setEnabled(false); // chỉ bật sau khi tìm thấy

        pnlButtons.add(btnCancel);
        pnlButtons.add(btnDelete);
        add(pnlButtons, BorderLayout.SOUTH);

        // ===== Sự kiện =====
        btnCancel.addActionListener(e -> dispose());

        btnTim.addActionListener(e -> {
            String ma = txtMaHS.getText().trim();
            if (ma.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập mã học sinh!");
                return;
            }

            if (fakeData.containsKey(ma)) {
                String[] info = fakeData.get(ma);
                lblHoTen.setText(info[0]);
                lblLop.setText(info[1]);
                lblKetQua.setText("Đã tìm thấy học sinh!");
                lblKetQua.setForeground(new Color(0, 128, 0));
                btnDelete.setEnabled(true);
            } else {
                lblHoTen.setText("-");
                lblLop.setText("-");
                lblKetQua.setText("Không tìm thấy học sinh có mã " + ma);
                lblKetQua.setForeground(Color.RED);
                btnDelete.setEnabled(false);
            }
        });

        btnDelete.addActionListener(e -> {
            String ma = txtMaHS.getText().trim();
            String ten = lblHoTen.getText();
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Bạn có chắc chắn muốn xóa học sinh:\n" + ten + " (" + ma + ")?",
                    "Xác nhận xóa", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                fakeData.remove(ma);
                JOptionPane.showMessageDialog(this, "Đã xóa học sinh " + ten + " (" + ma + ") thành công!");
                dispose();
            }
        });
    }

    // Để test riêng
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new HocSinhDeleteDialog(null).setVisible(true));
    }
}
