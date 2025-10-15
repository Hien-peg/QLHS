package com.sgu.qlhs.ui.dialogs;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class DiemTrungBinhTatCaMonDialog extends JDialog {
    private final JComboBox<String> cboLop = new JComboBox<>(new String[]{"10A1","10A2","11A1","12A1"});
    private final JComboBox<String> cboHK  = new JComboBox<>(new String[]{"HK1","HK2","Cả năm"});
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"Mã HS","Họ tên","Toán","Văn","Anh","Lý","Hóa","Sinh","Trung bình","Xếp loại"}, 0) {
        @Override public boolean isCellEditable(int r,int c){ return c>=2 && c<=7; } // cho phép sửa điểm
        @Override public Class<?> getColumnClass(int c){ return (c>=2 && c<=8)? Double.class:String.class; }
    };

    public DiemTrungBinhTatCaMonDialog(Window owner){
        super(owner, "Tính điểm trung bình tất cả các môn", ModalityType.APPLICATION_MODAL);
        setMinimumSize(new Dimension(1000, 550));
        setLocationRelativeTo(owner);
        build(); pack();
    }

    private void build(){
        var root = new JPanel(new BorderLayout(12,12));
        root.setBorder(new EmptyBorder(16,16,16,16));
        setContentPane(root);

        // ===== Thanh lọc trên cùng =====
        var bar = new JPanel(new FlowLayout(FlowLayout.LEFT,10,0));
        bar.add(new JLabel("Lớp:")); bar.add(cboLop);
        bar.add(new JLabel("Học kỳ:")); bar.add(cboHK);
        var btnCalc = new JButton("Tính điểm TB tất cả môn");
        bar.add(btnCalc);
        root.add(bar, BorderLayout.NORTH);

        // ===== Bảng điểm =====
        var tbl = new JTable(model);
        tbl.setRowHeight(26);
        root.add(new JScrollPane(tbl), BorderLayout.CENTER);

        // === Dữ liệu mẫu ===
        model.addRow(new Object[]{"HS01","Nguyễn Văn A",8.0,7.5,8.2,7.8,8.0,7.9,null,null});
        model.addRow(new Object[]{"HS02","Trần Thị B",9.0,8.5,8.0,8.5,9.0,8.8,null,null});
        model.addRow(new Object[]{"HS03","Lê Văn C",6.8,7.2,6.5,7.0,6.8,7.1,null,null});

        // ===== Nút tính toán =====
        btnCalc.addActionListener(e -> {
            for(int r=0; r<model.getRowCount(); r++){
                double tong = 0; int dem = 0;
                for(int c=2; c<=7; c++){
                    double d = val(model.getValueAt(r,c));
                    tong += d; dem++;
                }
                double tb = dem>0 ? round1(tong/dem) : 0;
                model.setValueAt(tb, r, 8);
                model.setValueAt(xepLoai(tb), r, 9);
            }
        });

        // ===== Nút lưu + đóng =====
        var btnPane = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        var btnClose = new JButton("Đóng");
        var btnSave  = new JButton("Lưu kết quả");
        btnSave.addActionListener(e -> { /* TODO: Ghi dữ liệu ra DB sau */ dispose(); });
        btnClose.addActionListener(e -> dispose());
        btnPane.add(btnClose); btnPane.add(btnSave);
        root.add(btnPane, BorderLayout.SOUTH);
    }

    // ===== Hàm hỗ trợ =====
    private static double val(Object o){ return o==null? 0.0 : ((Number)o).doubleValue(); }
    private static double round1(double x){ return Math.round(x*10.0)/10.0; }
    private static String xepLoai(double tb){
        if(tb>=8.0) return "Giỏi";
        if(tb>=6.5) return "Khá";
        if(tb>=5.0) return "Trung bình";
        return "Yếu";
    }
}
