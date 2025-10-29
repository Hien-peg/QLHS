package com.sgu.qlhs.ui.dialogs;

import com.sgu.qlhs.bus.DiemHocKyBUS;
import com.sgu.qlhs.bus.LopBUS;
import com.sgu.qlhs.bus.HocSinhBUS;
import com.sgu.qlhs.dto.LopDTO;
import com.sgu.qlhs.dto.HocSinhDTO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Dialog để xem điểm trung bình theo học kỳ và cả năm
 */
public class DiemXemTheoHocKyDialog extends JDialog {
    private final JComboBox<String> cboLoaiXem = new JComboBox<>(new String[] { "Học kỳ 1", "Học kỳ 2", "Cả năm" });
    private final JComboBox<String> cboLop = new JComboBox<>();
    private final JComboBox<String> cboNamHoc = new JComboBox<>(new String[] { "2024-2025", "2023-2024", "2022-2023" });
    private DefaultTableModel model;
    private JTable table;
    private DiemHocKyBUS dao;
    // BUS helpers
    private LopBUS lopBUS;
    private HocSinhBUS hocSinhBUS;
    // lưu danh sách lớp để map index -> MaLop / TenLop
    private List<LopDTO> lopList = new ArrayList<>();

    public DiemXemTheoHocKyDialog(Window owner) {
        super(owner, "Xem điểm theo học kỳ / Cả năm", ModalityType.APPLICATION_MODAL);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(owner);
        dao = new DiemHocKyBUS();
        lopBUS = new LopBUS();
        hocSinhBUS = new HocSinhBUS();
        build();
        loadLopData();
        pack();
    }

    private void build() {
        var root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(new EmptyBorder(16, 16, 16, 16));
        setContentPane(root);

        // ===== THANH CÔNG CỤ =====
        var toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        toolbar.add(new JLabel("Xem theo:"));
        toolbar.add(cboLoaiXem);
        toolbar.add(Box.createHorizontalStrut(10));
        toolbar.add(new JLabel("Lớp:"));
        toolbar.add(cboLop);
        toolbar.add(Box.createHorizontalStrut(10));
        toolbar.add(new JLabel("Năm học:"));
        toolbar.add(cboNamHoc);

        var btnLoad = new JButton("Tải dữ liệu");
        var btnExport = new JButton("Xuất CSV");
        var btnClose = new JButton("Đóng");

        toolbar.add(Box.createHorizontalStrut(20));
        toolbar.add(btnLoad);
        toolbar.add(btnExport);
        toolbar.add(btnClose);

        root.add(toolbar, BorderLayout.NORTH);

        // ===== BẢNG DỮ LIỆU =====
        model = new DefaultTableModel(new Object[] { "Mã HS", "Họ tên", "Lớp", "HK1", "HK2", "Cả năm", "Xếp loại" },
                0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);
        table.setAutoCreateRowSorter(true);
        table.setRowHeight(28);
        table.getTableHeader().setFont(table.getTableHeader().getFont().deriveFont(Font.BOLD));

        var scrollPane = new JScrollPane(table);
        root.add(scrollPane, BorderLayout.CENTER);

        // ===== THÔNG TIN THỐNG KÊ =====
        var statsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statsPanel.setBorder(BorderFactory.createTitledBorder("Thống kê"));
        var lblStats = new JLabel("Tổng: 0 học sinh");
        statsPanel.add(lblStats);
        root.add(statsPanel, BorderLayout.SOUTH);

        // ===== SỰ KIỆN =====
        cboLoaiXem.addActionListener(e -> updateTableColumns());

        btnLoad.addActionListener(e -> loadData(lblStats));

        btnExport.addActionListener(e -> exportToCSV());

        btnClose.addActionListener(e -> dispose());

        // Cập nhật cột ban đầu
        updateTableColumns();
    }

    /**
     * Cập nhật cột của bảng theo loại xem
     */
    private void updateTableColumns() {
        String loaiXem = (String) cboLoaiXem.getSelectedItem();

        if (loaiXem.equals("Cả năm")) {
            model.setColumnIdentifiers(
                    new Object[] { "Mã HS", "Họ tên", "Lớp", "TB HK1", "TB HK2", "TB Cả năm", "Xếp loại" });
        } else {
            model.setColumnIdentifiers(new Object[] { "Mã HS", "Họ tên", "Lớp", "TB Học kỳ", "Xếp loại" });
        }

        model.setRowCount(0);
    }

    /**
     * Load danh sách lớp vào ComboBox
     */
    private void loadLopData() {
        cboLop.removeAllItems();
        cboLop.addItem("-- Tất cả --");
        lopList = lopBUS.getAllLop();
        for (LopDTO lop : lopList) {
            String tenLop = lop.getTenLop();
            int khoi = lop.getKhoi();
            cboLop.addItem(tenLop + " (Khối " + khoi + ")");
        }
    }

    /**
     * Load dữ liệu điểm
     */
    private void loadData(JLabel lblStats) {
        model.setRowCount(0);

        String loaiXem = (String) cboLoaiXem.getSelectedItem();
        int maNK = 1; // Mặc định năm khóa 1, có thể lấy từ cboNamHoc

        try {
            if (loaiXem.equals("Cả năm")) {
                int sel = cboLop.getSelectedIndex();
                if (sel > 0) {
                    // lọc theo lớp
                    String tenLop = lopList.get(sel - 1).getTenLop();
                    loadDiemCaNamFiltered(maNK, tenLop);
                } else {
                    loadDiemCaNam(maNK);
                }
            } else {
                int hocKy = loaiXem.equals("Học kỳ 1") ? 1 : 2;
                int sel = cboLop.getSelectedIndex();
                if (sel > 0) {
                    int maLop = lopList.get(sel - 1).getMaLop();
                    loadDiemHocKyForClass(maLop, hocKy, maNK);
                } else {
                    loadDiemHocKy(hocKy, maNK);
                }
            }

            lblStats.setText("Tổng: " + model.getRowCount() + " học sinh");

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Lỗi khi tải dữ liệu: " + ex.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    /**
     * Load điểm theo học kỳ
     */
    private void loadDiemHocKy(int hocKy, int maNK) {
        var data = dao.getDiemTrungBinhHocKy(hocKy, maNK);

        for (Object[] row : data) {
            // Lấy thêm tên lớp (tạm thời để trống, có thể JOIN thêm)
            model.addRow(new Object[] {
                    row[0], // MaHS
                    row[1], // HoTen
                    "", // TenLop (cần JOIN thêm nếu cần)
                    row[2], // DiemTB
                    row[3] // XepLoai
            });
        }
    }

    /**
     * Load điểm học kỳ cho 1 lớp cụ thể: tính TBHK cho từng học sinh trong lớp
     */
    private void loadDiemHocKyForClass(int maLop, int hocKy, int maNK) {
        model.setRowCount(0);
        var data = dao.getDiemChiTietHocKy(maLop, hocKy, maNK);

        // Aggregate per student: map MaHS -> {hoTen, sum, count}
        Map<Integer, String> name = new HashMap<>();
        Map<Integer, Double> sum = new HashMap<>();
        Map<Integer, Integer> cnt = new HashMap<>();

        for (Object[] r : data) {
            int maHS = (int) r[0];
            String hoTen = (String) r[1];
            double diemTB = ((Number) r[7]).doubleValue();

            name.put(maHS, hoTen);
            sum.put(maHS, sum.getOrDefault(maHS, 0.0) + diemTB);
            cnt.put(maHS, cnt.getOrDefault(maHS, 0) + 1);
        }

        for (Integer maHS : sum.keySet()) {
            double avg = Math.round((sum.get(maHS) / cnt.get(maHS)) * 10) / 10.0;
            String hoTen = name.get(maHS);
            String xep = xepLoai(avg);
            // TenLop: get from lopList by maLop
            String tenLop = "";
            for (LopDTO l : lopList) {
                if (l.getMaLop() == maLop) {
                    tenLop = l.getTenLop();
                    break;
                }
            }
            model.addRow(new Object[] { maHS, hoTen, tenLop, avg, xep });
        }
    }

    /**
     * Load điểm cả năm nhưng chỉ hiển thị học sinh trong lớp tenLop
     */
    private void loadDiemCaNamFiltered(int maNK, String tenLop) {
        model.setRowCount(0);
        var data = dao.getDiemTrungBinhCaNam(maNK);
        for (Object[] row : data) {
            String lop = (String) row[2];
            if (lop != null && lop.equals(tenLop)) {
                model.addRow(new Object[] {
                        row[0], // MaHS
                        row[1], // HoTen
                        row[2], // TenLop
                        row[3], // DiemHK1
                        row[4], // DiemHK2
                        row[5], // DiemCaNam
                        row[6] // XepLoai
                });
            }
        }
    }

    private String getTenLopByMaHS(int maHS) {
        HocSinhDTO h = hocSinhBUS.getHocSinhByMaHS(maHS);
        if (h != null)
            return h.getTenLop();
        return "";
    }

    /**
     * Load điểm cả năm
     */
    private void loadDiemCaNam(int maNK) {
        var data = dao.getDiemTrungBinhCaNam(maNK);

        for (Object[] row : data) {
            model.addRow(new Object[] {
                    row[0], // MaHS
                    row[1], // HoTen
                    row[2], // TenLop
                    row[3], // DiemHK1
                    row[4], // DiemHK2
                    row[5], // DiemCaNam
                    row[6] // XepLoai
            });
        }
    }

    // local xếp loại (same as DAO)
    private static String xepLoai(double diemTB) {
        if (diemTB >= 9.0)
            return "Xuất sắc";
        if (diemTB >= 8.0)
            return "Giỏi";
        if (diemTB >= 6.5)
            return "Khá";
        if (diemTB >= 5.0)
            return "Trung bình";
        if (diemTB >= 3.5)
            return "Yếu";
        return "Kém";
    }

    /**
     * Xuất dữ liệu ra file CSV
     */
    private void exportToCSV() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Lưu file CSV");
        fileChooser.setSelectedFile(new java.io.File("diem_hoc_ky.csv"));

        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            java.io.File fileToSave = fileChooser.getSelectedFile();

            try (FileWriter writer = new FileWriter(fileToSave)) {
                // Ghi header
                for (int i = 0; i < model.getColumnCount(); i++) {
                    writer.append(model.getColumnName(i));
                    if (i < model.getColumnCount() - 1) {
                        writer.append(",");
                    }
                }
                writer.append("\n");

                // Ghi dữ liệu
                for (int i = 0; i < model.getRowCount(); i++) {
                    for (int j = 0; j < model.getColumnCount(); j++) {
                        Object value = model.getValueAt(i, j);
                        writer.append(value != null ? value.toString() : "");
                        if (j < model.getColumnCount() - 1) {
                            writer.append(",");
                        }
                    }
                    writer.append("\n");
                }

                JOptionPane.showMessageDialog(this,
                        "Xuất file thành công!\nĐường dẫn: " + fileToSave.getAbsolutePath(),
                        "Thành công",
                        JOptionPane.INFORMATION_MESSAGE);

            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                        "Lỗi khi xuất file: " + ex.getMessage(),
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
