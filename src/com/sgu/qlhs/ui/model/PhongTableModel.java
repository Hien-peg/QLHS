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

public class PhongTableModel extends AbstractTableModel {
    private final String[] cols={"Mã phòng","Tên phòng","Loại","Sức chứa","Vị trí"};
    private final List<Object[]> data=new ArrayList<>();
    public PhongTableModel(){ data.add(new Object[]{101,"P101","Lý thuyết",45,"Tầng 1"}); data.add(new Object[]{202,"P202","Thí nghiệm",30,"Tầng 2"}); }
    @Override public int getRowCount(){return data.size();}
    @Override public int getColumnCount(){return cols.length;}
    @Override public String getColumnName(int c){return cols[c];}
    @Override public Object getValueAt(int r,int c){return data.get(r)[c];}
}
