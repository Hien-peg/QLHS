package com.sgu.qlhs.ui.model;

import javax.swing.table.AbstractTableModel;
import java.util.*;

public class HocSinhTableModel extends AbstractTableModel {
    private final String[] cols = {"Mã", "Họ tên", "Ngày sinh", "Giới tính", "Lớp"};
    private final List<Object[]> data = new ArrayList<>();

    public HocSinhTableModel() {
        data.add(new Object[]{1, "Nguyễn Văn A", "2007-09-01", "Nam", "10A1"});
        data.add(new Object[]{2, "Trần Thị B", "2007-12-11", "Nữ", "10A1"});
        data.add(new Object[]{3, "Phạm Minh C", "2007-05-10", "Nam", "10A2"});
        data.add(new Object[]{4, "Lê Thu D", "2007-03-22", "Nữ", "11A1"});
        data.add(new Object[]{5, "Đỗ Hải E", "2006-10-09", "Khác", "12A1"});
    }

    @Override
    public int getRowCount() { return data.size(); }

    @Override
    public int getColumnCount() { return cols.length; }

    @Override
    public String getColumnName(int c) { return cols[c]; }

    @Override
    public Object getValueAt(int r, int c) { return data.get(r)[c]; }
}
