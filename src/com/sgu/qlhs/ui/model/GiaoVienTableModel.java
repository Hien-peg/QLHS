/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sgu.qlhs.ui.model;

/**
 *
 * @author minho
 */

import com.sgu.qlhs.ui.database.GiaoVienDAO;
import javax.swing.table.AbstractTableModel;
import java.util.*;

public class GiaoVienTableModel extends AbstractTableModel {
    private final String[] cols = { "Mã GV", "Họ tên", "SĐT", "Email" };
    private final List<Object[]> data;

    public GiaoVienTableModel() {
        GiaoVienDAO dao = new GiaoVienDAO();
        this.data = dao.getAllGiaoVien();
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
