/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sgu.qlhs.ui.dialogs;

/**
 *
 * @author minho
 */
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import com.sgu.qlhs.bus.LopBUS;
import com.sgu.qlhs.bus.PhongBUS;
import com.sgu.qlhs.dto.LopDTO;
import com.sgu.qlhs.dto.PhongDTO;

import java.util.List;
import java.util.Vector;

public class LopQuanLyDialog extends JDialog {
    private final JTextField txtSearch = new JTextField();
    private final DefaultTableModel model = new DefaultTableModel(new Object[] { "Mã lớp", "Tên lớp", "Khối", "Phòng" },
            0);

    // Thêm BUS
    private final LopBUS lopBUS = new LopBUS();
    private final PhongBUS phongBUS = new PhongBUS();

    public LopQuanLyDialog(Window owner) {
        super(owner, "Quản lý lớp", ModalityType.APPLICATION_MODAL);
        setMinimumSize(new Dimension(720, 480));
        setLocationRelativeTo(owner);
        build();
        pack();
    }

    private void build() {
        var root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(new EmptyBorder(16, 16, 16, 16));
        setContentPane(root);

        var bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        txtSearch.setColumns(18);
        txtSearch.setBorder(BorderFactory.createTitledBorder("Tìm kiếm"));
        JButton btnAdd = new JButton("Thêm"), btnEdit = new JButton("Sửa"), btnDel = new JButton("Xóa");
        bar.add(txtSearch);
        bar.add(btnAdd);
        bar.add(btnEdit);
        bar.add(btnDel);
        root.add(bar, BorderLayout.NORTH);

        var tbl = new JTable(model);
        tbl.setAutoCreateRowSorter(true);
        root.add(new JScrollPane(tbl), BorderLayout.CENTER);

        reloadTable(lopBUS, model);

        // ===== SỰ KIỆN THÊM (ĐÃ VIẾT LẠI) =====
        btnAdd.addActionListener(e -> {
            // Lấy danh sách phòng mới nhất
            List<PhongDTO> phongList = phongBUS.getAllPhong();
            if (phongList.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Cần phải thêm phòng học trước!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Tạo form
            Object[] formElements = createLopForm(null, phongList);
            JPanel formPanel = (JPanel) formElements[0];

            int result = JOptionPane.showConfirmDialog(this, formPanel, "Thêm Lớp",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (result == JOptionPane.OK_OPTION) {
                // Lấy giá trị từ form
                JTextField txtTenLop = (JTextField) formElements[1];
                JSpinner spnKhoi = (JSpinner) formElements[2];
                JComboBox<PhongDTO> cboPhong = (JComboBox<PhongDTO>) formElements[3];

                String ten = txtTenLop.getText().trim();
                int khoi = (Integer) spnKhoi.getValue();
                PhongDTO selectedPhong = (PhongDTO) cboPhong.getSelectedItem();
                int maPhong = (selectedPhong != null) ? selectedPhong.getMaPhong() : 0;

                if (ten.isEmpty() || maPhong == 0) {
                    JOptionPane.showMessageDialog(this, "Tên lớp và phòng không được để trống!", "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                lopBUS.saveLop(ten, khoi, maPhong);
                reloadTable(lopBUS, model);
            }
        });

        // ===== SỰ KIỆN SỬA (ĐÃ VIẾT LẠI) =====
        btnEdit.addActionListener(e -> {
            int r = tbl.getSelectedRow();
            if (r < 0) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn một lớp để sửa.");
                return;
            }
            int modelRow = tbl.convertRowIndexToModel(r);
            int maLop = (int) model.getValueAt(modelRow, 0);

            LopDTO l = lopBUS.getLopByMa(maLop);
            if (l == null) {
                JOptionPane.showMessageDialog(this, "Không tìm thấy lớp!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Lấy danh sách phòng
            List<PhongDTO> phongList = phongBUS.getAllPhong();

            // Tạo form và điền sẵn thông tin
            Object[] formElements = createLopForm(l, phongList);
            JPanel formPanel = (JPanel) formElements[0];

            int result = JOptionPane.showConfirmDialog(this, formPanel, "Sửa Lớp",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (result == JOptionPane.OK_OPTION) {
                // Lấy giá trị từ form
                JTextField txtTenLop = (JTextField) formElements[1];
                JSpinner spnKhoi = (JSpinner) formElements[2];
                JComboBox<PhongDTO> cboPhong = (JComboBox<PhongDTO>) formElements[3];

                String ten = txtTenLop.getText().trim();
                int khoi = (Integer) spnKhoi.getValue();
                PhongDTO selectedPhong = (PhongDTO) cboPhong.getSelectedItem();
                int maPhong = (selectedPhong != null) ? selectedPhong.getMaPhong() : 0;

                if (ten.isEmpty() || maPhong == 0) {
                    JOptionPane.showMessageDialog(this, "Tên lớp và phòng không được để trống!", "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                lopBUS.updateLop(maLop, ten, khoi, maPhong);
                reloadTable(lopBUS, model);
            }
        });

        // ===== SỰ KIỆN XÓA (Giữ nguyên) =====
        btnDel.addActionListener(e -> {
            int r = tbl.getSelectedRow();
            if (r < 0) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn một lớp để xóa.");
                return;
            }
            int modelRow = tbl.convertRowIndexToModel(r);
            Object idObj = model.getValueAt(modelRow, 0);
            if (idObj == null)
                return;
            int maLop = Integer.parseInt(idObj.toString());
            int conf = JOptionPane.showConfirmDialog(this, "Xóa lớp có mã " + maLop + "?", "Xác nhận",
                    JOptionPane.YES_NO_OPTION);
            if (conf == JOptionPane.YES_OPTION) {
                lopBUS.deleteLop(maLop);
                reloadTable(lopBUS, model);
            }
        });
    }

    /**
     * Tạo JPanel chứa form thêm/sửa lớp
     * 
     * @param lop       DTO của lớp cần sửa (hoặc null nếu thêm mới)
     * @param phongList Danh sách phòng để hiển thị trong JComboBox
     * @return Một Object[] chứa [JPanel, JTextField, JSpinner, JComboBox]
     */
    private Object[] createLopForm(LopDTO lop, List<PhongDTO> phongList) {
        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));

        // 1. Tên Lớp
        JTextField txtTenLop = new JTextField(lop != null ? lop.getTenLop() : "");
        panel.add(new JLabel("Tên lớp:"));
        panel.add(txtTenLop);

        // 2. Khối
        // Spinner cho phép chọn từ 1 đến 12, giá trị mặc định là 10
        SpinnerModel khoiModel = new SpinnerNumberModel(lop != null ? lop.getKhoi() : 10, 1, 12, 1);
        JSpinner spnKhoi = new JSpinner(khoiModel);
        panel.add(new JLabel("Khối:"));
        panel.add(spnKhoi);

        // 3. Phòng
        // Chuyển List<PhongDTO> thành Vector để JComboBox chấp nhận
        Vector<PhongDTO> phongVector = new Vector<>(phongList);
        JComboBox<PhongDTO> cboPhong = new JComboBox<>(phongVector);

        if (lop != null) { // Nếu là Sửa, tìm và chọn đúng phòng
            for (PhongDTO p : phongList) {
                if (p.getMaPhong() == lop.getMaPhong()) {
                    cboPhong.setSelectedItem(p);
                    break;
                }
            }
        }

        panel.add(new JLabel("Phòng học:"));
        panel.add(cboPhong);

        // Trả về mảng các đối tượng để có thể truy cập sau này
        return new Object[] { panel, txtTenLop, spnKhoi, cboPhong };
    }

    private void reloadTable(LopBUS lopBUS, DefaultTableModel model) {
        model.setRowCount(0);
        java.util.List<LopDTO> list = lopBUS.getAllLop();
        for (LopDTO l : list) {
            model.addRow(new Object[] { l.getMaLop(), l.getTenLop(), l.getKhoi(), l.getTenPhong() });
        }
    }
}