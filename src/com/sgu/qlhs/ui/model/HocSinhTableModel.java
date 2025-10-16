package com.sgu.qlhs.ui.model;

import com.sgu.qlhs.ui.database.HocSinhDAO;
import javax.swing.table.AbstractTableModel;
import java.util.*;

public class HocSinhTableModel extends AbstractTableModel {
    private final String[] cols = { "Mã", "Họ tên", "Ngày sinh", "Giới tính", "Lớp" };
    private final List<Object[]> data;

    public HocSinhTableModel() {
        HocSinhDAO dao = new HocSinhDAO();
        this.data = dao.getAllHocSinh();
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
}
