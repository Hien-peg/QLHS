package com.sgu.qlhs.ui.dialogs;

import com.sgu.qlhs.ui.database.DiemHocKyDAO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Dialog để xem điểm trung bình theo học kỳ và cả năm
 */
public class DiemXemTheoHocKyDialog extends JDialog {
    private final JComboBox<String> cboLoaiXem = new JComboBox<>(new String[] { "Học kỳ 1", "Học kỳ 2", "Cả năm" });
    private final JComboBox<String> cboLop = new JComboBox<>();
    private final JComboBox<String> cboNamHoc = new JComboBox<>(new String[] { "2024-2025", "2023-2024", "2022-2023" });
    private DefaultTableModel model;
    private JTable table;
    private DiemHocKyDAO dao;

    public DiemXemTheoHocKyDialog(Window owner) {
        super(owner, "Xem điểm theo học kỳ / Cả năm", ModalityType.APPLICATION_MODAL);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(owner);
        dao = new DiemHocKyDAO();
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

        var lopList = dao.getAllLop();
        for (Object[] lop : lopList) {
            cboLop.addItem(lop[1] + " (Khối " + lop[2] + ")");
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
                loadDiemCaNam(maNK);
            } else {
                int hocKy = loaiXem.equals("Học kỳ 1") ? 1 : 2;
                loadDiemHocKy(hocKy, maNK);
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
