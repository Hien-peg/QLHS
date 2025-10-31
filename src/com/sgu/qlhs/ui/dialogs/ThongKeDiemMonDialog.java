/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sgu.qlhs.ui.dialogs;

/**
 *
 * @author minho
 */
import com.sgu.qlhs.ui.components.BarChartCanvas;
import com.sgu.qlhs.bus.LopBUS;
import com.sgu.qlhs.bus.DiemBUS;
import com.sgu.qlhs.dto.LopDTO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ThongKeDiemMonDialog extends JDialog {
    private final JComboBox<String> cboKhoi = new JComboBox<>(new String[] { "10", "11", "12" });
    private final JComboBox<String> cboHK = new JComboBox<>(new String[] { "HK1", "HK2" });
    private LopBUS lopBUS;
    private DiemBUS diemBUS;
    private com.sgu.qlhs.bus.MonBUS monBUS;
    private BarChartCanvas chart;
    private java.util.List<LopDTO> lops = new java.util.ArrayList<>();

    public ThongKeDiemMonDialog(Window owner) {
        super(owner, "Thống kê điểm TB theo môn", ModalityType.APPLICATION_MODAL);
        setMinimumSize(new Dimension(720, 460));
        setLocationRelativeTo(owner);

        var root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(new EmptyBorder(16, 16, 16, 16));
        setContentPane(root);

        var top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        top.add(new JLabel("Khối:"));
        top.add(cboKhoi);
        top.add(new JLabel("Học kỳ:"));
        top.add(cboHK);
        var btnLoad = new JButton("Tải");
        top.add(btnLoad);
        root.add(top, BorderLayout.NORTH);

        // init buses
        lopBUS = new LopBUS();
        diemBUS = new DiemBUS();
        monBUS = new com.sgu.qlhs.bus.MonBUS();

        // build initial chart from DB-backed subject list (fallback to defaults)
        java.util.List<com.sgu.qlhs.dto.MonHocDTO> monList = monBUS.getAllMon(); // <--- Lấy tất cả môn
        String[] mons = monList.isEmpty() ? new String[] { "Toán", "Văn", "Anh", "Lý", "Hóa", "Sinh" }
                : monList.stream().map(m -> m.getTenMon()).toArray(String[]::new);
        int[] tbs = new int[mons.length];
        for (int i = 0; i < tbs.length; i++)
            tbs[i] = 0;
        chart = new BarChartCanvas("Điểm TB theo môn (x10)", mons, tbs);
        root.add(chart, BorderLayout.CENTER);

        btnLoad.addActionListener((java.awt.event.ActionEvent __) -> {
            if (__ == null) {
            }
            int khoi = Integer.parseInt((String) cboKhoi.getSelectedItem());
            int hocKy = cboHK.getSelectedIndex() + 1;
            int maNK = com.sgu.qlhs.bus.NienKhoaBUS.current();
            // aggregate per subject
            java.util.Map<String, double[]> agg = new java.util.HashMap<>(); // tenMon -> {sum, cnt}
            lops = lopBUS.getAllLop();
            for (LopDTO l : lops) {
                if (l.getKhoi() != khoi)
                    continue;
                int maLop = l.getMaLop();

                // ================== SỬA LỖI ==================
                // 1. Dùng lại getDiemByLopHocKy để lấy các điểm thành phần
                java.util.List<com.sgu.qlhs.dto.DiemDTO> list = diemBUS.getDiemByLopHocKy(maLop, hocKy, maNK);

                for (com.sgu.qlhs.dto.DiemDTO d : list) {
                    String mon = d.getTenMon();

                    // 2. Tự tính toán TB vì d.getDiemTB() trả về 0
                    double tb = Math.round((d.getDiemMieng() * 0.1 + d.getDiem15p() * 0.2 + d.getDiemGiuaKy() * 0.3
                            + d.getDiemCuoiKy() * 0.4) * 10.0) / 10.0;
                    // ==========================================

                    double[] a = agg.getOrDefault(mon, new double[2]);
                    a[0] += tb;
                    a[1]++;
                    agg.put(mon, a);
                }
            }
            // determine subjects dynamically from DB (fallback to defaults)
            java.util.List<com.sgu.qlhs.dto.MonHocDTO> monList2 = monBUS.getAllMon(); // <--- Lấy tất cả môn
            String[] monsList = monList2.isEmpty() ? new String[] { "Toán", "Văn", "Anh", "Lý", "Hóa", "Sinh" }
                    : monList2.stream().map(m -> m.getTenMon()).toArray(String[]::new);
            int[] tbs2 = new int[monsList.length];
            for (int i = 0; i < monsList.length; i++) {
                double[] a = agg.get(monsList[i]);
                double avg = (a == null || a[1] == 0) ? 0.0 : (a[0] / a[1]);
                tbs2[i] = (int) Math.round(avg * 10);
            }
            // replace chart
            root.remove(chart);
            chart = new BarChartCanvas("Điểm TB theo môn (x10)", monsList, tbs2);
            root.add(chart, BorderLayout.CENTER);
            root.revalidate();
            root.repaint();
        });

        var south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        var close = new JButton("Đóng");
        close.addActionListener((java.awt.event.ActionEvent __) -> {
            if (__ == null) {
            }
            dispose();
        });
        south.add(close);
        root.add(south, BorderLayout.SOUTH);
        pack();
    }
}