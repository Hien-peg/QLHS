package com.sgu.qlhs.ui.dialogs;

import com.sgu.qlhs.DatabaseConnection;
import com.sgu.qlhs.bus.DiemBUS;
import com.sgu.qlhs.bus.LopBUS;
import com.sgu.qlhs.bus.NienKhoaBUS;
import com.sgu.qlhs.dto.DiemDTO;
import com.sgu.qlhs.dto.LopDTO;
import com.sgu.qlhs.ui.components.PieChartCanvas;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Dialog mới: Thống kê Xếp loại học lực (Giỏi, Khá, TB, Yếu) theo Khối, Học Kỳ, Năm Học.
 */
public class ThongKeHocLucDialog extends JDialog {

    private final JComboBox<String> cboKhoi = new JComboBox<>(new String[]{"10", "11", "12"});
    private final JComboBox<String> cboHocKy = new JComboBox<>(new String[]{"Học kỳ 1", "Học kỳ 2"});
    private final JComboBox<String> cboNamHoc = new JComboBox<>();
    private final JPanel pnlChartContainer;

    private final List<Integer> nienKhoaIds = new ArrayList<>();
    private final DiemBUS diemBUS = new DiemBUS();
    private final LopBUS lopBUS = new LopBUS();

    public ThongKeHocLucDialog(Window owner) {
        super(owner, "Thống kê Xếp loại Học lực theo Khối", ModalityType.APPLICATION_MODAL);
        setMinimumSize(new Dimension(600, 500));
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        // Panel lọc
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        filterPanel.setBorder(new EmptyBorder(10, 10, 0, 10));
        filterPanel.add(new JLabel("Khối:"));
        filterPanel.add(cboKhoi);
        filterPanel.add(new JLabel("Học kỳ:"));
        filterPanel.add(cboHocKy);
        filterPanel.add(new JLabel("Năm học:"));
        filterPanel.add(cboNamHoc);
        JButton btnLoad = new JButton("Tải thống kê");
        filterPanel.add(btnLoad);
        add(filterPanel, BorderLayout.NORTH);

        // Panel chứa biểu đồ
        pnlChartContainer = new JPanel(new BorderLayout());
        pnlChartContainer.setOpaque(false);
        pnlChartContainer.setBorder(new EmptyBorder(5, 10, 10, 10));
        pnlChartContainer.add(new JLabel("Vui lòng nhấn 'Tải thống kê'", SwingConstants.CENTER), BorderLayout.CENTER);
        add(pnlChartContainer, BorderLayout.CENTER);

        // Nút đóng
        JPanel pnlSouth = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnClose = new JButton("Đóng");
        btnClose.addActionListener(e -> dispose());
        pnlSouth.add(btnClose);
        add(pnlSouth, BorderLayout.SOUTH);

        // Tải dữ liệu ban đầu
        loadNienKhoa();

        // Sự kiện
        btnLoad.addActionListener(e -> loadData());
    }

    private void loadNienKhoa() {
        cboNamHoc.removeAllItems();
        nienKhoaIds.clear();
        String sql = "SELECT MaNK, NamBatDau, NamKetThuc FROM NienKhoa ORDER BY NamBatDau DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                int maNK = rs.getInt("MaNK");
                String label = rs.getInt("NamBatDau") + "-" + rs.getInt("NamKetThuc");
                cboNamHoc.addItem(label);
                nienKhoaIds.add(maNK);
            }
        } catch (SQLException ex) {
            cboNamHoc.addItem(NienKhoaBUS.currentNamHoc());
            nienKhoaIds.add(NienKhoaBUS.current());
        }
    }

    private void loadData() {
        // 1. Lấy lựa chọn
        int khoi = Integer.parseInt(cboKhoi.getSelectedItem().toString());
        int hocKy = cboHocKy.getSelectedIndex() + 1;
        int maNK = nienKhoaIds.get(cboNamHoc.getSelectedIndex());

        // 2. Lấy Map ánh xạ MaLop -> Khoi
        Map<Integer, Integer> lopToKhoiMap = lopBUS.getAllLop().stream()
                .collect(Collectors.toMap(LopDTO::getMaLop, LopDTO::getKhoi, (a, b) -> a));

        // 3. Tải tất cả điểm của học kỳ đó
        List<DiemDTO> allDiem = diemBUS.getDiemFiltered(null, null, hocKy, maNK, null, null);

        // 4. Lọc điểm theo khối và chỉ lấy môn 'TinhDiem', sau đó gom nhóm theo MaHS
        Map<Integer, List<DiemDTO>> diemTheoHS = allDiem.stream()
                .filter(d -> "TinhDiem".equals(d.getLoaiMon()) &&
                             khoi == lopToKhoiMap.getOrDefault(d.getMaLop(), 0))
                .collect(Collectors.groupingBy(DiemDTO::getMaHS));

        // 5. Tính TBHK cho từng học sinh và đếm
        long countGioi = 0, countKha = 0, countTB = 0, countYeu = 0;

        for (List<DiemDTO> diemCuaHS : diemTheoHS.values()) {
            if (diemCuaHS.isEmpty()) continue;
            
            // Tính TBHK của học sinh này
            double avg = diemCuaHS.stream().mapToDouble(DiemDTO::getDiemTB).average().orElse(0.0);

            // Phân loại
            if (avg >= 8.0) countGioi++;
            else if (avg >= 6.5) countKha++;
            else if (avg >= 5.0) countTB++;
            else countYeu++;
        }

        // 6. Chuẩn bị dữ liệu cho biểu đồ
        double[] values = {(double) countGioi, (double) countKha, (double) countTB, (double) countYeu};
        String[] labels = {
                "Giỏi (" + countGioi + ")",
                "Khá (" + countKha + ")",
                "Trung bình (" + countTB + ")",
                "Yếu (" + countYeu + ")"
        };
        String title = String.format("Xếp loại học lực Khối %d - %s", khoi, cboHocKy.getSelectedItem());

        // 7. Vẽ biểu đồ
        PieChartCanvas chart = new PieChartCanvas(title, values, labels);
        pnlChartContainer.removeAll();
        pnlChartContainer.add(chart, BorderLayout.CENTER);
        pnlChartContainer.revalidate();
        pnlChartContainer.repaint();
    }
}