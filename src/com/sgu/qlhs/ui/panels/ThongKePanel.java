package com.sgu.qlhs.ui.panels;

import com.sgu.qlhs.ui.components.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import static com.sgu.qlhs.ui.MainDashboard.*;

import com.sgu.qlhs.dto.NguoiDungDTO;
import com.sgu.qlhs.ui.MainDashboard;
import com.sgu.qlhs.bus.DiemBUS;
import com.sgu.qlhs.bus.HocSinhBUS;
import com.sgu.qlhs.bus.NienKhoaBUS;
import com.sgu.qlhs.bus.LopBUS;
import com.sgu.qlhs.bus.PhanCongDayBUS;
import com.sgu.qlhs.dto.DiemDTO;
import com.sgu.qlhs.dto.HocSinhDTO;
import com.sgu.qlhs.dto.LopDTO; 
import java.awt.event.HierarchyListener;
import java.awt.event.HierarchyEvent;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Comparator;
import java.util.stream.Collectors;

public class ThongKePanel extends JPanel {

    private boolean isStudentView = false;
    private NguoiDungDTO currentUser;
    private int currentMaHS = -1;
    private HocSinhDTO currentHocSinh;

    // === SỬA: Thêm các component cho Giáo viên ===
    private JComboBox<String> cboThongKe;
    private JComboBox<String> cboHocKy;
    private JComboBox<LopDTO> cboLopGV; // Combobox lớp cho giáo viên
    private JPanel chartContainer; // Panel chính để chứa CardLayout
    private CardLayout chartCards;

    private DiemBUS diemBUS;
    private HocSinhBUS hocSinhBUS;
    private LopBUS lopBUS;
    private PhanCongDayBUS phanCongBUS;
    private List<LopDTO> lopListGV; // Danh sách lớp giáo viên được phân công

    // Card keys
    private final String CARD_GV_XEPLOAI = "GV_XEPLOAI";
    private final String CARD_GV_PHODIEM = "GV_PHODIEM";
    private final String CARD_RANKING = "RANKING";
    private final String CARD_AVERAGES = "AVERAGES";
    private final String CARD_ADMIN = "ADMIN_DEFAULT";
    private final String CARD_EMPTY = "EMPTY";
    private final String CARD_ERROR = "ERROR";

    public ThongKePanel() {
        super(new BorderLayout());
        setOpaque(false);

        addHierarchyListener(new HierarchyListener() {
            @Override
            public void hierarchyChanged(HierarchyEvent e) {
                if ((e.getChangeFlags() & HierarchyEvent.PARENT_CHANGED) != 0 &&
                        SwingUtilities.getWindowAncestor(ThongKePanel.this) instanceof MainDashboard) {

                    MainDashboard md = (MainDashboard) SwingUtilities.getWindowAncestor(ThongKePanel.this);
                    if (md == null) return;
                    
                    currentUser = md.getNguoiDung();

                    // === SỬA: Phân nhánh 3 vai trò ===
                    if (currentUser != null) {
                        String vaiTro = currentUser.getVaiTro();
                        if ("hoc_sinh".equalsIgnoreCase(vaiTro)) {
                            isStudentView = true;
                            currentMaHS = currentUser.getId();
                            initStudentView();
                        } else if ("giao_vien".equalsIgnoreCase(vaiTro)) {
                            isStudentView = false;
                            initTeacherView(); // Giao diện mới cho giáo viên
                        } else {
                            isStudentView = false;
                            initAdminView(); // Giao diện cho Admin
                        }
                    } else {
                        isStudentView = false;
                        initAdminView(); // Mặc định
                    }
                    // ==================================
                    
                    removeHierarchyListener(this);
                }
            }
        });
    }

    /**
     * Giao diện cho Admin (tách ra từ initAdminGiaoVienView)
     */
    private void initAdminView() {
        this.removeAll(); 

        var outer = new RoundedPanel(18, CARD_BG, CARD_BORDER);
        outer.setLayout(new BorderLayout());
        var lbl = new JLabel("Thống kê (Chung)");
        lbl.setBorder(new EmptyBorder(12, 16, 8, 16));
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 18f));
        outer.add(lbl, BorderLayout.NORTH);

        // TODO: Dữ liệu giới tính này nên được tải động từ HocSinhBUS
        String[] cats = { "Nam", "Nữ" };
        int[] values = { 58, 42 };
        var chart = new BarChartCanvas("Tỉ lệ giới tính học sinh", cats, values);
        outer.add(chart, BorderLayout.CENTER);

        this.add(outer, BorderLayout.CENTER);
        this.revalidate();
        this.repaint();
    }

    /**
     * Giao diện mới cho Giáo viên
     */
    private void initTeacherView() {
        this.removeAll();

        // Khởi tạo BUS
        diemBUS = new DiemBUS();
        hocSinhBUS = new HocSinhBUS();
        lopBUS = new LopBUS();
        phanCongBUS = new PhanCongDayBUS();

        var outer = new RoundedPanel(18, CARD_BG, CARD_BORDER);
        outer.setLayout(new BorderLayout());

        // Tiêu đề
        var lbl = new JLabel("Thống kê Lớp");
        lbl.setBorder(new EmptyBorder(12, 16, 8, 16));
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 18f));

        // Panel bộ lọc
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.setBorder(new EmptyBorder(5, 12, 5, 12));
        
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterPanel.setOpaque(false);

        filterPanel.add(new JLabel("Lớp dạy:"));
        cboLopGV = new JComboBox<>();
        loadTeacherLopOptions(); // Tải các lớp GV dạy
        filterPanel.add(cboLopGV);
        
        filterPanel.add(new JLabel("Loại thống kê:"));
        cboThongKe = new JComboBox<>(new String[]{
                "Phân loại Học lực (Biểu đồ tròn)", 
                "Phổ điểm TB (Biểu đồ cột)"
        });
        filterPanel.add(cboThongKe);

        filterPanel.add(new JLabel("Học kỳ:"));
        cboHocKy = new JComboBox<>(new String[]{"Học kỳ 1", "Học kỳ 2"});
        filterPanel.add(cboHocKy);

        topPanel.add(lbl, BorderLayout.NORTH);
        topPanel.add(filterPanel, BorderLayout.CENTER);
        outer.add(topPanel, BorderLayout.NORTH);

        // Panel chứa biểu đồ
        chartCards = new CardLayout();
        chartContainer = new JPanel(chartCards);
        chartContainer.setOpaque(false);
        chartContainer.setBorder(new EmptyBorder(10, 10, 10, 10));
        chartContainer.add(new JLabel("Vui lòng chọn lớp để xem thống kê.", SwingConstants.CENTER), CARD_EMPTY);
        outer.add(chartContainer, BorderLayout.CENTER);
        
        this.add(outer, BorderLayout.CENTER);

        // Gắn sự kiện
        cboLopGV.addActionListener(e -> loadTeacherChart());
        cboThongKe.addActionListener(e -> loadTeacherChart());
        cboHocKy.addActionListener(e -> loadTeacherChart());

        // Tải biểu đồ lần đầu
        loadTeacherChart();
        
        this.revalidate();
        this.repaint();
    }
    
    /**
     * Tải danh sách lớp mà giáo viên đang đăng nhập được phân công
     */
    private void loadTeacherLopOptions() {
        cboLopGV.removeAllItems();
        int maGV = currentUser.getId();
        
        // === PHẦN SỬA ===
        String namHoc = NienKhoaBUS.currentNamHoc(); // Lấy chuỗi năm học
        
        // Lấy tất cả lớp GV dạy trong năm học (cả 2 học kỳ)
        List<Integer> maLopList = phanCongBUS.getDistinctMaLopByGiaoVien(maGV, namHoc, null);
        // === KẾT THÚC PHẦN SỬA ===
        
        if(maLopList == null || maLopList.isEmpty()) {
            cboLopGV.setEnabled(false);
            return;
        }

        lopListGV = new ArrayList<>();
        for (Integer maLop : maLopList) {
            LopDTO lop = lopBUS.getLopByMa(maLop);
            if (lop != null) {
                lopListGV.add(lop);
                cboLopGV.addItem(lop); // JComboBox sẽ tự gọi lop.toString()
            }
        }
    }

    /**
     * Tải biểu đồ cho giáo viên dựa trên các lựa chọn
     */
    private void loadTeacherChart() {
        LopDTO selectedLop = (LopDTO) cboLopGV.getSelectedItem();
        if (selectedLop == null) {
            chartCards.show(chartContainer, CARD_EMPTY);
            return;
        }

        String loaiTK = (String) cboThongKe.getSelectedItem();
        int hocKy = (cboHocKy.getSelectedIndex() == 0) ? 1 : 2;
        int maNK = NienKhoaBUS.current();
        int maLop = selectedLop.getMaLop();

        String cardKey = maLop + "_" + loaiTK + "_" + hocKy;

        // Lấy danh sách điểm TBHK của cả lớp
        List<Double> dsDiemTBHK = tinhDiemTBHKChoLop(maLop, hocKy, maNK);

        if (dsDiemTBHK.isEmpty()) {
            chartContainer.add(new JLabel("Lớp chưa có dữ liệu điểm học kỳ " + hocKy, SwingConstants.CENTER), cardKey);
            chartCards.show(chartContainer, cardKey);
            return;
        }

        if ("Phân loại Học lực (Biểu đồ tròn)".equals(loaiTK)) {
            JComponent chart = createHocLucPieChart(dsDiemTBHK);
            chartContainer.add(chart, cardKey);
            chartCards.show(chartContainer, cardKey);
        } else if ("Phổ điểm TB (Biểu đồ cột)".equals(loaiTK)) {
            JComponent chart = createPhoDiemBarChart(dsDiemTBHK);
            chartContainer.add(chart, cardKey);
            chartCards.show(chartContainer, cardKey);
        }
    }

    /**
     * (GV) Tính toán danh sách điểm TB học kỳ cho 1 lớp
     */
    private List<Double> tinhDiemTBHKChoLop(int maLop, int hocKy, int maNK) {
        List<Double> dsDiemTBHK = new ArrayList<>();
        // Lấy tất cả học sinh trong lớp
        List<HocSinhDTO> dsHS = hocSinhBUS.getHocSinhByMaLop(maLop);
        if (dsHS == null || dsHS.isEmpty()) {
            return dsDiemTBHK;
        }

        // Lấy tất cả điểm của lớp
        List<DiemDTO> allScoresInClass = diemBUS.getDiemFiltered(maLop, null, hocKy, maNK, null, null);

        // Group theo MaHS
        Map<Integer, List<DiemDTO>> diemTheoHS = allScoresInClass.stream()
                .collect(Collectors.groupingBy(DiemDTO::getMaHS));

        for (HocSinhDTO hs : dsHS) {
            List<DiemDTO> diemCuaHS = diemTheoHS.get(hs.getMaHS());
            if (diemCuaHS == null || diemCuaHS.isEmpty()) {
                continue;
            }

            // Lọc các môn TinhDiem và tính TB
            double tongDiem = 0;
            int soMon = 0;
            for (DiemDTO d : diemCuaHS) {
                if ("TinhDiem".equals(d.getLoaiMon())) {
                    tongDiem += d.getDiemTB();
                    soMon++;
                }
            }
            
            if (soMon > 0) {
                dsDiemTBHK.add(tongDiem / soMon);
            }
        }
        return dsDiemTBHK;
    }

    /**
     * (GV) Tạo biểu đồ tròn Phân loại học lực
     */
    private JComponent createHocLucPieChart(List<Double> dsDiemTBHK) {
        double[] values = new double[4]; // Giỏi, Khá, TB, Yếu
        String[] labels = {"Giỏi (8.0+)", "Khá (6.5-7.9)", "Trung bình (5.0-6.4)", "Yếu (<5.0)"};
        
        for (double diem : dsDiemTBHK) {
            if (diem >= 8.0) values[0]++;
            else if (diem >= 6.5) values[1]++;
            else if (diem >= 5.0) values[2]++;
            else values[3]++;
        }
        
        String title = "Phân loại Học lực Lớp - " + cboHocKy.getSelectedItem();
        return new PieChartCanvas(title, values, labels);
    }
    
    /**
     * (GV) Tạo biểu đồ cột Phổ điểm
     */
    private JComponent createPhoDiemBarChart(List<Double> dsDiemTBHK) {
        int[] bins = new int[10]; // 0-1, 1-2, ..., 9-10
        String[] cats = {"0-1", "1-2", "2-3", "3-4", "4-5", "5-6", "6-7", "7-8", "8-9", "9-10"};

        for (double diem : dsDiemTBHK) {
            if (diem >= 10.0) {
                bins[9]++; // Cho điểm 10 vào bin cuối
            } else if (diem >= 0) {
                bins[(int)diem]++;
            }
        }

        String title = "Phổ điểm TB Học kỳ - " + cboHocKy.getSelectedItem();
        return new BarChartCanvas(title, cats, bins);
    }

    /**
     * Giao diện cho Học sinh (như cũ)
     */
    private void initStudentView() {
        this.removeAll(); 

        diemBUS = new DiemBUS();
        hocSinhBUS = new HocSinhBUS();
        lopBUS = new LopBUS();
        currentHocSinh = hocSinhBUS.getHocSinhByMaHS(currentMaHS);

        var outer = new RoundedPanel(18, CARD_BG, CARD_BORDER);
        outer.setLayout(new BorderLayout());

        String tenHS = (currentHocSinh != null) ? currentHocSinh.getHoTen() : currentUser.getHoTen();
        var lbl = new JLabel("Thống kê: " + tenHS);
        lbl.setBorder(new EmptyBorder(12, 16, 8, 16));
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 18f));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.setBorder(new EmptyBorder(5, 12, 5, 12));
        
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterPanel.setOpaque(false);
        
        filterPanel.add(new JLabel("Loại thống kê:"));
        cboThongKe = new JComboBox<>(new String[]{
                "Thứ hạng ĐTB theo môn", 
                "Điểm TB các môn"
        });
        filterPanel.add(cboThongKe);

        filterPanel.add(new JLabel("Học kỳ:"));
        cboHocKy = new JComboBox<>(new String[]{"Học kỳ 1", "Học kỳ 2"});
        filterPanel.add(cboHocKy);

        topPanel.add(lbl, BorderLayout.NORTH);
        topPanel.add(filterPanel, BorderLayout.CENTER);
        outer.add(topPanel, BorderLayout.NORTH);

        chartCards = new CardLayout();
        chartContainer = new JPanel(chartCards);
        chartContainer.setOpaque(false);
        chartContainer.setBorder(new EmptyBorder(10, 10, 10, 10));
        chartContainer.add(new JLabel("Đang tải...", SwingConstants.CENTER), CARD_EMPTY);
        chartContainer.add(new JLabel("Không tìm thấy dữ liệu.", SwingConstants.CENTER), CARD_ERROR);
        outer.add(chartContainer, BorderLayout.CENTER);
        
        this.add(outer, BorderLayout.CENTER);

        cboThongKe.addActionListener(e -> loadStudentChart());
        cboHocKy.addActionListener(e -> loadStudentChart());
        loadStudentChart();
        
        this.revalidate();
        this.repaint();
    }

    /**
     * (HS) Tải biểu đồ cho học sinh
     */
    private void loadStudentChart() {
        if (!isStudentView || diemBUS == null || currentHocSinh == null) {
            chartCards.show(chartContainer, CARD_EMPTY);
            return;
        }

        String loaiTK = (String) cboThongKe.getSelectedItem();
        int hocKy = (cboHocKy.getSelectedIndex() == 0) ? 1 : 2;
        int maNK = NienKhoaBUS.current();

        int maLop = -1;
        String tenLop = currentHocSinh.getTenLop();
        if (tenLop != null) {
            maLop = lopBUS.getAllLop().stream()
                    .filter(lop -> tenLop.equals(lop.getTenLop()))
                    .map(com.sgu.qlhs.dto.LopDTO::getMaLop)
                    .findFirst()
                    .orElse(-1);
        }

        if (maLop == -1) {
            chartContainer.add(new JLabel("Không tìm thấy thông tin lớp của học sinh."), "LopError");
            chartCards.show(chartContainer, "LopError");
            return;
        }

        String cardKey = loaiTK + "_" + hocKy;

        if ("Thứ hạng ĐTB theo môn".equals(loaiTK)) {
            JComponent chart = createRankingChart(currentMaHS, maLop, hocKy, maNK);
            chartContainer.add(chart, cardKey);
            chartCards.show(chartContainer, cardKey);
        } else if ("Điểm TB các môn".equals(loaiTK)) {
            JComponent chart = createAverageScoreChart(currentMaHS, hocKy, maNK);
            chartContainer.add(chart, cardKey);
            chartCards.show(chartContainer, cardKey);
        }
    }

    /**
     * (HS) Biểu đồ 1: Điểm TB cá nhân
     */
    private JComponent createAverageScoreChart(int maHS, int hocKy, int maNK) {
        List<DiemDTO> scores = diemBUS.getDiemByMaHS(maHS, hocKy, maNK, currentUser);
        
        if (scores == null || scores.isEmpty()) {
            return new JLabel("Chưa có dữ liệu điểm cho học kỳ này.", SwingConstants.CENTER);
        }

        List<DiemDTO> scoresTinhDiem = scores.stream()
                .filter(d -> "TinhDiem".equals(d.getLoaiMon()))
                .collect(Collectors.toList());

        if (scoresTinhDiem.isEmpty()) {
            return new JLabel("Chưa có dữ liệu điểm (tính số) cho học kỳ này.", SwingConstants.CENTER);
        }

        String[] cats = scoresTinhDiem.stream().map(DiemDTO::getTenMon).toArray(String[]::new);
        int[] values = scoresTinhDiem.stream().mapToInt(d -> (int) (d.getDiemTB() * 10)).toArray();
        
        String title = "Điểm TB các môn (x10) - " + cboHocKy.getSelectedItem();
        return new BarChartCanvas(title, cats, values);
    }

    /**
     * (HS) Biểu đồ 2: Thứ hạng
     */
    private JComponent createRankingChart(int maHS, int maLop, int hocKy, int maNK) {
        List<DiemDTO> allScoresInClass = diemBUS.getDiemFiltered(maLop, null, hocKy, maNK, null, null);

        if (allScoresInClass == null || allScoresInClass.isEmpty()) {
            return new JLabel("Chưa có dữ liệu điểm của lớp cho học kỳ này.", SwingConstants.CENTER);
        }

        Map<String, List<DiemDTO>> scoresBySubject = allScoresInClass.stream()
                .filter(d -> "TinhDiem".equals(d.getLoaiMon()))
                .collect(Collectors.groupingBy(DiemDTO::getTenMon));

        Map<String, Integer> ranks = new HashMap<>();

        for (Map.Entry<String, List<DiemDTO>> entry : scoresBySubject.entrySet()) {
            String tenMon = entry.getKey();
            List<DiemDTO> subjectScores = entry.getValue();
            
            subjectScores.sort(Comparator.comparing(DiemDTO::getDiemTB).reversed());
            
            int rank = -1;
            for (int i = 0; i < subjectScores.size(); i++) {
                if (subjectScores.get(i).getMaHS() == maHS) {
                    rank = i + 1; 
                    break;
                }
            }
            ranks.put(tenMon, rank);
        }

        List<DiemDTO> myScores = diemBUS.getDiemByMaHS(maHS, hocKy, maNK, currentUser);
        if (myScores.isEmpty()) {
             return new JLabel("Không thể tải thứ hạng (HS chưa có điểm).", SwingConstants.CENTER);
        }
        
        List<DiemDTO> myScoresTinhDiem = myScores.stream()
                .filter(d -> "TinhDiem".equals(d.getLoaiMon()))
                .collect(Collectors.toList());

        if (myScoresTinhDiem.isEmpty()) {
            return new JLabel("Không có môn tính điểm để xếp hạng.", SwingConstants.CENTER);
        }
        
        String[] cats = myScoresTinhDiem.stream().map(DiemDTO::getTenMon).toArray(String[]::new);
        int[] values = new int[cats.length];
        for (int i = 0; i < cats.length; i++) {
            values[i] = ranks.getOrDefault(cats[i], 0);
        }
        
        String title = "Thứ hạng ĐTB theo môn - " + cboHocKy.getSelectedItem();
        return new BarChartCanvas(title, cats, values);
    }
}