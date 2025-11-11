package com.sgu.qlhs.ui.dialogs;

import javax.swing.*;
import java.awt.*;
import com.sgu.qlhs.bus.HocSinhBUS;
import com.sgu.qlhs.dto.HocSinhDTO;

public class HocSinhDeleteDialog extends JDialog {
    private JTextField txtMaHS;
    private JLabel lblHoTen, lblLop, lblKetQua;
    private JButton btnTim, btnDelete, btnCancel;

    //private HocSinhDAO hocSinhDAO = new HocSinhDAO();
    private HocSinhBUS hocSinhBUS = new HocSinhBUS();

    private boolean deleteSuccessful = false;

    public HocSinhDeleteDialog(Window owner) {
        super(owner, "Xóa học sinh", ModalityType.APPLICATION_MODAL);
        setSize(420, 300);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        buildForm();
    }

    public boolean isDeleteSuccessful() {
        return this.deleteSuccessful;
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
        btnTim.addActionListener(e -> timHocSinh());
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
            
            HocSinhDTO hs = hocSinhBUS.getHocSinhByMaHS(maHS);

            if (hs != null) {
                lblHoTen.setText(hs.getHoTen());
                lblLop.setText(hs.getTenLop());
                lblKetQua.setText("Đã tìm thấy học sinh!");
                lblKetQua.setForeground(new Color(0, 128, 0));
                btnDelete.setEnabled(true);
            } else {
                lblHoTen.setText("-");
                lblLop.setText("-");
                // Thông báo rõ là không tìm thấy hoặc đã bị xóa
                lblKetQua.setText("Không tìm thấy học sinh (hoặc học sinh đã bị xóa)");
                lblKetQua.setForeground(Color.RED);
                btnDelete.setEnabled(false);
            }
            
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Mã học sinh phải là số!");
        } catch (Exception e) {
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
                "Bạn có chắc chắn muốn xóa học sinh:\n" + ten + " (Mã: " + maText + ")?\n(Phụ huynh liên quan cũng sẽ bị ẩn đi)",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            int maHS = Integer.parseInt(maText);
            boolean success = hocSinhBUS.deleteHocSinh(maHS);
            if (success) {
                JOptionPane.showMessageDialog(this, "✅ Đã xóa học sinh thành công!");
                
                this.deleteSuccessful = true;
                
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "❌ Xóa thất bại! Học sinh có thể không tồn tại.");
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new HocSinhDeleteDialog(null).setVisible(true));
    }
}