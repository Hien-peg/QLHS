package com.sgu.qlhs.ui.model;

import javax.swing.table.AbstractTableModel;
import java.util.*;

public class LopTableModel extends AbstractTableModel {
    private final String[] cols = {"Mã lớp", "Tên lớp", "Khối", "Phòng"};
    private final List<Object[]> data = new ArrayList<>();

    // Dữ liệu mẫu cho các lớp
    public LopTableModel() {
        data.add(new Object[]{1, "10A1", 10, "P101"});
        data.add(new Object[]{2, "10A2", 10, "P102"});
        data.add(new Object[]{3, "11A1", 11, "P201"});
        data.add(new Object[]{4, "12A1", 12, "P301"});
    }

    @Override public int getRowCount() { return data.size(); }
    @Override public int getColumnCount() { return cols.length; }
    @Override public String getColumnName(int c) { return cols[c]; }
    @Override public Object getValueAt(int r, int c) { return data.get(r)[c]; }

    // ✅ Lấy thông tin cơ bản của 1 lớp
    public int getMaLop(int row) { return (int) data.get(row)[0]; }
    public String getTenLop(int row) { return (String) data.get(row)[1]; }

    // ✅ Giả lập danh sách học sinh của từng lớp
    // Có thể thay bằng dữ liệu lấy từ DB sau này
    public List<Object[]> getHocSinhByLop(int maLop) {
        List<Object[]> list = new ArrayList<>();
        switch (maLop) {
            case 1 -> {
                list.add(new Object[]{"HS101", "Nguyễn Văn A", "Nam", "2007-03-12"});
                list.add(new Object[]{"HS102", "Trần Thị B", "Nữ", "2007-05-22"});
            }
            case 2 -> {
                list.add(new Object[]{"HS201", "Lê Văn C", "Nam", "2007-11-10"});
                list.add(new Object[]{"HS202", "Phạm Thị D", "Nữ", "2007-09-14"});
            }
            case 3 -> {
                list.add(new Object[]{"HS301", "Đặng Văn E", "Nam", "2006-10-02"});
            }
            case 4 -> {
                list.add(new Object[]{"HS401", "Trần Thị F", "Nữ", "2005-08-09"});
            }
        }
        return list;
    }
}
