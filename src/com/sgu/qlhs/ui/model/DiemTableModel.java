/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sgu.qlhs.ui.model;

/**
 *
 * @author minho
 */

import com.sgu.qlhs.ui.database.DiemDAO;
import javax.swing.table.AbstractTableModel;
import java.util.*;

public class DiemTableModel extends AbstractTableModel {
    private final String[] cols = { "Mã HS", "Họ tên", "Môn", "HK", "Miệng", "15p", "Giữa kỳ", "Cuối kỳ" };
    private final List<Object[]> data;

    public DiemTableModel() {
        DiemDAO dao = new DiemDAO();
        this.data = dao.getAllDiem();
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
