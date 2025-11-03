package com.sgu.qlhs.ui.dialogs;

import javax.swing.*;
import java.awt.*;
import java.sql.Date;
import java.util.List;
import com.sgu.qlhs.bus.*;
import com.sgu.qlhs.dto.*;

public class ChuNhiemDialog extends JDialog {
    private JComboBox<ComboItem> cboGV, cboLop;
    private JSpinner spNgayNhan, spNgayKT;
    private JButton btnLuu, btnHuy;
    private final ChuNhiemBUS cnBUS;
    private final GiaoVienBUS gvBUS;
    private final LopBUS lopBUS;
    private ChuNhiemDTO editing;

    public ChuNhiemDialog(Frame parent, ChuNhiemBUS bus, ChuNhiemDTO editing) {
        super(parent, editing == null ? "Thêm giáo viên chủ nhiệm" : "Sửa giáo viên chủ nhiệm", true);
        this.cnBUS = bus;
        this.gvBUS = new GiaoVienBUS();
        this.lopBUS = new LopBUS();
        this.editing = editing;

        setSize(460, 300);
        setLocationRelativeTo(parent);
        setLayout(new GridBagLayout());

        cboGV = new JComboBox<>();
        cboLop = new JComboBox<>();
        spNgayNhan = new JSpinner(new SpinnerDateModel());
        spNgayKT = new JSpinner(new SpinnerDateModel());
        spNgayNhan.setEditor(new JSpinner.DateEditor(spNgayNhan, "yyyy-MM-dd"));
        spNgayKT.setEditor(new JSpinner.DateEditor(spNgayKT, "yyyy-MM-dd"));

        btnLuu = new JButton("Lưu");
        btnHuy = new JButton("Hủy");

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(8, 10, 8, 10);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;

        int row = 0;
        addRow(gc, row++, "Giáo viên:", cboGV);
        addRow(gc, row++, "Lớp:", cboLop);
        addRow(gc, row++, "Ngày nhận:", spNgayNhan);
        addRow(gc, row++, "Ngày kết thúc:", spNgayKT);
        gc.gridx = 0; gc.gridy = row; add(btnLuu, gc);
        gc.gridx = 1; add(btnHuy, gc);

        loadCombos();

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

    private void loadCombos() {
        for (GiaoVienDTO gv : gvBUS.getAllGiaoVien()) {
            cboGV.addItem(new ComboItem(gv.getMaGV(), gv.getHoTen()));
        }
        for (LopDTO l : lopBUS.getAllLop()) {
            cboLop.addItem(new ComboItem(l.getMaLop(), l.getTenLop()));
        }
    }

    private void fillForm(ChuNhiemDTO dto) {
        selectByValue(cboGV, dto.getMaGV());
        selectByValue(cboLop, dto.getMaLop());
        spNgayNhan.setValue(dto.getNgayNhanNhiem());
        spNgayKT.setValue(dto.getNgayKetThuc());
    }

    private void selectByValue(JComboBox<ComboItem> cbo, int value) {
        for (int i = 0; i < cbo.getItemCount(); i++) {
            if (cbo.getItemAt(i).value == value) {
                cbo.setSelectedIndex(i);
                break;
            }
        }
    }

    private void onSave() {
        ComboItem gv = (ComboItem) cboGV.getSelectedItem();
        ComboItem lop = (ComboItem) cboLop.getSelectedItem();
        if (gv == null || lop == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn đủ thông tin!");
            return;
        }

        Date ngayNhan = new Date(((java.util.Date) spNgayNhan.getValue()).getTime());
        Date ngayKT = new Date(((java.util.Date) spNgayKT.getValue()).getTime());

        ChuNhiemDTO dto = new ChuNhiemDTO();
        if (editing != null) dto.setMaCN(editing.getMaCN());
        dto.setMaGV(gv.value);
        dto.setMaLop(lop.value);
        dto.setNgayNhanNhiem(ngayNhan);
        dto.setNgayKetThuc(ngayKT);

        String msg = (editing == null) ? cnBUS.insert(dto) : cnBUS.update(dto);
        JOptionPane.showMessageDialog(this, msg);
        if (msg.startsWith("✅")) dispose();
    }

    private static class ComboItem {
        int value; String label;
        ComboItem(int v, String l) { value = v; label = l; }
        @Override public String toString() { return label; }
    }
}
