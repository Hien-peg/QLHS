/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sgu.qlhs.ui.model;

/**
 *
 * @author minho
 */
import com.sgu.qlhs.bus.PhongBUS;
import com.sgu.qlhs.dto.PhongDTO;
import javax.swing.table.AbstractTableModel;
import java.util.*;

public class PhongTableModel extends AbstractTableModel {
    private final String[] cols = { "Mã phòng", "Tên phòng", "Loại", "Sức chứa", "Vị trí" };
    private final List<PhongDTO> data;

    public PhongTableModel() {
        PhongBUS bus = new PhongBUS();
        this.data = bus.getAllPhong();
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
        PhongDTO p = data.get(r);
        switch (c) {
            case 0:
                return p.getMaPhong();
            case 1:
                return p.getTenPhong();
            case 2:
                return p.getLoaiPhong();
            case 3:
                return p.getSucChua();
            case 4:
                return p.getViTri();
            default:
                return null;
        }
    }

}
