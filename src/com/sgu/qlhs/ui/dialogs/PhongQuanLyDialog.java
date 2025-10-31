/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sgu.qlhs.ui.dialogs;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import com.sgu.qlhs.bus.PhongBUS;
import com.sgu.qlhs.dto.PhongDTO;

public class PhongQuanLyDialog extends JDialog {
    private final JTextField txtSearch = new JTextField();
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[] { "Mã phòng", "Tên phòng", "Loại", "Sức chứa", "Vị trí" }, 0);

    private final PhongBUS phongBUS = new PhongBUS();

    public PhongQuanLyDialog(Window owner) {
        super(owner, "Quản lý phòng học", ModalityType.APPLICATION_MODAL);
        setMinimumSize(new Dimension(780, 480));
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

        reloadTable(phongBUS, model);

        // ===== SỰ KIỆN THÊM (ĐÃ VIẾT LẠI) =====
        btnAdd.addActionListener(e -> {
            // Tạo form
            Object[] formElements = createPhongForm(null);
            JPanel formPanel = (JPanel) formElements[0];

            int result = JOptionPane.showConfirmDialog(this, formPanel, "Thêm Phòng Học",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (result == JOptionPane.OK_OPTION) {
                // Lấy giá trị từ form
                JTextField txtTen = (JTextField) formElements[1];
                JTextField txtLoai = (JTextField) formElements[2];
                JSpinner spnSucChua = (JSpinner) formElements[3];
                JTextField txtViTri = (JTextField) formElements[4];

                String ten = txtTen.getText().trim();
                String loai = txtLoai.getText().trim();
                int sucChua = (Integer) spnSucChua.getValue();
                String viTri = txtViTri.getText().trim();

                if (ten.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Tên phòng không được để trống!", "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                phongBUS.savePhong(ten, loai, sucChua, viTri);
                reloadTable(phongBUS, model);
            }
        });

        // ===== SỰ KIỆN SỬA (ĐÃ VIẾT LẠI) =====
        btnEdit.addActionListener(e -> {
            int r = tbl.getSelectedRow();
            if (r < 0) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn một phòng để sửa.");
                return;
            }
            int modelRow = tbl.convertRowIndexToModel(r);
            int ma = (int) model.getValueAt(modelRow, 0);

            PhongDTO p = phongBUS.getPhongByMa(ma);
            if (p == null) {
                JOptionPane.showMessageDialog(this, "Không tìm thấy phòng!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Tạo form và điền sẵn thông tin
            Object[] formElements = createPhongForm(p);
            JPanel formPanel = (JPanel) formElements[0];

            int result = JOptionPane.showConfirmDialog(this, formPanel, "Sửa Phòng Học",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (result == JOptionPane.OK_OPTION) {
                // Lấy giá trị từ form
                JTextField txtTen = (JTextField) formElements[1];
                JTextField txtLoai = (JTextField) formElements[2];
                JSpinner spnSucChua = (JSpinner) formElements[3];
                JTextField txtViTri = (JTextField) formElements[4];

                String ten = txtTen.getText().trim();
                String loai = txtLoai.getText().trim();
                int sucChua = (Integer) spnSucChua.getValue();
                String viTri = txtViTri.getText().trim();

                if (ten.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Tên phòng không được để trống!", "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                phongBUS.updatePhong(ma, ten, loai, sucChua, viTri);
                reloadTable(phongBUS, model);
            }
        });

        // ===== SỰ KIỆN XÓA (Giữ nguyên) =====
        btnDel.addActionListener(e -> {
            int r = tbl.getSelectedRow();
            if (r < 0) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn một phòng để xóa.");
                return;
            }
            int modelRow = tbl.convertRowIndexToModel(r);
            Object idObj = model.getValueAt(modelRow, 0);
            if (idObj == null)
                return;
            int ma = Integer.parseInt(idObj.toString());
            int conf = JOptionPane.showConfirmDialog(this, "Xóa phòng có mã " + ma + "?", "Xác nhận",
                    JOptionPane.YES_NO_OPTION);
            if (conf == JOptionPane.YES_OPTION) {
                phongBUS.deletePhong(ma);
                reloadTable(phongBUS, model);
            }
        });
    }

    /**
     * Tạo JPanel chứa form thêm/sửa phòng
     * 
     * @param phong DTO của phòng cần sửa (hoặc null nếu thêm mới)
     * @return Một Object[] chứa [JPanel, và các JTextField, JSpinner...]
     */
    private Object[] createPhongForm(PhongDTO phong) {
        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));

        // 1. Tên phòng
        JTextField txtTen = new JTextField(phong != null ? phong.getTenPhong() : "");
        panel.add(new JLabel("Tên phòng:"));
        panel.add(txtTen);

        // 2. Loại phòng
        JTextField txtLoai = new JTextField(phong != null ? phong.getLoaiPhong() : "Lý thuyết");
        panel.add(new JLabel("Loại phòng:"));
        panel.add(txtLoai);

        // 3. Sức chứa
        SpinnerModel sucChuaModel = new SpinnerNumberModel(phong != null ? phong.getSucChua() : 30, 0, 500, 1);
        JSpinner spnSucChua = new JSpinner(sucChuaModel);
        panel.add(new JLabel("Sức chứa:"));
        panel.add(spnSucChua);

        // 4. Vị trí
        JTextField txtViTri = new JTextField(phong != null ? phong.getViTri() : "");
        panel.add(new JLabel("Vị trí:"));
        panel.add(txtViTri);

        return new Object[] { panel, txtTen, txtLoai, spnSucChua, txtViTri };
    }

    private void reloadTable(PhongBUS phongBUS, DefaultTableModel model) {
        model.setRowCount(0);
        java.util.List<PhongDTO> list = phongBUS.getAllPhong();
        for (PhongDTO p : list) {
            model.addRow(
                    new Object[] { p.getMaPhong(), p.getTenPhong(), p.getLoaiPhong(), p.getSucChua(), p.getViTri() });
        }
    }
}