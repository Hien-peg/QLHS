package com.sgu.qlhs.ui.dialogs;

import javax.swing.*;
import java.awt.*;
import java.util.List;

import com.sgu.qlhs.bus.ThoiKhoaBieuBUS;
import com.sgu.qlhs.bus.PhanCongDayBUS;
import com.sgu.qlhs.dto.PhanCongDayDTO;
import com.sgu.qlhs.dto.ThoiKhoaBieuDTO;

public class TKBDialog extends JDialog {

    private JComboBox<ComboItem> cboPhanCong;
    private JComboBox<String> cboThu;
    private JSpinner spTietBD, spTietKT;
    private JButton btnLuu, btnHuy;

    private final ThoiKhoaBieuBUS tkbBUS;
    private final PhanCongDayBUS pcdBUS;
    private ThoiKhoaBieuDTO editing;

    public TKBDialog(Frame parent, ThoiKhoaBieuDTO editing, ThoiKhoaBieuBUS bus) {
        super(parent, editing == null ? "Thêm thời khóa biểu" : "Sửa thời khóa biểu", true);
        this.tkbBUS = bus;
        this.pcdBUS = new PhanCongDayBUS();
        this.editing = editing;

        setSize(480, 360);
        setLocationRelativeTo(parent);
        setLayout(new GridBagLayout());

        cboPhanCong = new JComboBox<>();
        cboThu = new JComboBox<>(new String[]{"Thứ 2","Thứ 3","Thứ 4","Thứ 5","Thứ 6","Thứ 7"});
        spTietBD = new JSpinner(new SpinnerNumberModel(1, 1, 10, 1));
        spTietKT = new JSpinner(new SpinnerNumberModel(1, 1, 10, 1));
        btnLuu = new JButton("Lưu");
        btnHuy = new JButton("Hủy");

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(8, 10, 8, 10);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;

        int row = 0;
        addRow(gc, row++, "Phân công dạy:", cboPhanCong);
        addRow(gc, row++, "Thứ trong tuần:", cboThu);
        addRow(gc, row++, "Tiết bắt đầu:", spTietBD);
        addRow(gc, row++, "Tiết kết thúc:", spTietKT);

        gc.gridx = 0; gc.gridy = row; add(btnLuu, gc);
        gc.gridx = 1; gc.gridy = row; add(btnHuy, gc);

        loadPhanCong();

        btnHuy.addActionListener(e -> dispose());
        btnLuu.addActionListener(e -> onSave());

        if (editing != null) fillForm(editing);
    }

    private void addRow(GridBagConstraints gc, int row, String label, JComponent comp) {
        gc.gridx = 0; gc.gridy = row;
        add(new JLabel(label), gc);
        gc.gridx = 1;
        add(comp, gc);
    }

    // ====== Nạp danh sách phân công dạy ======
    private void loadPhanCong() {
        try {
            cboPhanCong.removeAllItems();
            List<PhanCongDayDTO> ds = pcdBUS.getAll();
            for (PhanCongDayDTO p : ds) {
                String text = String.format("%s - %s - %s (%s)",
                        p.getTenGV(), p.getTenMon(), p.getTenLop(), p.getTenPhong());
                cboPhanCong.addItem(new ComboItem(p.getMaPCD(), text, p.getMaGV(), p.getMaLop(), p.getMaPhong(), p.getHocKy(), p.getNamHoc()));
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi khi tải danh sách phân công dạy:\n" + ex.getMessage());
        }
    }

    private void fillForm(ThoiKhoaBieuDTO t) {
        selectComboByValue(cboPhanCong, t.getMaPCD());
        cboThu.setSelectedItem(t.getThu());
        spTietBD.setValue(t.getTietBD());
        spTietKT.setValue(t.getTietKT());
    }

    private void selectComboByValue(JComboBox<ComboItem> combo, int value) {
        for (int i = 0; i < combo.getItemCount(); i++) {
            if (combo.getItemAt(i).value == value) {
                combo.setSelectedIndex(i);
                break;
            }
        }
    }

    // ====== Lưu thời khóa biểu ======
    private void onSave() {
        ComboItem pc = (ComboItem) cboPhanCong.getSelectedItem();
        String thu = (String) cboThu.getSelectedItem();
        int tietBD = (int) spTietBD.getValue();
        int tietKT = (int) spTietKT.getValue();

        if (pc == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn phân công dạy!");
            return;
        }
        if (tietBD > tietKT) {
            JOptionPane.showMessageDialog(this, "Tiết bắt đầu không được lớn hơn tiết kết thúc!");
            return;
        }

        ThoiKhoaBieuDTO dto = new ThoiKhoaBieuDTO();
        if (editing != null) dto.setMaTKB(editing.getMaTKB());
        dto.setMaPCD(pc.value);
        dto.setThu(thu);
        dto.setTietBD(tietBD);
        dto.setTietKT(tietKT);
        dto.setTrangThai(1);

        // Thông tin liên kết (từ phân công dạy)
        dto.setMaGV(pc.maGV);
        dto.setMaLop(pc.maLop);
        dto.setMaPhong(pc.maPhong);
        dto.setHocKy(pc.hocKy);
        dto.setNamHoc(pc.namHoc);

        // Gọi logic kiểm tra trùng
        String result = (editing == null)
                ? tkbBUS.addTKB(dto)
                : tkbBUS.updateTKB(dto);

        if (result.startsWith("✅")) {
            JOptionPane.showMessageDialog(this, result);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, result);
        }
    }

    // ====== Inner class ComboItem ======
    private static class ComboItem {
        int value;
        String label;
        int maGV, maLop, maPhong;
        String hocKy, namHoc;

        ComboItem(int value, String label, int maGV, int maLop, int maPhong, String hocKy, String namHoc) {
            this.value = value;
            this.label = label;
            this.maGV = maGV;
            this.maLop = maLop;
            this.maPhong = maPhong;
            this.hocKy = hocKy;
            this.namHoc = namHoc;
        }

        @Override
        public String toString() {
            return label;
        }
    }
}
