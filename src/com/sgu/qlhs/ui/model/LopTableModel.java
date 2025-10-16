package com.sgu.qlhs.ui.model;

import com.sgu.qlhs.ui.database.HocSinhDAO;
import com.sgu.qlhs.ui.database.LopDAO;
import javax.swing.table.AbstractTableModel;
import java.util.*;

public class LopTableModel extends AbstractTableModel {
    private final String[] cols = { "Mã lớp", "Tên lớp", "Khối", "Phòng" };
    private final List<Object[]> data;

    // Tạo một instance của HocSinhDAO để sử dụng
    private final HocSinhDAO hocSinhDAO = new HocSinhDAO();

    public LopTableModel() {
        LopDAO lopDAO = new LopDAO();
        this.data = lopDAO.getAllLop();
    }

    @Override
    public int getRowCount() {
        return data.size();
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
        return data.get(r)[c];
    }

    // Khôi phục và sửa đổi phương thức getMaLop()
    public int getMaLop(int row) {
        return (int) data.get(row)[0];
    }

    // Khôi phục và sửa đổi phương thức getTenLop()
    public String getTenLop(int row) {
        return (String) data.get(row)[1];
    }

    // Khôi phục và sửa đổi phương thức getHocSinhByLop()
    public List<Object[]> getHocSinhByLop(int maLop) {
        return hocSinhDAO.getHocSinhByMaLop(maLop);
    }
}
