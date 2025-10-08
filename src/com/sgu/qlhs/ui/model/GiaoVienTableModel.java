/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sgu.qlhs.ui.model;

/**
 *
 * @author minho
 */
import javax.swing.table.AbstractTableModel;
import java.util.*;

public class GiaoVienTableModel extends AbstractTableModel {
    private final String[] cols={"Mã GV","Họ tên","SĐT","Email"};
    private final List<Object[]> data=new ArrayList<>();
    public GiaoVienTableModel(){
        data.add(new Object[]{101,"Phạm Văn C","0901111222","c@gv.example.com"});
        data.add(new Object[]{102,"Lê Thị D","0902222333","d@gv.example.com"});
        data.add(new Object[]{103,"Nguyễn Hữu E","0903333444","e@gv.example.com"});
    }
    @Override public int getRowCount(){return data.size();}
    @Override public int getColumnCount(){return cols.length;}
    @Override public String getColumnName(int c){return cols[c];}
    @Override public Object getValueAt(int r,int c){return data.get(r)[c];}
}

