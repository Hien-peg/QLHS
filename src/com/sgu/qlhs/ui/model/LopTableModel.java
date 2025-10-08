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

public class LopTableModel extends AbstractTableModel {
    private final String[] cols={"Mã lớp","Tên lớp","Khối","Phòng"};
    private final List<Object[]> data=new ArrayList<>();
    public LopTableModel(){ data.add(new Object[]{1,"10A1",10,"P101"}); data.add(new Object[]{2,"10A2",10,"P102"}); data.add(new Object[]{3,"11A1",11,"P201"}); data.add(new Object[]{4,"12A1",12,"P301"}); }
    @Override public int getRowCount(){return data.size();}
    @Override public int getColumnCount(){return cols.length;}
    @Override public String getColumnName(int c){return cols[c];}
    @Override public Object getValueAt(int r,int c){return data.get(r)[c];}
}
