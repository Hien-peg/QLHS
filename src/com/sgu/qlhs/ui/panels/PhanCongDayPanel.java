package com.sgu.qlhs.ui.panels;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.stream.Collectors;
import com.sgu.qlhs.bus.*;
import com.sgu.qlhs.dto.*;
import com.sgu.qlhs.ui.dialogs.PhanCongDayDialog;

public class PhanCongDayPanel extends JPanel {
    private JTable tblPhanCong;
    private DefaultTableModel model;
    private JButton btnThem, btnSua, btnXoa, btnLamMoi;
    private JTextField txtTimGV;
    private JComboBox<String> cboHocKy;
    private PhanCongDayBUS pcdBUS;

    public PhanCongDayPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        pcdBUS = new PhanCongDayBUS();
        initUI();
        loadData(null, null);
    }

    private void initUI() {
        // ===== Bộ lọc tìm kiếm =====
        JPanel pnlFilter = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        pnlFilter.add(new JLabel("Tìm giáo viên:"));
        txtTimGV = new JTextField(20);
        pnlFilter.add(txtTimGV);

        pnlFilter.add(new JLabel("Học kỳ:"));
        cboHocKy = new JComboBox<>(new String[]{"Tất cả", "HK1", "HK2"});
        pnlFilter.add(cboHocKy);
        add(pnlFilter, BorderLayout.NORTH);

        // ===== Bảng danh sách =====
        String[] columns = {"Mã PC", "Giáo viên", "Môn học", "Lớp", "Phòng", "Học kỳ", "Năm học"};
        model = new DefaultTableModel(columns, 0);
        tblPhanCong = new JTable(model);
        tblPhanCong.setRowHeight(26);
        add(new JScrollPane(tblPhanCong), BorderLayout.CENTER);

        // ===== Nút chức năng =====
        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        btnThem = new JButton("Thêm");
        btnSua = new JButton("Sửa");
        btnXoa = new JButton("Xóa");
        btnLamMoi = new JButton("Làm mới");
        pnlButtons.add(btnThem);
        pnlButtons.add(btnSua);
        pnlButtons.add(btnXoa);
        pnlButtons.add(btnLamMoi);
        add(pnlButtons, BorderLayout.SOUTH);

        // ===== Sự kiện =====
        txtTimGV.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                filterData();
            }
        });

        cboHocKy.addActionListener(e -> filterData());
        btnLamMoi.addActionListener(e -> {
            txtTimGV.setText("");
            cboHocKy.setSelectedIndex(0);
            loadData(null, null);
        });

        btnThem.addActionListener(e -> moDialogThem());
        btnSua.addActionListener(e -> moDialogSua());
        btnXoa.addActionListener(e -> xoaPhanCong());
    }

    // ========== Hiển thị dữ liệu ==========
    private void loadData(String keyword, String hocKy) {
        model.setRowCount(0);
        List<PhanCongDayDTO> list = pcdBUS.getAll();

        // Lọc theo tên GV
        if (keyword != null && !keyword.isEmpty()) {
            list = list.stream()
                    .filter(p -> p.getTenGV() != null && p.getTenGV().toLowerCase().contains(keyword.toLowerCase()))
                    .collect(Collectors.toList());
        }

        // Lọc theo học kỳ
        if (hocKy != null && !hocKy.equals("Tất cả")) {
            list = list.stream()
                    .filter(p -> p.getHocKy() != null && p.getHocKy().equalsIgnoreCase(hocKy))
                    .collect(Collectors.toList());
        }

        for (PhanCongDayDTO p : list) {
            model.addRow(new Object[]{
                    p.getMaPCD(),
                    p.getTenGV(),
                    p.getTenMon(),
                    p.getTenLop(),
                    p.getTenPhong(),
                    p.getHocKy(),
                    p.getNamHoc()
            });
        }
    }

    // ========== Lọc theo tìm kiếm và học kỳ ==========
    private void filterData() {
        String keyword = txtTimGV.getText().trim();
        String hocKy = cboHocKy.getSelectedItem().toString();
        loadData(keyword.isEmpty() ? null : keyword, hocKy);
    }

    // ========== Thêm phân công ==========
    private void moDialogThem() {
        PhanCongDayDialog dialog = new PhanCongDayDialog(null, pcdBUS);
        dialog.setVisible(true);
        filterData(); // refresh sau khi thêm
    }

    // ========== Sửa phân công ==========
    private void moDialogSua() {
        int row = tblPhanCong.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn dòng cần sửa");
            return;
        }

        int maPCD = (int) model.getValueAt(row, 0);
        PhanCongDayDTO dto = pcdBUS.findById(maPCD);

        if (dto == null) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy phân công dạy này!");
            return;
        }

        PhanCongDayDialog dialog = new PhanCongDayDialog(dto, pcdBUS);
        dialog.setVisible(true);
        filterData();
    }

    // ========== Xóa phân công ==========
    private void xoaPhanCong() {
        int row = tblPhanCong.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn dòng cần xóa");
            return;
        }

        int maPCD = (int) model.getValueAt(row, 0);
        if (JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn xóa phân công này?\n(Các tiết trong TKB liên quan sẽ bị xóa theo)",
                "Xác nhận", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {

            if (pcdBUS.delete(maPCD)) {
                JOptionPane.showMessageDialog(this, "Đã xóa thành công!");
                filterData();
            } else {
                JOptionPane.showMessageDialog(this, "Xóa thất bại!");
            }
        }
    }
}
