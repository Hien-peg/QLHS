package com.sgu.qlhs.ui.dialogs;

import javax.swing.*;
import java.awt.*;
import java.util.List;

import com.sgu.qlhs.bus.ThoiKhoaBieuBUS;
import com.sgu.qlhs.bus.LopBUS;
import com.sgu.qlhs.bus.MonBUS;
import com.sgu.qlhs.bus.GiaoVienBUS;
import com.sgu.qlhs.bus.PhongBUS;

import com.sgu.qlhs.dto.ThoiKhoaBieuDTO;
import com.sgu.qlhs.dto.LopDTO;
import com.sgu.qlhs.dto.MonHocDTO;
import com.sgu.qlhs.dto.GiaoVienDTO;
import com.sgu.qlhs.dto.PhongDTO;

public class TKBDialog extends JDialog {

    private JComboBox<ComboItem> cboLop, cboMon, cboGV, cboPhong;
    private JComboBox<String> cboThu, cboHocKy;
    private JSpinner spTietBD, spTietKT;
    private JButton btnLuu, btnHuy;

    private final ThoiKhoaBieuBUS tkbBUS;
    private final LopBUS lopBUS;
    private final MonBUS monBUS;
    private final GiaoVienBUS gvBUS;
    private final PhongBUS phongBUS;

    // ===== NEW: hỗ trợ SỬA =====
    private Integer editingId = null; // null = thêm mới, != null = sửa

    // Constructor THÊM (giữ nguyên cho code cũ)
    public TKBDialog(Frame parent,
                     ThoiKhoaBieuBUS tkbBUS,
                     LopBUS lopBUS,
                     MonBUS monBUS,
                     GiaoVienBUS gvBUS,
                     PhongBUS phongBUS) {
        this(parent, tkbBUS, lopBUS, monBUS, gvBUS, phongBUS, null);
    }

    // Constructor THÊM / SỬA (editing != null => Sửa)
    public TKBDialog(Frame parent,
                     ThoiKhoaBieuBUS tkbBUS,
                     LopBUS lopBUS,
                     MonBUS monBUS,
                     GiaoVienBUS gvBUS,
                     PhongBUS phongBUS,
                     ThoiKhoaBieuDTO editing) {
        super(parent, editing == null ? "Thêm Thời khóa biểu" : "Sửa Thời khóa biểu", true);
        this.tkbBUS = tkbBUS;
        this.lopBUS = lopBUS;
        this.monBUS = monBUS;
        this.gvBUS = gvBUS;
        this.phongBUS = phongBUS;

        setSize(500, 500);
        setLocationRelativeTo(parent);
        setLayout(new GridBagLayout());

        cboLop   = new JComboBox<>();
        cboMon   = new JComboBox<>();
        cboGV    = new JComboBox<>();
        cboPhong = new JComboBox<>();
        cboThu   = new JComboBox<>(new String[]{"Thứ 2","Thứ 3","Thứ 4","Thứ 5","Thứ 6","Thứ 7"});
        cboHocKy = new JComboBox<>(new String[]{"HK1","HK2"});

        spTietBD = new JSpinner(new SpinnerNumberModel(1, 1, 10, 1));
        spTietKT = new JSpinner(new SpinnerNumberModel(1, 1, 10, 1));

        btnLuu = new JButton("Lưu");
        btnHuy = new JButton("Hủy");

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 10, 6, 10);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;

        int r = 0;
        addRow(gc, r++, "Lớp:", cboLop);
        addRow(gc, r++, "Môn học:", cboMon);
        addRow(gc, r++, "Giáo viên:", cboGV);
        addRow(gc, r++, "Phòng học:", cboPhong);
        addRow(gc, r++, "Thứ:", cboThu);
        addRow(gc, r++, "Tiết bắt đầu:", spTietBD);
        addRow(gc, r++, "Tiết kết thúc:", spTietKT);
        addRow(gc, r++, "Học kỳ:", cboHocKy);

        gc.gridx = 0; gc.gridy = r; gc.gridwidth = 1;
        add(btnLuu, gc);
        gc.gridx = 1; gc.gridy = r;
        add(btnHuy, gc);

        btnHuy.addActionListener(e -> dispose());
        btnLuu.addActionListener(e -> onSave());

        loadComboData(); // nạp dữ liệu combobox

        // ===== NEW: nếu là SỬA, đổ form & lưu editingId
        if (editing != null) {
            editingId = editing.getMaTKB();
            fillForm(editing);
        }
    }

    private void addRow(GridBagConstraints gc, int row, String label, JComponent comp) {
        gc.gridx = 0; gc.gridy = row; gc.gridwidth = 1;
        add(new JLabel(label), gc);
        gc.gridx = 1; gc.gridy = row; gc.gridwidth = 1;
        add(comp, gc);
    }

    private void loadComboData() {
        try {
            // Lớp
            cboLop.removeAllItems();
            List<LopDTO> dsLop = lopBUS.getAllLop(); // đổi tên nếu hàm khác
            for (LopDTO x : dsLop) {
                cboLop.addItem(new ComboItem(x.getMaLop(), x.getTenLop()));
            }

            // Môn
            cboMon.removeAllItems();
            List<MonHocDTO> dsMon = monBUS.getAllMon();
            for (MonHocDTO x : dsMon) {
                cboMon.addItem(new ComboItem(x.getMaMon(), x.getTenMon()));
            }

            // Giáo viên
            cboGV.removeAllItems();
            List<GiaoVienDTO> dsGV = gvBUS.getAllGiaoVien();
            for (GiaoVienDTO x : dsGV) {
                String ten = x.getHoTen() != null ? x.getHoTen() : ("GV#" + x.getMaGV());
                cboGV.addItem(new ComboItem(x.getMaGV(), ten));
            }

            // Phòng
            cboPhong.removeAllItems();
            List<PhongDTO> dsPhong = phongBUS.getAllPhong();
            for (PhongDTO x : dsPhong) {
                String label = (x.getTenPhong() != null && !x.getTenPhong().isEmpty())
                        ? x.getTenPhong()
                        : ("P" + x.getMaPhong());
                cboPhong.addItem(new ComboItem(x.getMaPhong(), label));
            }

            if (cboLop.getItemCount() > 0) cboLop.setSelectedIndex(0);
            if (cboMon.getItemCount() > 0) cboMon.setSelectedIndex(0);
            if (cboGV.getItemCount() > 0) cboGV.setSelectedIndex(0);
            if (cboPhong.getItemCount() > 0) cboPhong.setSelectedIndex(0);

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Không thể tải dữ liệu từ DB.\n" + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ===== NEW: đổ dữ liệu khi SỬA
    private void fillForm(ThoiKhoaBieuDTO t) {
        selectById(cboLop,   t.getMaLop());
        selectById(cboMon,   t.getMaMon());
        selectById(cboGV,    t.getMaGV());
        selectById(cboPhong, t.getMaPhong());
        cboThu.setSelectedItem(t.getThuTrongTuan());
        cboHocKy.setSelectedItem(t.getHocKy());
        spTietBD.setValue(t.getTietBatDau());
        spTietKT.setValue(t.getTietKetThuc());
        // NamHoc: hiện đang đặt cố định trong onSave = "2024-2025"
        // Nếu muốn cho chọn, ta thêm ComboBox năm học tương tự HocKy.
    }

    private void selectById(JComboBox<ComboItem> combo, int id) {
        for (int i = 0; i < combo.getItemCount(); i++) {
            ComboItem it = combo.getItemAt(i);
            if (it.id.equals(id)) { combo.setSelectedIndex(i); return; }
        }
    }

    private void onSave() {
        try {
            Integer maLop   = getSelectedId(cboLop);
            Integer maMon   = getSelectedId(cboMon);
            Integer maGV    = getSelectedId(cboGV);
            Integer maPhong = getSelectedId(cboPhong);

            if (maLop == null || maMon == null || maGV == null || maPhong == null) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn đủ Lớp/Môn/Giáo viên/Phòng.");
                return;
            }

            int tietBD = (int) spTietBD.getValue();
            int tietKT = (int) spTietKT.getValue();
            if (tietBD > tietKT) {
                JOptionPane.showMessageDialog(this, "Tiết bắt đầu không được lớn hơn tiết kết thúc.");
                return;
            }

            ThoiKhoaBieuDTO tkb = new ThoiKhoaBieuDTO();
            if (editingId != null) tkb.setMaTKB(editingId); // quan trọng khi SỬA
            tkb.setMaLop(maLop);
            tkb.setMaMon(maMon);
            tkb.setMaGV(maGV);
            tkb.setMaPhong(maPhong);
            tkb.setThuTrongTuan((String) cboThu.getSelectedItem());
            tkb.setTietBatDau(tietBD);
            tkb.setTietKetThuc(tietKT);
            tkb.setHocKy((String) cboHocKy.getSelectedItem());
            tkb.setNamHoc("2024-2025"); // TODO: nếu cần, thêm combobox Năm học
            tkb.setTrangThai(1);

            String result = (editingId == null)
                    ? tkbBUS.addTKB(tkb)
                    : tkbBUS.updateTKB(tkb);

            JOptionPane.showMessageDialog(this, result);
            if (result != null && result.startsWith("✅")) dispose();

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Có lỗi khi lưu TKB.\n" + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Integer getSelectedId(JComboBox<ComboItem> combo) {
        ComboItem item = (ComboItem) combo.getSelectedItem();
        return item != null ? item.id : null;
    }

    private static class ComboItem {
        final Integer id;
        final String label;
        ComboItem(Integer id, String label) { this.id = id; this.label = label; }
        @Override public String toString() { return label; }
    }
}