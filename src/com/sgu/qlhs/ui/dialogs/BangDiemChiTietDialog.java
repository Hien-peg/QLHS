package com.sgu.qlhs.ui.dialogs;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.text.SimpleDateFormat;
import java.util.Date;
import com.sgu.qlhs.bus.HocSinhBUS;
import com.sgu.qlhs.bus.DiemBUS;
import com.sgu.qlhs.dto.DiemDTO;
import com.sgu.qlhs.dto.HocSinhDTO;
import com.sgu.qlhs.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Dialog hiển thị bảng điểm chi tiết của học sinh theo định dạng chính thức
 */
public class BangDiemChiTietDialog extends JDialog {
    private final JComboBox<String> cboHocSinh = new JComboBox<>();
    private final JComboBox<String> cboHocKy = new JComboBox<>(new String[] { "Học kỳ 1", "Học kỳ 2" });
    private final JComboBox<String> cboNamHoc = new JComboBox<>();
    // map combo index -> MaNK
    private java.util.List<Integer> nienKhoaIds = new java.util.ArrayList<>();
    private JPanel pnlBangDiem;
    private DefaultTableModel model;
    private JTable table;

    private String tenHocSinh = "";
    private String tenTruong = "ĐẠI HỌC SÀI GÒN - SGU";
    private String diaPhuong = "SỞ GD&ĐT HỒ CHÍ MINH";
    private final HocSinhBUS hocSinhBUS = new HocSinhBUS();
    private final DiemBUS diemBUS = new DiemBUS();

    public BangDiemChiTietDialog(Window owner) {
        super(owner, "Bảng điểm chi tiết học sinh", ModalityType.APPLICATION_MODAL);
        setSize(1000, 800);
        setLocationRelativeTo(owner);
        build();
        // load niên khóa list after UI built
        loadNienKhoa();
    }

    private void build() {
        var root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(new EmptyBorder(16, 16, 16, 16));
        setContentPane(root);

        // ===== THANH CHỌN =====
        var toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        toolbar.add(new JLabel("Học sinh:"));
        cboHocSinh.setPreferredSize(new Dimension(200, 25));
        toolbar.add(cboHocSinh);

        toolbar.add(Box.createHorizontalStrut(10));
        toolbar.add(new JLabel("Học kỳ:"));
        toolbar.add(cboHocKy);

        toolbar.add(Box.createHorizontalStrut(10));
        toolbar.add(new JLabel("Năm học:"));
        toolbar.add(cboNamHoc);

        var btnLoad = new JButton("Xem bảng điểm");
        var btnPrint = new JButton("In");
        var btnClose = new JButton("Đóng");

        toolbar.add(Box.createHorizontalStrut(20));
        toolbar.add(btnLoad);
        toolbar.add(btnPrint);
        toolbar.add(btnClose);

        root.add(toolbar, BorderLayout.NORTH);

        // ===== PANEL HIỂN THỊ BẢNG ĐIỂM =====
        pnlBangDiem = new JPanel(new BorderLayout());
        pnlBangDiem.setBackground(Color.WHITE);
        pnlBangDiem.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));

        var scrollPane = new JScrollPane(pnlBangDiem);
        scrollPane.getViewport().setBackground(Color.WHITE);
        root.add(scrollPane, BorderLayout.CENTER);

        // ===== SỰ KIỆN =====
        btnLoad.addActionListener(e -> loadBangDiem());
        btnPrint.addActionListener(e -> printBangDiem());
        btnClose.addActionListener(e -> dispose());

        // Load danh sách học sinh
        loadHocSinh();
    }

    /** Load Niên khóa options from DB into cboNamHoc and nienKhoaIds */
    private void loadNienKhoa() {
        cboNamHoc.removeAllItems();
        nienKhoaIds.clear();
        String sql = "SELECT MaNK, NamBatDau, NamKetThuc FROM NienKhoa ORDER BY NamBatDau ASC, MaNK ASC";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                int maNK = rs.getInt("MaNK");
                int nb = rs.getInt("NamBatDau");
                int nk = rs.getInt("NamKetThuc");
                String label = nb + "-" + nk;
                cboNamHoc.addItem(label);
                nienKhoaIds.add(maNK);
            }
        } catch (SQLException ex) {
            // fallback to a few recent labels if DB read fails
            cboNamHoc.addItem("2024-2025");
            cboNamHoc.addItem("2023-2024");
            cboNamHoc.addItem("2022-2023");
        }
        // select current MaNK if present
        int current = com.sgu.qlhs.bus.NienKhoaBUS.current();
        int idx = nienKhoaIds.indexOf(current);
        if (idx >= 0)
            cboNamHoc.setSelectedIndex(idx);
        else if (cboNamHoc.getItemCount() > 0)
            cboNamHoc.setSelectedIndex(0);
    }

    private void loadHocSinh() {
        // Load students from BUS
        cboHocSinh.removeAllItems();
        java.util.List<HocSinhDTO> list = hocSinhBUS.getAllHocSinh();
        for (HocSinhDTO h : list) {
            // store item as "<MaHS> - <HoTen>" so we can parse ID back
            cboHocSinh.addItem(h.getMaHS() + " - " + h.getHoTen());
        }
    }

    private void loadBangDiem() {
        tenHocSinh = (String) cboHocSinh.getSelectedItem();
        String hocKy = (String) cboHocKy.getSelectedItem();
        String namHoc = (String) cboNamHoc.getSelectedItem();

        pnlBangDiem.removeAll();

        // Tạo panel chứa nội dung bảng điểm
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Color.WHITE);
        content.setBorder(new EmptyBorder(30, 40, 30, 40));

        // ===== HEADER =====
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);

        // Cột trái
        JPanel leftHeader = new JPanel();
        leftHeader.setLayout(new BoxLayout(leftHeader, BoxLayout.Y_AXIS));
        leftHeader.setBackground(Color.WHITE);
        JLabel lblDiaPhuong = new JLabel(diaPhuong);
        lblDiaPhuong.setFont(new Font("Arial", Font.BOLD, 13));
        JLabel lblTruong = new JLabel(tenTruong);
        lblTruong.setFont(new Font("Arial", Font.BOLD, 14));
        leftHeader.add(lblDiaPhuong);
        leftHeader.add(Box.createVerticalStrut(5));
        leftHeader.add(lblTruong);

        // Cột phải
        JPanel rightHeader = new JPanel();
        rightHeader.setLayout(new BoxLayout(rightHeader, BoxLayout.Y_AXIS));
        rightHeader.setBackground(Color.WHITE);
        rightHeader.setAlignmentX(Component.RIGHT_ALIGNMENT);
        JLabel lblQuocGia = new JLabel("CỘNG HÒA XÃ HỘI CHỦ NGHĨA VIỆT NAM");
        lblQuocGia.setFont(new Font("Arial", Font.BOLD, 12));
        JLabel lblDevise = new JLabel("Độc lập - Tự do - Hạnh phúc");
        lblDevise.setFont(new Font("Arial", Font.BOLD, 12));
        SimpleDateFormat sdf = new SimpleDateFormat("'Ngày' dd 'tháng' MM 'năm' yyyy");
        JLabel lblNgay = new JLabel(sdf.format(new Date()));
        lblNgay.setFont(new Font("Arial", Font.ITALIC, 11));
        rightHeader.add(lblQuocGia);
        rightHeader.add(Box.createVerticalStrut(5));
        rightHeader.add(lblDevise);
        rightHeader.add(Box.createVerticalStrut(5));
        rightHeader.add(lblNgay);

        headerPanel.add(leftHeader, BorderLayout.WEST);
        headerPanel.add(rightHeader, BorderLayout.EAST);
        content.add(headerPanel);
        content.add(Box.createVerticalStrut(30));

        // ===== TIÊU ĐỀ =====
        JLabel lblTitle = new JLabel("BẢNG ĐIỂM HỌC SINH");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(lblTitle);
        content.add(Box.createVerticalStrut(10));

        JLabel lblHocSinh = new JLabel("Học sinh: " + tenHocSinh);
        lblHocSinh.setFont(new Font("Arial", Font.BOLD, 14));
        lblHocSinh.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(lblHocSinh);
        content.add(Box.createVerticalStrut(5));

        JLabel lblHocKyNam = new JLabel(hocKy + " năm học " + namHoc);
        lblHocKyNam.setFont(new Font("Arial", Font.BOLD, 13));
        lblHocKyNam.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(lblHocKyNam);
        content.add(Box.createVerticalStrut(20));

        // ===== BẢNG ĐIỂM =====
        String[] columns = { "STT", "Tên môn học", "Miệng", "15 Phút", "1 Tiết", "Học kỳ", "TBHK", "Ghi chú" };
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // Build table rows from DiemBUS for the selected student and semester
        Object sel = cboHocSinh.getSelectedItem();
        if (sel == null) {
            // nothing selected
            table = new JTable(model);
        } else {
            String s = sel.toString();
            int dash = s.indexOf(" - ");
            int maHS = 0;
            try {
                maHS = Integer.parseInt(dash > 0 ? s.substring(0, dash).trim() : s);
            } catch (Exception ex) {
                maHS = 0;
            }
            int hkNum = cboHocKy.getSelectedIndex() + 1; // Học kỳ 1 -> 1
            // determine niên khóa: use selected combo mapping if available, otherwise
            // current()
            int maNK = com.sgu.qlhs.bus.NienKhoaBUS.current();
            int selNk = cboNamHoc.getSelectedIndex();
            if (selNk >= 0 && selNk < nienKhoaIds.size()) {
                maNK = nienKhoaIds.get(selNk);
            }

            java.util.List<DiemDTO> diemList = diemBUS.getDiemByMaHS(maHS, hkNum, maNK);
            int idx = 1;
            for (DiemDTO d : diemList) {
                double mieng = d.getDiemMieng();
                double p15 = d.getDiem15p();
                double gk = d.getDiemGiuaKy();
                double ck = d.getDiemCuoiKy();
                double tb = Math.round((mieng * 0.10 + p15 * 0.20 + gk * 0.30 + ck * 0.40) * 10.0) / 10.0;
                model.addRow(new Object[] { String.valueOf(idx++), d.getTenMon(), mieng, p15, gk, ck, tb, "" });
            }
            table = new JTable(model);
        }

        table.setFont(new Font("Arial", Font.PLAIN, 12));
        table.setRowHeight(30);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(240, 240, 240));

        // Căn giữa các cột
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        // Đặt độ rộng cột
        table.getColumnModel().getColumn(0).setPreferredWidth(40); // STT
        table.getColumnModel().getColumn(1).setPreferredWidth(120); // Tên môn
        table.getColumnModel().getColumn(2).setPreferredWidth(60); // Miệng
        table.getColumnModel().getColumn(3).setPreferredWidth(100); // 15 phút
        table.getColumnModel().getColumn(4).setPreferredWidth(100); // 1 tiết
        table.getColumnModel().getColumn(5).setPreferredWidth(60); // Học kỳ
        table.getColumnModel().getColumn(6).setPreferredWidth(60); // TBHK
        table.getColumnModel().getColumn(7).setPreferredWidth(100); // Ghi chú

        // Thêm viền cho bảng
        table.setShowGrid(true);
        table.setGridColor(Color.BLACK);
        table.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));

        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        tableScroll.setPreferredSize(new Dimension(900, 450));
        content.add(tableScroll);

        pnlBangDiem.add(content, BorderLayout.CENTER);
        pnlBangDiem.revalidate();
        pnlBangDiem.repaint();
    }

    private void printBangDiem() {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPrintable(new Printable() {
            @Override
            public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
                if (pageIndex > 0) {
                    return NO_SUCH_PAGE;
                }

                Graphics2D g2d = (Graphics2D) graphics;
                g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

                // Scale để fit trang
                double scaleX = pageFormat.getImageableWidth() / pnlBangDiem.getWidth();
                double scaleY = pageFormat.getImageableHeight() / pnlBangDiem.getHeight();
                double scale = Math.min(scaleX, scaleY);
                g2d.scale(scale, scale);

                pnlBangDiem.printAll(graphics);
                return PAGE_EXISTS;
            }
        });

        boolean doPrint = job.printDialog();
        if (doPrint) {
            try {
                job.print();
                JOptionPane.showMessageDialog(this,
                        "In bảng điểm thành công!",
                        "Thành công",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (PrinterException ex) {
                JOptionPane.showMessageDialog(this,
                        "Lỗi khi in: " + ex.getMessage(),
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
