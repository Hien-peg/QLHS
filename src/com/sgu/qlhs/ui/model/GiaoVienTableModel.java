package com.sgu.qlhs.ui.model;

import com.sgu.qlhs.bus.GiaoVienBUS;
import com.sgu.qlhs.dto.GiaoVienDTO;
import javax.swing.table.AbstractTableModel;
import java.util.List;

public class GiaoVienTableModel extends AbstractTableModel {
    private final String[] cols = { "Mã GV", "Họ tên", "SĐT", "Email" };
    private final List<GiaoVienDTO> data;

    public GiaoVienTableModel() {
        GiaoVienBUS bus = new GiaoVienBUS();
        this.data = bus.getAllGiaoVien();
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
        GiaoVienDTO g = data.get(r);
        switch (c) {
            case 0:
                return g.getMaGV();
            case 1:
                return g.getHoTen();
            case 2:
                return g.getSoDienThoai();
            case 3:
                return g.getEmail();
            default:
                return null;
        }
    }
}
