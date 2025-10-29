package com.sgu.qlhs.ui.model;

import com.sgu.qlhs.bus.HocSinhBUS;
import com.sgu.qlhs.dto.HocSinhDTO;
import javax.swing.table.AbstractTableModel;
import java.util.List;

public class HocSinhTableModel extends AbstractTableModel {
    private final String[] cols = { "Mã", "Họ tên", "Ngày sinh", "Giới tính", "Lớp" };
    private final List<HocSinhDTO> data;

    public HocSinhTableModel() {
        HocSinhBUS bus = new HocSinhBUS();
        this.data = bus.getAllHocSinh();
    }

    @Override
    public int getRowCount() {
        return data == null ? 0 : data.size();
    }

    @Override
    public int getColumnCount() {
        return cols.length;
    }

    @Override
    public String getColumnName(int c) {
        return cols[c];
    }

    @Override
    public Object getValueAt(int r, int c) {
        HocSinhDTO hs = data.get(r);
        switch (c) {
            case 0:
                return hs.getMaHS();
            case 1:
                return hs.getHoTen();
            case 2:
                return hs.getNgaySinh();
            case 3:
                return hs.getGioiTinh();
            case 4:
                return hs.getTenLop();
            default:
                return null;
        }
    }
}
