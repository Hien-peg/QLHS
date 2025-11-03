package com.sgu.qlhs.ui.dialogs;

import javax.swing.*;
import java.awt.*;
import com.sgu.qlhs.bus.*;
import com.sgu.qlhs.dto.*;
import com.sgu.qlhs.ui.components.ComboItem;

public class PhanCongDayDialog extends JDialog {
    private JComboBox<ComboItem> cboGV, cboMon, cboLop, cboPhong;
    private JComboBox<String> cboHocKy, cboNamHoc;
    private JButton btnLuu, btnHuy;
    private PhanCongDayBUS pcdBUS;
    private PhanCongDayDTO current;

    public PhanCongDayDialog(PhanCongDayDTO dto, PhanCongDayBUS bus) {
        this.pcdBUS = bus;
        this.current = dto;

        setTitle(dto == null ? "Thêm phân công dạy" : "Sửa phân công dạy");
        setModal(true);
        setSize(450, 320);
        setLocationRelativeTo(null);
        initUI();
        loadComboData();
        if (dto != null) fillData(dto);
    }

    private void initUI() {
        setLayout(new BorderLayout(10,10));
        JPanel pnl = new JPanel(new GridLayout(6,2,10,10));
        pnl.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        pnl.add(new JLabel("Giáo viên:"));
        cboGV = new JComboBox<>(); pnl.add(cboGV);

        pnl.add(new JLabel("Môn học:"));
        cboMon = new JComboBox<>(); pnl.add(cboMon);

        pnl.add(new JLabel("Lớp học:"));
        cboLop = new JComboBox<>(); pnl.add(cboLop);

        pnl.add(new JLabel("Phòng học:"));
        cboPhong = new JComboBox<>(); pnl.add(cboPhong);

        pnl.add(new JLabel("Học kỳ:"));
        cboHocKy = new JComboBox<>(new String[]{"HK1", "HK2"}); pnl.add(cboHocKy);

        pnl.add(new JLabel("Năm học:"));
        cboNamHoc = new JComboBox<>(new String[]{"2024-2025"}); pnl.add(cboNamHoc);

        add(pnl, BorderLayout.CENTER);

        JPanel pnlBtn = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnLuu = new JButton("Lưu");
        btnHuy = new JButton("Hủy");
        pnlBtn.add(btnLuu);
        pnlBtn.add(btnHuy);
        add(pnlBtn, BorderLayout.SOUTH);

        btnHuy.addActionListener(e -> dispose());
        btnLuu.addActionListener(e -> save());
    }

    // ===== Load dữ liệu vào các ComboBox =====
    private void loadComboData() {
        cboGV.removeAllItems();
        GiaoVienBUS gvBUS = new GiaoVienBUS();
        for (GiaoVienDTO gv : gvBUS.getAllGiaoVien())
            cboGV.addItem(new ComboItem(gv.getMaGV(), gv.getHoTen()));

        cboMon.removeAllItems();
        MonBUS monBUS = new MonBUS();
        for (MonHocDTO m : monBUS.getAllMon())
            cboMon.addItem(new ComboItem(m.getMaMon(), m.getTenMon()));

        cboLop.removeAllItems();
        LopBUS lopBUS = new LopBUS();
        for (LopDTO l : lopBUS.getAllLop())
            cboLop.addItem(new ComboItem(l.getMaLop(), l.getTenLop()));

        cboPhong.removeAllItems();
        PhongBUS phongBUS = new PhongBUS();
        for (PhongDTO p : phongBUS.getAllPhong())
            cboPhong.addItem(new ComboItem(p.getMaPhong(), p.getTenPhong()));
    }

    // ===== Điền dữ liệu khi mở dialog "Sửa" =====
    private void fillData(PhanCongDayDTO dto) {
        selectComboItemByValue(cboGV, dto.getMaGV());
        selectComboItemByValue(cboMon, dto.getMaMon());
        selectComboItemByValue(cboLop, dto.getMaLop());
        selectComboItemByValue(cboPhong, dto.getMaPhong());
        cboHocKy.setSelectedItem(dto.getHocKy());
        cboNamHoc.setSelectedItem(dto.getNamHoc());
    }

    private void selectComboItemByValue(JComboBox<ComboItem> combo, int value) {
        for (int i = 0; i < combo.getItemCount(); i++) {
            ComboItem item = combo.getItemAt(i);
            if (item.getValue() == value) {
                combo.setSelectedIndex(i);
                break;
            }
        }
    }

    // ===== Xử lý lưu (thêm hoặc sửa) =====
    private void save() {
        ComboItem gv = (ComboItem) cboGV.getSelectedItem();
        ComboItem mon = (ComboItem) cboMon.getSelectedItem();
        ComboItem lop = (ComboItem) cboLop.getSelectedItem();
        ComboItem phong = (ComboItem) cboPhong.getSelectedItem();
        String hk = (String) cboHocKy.getSelectedItem();
        String nh = (String) cboNamHoc.getSelectedItem();

        if (gv == null || mon == null || lop == null || phong == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn đầy đủ thông tin!");
            return;
        }

        PhanCongDayDTO dto = new PhanCongDayDTO();
        dto.setMaGV(gv.getValue());
        dto.setMaMon(mon.getValue());
        dto.setMaLop(lop.getValue());
        dto.setMaPhong(phong.getValue());
        dto.setHocKy(hk);
        dto.setNamHoc(nh);

        boolean ok;
        if (current == null) {
            ok = pcdBUS.insert(dto);
        } else {
            dto.setMaPCD(current.getMaPCD());
            ok = pcdBUS.update(dto);
        }

        if (ok) {
            JOptionPane.showMessageDialog(this, "Lưu thành công!");
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Phân công bị trùng hoặc dữ liệu không hợp lệ!");
        }
    }
}
