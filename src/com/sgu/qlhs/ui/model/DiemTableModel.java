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

public class DiemTableModel extends AbstractTableModel {
    private final String[] cols={"Mã HS","Họ tên","Môn","HK","Miệng","15p","Giữa kỳ","Cuối kỳ"};
    private final List<Object[]> data=new ArrayList<>();
    public DiemTableModel(){
        data.add(new Object[]{1,"Nguyễn Văn A","Toán",1,8.5,7.0,8.0,8.5});
        data.add(new Object[]{2,"Trần Thị B","Văn",1,7.5,8.0,8.0,8.5});
        data.add(new Object[]{3,"Phạm Minh C","Anh",2,8.0,8.0,8.0,9.0});
    }
    @Override public int getRowCount(){return data.size();}
    @Override public int getColumnCount(){return cols.length;}
    @Override public String getColumnName(int c){return cols[c];}
    @Override public Object getValueAt(int r,int c){return data.get(r)[c];}
}
