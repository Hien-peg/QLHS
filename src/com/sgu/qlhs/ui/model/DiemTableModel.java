/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sgu.qlhs.ui.model;

import com.sgu.qlhs.bus.DiemBUS;
import com.sgu.qlhs.dto.DiemDTO;
import javax.swing.table.AbstractTableModel;
import java.util.List;

public class DiemTableModel extends AbstractTableModel {
    private final String[] cols = { "Mã HS", "Họ tên", "Môn", "HK", "Miệng", "15p", "Giữa kỳ", "Cuối kỳ" };
    private final List<DiemDTO> data;

    public DiemTableModel() {
        DiemBUS bus = new DiemBUS();
        this.data = bus.getAllDiem();
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
        DiemDTO d = data.get(r);
        switch (c) {
            case 0:
                return d.getMaHS();
            case 1:
                return d.getHoTen();
            case 2:
                return d.getTenMon();
            case 3:
                return d.getHocKy();
            case 4:
                return d.getDiemMieng();
            case 5:
                return d.getDiem15p();
            case 6:
                return d.getDiemGiuaKy();
            case 7:
                return d.getDiemCuoiKy();
            default:
                return null;
        }
    }
}
