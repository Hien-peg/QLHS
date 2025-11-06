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
import com.sgu.qlhs.bus.PhongBUS;
import com.sgu.qlhs.dto.PhongDTO;
import com.sgu.qlhs.ui.components.PieChartCanvas;
import java.util.Collections;
// =========================

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
                        } else { // Admin
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
     * Giao diện cho Admin (ĐÃ NÂNG CẤP)
     */
    private void initAdminView() {
        this.removeAll(); 
        this.setLayout(new BorderLayout(10, 10));
        this.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Khởi tạo các BUS cần thiết
        diemBUS = new DiemBUS();
        hocSinhBUS = new HocSinhBUS();
        lopBUS = new LopBUS();

        JTabbedPane tabbedPane = new JTabbedPane();
        this.add(tabbedPane, BorderLayout.CENTER);

        // Tab 1: Thống kê giới tính
        JPanel pnlGioiTinh = buildAdminGioiTinhTab();
        tabbedPane.addTab("Thống Kê Giới Tính", pnlGioiTinh);

        // Tab 2: Xếp loại học lực theo Khối
        JPanel pnlHocLuc = buildAdminHocLucKhoiTab();
        tabbedPane.addTab("Xếp Loại Học Lực (Theo Khối)", pnlHocLuc);

        // Tab 3: Điểm TB Môn theo Khối
        JPanel pnlPhoDiem = buildAdminPhoDiemMonKhoiTab();
        tabbedPane.addTab("Điểm TB Môn (Theo Khối)", pnlPhoDiem);
        
        this.revalidate();
        this.repaint();
    }

    /**
     * (Admin) Tab 1: Biểu đồ tròn giới tính
     */
    private JPanel buildAdminGioiTinhTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        int nam = 0, nu = 0;
        try {
            List<HocSinhDTO> allHS = hocSinhBUS.getAllHocSinh();
            for (HocSinhDTO hs : allHS) {
                if ("Nam".equalsIgnoreCase(hs.getGioiTinh())) {
                    nam++;
                } else {
                    nu++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        double[] values = {(double) nam, (double) nu};
        String[] cats = {"Nam (" + nam + ")", "Nữ (" + nu + ")"};
        
        panel.add(new PieChartCanvas("Tỉ lệ giới tính toàn trường", values, cats), BorderLayout.CENTER);
        return panel;
    }

    /**
     * (Admin) Tab 2: Panel Xếp loại học lực (GV, K, TB, Y) theo Khối
     */
    private JPanel buildAdminHocLucKhoiTab() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setOpaque(false);

        // Panel chứa 3 biểu đồ
        JPanel chartPanel = new JPanel(new GridLayout(1, 3, 10, 10));
        chartPanel.setOpaque(false);

        // Panel lọc (chọn học kỳ)
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.setOpaque(false);
        JComboBox<String> cboHK = new JComboBox<>(new String[]{"Học kỳ 1", "Học kỳ 2"});
        filterPanel.add(new JLabel("Chọn học kỳ:"));
        filterPanel.add(cboHK);

        mainPanel.add(filterPanel, BorderLayout.NORTH);
        mainPanel.add(chartPanel, BorderLayout.CENTER);

        // Gắn sự kiện
        cboHK.addActionListener(e -> {
            updateHocLucCharts(chartPanel, cboHK.getSelectedIndex() + 1);
        });

        // Tải lần đầu
        updateHocLucCharts(chartPanel, 1);
        return mainPanel;
    }

    /**
     * (Admin) Cập nhật 3 biểu đồ tròn Xếp loại học lực
     */
    private void updateHocLucCharts(JPanel chartPanel, int hocKy) {
        chartPanel.removeAll();
        int maNK = NienKhoaBUS.current();

        List<DiemDTO> allDiem = diemBUS.getDiemFiltered(null, null, hocKy, maNK, null, null);
        List<LopDTO> allLop = lopBUS.getAllLop();
        Map<Integer, Integer> lopToKhoiMap = allLop.stream()
                .collect(Collectors.toMap(LopDTO::getMaLop, LopDTO::getKhoi, (a, b) -> a));

        // Tính TBHK cho từng học sinh
        Map<Integer, List<DiemDTO>> diemTheoHS = allDiem.stream()
                .filter(d -> "TinhDiem".equals(d.getLoaiMon()))
                .collect(Collectors.groupingBy(DiemDTO::getMaHS));

        Map<Integer, Double> tbhkMap = new HashMap<>();
        Map<Integer, Integer> khoiCuaHS = new HashMap<>();

        for (Map.Entry<Integer, List<DiemDTO>> entry : diemTheoHS.entrySet()) {
            int maHS = entry.getKey();
            List<DiemDTO> diemList = entry.getValue();
            if (diemList.isEmpty()) continue;

            double avg = diemList.stream().mapToDouble(DiemDTO::getDiemTB).average().orElse(0.0);
            tbhkMap.put(maHS, avg);
            
            // Lấy khối từ MaLop (giả định hs không chuyển lớp giữa kỳ)
            int maLop = diemList.get(0).getMaLop();
            khoiCuaHS.put(maHS, lopToKhoiMap.getOrDefault(maLop, 0));
        }

        // Đếm kết quả theo Khối
        Map<Integer, long[]> counts = new HashMap<>();
        counts.put(10, new long[4]); // [Giỏi, Khá, TB, Yếu]
        counts.put(11, new long[4]);
        counts.put(12, new long[4]);

        for (Map.Entry<Integer, Double> entry : tbhkMap.entrySet()) {
            int maHS = entry.getKey();
            double tb = entry.getValue();
            int khoi = khoiCuaHS.getOrDefault(maHS, 0);

            long[] khoiCounts = counts.get(khoi);
            if (khoiCounts == null) continue;

            if (tb >= 8.0) khoiCounts[0]++;
            else if (tb >= 6.5) khoiCounts[1]++;
            else if (tb >= 5.0) khoiCounts[2]++;
            else khoiCounts[3]++;
        }

        // Tạo biểu đồ
        chartPanel.add(createHocLucPieChart(counts.get(10), "Khối 10 - HK " + hocKy));
        chartPanel.add(createHocLucPieChart(counts.get(11), "Khối 11 - HK " + hocKy));
        chartPanel.add(createHocLucPieChart(counts.get(12), "Khối 12 - HK " + hocKy));

        chartPanel.revalidate();
        chartPanel.repaint();
    }

    /**
     * (Admin) Helper tạo biểu đồ tròn Xếp loại
     */
    private JComponent createHocLucPieChart(long[] data, String title) {
        if (data == null) return new JPanel();
        double[] values = {(double) data[0], (double) data[1], (double) data[2], (double) data[3]};
        String[] labels = {
            "Giỏi (" + data[0] + ")", 
            "Khá (" + data[1] + ")", 
            "TB (" + data[2] + ")", 
            "Yếu (" + data[3] + ")"
        };
        return new PieChartCanvas(title, values, labels);
    }

    /**
     * (Admin) Tab 3: Panel Phổ điểm TB Môn theo Khối
     */
    private JPanel buildAdminPhoDiemMonKhoiTab() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setOpaque(false);

        // Panel chứa 3 biểu đồ
        JPanel chartPanel = new JPanel(new GridLayout(1, 3, 10, 10));
        chartPanel.setOpaque(false);

        // Panel lọc
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.setOpaque(false);
        JComboBox<String> cboHK = new JComboBox<>(new String[]{"Học kỳ 1", "Học kỳ 2"});
        filterPanel.add(new JLabel("Chọn học kỳ:"));
        filterPanel.add(cboHK);

        mainPanel.add(filterPanel, BorderLayout.NORTH);
        mainPanel.add(chartPanel, BorderLayout.CENTER);

        // Gắn sự kiện
        cboHK.addActionListener(e -> {
            updatePhoDiemCharts(chartPanel, cboHK.getSelectedIndex() + 1);
        });

        // Tải lần đầu
        updatePhoDiemCharts(chartPanel, 1);
        return mainPanel;
    }

    /**
     * (Admin) Cập nhật 3 biểu đồ cột Phổ điểm TB Môn
     */
    private void updatePhoDiemCharts(JPanel chartPanel, int hocKy) {
        chartPanel.removeAll();
        int maNK = NienKhoaBUS.current();

        List<DiemDTO> allDiem = diemBUS.getDiemFiltered(null, null, hocKy, maNK, null, null);
        List<LopDTO> allLop = lopBUS.getAllLop();
        Map<Integer, Integer> lopToKhoiMap = allLop.stream()
                .collect(Collectors.toMap(LopDTO::getMaLop, LopDTO::getKhoi, (a, b) -> a));

        // Gom điểm: Map<Khối, Map<Tên Môn, List<ĐiểmTB>>>
        Map<Integer, Map<String, List<Double>>> diemTheoKhoiTheoMon = new HashMap<>();

        for (DiemDTO d : allDiem) {
            if ("DanhGia".equals(d.getLoaiMon())) continue; // Bỏ qua môn đánh giá

            int khoi = lopToKhoiMap.getOrDefault(d.getMaLop(), 0);
            if (khoi == 0) continue;

            diemTheoKhoiTheoMon
                .computeIfAbsent(khoi, k -> new HashMap<>())
                .computeIfAbsent(d.getTenMon(), k -> new ArrayList<>())
                .add(d.getDiemTB());
        }

        // Tính trung bình: Map<Khối, Map<Tên Môn, ĐiểmTB_Chung>>
        Map<Integer, Map<String, Double>> avgData = new HashMap<>();
        for (Integer khoi : diemTheoKhoiTheoMon.keySet()) {
            Map<String, List<Double>> monMap = diemTheoKhoiTheoMon.get(khoi);
            Map<String, Double> avgMonMap = new HashMap<>();
            for (String tenMon : monMap.keySet()) {
                double avg = monMap.get(tenMon).stream().mapToDouble(val -> val).average().orElse(0.0);
                avgMonMap.put(tenMon, avg);
            }
            avgData.put(khoi, avgMonMap);
        }

        // Tạo biểu đồ
        chartPanel.add(createPhoDiemBarChart(avgData.get(10), "Điểm TB Môn - Khối 10 - HK " + hocKy));
        chartPanel.add(createPhoDiemBarChart(avgData.get(11), "Điểm TB Môn - Khối 11 - HK " + hocKy));
        chartPanel.add(createPhoDiemBarChart(avgData.get(12), "Điểm TB Môn - Khối 12 - HK " + hocKy));

        chartPanel.revalidate();
        chartPanel.repaint();
    }
    
    /**
     * (Admin) Helper tạo biểu đồ cột Phổ điểm
     */
    private JComponent createPhoDiemBarChart(Map<String, Double> data, String title) {
        if (data == null || data.isEmpty()) {
            JPanel p = new JPanel(new GridBagLayout());
            p.setOpaque(false);
            p.add(new JLabel("Không có dữ liệu cho " + title));
            return p;
        }

        // Sắp xếp môn học theo tên
        List<String> sortedKeys = new ArrayList<>(data.keySet());
        Collections.sort(sortedKeys);

        String[] cats = new String[sortedKeys.size()];
        double[] vals = new double[sortedKeys.size()];

        for (int i = 0; i < sortedKeys.size(); i++) {
            cats[i] = sortedKeys.get(i);
            vals[i] = data.get(cats[i]);
        }
        
        return new BarChartCanvas(title, cats, vals);
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
        Object selectedMon = cbo.getSelectedItem();
        cbo.removeAllItems(); 
        
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
        
        if (monTrungKhop != null) {
            cbo.setSelectedItem(monTrungKhop);
        }
        cbo.setEnabled(true);
    }

    /**
     * (GVCN) Tải TẤT CẢ môn học vào combobox
     */
    private void loadAllMonOptions(JComboBox<MonHocDTO> cbo) {
        Object selectedMon = cbo.getSelectedItem();
        cbo.removeAllItems();
        
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
                return;
            }
            
            boolean isHomeroom = (selectedLop.getMaLop() == gvcnInfo.getMaLop());
    
            String loaiTK = (String) cboThongKe.getSelectedItem();
            int hocKy = (cboHocKy.getSelectedIndex() == 0) ? 1 : 2;
            int maNK = NienKhoaBUS.current();
            int maLop = selectedLop.getMaLop();
    
            if (!isHomeroom) {
                if (!"Phổ điểm theo Từng môn".equals(loaiTK)) {
                     cboThongKe.setSelectedItem("Phổ điểm theo Từng môn");
                     loaiTK = "Phổ điểm theo Từng môn"; 
                }
                cboThongKe.setEnabled(false);
                cboMonGVCN.setVisible(true);
                loadTeacherMonOptions(cboMonGVCN); 
            } else {
                cboThongKe.setEnabled(true);
                if ("Phổ điểm theo Từng môn".equals(loaiTK)) {
                     cboMonGVCN.setVisible(true);
                     loadAllMonOptions(cboMonGVCN); 
                } else {
                     cboMonGVCN.setVisible(false);
                }
            }
            
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
        
        int countG = 0, countK = 0, countTB = 0, countY = 0;
        
        for (double diem : dsDiemTBHK) {
            if (diem >= 8.0) { values[0]++; countG++; }
            else if (diem >= 6.5) { values[1]++; countK++; }
            else if (diem >= 5.0) { values[2]++; countTB++; }
            else { values[3]++; countY++; }
        }
        
        labels[0] = "Giỏi (" + countG + ")";
        labels[1] = "Khá (" + countK + ")";
        labels[2] = "TB (" + countTB + ")";
        labels[3] = "Yếu (" + countY + ")";
        
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
        
        String[] cats = {"Đạt (" + dat + ")", "KĐ (" + khongDat + ")"};
        double[] values = {(double)dat, (double)khongDat};
    
        String title = "Thống kê Đạt/KĐ (" + tenMon + ") - " + cboHocKy.getSelectedItem();
        return new BarChartCanvas(title, cats, values);
    }


    /**
     * Giao diện cho Học sinh
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
                "Điểm TB các môn",
                "Xếp hạng TB (Lớp)" // <-- Đổi tên
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
            if (currentHocSinh.getMaLop() != 0) {
                 maLop = currentHocSinh.getMaLop();
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
            // SỬA: Cập nhật else if
            else if ("Xếp hạng TB (Lớp)".equals(loaiTK)) {
                JComponent display = createOverallRankingAndHocLucDisplay(currentMaHS, maLop, hocKy, maNK);
                chartContainer.add(display, cardKey);
                chartCards.show(chartContainer, cardKey);
            }
        } finally {
            isUpdatingChart = false;
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
    
    /**
     * (HS) Tính toán điểm TB học kỳ cho CHỈ MỘT học sinh
     */
    private double tinhDiemTBHKChoMotHS(int maHS, int hocKy, int maNK) {
        List<DiemDTO> diemCuaHS = diemBUS.getDiemByMaHS(maHS, hocKy, maNK, currentUser);
        if (diemCuaHS == null || diemCuaHS.isEmpty()) return 0.0;
        
        double tongDiem = 0;
        int soMon = 0;
        for (DiemDTO d : diemCuaHS) {
            if ("TinhDiem".equals(d.getLoaiMon())) {
                tongDiem += d.getDiemTB();
                soMon++;
            }
        }
        return (soMon > 0) ? (tongDiem / soMon) : 0.0;
    }

    /**
     * (HS) Hiển thị xếp hạng TB Chung VÀ HỌC LỰC
     */
    private JComponent createOverallRankingAndHocLucDisplay(int maHS, int maLop, int hocKy, int maNK) {
        // 1. Lấy điểm TBHK của cả lớp
        List<Double> dsDiemTBHK_Lop = tinhDiemTBHKChoLop(maLop, hocKy, maNK);
        if (dsDiemTBHK_Lop.isEmpty()) {
            return new JLabel("Chưa có dữ liệu của lớp để xếp hạng.", SwingConstants.CENTER);
        }

        // 2. Lấy điểm TBHK của học sinh này
        double myTBHK = tinhDiemTBHKChoMotHS(maHS, hocKy, maNK);

        // 3. THÊM MỚI: Tính Học lực
        String hocLucStr;
        if (myTBHK >= 8.0) hocLucStr = "Giỏi";
        else if (myTBHK >= 6.5) hocLucStr = "Khá";
        else if (myTBHK >= 5.0) hocLucStr = "Trung bình";
        else hocLucStr = "Yếu";
        // ========================

        // 4. Sắp xếp giảm dần
        dsDiemTBHK_Lop.sort(Comparator.reverseOrder());

        // 5. Tìm thứ hạng
        int rank = 0;
        for (int i = 0; i < dsDiemTBHK_Lop.size(); i++) {
            if (Math.abs(dsDiemTBHK_Lop.get(i) - myTBHK) < 0.001) {
                rank = i + 1;
                break;
            }
        }
        if (rank == 0) {
            for (int i = 0; i < dsDiemTBHK_Lop.size(); i++) {
                if (myTBHK >= dsDiemTBHK_Lop.get(i) - 0.001) {
                    rank = i + 1;
                    break;
                }
            }
            if (rank == 0) rank = dsDiemTBHK_Lop.size() + 1;
        }

        int siSo = dsDiemTBHK_Lop.size();

        // 6. Tạo giao diện hiển thị
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        JLabel lblRank = new JLabel(String.format("Hạng: %d / %d (Lớp)", rank, siSo));
        lblRank.setFont(new Font("Arial", Font.BOLD, 36));
        lblRank.setForeground(new Color(29, 35, 66));
        
        // Label Học Lực
        JLabel lblHocLuc = new JLabel("Học lực: " + hocLucStr);
        lblHocLuc.setFont(new Font("Arial", Font.BOLD, 24));
        lblHocLuc.setForeground(new Color(33, 84, 170)); // ICON_FG
        
        JLabel lblScore = new JLabel(String.format("ĐTB Học kỳ: %.2f", myTBHK));
        lblScore.setFont(new Font("Arial", Font.PLAIN, 18));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0; panel.add(lblRank, gbc);
        
        // Thêm Học lực vào GBC
        gbc.gridy = 1; gbc.insets = new Insets(10, 0, 0, 0); panel.add(lblHocLuc, gbc);
        
        gbc.gridy = 2; gbc.insets = new Insets(10, 0, 0, 0); panel.add(lblScore, gbc);
        
        return panel;
    }
}