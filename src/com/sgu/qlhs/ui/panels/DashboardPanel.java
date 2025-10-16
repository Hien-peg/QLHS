/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sgu.qlhs.ui.panels;

import com.sgu.qlhs.ui.components.*;
import com.sgu.qlhs.ui.dialogs.LopQuanLyDialog;
import com.sgu.qlhs.ui.dialogs.MonQuanLyDialog;
import com.sgu.qlhs.ui.dialogs.PhongQuanLyDialog;
import com.sgu.qlhs.ui.dialogs.DiemNhapDialog;
import com.sgu.qlhs.ui.dialogs.DiemTinhXepLoaiDialog;
import com.sgu.qlhs.ui.dialogs.DiemExportPdfDialog;
import com.sgu.qlhs.ui.dialogs.DiemXemTheoHocKyDialog;
import com.sgu.qlhs.ui.dialogs.BangDiemChiTietDialog;
import com.sgu.qlhs.ui.dialogs.HocSinhAddDialog;
import com.sgu.qlhs.ui.dialogs.HocSinhDeleteDialog;
import com.sgu.qlhs.ui.dialogs.HocSinhEditDialog;
import com.sgu.qlhs.ui.dialogs.ThongKeLopSucChuaDialog;
import com.sgu.qlhs.ui.dialogs.ThongKeGioiTinhDialog;
import com.sgu.qlhs.ui.dialogs.ThongKeDiemMonDialog;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

import static com.sgu.qlhs.ui.MainDashboard.*;

public class DashboardPanel extends JPanel {
    public DashboardPanel() {
        setOpaque(false);
        setLayout(new GridLayout(2, 2, 18, 18));
        add(makeSection("Học sinh", new SectionItem[] {
                new SectionItem("Thêm học sinh", IconType.ADD),
                new SectionItem("Sửa học sinh", IconType.EDIT),
                new SectionItem("Xóa học sinh", IconType.DELETE) }));
        add(makeSection("Lớp / Môn / Phòng", new SectionItem[] {
                new SectionItem("Quản lý lớp", IconType.HOME),
                new SectionItem("Quản lý môn", IconType.ABC),
                new SectionItem("Quản lý phòng", IconType.ROOM) }));
        add(makeSection("Điểm", new SectionItem[] {
                new SectionItem("Nhập điểm", IconType.CLIPBOARD),
                new SectionItem("Tính TB từng môn", IconType.TABLE),
                new SectionItem("Tính TB tất cả môn", IconType.TABLE),
                new SectionItem("Xem điểm HK/Năm", IconType.BARCHART),
                new SectionItem("Bảng điểm chi tiết", IconType.ABC),
                new SectionItem("Xuất PDF", IconType.PDF) }));
        add(makeSection("Thống kê", new SectionItem[] {
                new SectionItem("Thống kê giới tính", IconType.CHART),
                new SectionItem("Thống kê điểm TB môn", IconType.BARCHART),
                new SectionItem("Thống kê sức chứa lớp", IconType.MATRIX) }));
    }

    private JPanel makeSection(String title, SectionItem[] items) {
        var panel = new RoundedPanel(18, CARD_BG, CARD_BORDER);
        panel.setLayout(new BorderLayout());
        var header = new JLabel(title);
        header.setBorder(new EmptyBorder(12, 16, 8, 16));
        header.setFont(header.getFont().deriveFont(Font.BOLD, 18f));
        panel.add(header, BorderLayout.NORTH);

        var list = new JPanel();
        list.setOpaque(false);
        list.setLayout(new GridLayout(items.length, 1, 4, 6));
        list.setBorder(new EmptyBorder(4, 12, 12, 12));
        for (SectionItem it : items)
            list.add(makeRow(it));
        panel.add(list, BorderLayout.CENTER);
        return panel;
    }

    private JPanel makeRow(SectionItem item) {
        var row = new RoundedPanel(12, Color.WHITE, new Color(232, 236, 244));
        row.setLayout(new BorderLayout());
        row.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        row.setBorder(new EmptyBorder(10, 12, 10, 12));

        var icon = new ChipIcon(22, ICON_BG, ICON_FG, item.iconType);
        var lbl = new JLabel(item.text);
        lbl.setFont(lbl.getFont().deriveFont(Font.PLAIN, 12f));

        var left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);
        left.add(icon);
        left.add(lbl);
        row.add(left, BorderLayout.CENTER);

        row.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                row.setBorder(new EmptyBorder(9, 11, 9, 11));
                row.repaint();
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                row.setBorder(new EmptyBorder(10, 12, 10, 12));
                row.repaint();
            }

            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                switch (item.text) {

                    case "Thêm học sinh" ->
                        new HocSinhAddDialog(null).setVisible(true);

                    case "Sửa học sinh" ->
                        new HocSinhEditDialog(null).setVisible(true);

                    case "Xóa học sinh" ->
                        new HocSinhDeleteDialog(null).setVisible(true);

                    case "Quản lý lớp" -> new LopQuanLyDialog(null).setVisible(true);
                    case "Quản lý môn" -> new MonQuanLyDialog(null).setVisible(true);
                    case "Quản lý phòng" -> new PhongQuanLyDialog(null).setVisible(true);

                    case "Nhập điểm" -> new DiemNhapDialog(null).setVisible(true);
                    case "Tính TB từng môn" -> new DiemTinhXepLoaiDialog(null).setVisible(true);
                    case "Tính TB tất cả môn" ->
                        new com.sgu.qlhs.ui.dialogs.DiemTrungBinhTatCaMonDialog(null).setVisible(true);
                    case "Xem điểm HK/Năm" -> new DiemXemTheoHocKyDialog(null).setVisible(true);
                    case "Bảng điểm chi tiết" -> new BangDiemChiTietDialog(null).setVisible(true);
                    case "Xuất PDF" -> new DiemExportPdfDialog(null).setVisible(true);

                    case "Thống kê giới tính" -> new ThongKeGioiTinhDialog(null).setVisible(true);
                    case "Thống kê điểm TB môn" -> new ThongKeDiemMonDialog(null).setVisible(true);
                    case "Thống kê sức chứa lớp" -> new ThongKeLopSucChuaDialog(null).setVisible(true);

                    default -> JOptionPane.showMessageDialog(DashboardPanel.this, "[Demo] Chọn: " + item.text);
                }
            }
        });
        return row;
    }

    public static class SectionItem {
        public final String text;
        public final IconType iconType;

        public SectionItem(String text, IconType iconType) {
            this.text = text;
            this.iconType = iconType;
        }
    }
}
