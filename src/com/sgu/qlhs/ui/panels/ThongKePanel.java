package com.sgu.qlhs.ui.panels;

import com.sgu.qlhs.ui.components.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import static com.sgu.qlhs.ui.MainDashboard.*;
import com.sgu.qlhs.dto.NguoiDungDTO;
import com.sgu.qlhs.ui.MainDashboard;
import com.sgu.qlhs.ui.ChuNhiemDashboard;
import com.sgu.qlhs.dto.ChuNhiemDTO;
import com.sgu.qlhs.bus.DiemBUS;
import com.sgu.qlhs.bus.HocSinhBUS;
import com.sgu.qlhs.bus.NienKhoaBUS;
import com.sgu.qlhs.bus.LopBUS;
import com.sgu.qlhs.bus.PhanCongDayBUS;
import com.sgu.qlhs.bus.MonBUS; 
import com.sgu.qlhs.dto.DiemDTO;
import com.sgu.qlhs.dto.HocSinhDTO;
import com.sgu.qlhs.dto.LopDTO;
import com.sgu.qlhs.dto.MonHocDTO; 
import com.sgu.qlhs.dto.PhanCongDayDTO;
import java.awt.event.HierarchyListener;
import java.awt.event.HierarchyEvent;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays; 
import java.util.Map;
import java.util.HashMap;
import java.util.Comparator;
import java.util.stream.Collectors;

public class ThongKePanel extends JPanel {

    private boolean isStudentView = false;
    private NguoiDungDTO currentUser;
    private int currentMaHS = -1;
    private HocSinhDTO currentHocSinh;

    // === Component dùng chung ===
    private JComboBox<String> cboThongKe;
    private JComboBox<String> cboHocKy;
    private JComboBox<LopDTO> cboLopGV; // Dùng cho cả GVBM và GVCN
    private JComboBox<MonHocDTO> cboMonGVBM; // Chỉ dùng cho GVBM
    private JComboBox<MonHocDTO> cboMonGVCN; // Chỉ dùng cho GVCN
    private JPanel chartContainer;
    private CardLayout chartCards;

    private DiemBUS diemBUS;
    private HocSinhBUS hocSinhBUS;
    private LopBUS lopBUS;
    private PhanCongDayBUS phanCongBUS;
    private List<LopDTO> lopListGV;
    private ChuNhiemDTO gvcnInfo; // Lưu thông tin GVCN
    
    private boolean isUpdatingChart = false;

    // Card keys
    private final String CARD_GVCN_XEPLOAI = "GVCN_XEPLOAI";
    private final String CARD_GVCN_PHODIEM_CHUNG = "GVCN_PHODIEM_CHUNG";
    private final String CARD_GVCN_PHODIEM_MON = "GVCN_PHODIEM_MON"; 
    private final String CARD_GVCN_DANHGIA_MON = "GVCN_DANHGIA_MON"; 
    private final String CARD_GVBM_PHODIEM_MON = "GVBM_PHODIEM_MON";
    private final String CARD_GVBM_DANHGIA = "GVBM_DANHGIA";
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

                    if (currentUser != null) {
                        String vaiTro = currentUser.getVaiTro();
                        if ("hoc_sinh".equalsIgnoreCase(vaiTro)) {
                            isStudentView = true;
                            currentMaHS = currentUser.getId();
                            initStudentView();
                        } else if ("giao_vien".equalsIgnoreCase(vaiTro)) {
                            isStudentView = false;
                            if (md instanceof ChuNhiemDashboard) {
                                gvcnInfo = ((ChuNhiemDashboard) md).getChuNhiemInfo();
                                initChuNhiemView(); // Giao diện GVCN
                            } else {
                                gvcnInfo = null;
                                initTeacherView(); // Giao diện GVBM
                            }
                        } else {
                            isStudentView = false;
                            initAdminView(); // Giao diện cho Admin
                        }
                    } else {
                        isStudentView = false;
                        initAdminView(); // Mặc định
                    }
                    
                    removeHierarchyListener(this);
                }
            }
        });
    }

    /**
     * Giao diện cho Admin
     */
    private void initAdminView() {
        this.removeAll(); 

        var outer = new RoundedPanel(18, CARD_BG, CARD_BORDER);
        outer.setLayout(new BorderLayout());
        var lbl = new JLabel("Thống kê (Chung)");
        lbl.setBorder(new EmptyBorder(12, 16, 8, 16));
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 18f));
        outer.add(lbl, BorderLayout.NORTH);

        String[] cats = { "Nam", "Nữ" };
        double[] values = { 58.0, 42.0 }; 
        var chart = new BarChartCanvas("Tỉ lệ giới tính học sinh", cats, values);
        outer.add(chart, BorderLayout.CENTER);

        this.add(outer, BorderLayout.CENTER);
        this.revalidate();
        this.repaint();
    }

    /**
     * Giao diện mới cho Giáo viên Chủ nhiệm (GVCN)
     */
    private void initChuNhiemView() {
        this.removeAll();

        diemBUS = new DiemBUS();
        hocSinhBUS = new HocSinhBUS();
        lopBUS = new LopBUS();
        phanCongBUS = new PhanCongDayBUS();

        if (gvcnInfo == null) {
            add(new JLabel("Lỗi: Không tìm thấy thông tin chủ nhiệm."));
            return;
        }

        var outer = new RoundedPanel(18, CARD_BG, CARD_BORDER);
        outer.setLayout(new BorderLayout());

        var lbl = new JLabel("Thống kê Giáo viên");
        lbl.setBorder(new EmptyBorder(12, 16, 8, 16));
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 18f));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.setBorder(new EmptyBorder(5, 12, 5, 12));
        
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterPanel.setOpaque(false);

        filterPanel.add(new JLabel("Chọn Lớp:"));
        cboLopGV = new JComboBox<>();
        loadTeacherLopOptions(cboLopGV); 
        for (int i = 0; i < cboLopGV.getItemCount(); i++) {
            if (cboLopGV.getItemAt(i).getMaLop() == gvcnInfo.getMaLop()) {
                cboLopGV.setSelectedIndex(i);
                break;
            }
        }
        filterPanel.add(cboLopGV);
        
        filterPanel.add(new JLabel("Loại thống kê:"));
        cboThongKe = new JComboBox<>(new String[]{
                "Phân loại Học lực (TB Chung)", 
                "Phổ điểm TB Chung (0-10)",
                "Phổ điểm theo Từng môn"
        });
        filterPanel.add(cboThongKe);

        filterPanel.add(new JLabel("Học kỳ:"));
        cboHocKy = new JComboBox<>(new String[]{"Học kỳ 1", "Học kỳ 2"});
        filterPanel.add(cboHocKy);

        filterPanel.add(new JLabel("Chọn môn:"));
        cboMonGVCN = new JComboBox<>();
        loadAllMonOptions(cboMonGVCN); 
        filterPanel.add(cboMonGVCN);

        topPanel.add(lbl, BorderLayout.NORTH);
        topPanel.add(filterPanel, BorderLayout.CENTER);
        outer.add(topPanel, BorderLayout.NORTH);

        chartCards = new CardLayout();
        chartContainer = new JPanel(chartCards);
        chartContainer.setOpaque(false);
        chartContainer.setBorder(new EmptyBorder(10, 10, 10, 10));
        chartContainer.add(new JLabel("Đang tải...", SwingConstants.CENTER), CARD_EMPTY);
        outer.add(chartContainer, BorderLayout.CENTER);
        
        this.add(outer, BorderLayout.CENTER);

        cboLopGV.addActionListener(e -> loadGvcnChart());
        cboThongKe.addActionListener(e -> loadGvcnChart());
        cboHocKy.addActionListener(e -> loadGvcnChart());
        cboMonGVCN.addActionListener(e -> loadGvcnChart()); 
        
        loadGvcnChart();
        
        this.revalidate();
        this.repaint();
    }

    /**
     * Giao diện mới cho Giáo viên Bộ môn (GVBM)
     */
    private void initTeacherView() {
        this.removeAll();

        diemBUS = new DiemBUS();
        hocSinhBUS = new HocSinhBUS();
        lopBUS = new LopBUS();
        phanCongBUS = new PhanCongDayBUS();

        var outer = new RoundedPanel(18, CARD_BG, CARD_BORDER);
        outer.setLayout(new BorderLayout());

        var lbl = new JLabel("Thống kê Môn dạy");
        lbl.setBorder(new EmptyBorder(12, 16, 8, 16));
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 18f));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.setBorder(new EmptyBorder(5, 12, 5, 12));
        
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterPanel.setOpaque(false);

        filterPanel.add(new JLabel("Lớp dạy:"));
        cboLopGV = new JComboBox<>();
        loadTeacherLopOptions(cboLopGV);
        filterPanel.add(cboLopGV);
        
        filterPanel.add(new JLabel("Môn dạy:"));
        cboMonGVBM = new JComboBox<>();
        filterPanel.add(cboMonGVBM);
        
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
        chartContainer.add(new JLabel("Vui lòng chọn lớp và môn để xem thống kê.", SwingConstants.CENTER), CARD_EMPTY);
        outer.add(chartContainer, BorderLayout.CENTER);
        
        this.add(outer, BorderLayout.CENTER);

        cboLopGV.addActionListener(e -> {
            loadTeacherMonOptions(cboMonGVBM); 
            loadGvbmChart(); 
        });
        cboMonGVBM.addActionListener(e -> loadGvbmChart());
        cboHocKy.addActionListener(e -> loadGvbmChart());

        loadTeacherMonOptions(cboMonGVBM);
        loadGvbmChart();
        
        this.revalidate();
        this.repaint();
    }
    
    /**
     * (GVBM/GVCN) Tải danh sách lớp mà giáo viên đang đăng nhập được phân công
     */
    private void loadTeacherLopOptions(JComboBox<LopDTO> cbo) {
        cbo.removeAllItems();
        int maGV = currentUser.getId();
        String namHoc = NienKhoaBUS.currentNamHoc();
        
        List<Integer> maLopList = phanCongBUS.getDistinctMaLopByGiaoVien(maGV, namHoc, null);
        
        if(maLopList == null || maLopList.isEmpty()) {
            cbo.setEnabled(false);
            return;
        }

        lopListGV = new ArrayList<>();
        List<LopDTO> allLops = lopBUS.getAllLop();
        for (Integer maLop : maLopList) {
            for(LopDTO lop : allLops) {
                if(lop.getMaLop() == maLop) {
                    if (!lopListGV.stream().anyMatch(l -> l.getMaLop() == maLop)) {
                         lopListGV.add(lop);
                        cbo.addItem(lop); 
                    }
                    break;
                }
            }
        }
        cbo.setEnabled(true);
    }

    /**
     * (GVBM / GVCN) Tải danh sách MÔN mà GV dạy cho LỚP đã chọn
     */
    private void loadTeacherMonOptions(JComboBox<MonHocDTO> cbo) {
        // === SỬA LỖI: Thêm dòng này ===
        Object selectedMon = cbo.getSelectedItem();
        cbo.removeAllItems(); 
        // =============================
        
        LopDTO selectedLop = (LopDTO) cboLopGV.getSelectedItem();
        if (selectedLop == null) {
            cbo.setEnabled(false);
            return;
        }

        int maGV = currentUser.getId();
        int maLop = selectedLop.getMaLop();
        String namHoc = NienKhoaBUS.currentNamHoc();

        List<PhanCongDayDTO> allPCD = phanCongBUS.getByGV(maGV);
        if (allPCD == null || allPCD.isEmpty()) {
            cbo.setEnabled(false);
            return;
        }

        List<PhanCongDayDTO> pcdHopLe = allPCD.stream()
                .filter(pcd -> pcd.getMaLop() == maLop && namHoc.equals(pcd.getNamHoc()))
                .collect(Collectors.toList());

        if (pcdHopLe.isEmpty()) {
            cbo.setEnabled(false);
            return;
        }

        MonBUS monBUS = new MonBUS(); 
        List<MonHocDTO> allMon = monBUS.getAllMon();
        
        // === SỬA LỖI TRÙNG MÔN ===
        List<Integer> uniqueMonIds = pcdHopLe.stream()
                                    .map(PhanCongDayDTO::getMaMon)
                                    .distinct() 
                                    .collect(Collectors.toList());

        MonHocDTO monTrungKhop = null;
        for (Integer maMon : uniqueMonIds) {
            for (MonHocDTO mon : allMon) {
                if (mon.getMaMon() == maMon) {
                    cbo.addItem(mon);
                    if (selectedMon != null && mon.getMaMon() == ((MonHocDTO)selectedMon).getMaMon()) {
                        monTrungKhop = mon;
                    }
                    break;
                }
            }
        }
        // ==========================
        
        if (monTrungKhop != null) {
            cbo.setSelectedItem(monTrungKhop);
        }
        cbo.setEnabled(true);
    }

    /**
     * (GVCN) Tải TẤT CẢ môn học vào combobox
     */
    private void loadAllMonOptions(JComboBox<MonHocDTO> cbo) {
        // === SỬA LỖI: Thêm dòng này ===
        Object selectedMon = cbo.getSelectedItem();
        cbo.removeAllItems();
        // =============================
        
        MonBUS monBUS = new MonBUS();
        List<MonHocDTO> allMon = monBUS.getAllMon();
        
        if (allMon == null || allMon.isEmpty()) {
            cbo.setEnabled(false);
            return;
        }
        
        MonHocDTO monTrungKhop = null;
        for (MonHocDTO mon : allMon) {
            cbo.addItem(mon);
            if (selectedMon != null && mon.getMaMon() == ((MonHocDTO)selectedMon).getMaMon()) {
                monTrungKhop = mon;
            }
        }
        
        if (monTrungKhop != null) {
            cbo.setSelectedItem(monTrungKhop);
        }
        cbo.setEnabled(true);
    }

    /**
     * (GVCN) Tải biểu đồ cho GVCN
     */
    private void loadGvcnChart() {
        if (isUpdatingChart) return;
        isUpdatingChart = true;
        
        try {
            LopDTO selectedLop = (LopDTO) cboLopGV.getSelectedItem();
            if (selectedLop == null) {
                chartCards.show(chartContainer, CARD_EMPTY);
                isUpdatingChart = false;
                return;
            }
            
            boolean isHomeroom = (selectedLop.getMaLop() == gvcnInfo.getMaLop());
    
            String loaiTK = (String) cboThongKe.getSelectedItem();
            int hocKy = (cboHocKy.getSelectedIndex() == 0) ? 1 : 2;
            int maNK = NienKhoaBUS.current();
            int maLop = selectedLop.getMaLop();
    
            // === SỬA LOGIC HIỂN THỊ ===
            if (!isHomeroom) {
                // Nếu không phải lớp CN, BẮT BUỘC xem "Phổ điểm theo Từng môn"
                if (!"Phổ điểm theo Từng môn".equals(loaiTK)) {
                     cboThongKe.setSelectedItem("Phổ điểm theo Từng môn");
                     // Thoát ra, vì listener của cboThongKe sẽ tự gọi lại hàm này
                     isUpdatingChart = false;
                     return; 
                }
                cboThongKe.setEnabled(false);
                cboMonGVCN.setVisible(true);
                loadTeacherMonOptions(cboMonGVCN); // Tải môn GV dạy ở lớp này
            } else {
                cboThongKe.setEnabled(true);
                if ("Phổ điểm theo Từng môn".equals(loaiTK)) {
                     cboMonGVCN.setVisible(true);
                     loadAllMonOptions(cboMonGVCN); // Tải tất cả các môn
                } else {
                     cboMonGVCN.setVisible(false);
                }
            }
            // =============================
            
            // --- Vẽ biểu đồ ---
            if ("Phân loại Học lực (TB Chung)".equals(loaiTK)) {
                String cardKey = "GVCN_XEPLOAI_" + maLop + "_" + hocKy;
                List<Double> dsDiemTBHK = tinhDiemTBHKChoLop(maLop, hocKy, maNK);
                 if (dsDiemTBHK.isEmpty()) {
                    chartContainer.add(new JLabel("Lớp chưa có dữ liệu điểm học kỳ " + hocKy, SwingConstants.CENTER), cardKey);
                    chartCards.show(chartContainer, cardKey);
                } else {
                    JComponent chart = createHocLucPieChart(dsDiemTBHK);
                    chartContainer.add(chart, cardKey);
                    chartCards.show(chartContainer, cardKey);
                }
    
            } else if ("Phổ điểm TB Chung (0-10)".equals(loaiTK)) {
                String cardKey = "GVCN_PHODIEM_CHUNG_" + maLop + "_" + hocKy;
                List<Double> dsDiemTBHK = tinhDiemTBHKChoLop(maLop, hocKy, maNK);
                 if (dsDiemTBHK.isEmpty()) {
                    chartContainer.add(new JLabel("Lớp chưa có dữ liệu điểm học kỳ " + hocKy, SwingConstants.CENTER), cardKey);
                    chartCards.show(chartContainer, cardKey);
                } else {
                    JComponent chart = createPhoDiemTBChungChart(dsDiemTBHK);
                    chartContainer.add(chart, cardKey);
                    chartCards.show(chartContainer, cardKey);
                }
    
            } else if ("Phổ điểm theo Từng môn".equals(loaiTK)) {
                MonHocDTO selectedMon = (MonHocDTO) cboMonGVCN.getSelectedItem();
                if (selectedMon == null) {
                    chartContainer.add(new JLabel("Lớp này không có môn nào được phân công.", SwingConstants.CENTER), CARD_EMPTY);
                    chartCards.show(chartContainer, CARD_EMPTY);
                } else {
                    int maMon = selectedMon.getMaMon();
                    String cardKey = "GVCN_MON_" + maLop + "_" + maMon + "_" + hocKy;
        
                    if ("DanhGia".equals(selectedMon.getLoaiMon())) {
                        JComponent chart = createDanhGiaBarChart(maLop, maMon, hocKy, maNK, selectedMon.getTenMon());
                        chartContainer.add(chart, cardKey);
                        chartCards.show(chartContainer, cardKey);
                    } else {
                        JComponent chart = createPhoDiemMonBarChart(maLop, maMon, hocKy, maNK, selectedMon.getTenMon());
                        chartContainer.add(chart, cardKey);
                        chartCards.show(chartContainer, cardKey);
                    }
                }
            }
        } finally {
            isUpdatingChart = false;
        }
    }

    /**
     * (GVBM) Tải biểu đồ Phổ điểm MÔN HỌC
     */
    private void loadGvbmChart() {
        if (isUpdatingChart) return;
        isUpdatingChart = true;
        
        try {
            LopDTO selectedLop = (LopDTO) cboLopGV.getSelectedItem();
            MonHocDTO selectedMon = (MonHocDTO) cboMonGVBM.getSelectedItem();
            
            if (selectedLop == null || selectedMon == null) {
                chartCards.show(chartContainer, CARD_EMPTY);
                isUpdatingChart = false;
                return;
            }
            
            int hocKy = (cboHocKy.getSelectedIndex() == 0) ? 1 : 2;
            int maNK = NienKhoaBUS.current();
            int maLop = selectedLop.getMaLop();
            int maMon = selectedMon.getMaMon();
            String cardKey = "GVBM_" + maLop + "_" + maMon + "_" + hocKy;
    
            if ("DanhGia".equals(selectedMon.getLoaiMon())) {
                JComponent chart = createDanhGiaBarChart(maLop, maMon, hocKy, maNK, selectedMon.getTenMon()); 
                chartContainer.add(chart, cardKey);
                chartCards.show(chartContainer, cardKey);
            } else {
                JComponent chart = createPhoDiemMonBarChart(maLop, maMon, hocKy, maNK, selectedMon.getTenMon());
                chartContainer.add(chart, cardKey);
                chartCards.show(chartContainer, cardKey);
            }
        } finally {
            isUpdatingChart = false;
        }
    }

    /**
     * (GVCN/GVBM) Tính toán danh sách điểm TB học kỳ cho 1 lớp
     */
    private List<Double> tinhDiemTBHKChoLop(int maLop, int hocKy, int maNK) {
        List<Double> dsDiemTBHK = new ArrayList<>();
        List<HocSinhDTO> dsHS = hocSinhBUS.getHocSinhByMaLop(maLop);
        if (dsHS == null || dsHS.isEmpty()) {
            return dsDiemTBHK;
        }

        List<DiemDTO> allScoresInClass = diemBUS.getDiemFiltered(maLop, null, hocKy, maNK, null, null);

        Map<Integer, List<DiemDTO>> diemTheoHS = allScoresInClass.stream()
                .collect(Collectors.groupingBy(DiemDTO::getMaHS));

        for (HocSinhDTO hs : dsHS) {
            List<DiemDTO> diemCuaHS = diemTheoHS.get(hs.getMaHS());
            if (diemCuaHS == null || diemCuaHS.isEmpty()) {
                continue;
            }

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
     * (GVCN) Tạo biểu đồ tròn Phân loại học lực
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
     * (GVCN) Tạo biểu đồ cột Phổ điểm TB HỌC KỲ
     */
    private JComponent createPhoDiemTBChungChart(List<Double> dsDiemTBHK) {
        int[] bins = new int[10]; 
        String[] cats = {"0-1", "1-2", "2-3", "3-4", "4-5", "5-6", "6-7", "7-8", "8-9", "9-10"};

        for (double diem : dsDiemTBHK) {
            if (diem >= 10.0) {
                bins[9]++; 
            } else if (diem >= 0) {
                bins[(int)diem]++;
            }
        }

        double[] doubleBins = Arrays.stream(bins).asDoubleStream().toArray();
        String title = "Phổ điểm TB Học kỳ - " + cboHocKy.getSelectedItem();
        return new BarChartCanvas(title, cats, doubleBins);
    }
    

    /**
     * (GVBM / GVCN) Tạo biểu đồ cột Phổ điểm MÔN HỌC
     */
    private JComponent createPhoDiemMonBarChart(int maLop, int maMon, int hocKy, int maNK, String tenMon) {
        List<DiemDTO> diemList = diemBUS.getDiemFiltered(maLop, maMon, hocKy, maNK, null, null);

        int[] bins = new int[10]; 
        String[] cats = {"0-1", "1-2", "2-3", "3-4", "4-5", "5-6", "6-7", "7-8", "8-9", "9-10"};

        if(diemList != null && !diemList.isEmpty()) {
             for (DiemDTO diem : diemList) {
                if ("TinhDiem".equals(diem.getLoaiMon())) {
                    double diemTBMon = diem.getDiemTB();
                    if (diemTBMon >= 10.0) {
                        bins[9]++; 
                    } else if (diemTBMon >= 0) {
                        bins[(int)diemTBMon]++;
                    }
                }
            }
        }
       
        double[] doubleBins = Arrays.stream(bins).asDoubleStream().toArray();
        String title = "Phổ điểm Môn (" + tenMon + ") - " + cboHocKy.getSelectedItem();
        return new BarChartCanvas(title, cats, doubleBins);
    }

    /**
     * (GVBM / GVCN) Tạo biểu đồ cột Đạt/Không Đạt
     */
    private JComponent createDanhGiaBarChart(int maLop, int maMon, int hocKy, int maNK, String tenMon) {
        List<DiemDTO> diemList = diemBUS.getDiemFiltered(maLop, maMon, hocKy, maNK, null, null);
    
        int dat = 0;
        int khongDat = 0;
    
        if (diemList != null && !diemList.isEmpty()) {
            for (DiemDTO diem : diemList) {
                if ("DanhGia".equals(diem.getLoaiMon())) {
                    if ("Đ".equals(diem.getKetQuaDanhGia())) {
                        dat++;
                    } else if ("KĐ".equals(diem.getKetQuaDanhGia())) {
                        khongDat++;
                    }
                }
            }
        }
        
        String[] cats = {"Đạt", "Không Đạt"};
        double[] values = {(double)dat, (double)khongDat};
    
        String title = "Thống kê Đạt/KĐ (" + tenMon + ") - " + cboHocKy.getSelectedItem();
        return new BarChartCanvas(title, cats, values);
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
        if (isUpdatingChart) return;
        isUpdatingChart = true;
        
        try {
            if (!isStudentView || diemBUS == null || currentHocSinh == null) {
                chartCards.show(chartContainer, CARD_EMPTY);
                isUpdatingChart = false;
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
                isUpdatingChart = false;
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
        } finally {
            isUpdatingChart = false;
        }
    }

    /**
     * (HS) Biểu đồ 1: Điểm TB cá nhân (ĐÃ SỬA LỖI)
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
        
        double[] values = scoresTinhDiem.stream().mapToDouble(DiemDTO::getDiemTB).toArray();
        
        String title = "Điểm TB các môn - " + cboHocKy.getSelectedItem();
        
        return new BarChartCanvas(title, cats, values);
    }

    /**
     * (HS) Biểu đồ 2: Thứ hạng (ĐÃ SỬA LỖI)
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
        double[] values = new double[cats.length];
        for (int i = 0; i < cats.length; i++) {
            values[i] = (double) ranks.getOrDefault(cats[i], 0);
        }
        
        String title = "Thứ hạng ĐTB theo môn - " + cboHocKy.getSelectedItem();
        return new BarChartCanvas(title, cats, values);
    }
}