package com.sgu.qlhs.ui.panels;

import com.sgu.qlhs.ui.components.RoundedPanel;
import com.sgu.qlhs.bus.DiemBUS;
import com.sgu.qlhs.bus.LopBUS;
import com.sgu.qlhs.bus.MonBUS;
import com.sgu.qlhs.bus.NienKhoaBUS;
import com.sgu.qlhs.bus.HanhKiemBUS;
import com.sgu.qlhs.bus.PhanCongDayBUS;
import com.sgu.qlhs.bus.ChuNhiemBUS;
import com.sgu.qlhs.dto.ChuNhiemDTO;
import com.sgu.qlhs.dto.NguoiDungDTO;
import com.sgu.qlhs.dto.PhanCongDayDTO;
import com.sgu.qlhs.bus.HocSinhBUS;
import com.sgu.qlhs.dto.DiemDTO;
import com.sgu.qlhs.dto.HocSinhDTO;
import com.sgu.qlhs.dto.LopDTO;
import com.sgu.qlhs.dto.MonHocDTO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.text.Collator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.awt.*;
import java.util.List;
import java.util.Locale;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;
import static com.sgu.qlhs.ui.MainDashboard.*;

public class DiemPanel extends JPanel {
    private final DiemBUS diemBUS = new DiemBUS();
    private final LopBUS lopBUS = new LopBUS();
    private final MonBUS monBUS = new MonBUS();
    private final JComboBox<String> cboLop = new JComboBox<>();
    private final JComboBox<String> cboMon = new JComboBox<>();
    private final JComboBox<String> cboHK = new JComboBox<>(new String[] { "-- Tất cả --", "HK1", "HK2" });
    private final JTextField txtSearch = new JTextField(20);
    private final DefaultTableModel model;
    private final JTable table;
    private final TableRowSorter<DefaultTableModel> sorter;
    // Tabbed UI: if user is chu nhiem we show two tabs
    private final JTabbedPane tabbedPane = new JTabbedPane();
    // Chủ nhiệm panel/table (read-only view of whole class)
    private DefaultTableModel modelCN;
    private JTable tableCN;
    private TableRowSorter<DefaultTableModel> sorterCN;
    private final JTextField txtSearchCN = new JTextField(18);
    private final JComboBox<String> cboHKCN = new JComboBox<>(new String[] { "-- Tất cả --", "HK1", "HK2" });
    // Subject filter for the Chủ nhiệm tab
    private final JComboBox<String> cboMonCN = new JComboBox<>();
    private java.util.List<com.sgu.qlhs.dto.DiemDTO> currentRowsCN = new java.util.ArrayList<>();
    private boolean isChuNhiem = false;
    private int chuNhiemMaLop = -1;
    private final HanhKiemBUS hanhKiemBUS = new HanhKiemBUS();
    private final PhanCongDayBUS phanCongBUS = new PhanCongDayBUS();
    private final HocSinhBUS hocSinhBUS = new HocSinhBUS();
    private List<LopDTO> lopList;
    private List<MonHocDTO> monList;
    // student view flags
    private boolean isStudentView = false;
    private int currentStudentMaHS = -1;
    // whether we've resolved the logged-in user context (run once when panel is
    // attached)
    private boolean userContextResolved = false;
    // keep last fetched rows so popup actions can map table rows to DTOs
    private java.util.List<com.sgu.qlhs.dto.DiemDTO> currentRows = new java.util.ArrayList<>();
    // pagination
    private int pageSize = 100;
    private int currentPage = 0; // zero-based
    private final JButton btnPrev = new JButton("← Trước");
    private final JButton btnNext = new JButton("Tiếp →");
    private final JLabel lblPageInfo = new JLabel("Trang 1");

    public DiemPanel() {
        setLayout(new BorderLayout());
        setOpaque(false);

        var outer = new RoundedPanel(18, CARD_BG, CARD_BORDER);
        outer.setLayout(new BorderLayout());

        var lbl = new JLabel("Điểm");
        lbl.setBorder(new EmptyBorder(12, 16, 8, 16));
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 18f));
        outer.add(lbl, BorderLayout.NORTH);

        // Top filter bar
        var filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        filterBar.add(new JLabel("Lớp:"));
        filterBar.add(cboLop);
        filterBar.add(new JLabel("Môn:"));
        filterBar.add(cboMon);
        filterBar.add(new JLabel("Học kỳ:"));
        filterBar.add(cboHK);
        filterBar.add(new JLabel("Tìm:"));
        filterBar.add(txtSearch);
        var btnFilter = new JButton("Lọc");
        var btnRefresh = new JButton("Làm mới");
        // button to open detailed grade report dialog
        var btnDetail = new JButton("Bảng điểm chi tiết");
        filterBar.add(btnFilter);
        filterBar.add(btnRefresh);
        filterBar.add(Box.createHorizontalStrut(8));
        filterBar.add(btnDetail);
        outer.add(filterBar, BorderLayout.PAGE_START);

        // Table model and table
        // Thêm cột "Kết quả" (sau Cuối kỳ)
        model = new DefaultTableModel(
                new Object[] { "MaDiem", "Mã HS", "Họ tên", "Lớp", "Môn", "HK", "Miệng", "15p", "Giữa kỳ", "Cuối kỳ",
                        "Kết quả", "Hạnh kiểm" },
                0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                // ensure numeric columns sort numerically
                switch (columnIndex) {
                    case 0: // MaDiem (hidden PK)
                    case 1: // Mã HS
                        return Integer.class;
                    case 5: // HK
                        return Integer.class;
                    case 6: // Miệng
                    case 7: // 15p
                    case 8: // Giữa kỳ
                    case 9: // Cuối kỳ
                        return Double.class;
                    case 10: // Kết quả (MỚI)
                        return Object.class; // Có thể là Double (TB) hoặc String (Đ/KĐ)
                    default:
                        return String.class;
                }
            }
        };
        table = new JTable(model);
        table.setAutoCreateRowSorter(true);
        table.setRowHeight(28);
        table.getTableHeader().setFont(table.getTableHeader().getFont().deriveFont(Font.BOLD));
        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        Collator collator = Collator.getInstance(Locale.forLanguageTag("vi-VN"));
        sorter.setComparator(2, (o1, o2) -> {
            String s1 = o1 == null ? "" : o1.toString().trim();
            String s2 = o2 == null ? "" : o2.toString().trim();
            String k1 = lastToken(s1).toLowerCase(Locale.forLanguageTag("vi-VN"));
            String k2 = lastToken(s2).toLowerCase(Locale.forLanguageTag("vi-VN"));
            int c = collator.compare(k1, k2);
            if (c != 0)
                return c;
            return collator.compare(s1, s2);
        });

        Pattern classPattern = Pattern.compile("^(\\d+)([^\\d]*?)(\\d*)$");
        sorter.setComparator(3, (o1, o2) -> {
            String s1 = o1 == null ? "" : o1.toString().trim();
            String s2 = o2 == null ? "" : o2.toString().trim();
            String t1 = s1.replaceAll("\\s+", "");
            String t2 = s2.replaceAll("\\s+", "");
            Matcher m1 = classPattern.matcher(t1);
            Matcher m2 = classPattern.matcher(t2);
            int grade1 = 0, grade2 = 0, idx1 = 0, idx2 = 0;
            String grp1 = "", grp2 = "";
            if (m1.matches()) {
                try {
                    grade1 = Integer.parseInt(m1.group(1));
                } catch (Exception ex) {
                    grade1 = 0;
                }
                grp1 = m1.group(2) == null ? "" : m1.group(2);
                try {
                    idx1 = (m1.group(3) == null || m1.group(3).isEmpty()) ? 0 : Integer.parseInt(m1.group(3));
                } catch (Exception ex) {
                    idx1 = 0;
                }
            }
            if (m2.matches()) {
                try {
                    grade2 = Integer.parseInt(m2.group(1));
                } catch (Exception ex) {
                    grade2 = 0;
                }
                grp2 = m2.group(2) == null ? "" : m2.group(2);
                try {
                    idx2 = (m2.group(3) == null || m2.group(3).isEmpty()) ? 0 : Integer.parseInt(m2.group(3));
                } catch (Exception ex) {
                    idx2 = 0;
                }
            }
            if (grade1 != grade2)
                return Integer.compare(grade1, grade2);
            int c = collator.compare(grp1.toLowerCase(Locale.forLanguageTag("vi-VN")),
                    grp2.toLowerCase(Locale.forLanguageTag("vi-VN")));
            if (c != 0)
                return c;
            return Integer.compare(idx1, idx2);
        });

        outer.add(new JScrollPane(table), BorderLayout.CENTER);

        javax.swing.SwingUtilities.invokeLater(() -> {
            if (table.getColumnModel().getColumnCount() > 0) {
                try {
                    table.removeColumn(table.getColumnModel().getColumn(0));
                } catch (Exception ex) {

                }
            }
        });

        var pager = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 8));
        pager.add(btnPrev);
        pager.add(lblPageInfo);
        pager.add(btnNext);
        btnPrev.setEnabled(false);
        btnNext.setEnabled(false);
        // Hide pagination controls by default — show all rows like the HocSinh panel
        pager.setVisible(false);
        outer.add(pager, BorderLayout.SOUTH);

        tabbedPane.addTab("Điểm bộ môn", outer);
        add(tabbedPane, BorderLayout.CENTER);

        createChuNhiemPanel();

        // Load filter options for the môn tab
        loadLopOptions();
        loadMonOptions();

        // initial data for môn tab
        loadData();

        // Actions
        btnFilter.addActionListener(e -> {
            currentPage = 0;
            loadData();
        });
        btnRefresh.addActionListener(e -> {
            txtSearch.setText("");
            cboLop.setSelectedIndex(0);
            cboMon.setSelectedIndex(0);
            cboHK.setSelectedIndex(0);
            currentPage = 0;
            loadData();
        });

        btnDetail.addActionListener(e -> {
            java.awt.Window w = null;
            try {
                w = javax.swing.SwingUtilities.getWindowAncestor(this);
            } catch (Exception ex) {
            }
            int targetMaHS = -1;
            if (isStudentView && currentStudentMaHS > 0) {
                targetMaHS = currentStudentMaHS;
            } else {
                int sel = table.getSelectedRow();
                if (sel >= 0) {
                    int modelRow = table.convertRowIndexToModel(sel);
                    if (modelRow >= 0 && modelRow < currentRows.size()) {
                        targetMaHS = currentRows.get(modelRow).getMaHS();
                    }
                }
            }

            if (targetMaHS <= 0) {
                JOptionPane.showMessageDialog(this,
                        "Vui lòng chọn 1 hàng học sinh trong bảng để xem bảng điểm chi tiết, hoặc đăng nhập bằng tài khoản học sinh để xem bảng điểm của chính mình.",
                        "Chú ý", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            com.sgu.qlhs.ui.dialogs.BangDiemChiTietDialog dlg = new com.sgu.qlhs.ui.dialogs.BangDiemChiTietDialog(w);
            try {
                try {
                    java.awt.Window ww = javax.swing.SwingUtilities.getWindowAncestor(this);
                    if (ww instanceof com.sgu.qlhs.ui.MainDashboard) {
                        com.sgu.qlhs.ui.MainDashboard md = (com.sgu.qlhs.ui.MainDashboard) ww;
                        NguoiDungDTO ndLocal = md.getNguoiDung();
                        if (ndLocal != null) {
                            dlg.setInjectedNguoiDung(ndLocal);
                        }
                    }
                } catch (Exception ex2) {

                }

                dlg.setInitialMaHS(targetMaHS);
            } catch (Exception ex) {

            }

            dlg.setVisible(true); // <-- Dòng này block cho đến khi dialog đóng

            // Sau khi dialog chi tiết đóng, làm mới lại dữ liệu của DiemPanel
            refreshData();
        });

        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                applyTextFilter();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                applyTextFilter();
            }

            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                applyTextFilter();
            }
        });

        this.addHierarchyListener(evt -> {
            if (userContextResolved)
                return;
            java.awt.Window w = javax.swing.SwingUtilities.getWindowAncestor(this);
            if (w instanceof com.sgu.qlhs.ui.MainDashboard) {
                com.sgu.qlhs.ui.MainDashboard md = (com.sgu.qlhs.ui.MainDashboard) w;
                NguoiDungDTO nd = md.getNguoiDung();
                try {
                    if (nd != null && "hoc_sinh".equalsIgnoreCase(nd.getVaiTro())) {
                        isStudentView = true;
                        currentStudentMaHS = nd.getId();
                        HocSinhDTO hs = hocSinhBUS.getHocSinhByMaHS(currentStudentMaHS);
                        String tenLop = hs != null && hs.getTenLop() != null ? hs.getTenLop() : "-- Tất cả --";
                        cboLop.removeAllItems();
                        cboLop.addItem(tenLop);
                        cboLop.setSelectedIndex(0);
                        cboLop.setEnabled(false);
                        cboMon.setEnabled(false);
                        txtSearch.setEnabled(false);
                        table.setComponentPopupMenu(null);
                    }

                    try {
                        if (nd != null && "giao_vien".equalsIgnoreCase(nd.getVaiTro())) {
                            ChuNhiemBUS cnBUS = new ChuNhiemBUS();
                            ChuNhiemDTO cn = cnBUS.getChuNhiemByGV(nd.getId());
                            if (cn != null && cn.getMaLop() > 0) {
                                isChuNhiem = true;
                                chuNhiemMaLop = cn.getMaLop();
                                if (tabbedPane.indexOfTab("Lớp chủ nhiệm") == -1) {
                                    tabbedPane.addTab("Lớp chủ nhiệm", buildChuNhiemScrollPane());
                                }
                                loadChuNhiemData();
                            }
                        }
                    } catch (Exception ex) {

                    }
                } catch (Exception ex) {

                }

                loadLopOptions();
                loadMonOptions();
                currentPage = 0;
                loadData();
                userContextResolved = true;
            }
        });

        btnPrev.addActionListener(e -> {
            if (currentPage > 0) {
                currentPage--;
                loadData();
            }
        });
        btnNext.addActionListener(e -> {
            currentPage++;
            loadData();
        });
    }

    private void loadLopOptions() {
        cboLop.removeAllItems();
        // SỬA: Thêm "-- Tất cả --" cho mọi vai trò (kể cả giáo viên)
        cboLop.addItem("-- Tất cả --");

        try {
            java.awt.Window w = javax.swing.SwingUtilities.getWindowAncestor(this);
            if (w instanceof com.sgu.qlhs.ui.MainDashboard) {
                com.sgu.qlhs.ui.MainDashboard md = (com.sgu.qlhs.ui.MainDashboard) w;
                NguoiDungDTO nd = md.getNguoiDung();
                if (nd != null && "giao_vien".equalsIgnoreCase(nd.getVaiTro())) {

                    int maNK = NienKhoaBUS.current();
                    String namHoc = (new NienKhoaBUS()).getNamHocString(maNK); // Lấy chuỗi năm học
                    int hkIdx = cboHK.getSelectedIndex();
                    // Chuyển Integer (1) sang String ("HK1")
                    String hkParam = hkIdx > 0 ? ("HK" + hkIdx) : null;

                    java.util.List<Integer> lopIds = phanCongBUS.getDistinctMaLopByGiaoVien(nd.getId(), namHoc,
                            hkParam);

                    lopList = new java.util.ArrayList<>();
                    for (Integer ml : lopIds) {
                        var l = lopBUS.getLopByMa(ml);
                        if (l != null) {
                            lopList.add(l);
                            cboLop.addItem(l.getTenLop());
                        }
                    }
                    if (cboLop.getItemCount() > 0)
                        cboLop.setSelectedIndex(0);
                    return;
                }
                if (nd != null && "hoc_sinh".equalsIgnoreCase(nd.getVaiTro())) {
                    isStudentView = true;
                    currentStudentMaHS = nd.getId();
                    HocSinhDTO hs = hocSinhBUS.getHocSinhByMaHS(currentStudentMaHS);
                    String tenLop = hs != null && hs.getTenLop() != null ? hs.getTenLop() : "-- Lớp --"; // Sửa
                    cboLop.removeAllItems(); // Học sinh không có "Tất cả"
                    cboLop.addItem(tenLop);
                    cboLop.setSelectedIndex(0);
                    cboLop.setEnabled(false);
                    cboMon.setEnabled(false);
                    txtSearch.setEnabled(false);

                    // Cần load lopList cho học sinh
                    lopList = new ArrayList<>();
                    if (hs != null) {
                        LopDTO lopCuaHS = lopBUS.getLopByMa(hs.getMaLop());
                        if (lopCuaHS != null) {
                            lopList.add(lopCuaHS);
                        }
                    }
                    return;
                }
            }
        } catch (Exception ex) {
            System.err.println("Lỗi lấy lớp theo giáo viên: " + ex.getMessage());
        }

        // fallback: (Admin) show all classes
        lopList = lopBUS.getAllLop();
        for (LopDTO l : lopList) {
            cboLop.addItem(l.getTenLop());
        }
    }

    private void loadMonOptions() {
        cboMon.removeAllItems();
        cboMon.addItem("-- Tất cả --");
        try {
            java.awt.Window w = javax.swing.SwingUtilities.getWindowAncestor(this);
            if (w instanceof com.sgu.qlhs.ui.MainDashboard) {
                com.sgu.qlhs.ui.MainDashboard md = (com.sgu.qlhs.ui.MainDashboard) w;
                NguoiDungDTO nd = md.getNguoiDung();
                if (nd != null && "giao_vien".equalsIgnoreCase(nd.getVaiTro())) {

                    int maNK = NienKhoaBUS.current();
                    String namHoc = (new NienKhoaBUS()).getNamHocString(maNK); // Lấy chuỗi năm học
                    int hkIdx = cboHK.getSelectedIndex();
                    // Chuyển Integer (1) sang String ("HK1")
                    String hkParam = hkIdx > 0 ? ("HK" + hkIdx) : null;

                    java.util.List<Integer> monIds = phanCongBUS.getDistinctMaMonByGiaoVien(nd.getId(), namHoc,
                            hkParam);

                    monList = new java.util.ArrayList<>();
                    java.util.List<MonHocDTO> allMons = monBUS.getAllMon();
                    for (Integer mm : monIds) {
                        for (MonHocDTO m : allMons) {
                            if (m.getMaMon() == mm) {
                                monList.add(m);
                                cboMon.addItem(m.getTenMon());
                                break;
                            }
                        }
                    }
                    if (cboMon.getItemCount() > 0)
                        cboMon.setSelectedIndex(0);
                    return;
                }
                if (nd != null && "hoc_sinh".equalsIgnoreCase(nd.getVaiTro())) {
                    cboMon.removeAllItems();
                    cboMon.addItem("-- Tất cả --");
                    cboMon.setSelectedIndex(0);
                    cboMon.setEnabled(false);
                    monList = monBUS.getAllMon();
                    return;
                }
            }
        } catch (Exception ex) {
            System.err.println("Lỗi lấy môn theo phân công: " + ex.getMessage());
        }

        // fallback: show all subjects
        monList = monBUS.getAllMon();
        for (MonHocDTO m : monList) {
            cboMon.addItem(m.getTenMon());
        }
    }

    private void loadData() {
        model.setRowCount(0);
        int maNK = NienKhoaBUS.current();
        String namHoc = NienKhoaBUS.currentNamHoc(); // Lấy năm học string

        // resolve current user (once) so both student and teacher branches can use it
        NguoiDungDTO nd = null;
        try {
            java.awt.Window w = javax.swing.SwingUtilities.getWindowAncestor(this);
            if (w instanceof com.sgu.qlhs.ui.MainDashboard) {
                com.sgu.qlhs.ui.MainDashboard md = (com.sgu.qlhs.ui.MainDashboard) w;
                nd = md.getNguoiDung();
            }
        } catch (Exception ex) {
            // ignore
        }

        // Server-side filtered fetch
        Integer selectedMaLop = null;
        Integer selectedMaMon = null;
        Integer selectedHocKy = null; // 1, 2, or null (all)
        int lopIdx = cboLop.getSelectedIndex();

        if (!isStudentView && lopIdx > 0 && lopIdx - 1 < lopList.size()) {
            selectedMaLop = lopList.get(lopIdx - 1).getMaLop();
        } else if (isStudentView && lopIdx >= 0 && lopIdx < lopList.size()) {
            // Học sinh chỉ có 1 lớp, index 0
            selectedMaLop = lopList.get(lopIdx).getMaLop();
        }

        int monIdx = cboMon.getSelectedIndex();
        if (monIdx > 0 && monList != null && monIdx - 1 < monList.size()) { // Sửa: monIdx - 1
            selectedMaMon = monList.get(monIdx - 1).getMaMon();
        }
        int hkIdx = cboHK.getSelectedIndex();
        if (hkIdx > 0) {
            selectedHocKy = hkIdx; // 1 or 2
        }

        // load all matching rows (no pagination). This makes search find any row
        // without requiring the user to navigate pages.
        List<com.sgu.qlhs.dto.DiemDTO> rows;
        boolean hasNext = false;

        if (isStudentView) {
            // student view: only fetch this student's records (may be both HK1/HK2)
            rows = new java.util.ArrayList<>();
            if (selectedHocKy != null && selectedHocKy > 0) {
                rows.addAll(diemBUS.getDiemByMaHS(currentStudentMaHS, selectedHocKy, maNK, nd));
            } else {
                rows.addAll(diemBUS.getDiemByMaHS(currentStudentMaHS, 1, maNK, nd));
                rows.addAll(diemBUS.getDiemByMaHS(currentStudentMaHS, 2, maNK, nd));
            }

            // filter by subject if selected
            if (selectedMaMon != null) {
                // SỬA LỖI: Dùng .equals() để so sánh Integer và int
                final Integer fSelectedMaMon = selectedMaMon; // Biến final cho lambda
                rows = rows.stream().filter(d -> fSelectedMaMon.equals(d.getMaMon())).collect(Collectors.toList());
            }
            hasNext = false;

            // ===============================================
            // === LOGIC MỚI CHO GIÁO VIÊN BỘ MÔN (GVBM) ===
            // ===============================================
        } else if (nd != null && "giao_vien".equalsIgnoreCase(nd.getVaiTro())) {

            int maGV = nd.getId();

            // 1. Lấy TẤT CẢ phân công của GV này trong năm học
            List<PhanCongDayDTO> allAssignments = phanCongBUS.getByGV(maGV).stream()
                    .filter(p -> p.getNamHoc().equals(namHoc))
                    .collect(Collectors.toList());

            // 2. Lọc danh sách phân công dựa trên CBB
            final Integer fSelectedMaLop = selectedMaLop;
            final Integer fSelectedMaMon = selectedMaMon;
            final Integer fSelectedHocKy = selectedHocKy;

            List<PhanCongDayDTO> assignmentsToDisplay = allAssignments.stream()
                    // SỬA LỖI: Dùng .equals() để so sánh Integer và int
                    .filter(p -> fSelectedMaLop == null || fSelectedMaLop.equals(p.getMaLop()))
                    .filter(p -> fSelectedMaMon == null || fSelectedMaMon.equals(p.getMaMon()))
                    .filter(p -> fSelectedHocKy == null || p.getHocKy().equals("HK" + fSelectedHocKy))
                    .collect(Collectors.toList());

            // 3. Lấy TẤT CẢ học sinh cho các lớp trong ds phân công ĐÃ LỌC
            Map<Integer, List<HocSinhDTO>> studentsByClass = new HashMap<>();
            Set<Integer> maLopsToFetch = assignmentsToDisplay.stream()
                    .map(PhanCongDayDTO::getMaLop)
                    .collect(Collectors.toSet());
            for (int maLop : maLopsToFetch) {
                studentsByClass.put(maLop, hocSinhBUS.getHocSinhByMaLop(maLop));
            }

            // 4. Lấy TẤT CẢ điểm liên quan (cho các lớp này)
            List<Integer> maLopListForQuery = new ArrayList<>(maLopsToFetch);
            List<DiemDTO> allScores = diemBUS.getDiemFilteredByMaLopList(maLopListForQuery, null, selectedHocKy, maNK,
                    null, null);

            // 5. Build Score Map (Key: "MaHS_MaMon_HocKy")
            Map<String, DiemDTO> scoreMap = new HashMap<>();
            for (DiemDTO d : allScores) {
                scoreMap.put(d.getMaHS() + "_" + d.getMaMon() + "_" + d.getHocKy(), d);
            }

            // 6. Build MonHoc Map (Key: MaMon)
            Map<Integer, MonHocDTO> monMap = new HashMap<>();
            for (MonHocDTO mon : monBUS.getAllMon()) {
                monMap.put(mon.getMaMon(), mon);
            }

            // 7. Xây dựng danh sách rows (gồm cả placeholder)
            List<DiemDTO> finalRows = new ArrayList<>();
            Set<String> uniqueEntries = new HashSet<>();

            for (PhanCongDayDTO pcd : assignmentsToDisplay) {
                int maLop = pcd.getMaLop();
                int maMon = pcd.getMaMon();
                int hk = pcd.getHocKy().equals("HK1") ? 1 : 2;

                MonHocDTO mon = monMap.get(maMon);
                if (mon == null)
                    continue; // Bỏ qua nếu không tìm thấy môn

                List<HocSinhDTO> studentsInClass = studentsByClass.get(maLop);
                if (studentsInClass == null)
                    continue; // Bỏ qua nếu lớp không có học sinh

                for (HocSinhDTO hs : studentsInClass) {
                    String uniqueKey = hs.getMaHS() + "_" + maMon + "_" + hk;
                    if (!uniqueEntries.add(uniqueKey))
                        continue; // Đã thêm (do GV dạy 2 lần 1 môn?)

                    DiemDTO diem = scoreMap.get(uniqueKey);

                    if (diem != null) {
                        // 7a. Đã có điểm
                        finalRows.add(diem);
                    } else {
                        // 7b. Chưa có điểm -> Tạo placeholder
                        DiemDTO placeholder = new DiemDTO();
                        placeholder.setMaDiem(0); // Đánh dấu là placeholder
                        placeholder.setMaHS(hs.getMaHS());
                        placeholder.setHoTen(hs.getHoTen());
                        placeholder.setMaLop(hs.getMaLop());
                        placeholder.setTenLop(hs.getTenLop()); // Lấy từ HocSinhDTO
                        placeholder.setMaMon(mon.getMaMon());
                        placeholder.setTenMon(mon.getTenMon());
                        placeholder.setLoaiMon(mon.getLoaiMon());
                        placeholder.setHocKy(hk);

                        finalRows.add(placeholder);
                    }
                }
            }

            // 8. No pagination: return all built rows so searching covers entire set
            rows = new ArrayList<>(finalRows);
            hasNext = false;

        } else {
            // Logic cho Admin (như cũ)
            // Request all matching rows from the BUS/DAO (no pagination parameters)
            List<com.sgu.qlhs.dto.DiemDTO> fetched = diemBUS.getDiemFiltered(selectedMaLop, selectedMaMon,
                    selectedHocKy, maNK, null, null);
            rows = fetched != null ? fetched : new ArrayList<>();
            hasNext = false;
        }
        currentRows.clear();
        currentRows.addAll(rows);

        java.util.Map<Integer, java.util.List<Integer>> hsByHocKy = new java.util.HashMap<>();
        for (var d : rows) {
            hsByHocKy.computeIfAbsent(d.getHocKy(), k -> new java.util.ArrayList<>()).add(d.getMaHS());
        }

        java.util.Map<Integer, java.util.Map<Integer, String>> hkMaps = new java.util.HashMap<>();
        for (var entry : hsByHocKy.entrySet()) {
            int hkVal = entry.getKey();
            java.util.List<Integer> maHsList = entry.getValue();
            try {
                java.util.Map<Integer, String> map = hanhKiemBUS.getHanhKiemForStudents(maHsList, maNK, hkVal);
                hkMaps.put(hkVal, map);
            } catch (Exception ex) {
                hkMaps.put(hkVal, new java.util.HashMap<>());
            }
        }

        // Lặp và thêm hàng dựa trên LoaiMon VÀ isPlaceholder
        for (var d : rows) {
            String hkStr = "";
            var mapForHK = hkMaps.get(d.getHocKy());
            if (mapForHK != null && mapForHK.containsKey(d.getMaHS()))
                hkStr = mapForHK.get(d.getMaHS());

            boolean isPlaceholder = (d.getMaDiem() == 0);

            // Kiểm tra LoaiMon từ DTO (đã được BUS nạp)
            if ("DanhGia".equals(d.getLoaiMon())) {
                model.addRow(new Object[] {
                        d.getMaDiem(), d.getMaHS(), d.getHoTen(), d.getTenLop(), d.getTenMon(),
                        d.getHocKy(),
                        null, // Miệng
                        null, // 15p
                        null, // Giữa kỳ
                        null, // Cuối kỳ
                        isPlaceholder ? null : d.getKetQuaDanhGia(), // Kết quả (Đ/KĐ)
                        hkStr // Hạnh kiểm
                });
            } else { // Môn TinhDiem
                model.addRow(new Object[] {
                        d.getMaDiem(), d.getMaHS(), d.getHoTen(), d.getTenLop(), d.getTenMon(),
                        d.getHocKy(),
                        isPlaceholder ? null : d.getDiemMieng(),
                        isPlaceholder ? null : d.getDiem15p(),
                        isPlaceholder ? null : d.getDiemGiuaKy(),
                        isPlaceholder ? null : d.getDiemCuoiKy(),
                        isPlaceholder ? null : d.getDiemTB(), // Kết quả (Điểm TB)
                        hkStr // Hạnh kiểm
                });
            }
        }

        updatePageControls(hasNext);
        applyTextFilter();
    }

    private void updatePageControls(boolean hasNext) {
        btnPrev.setEnabled(currentPage > 0);
        btnNext.setEnabled(hasNext);
        lblPageInfo.setText("Trang " + (currentPage + 1));
    }

    private void applyTextFilter() {
        String txt = txtSearch.getText();
        if (txt == null || txt.isBlank()) {
            sorter.setRowFilter(null);
            return;
        }
        String pattern = "(?i)" + java.util.regex.Pattern.quote(txt);
        // filter on Họ tên, Lớp and Tên môn (model indices: 2=HoTen,3=TenLop,4=TenMon)
        sorter.setRowFilter(RowFilter.regexFilter(pattern, 2, 3, 4));
    }

    private void doDeleteSelectedRows() {
        int[] sel = table.getSelectedRows();
        if (sel == null || sel.length == 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 hoặc nhiều hàng để xóa.", "Chú ý",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Xác nhận xóa điểm của các học sinh đã chọn?", "Xác nhận",
                JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION)
            return;

        // collect model rows and sort descending to remove safely
        java.util.List<Integer> modelRowsIndices = new java.util.ArrayList<>();
        for (int v : sel) {
            modelRowsIndices.add(table.convertRowIndexToModel(v));
        }
        // Sắp xếp giảm dần để xóa từ cuối lên
        modelRowsIndices.sort(java.util.Collections.reverseOrder());

        int maNK = NienKhoaBUS.current();
        NguoiDungDTO nd = null;
        try {
            java.awt.Window w = javax.swing.SwingUtilities.getWindowAncestor(this);
            if (w instanceof com.sgu.qlhs.ui.MainDashboard) {
                com.sgu.qlhs.ui.MainDashboard md = (com.sgu.qlhs.ui.MainDashboard) w;
                nd = md.getNguoiDung();
            }
        } catch (Exception ex) {
            // ignore
        }

        int failedDelete = 0;
        int placeholderSkipped = 0;

        for (int modelRowIndex : modelRowsIndices) {
            if (modelRowIndex < 0 || modelRowIndex >= currentRows.size())
                continue;
            var dto = currentRows.get(modelRowIndex);

            // SỬA: Không xóa placeholder
            if (dto.getMaDiem() == 0) {
                placeholderSkipped++;
                continue;
            }

            try {
                boolean ok = diemBUS.deleteDiem(dto.getMaHS(), dto.getMaMon(), dto.getHocKy(), maNK, nd);
                if (!ok) {
                    failedDelete++;
                }
            } catch (Exception ex) {
                System.err.println("Lỗi khi xóa: " + ex.getMessage());
                failedDelete++;
            }
        }

        if (failedDelete > 0) {
            JOptionPane.showMessageDialog(this, "Một số hàng không được xóa do thiếu quyền.", "Chú ý",
                    JOptionPane.WARNING_MESSAGE);
        } else if (placeholderSkipped == 0) {
            JOptionPane.showMessageDialog(this, "Đã xóa các điểm được chọn.", "Thành công",
                    JOptionPane.INFORMATION_MESSAGE);
        }

        // Tải lại toàn bộ dữ liệu sau khi xóa
        loadData();
    }

    private void doEditSelectedRow() {
        int sel = table.getSelectedRow();
        if (sel < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 hàng để sửa.", "Chú ý",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int modelRow = table.convertRowIndexToModel(sel);
        if (modelRow < 0 || modelRow >= currentRows.size())
            return;
        var dto = currentRows.get(modelRow);

        // SỬA: Nếu là placeholder (MaDiem=0), hãy mở dialog DiemNhap
        // Nếu là điểm đã có (MaDiem > 0), cũng mở DiemNhap
        // (Logic cũ đã đúng)

        // open DiemNhapDialog pre-selected for this class/subject/hk and student
        java.awt.Window w = javax.swing.SwingUtilities.getWindowAncestor(this);
        var dlg = new com.sgu.qlhs.ui.dialogs.DiemNhapDialog(w, dto.getMaLop(), dto.getMaMon(), dto.getHocKy(),
                dto.getMaHS());
        dlg.setVisible(true);
        // after dialog closes we can refresh the table to reflect any changes
        loadData();
    }

    // --- Chủ nhiệm panel helpers ---
    private void createChuNhiemPanel() {
        // model columns match the main model
        modelCN = new DefaultTableModel(new Object[] { "MaDiem", "Mã HS", "Họ tên", "Lớp", "Môn", "HK",
                "Miệng", "15p", "Giữa kỳ", "Cuối kỳ", "Kết quả", "Hạnh kiểm" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // strictly read-only
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                switch (columnIndex) {
                    case 0:
                    case 1:
                        return Integer.class;
                    case 5:
                        return Integer.class;
                    case 6:
                    case 7:
                    case 8:
                    case 9:
                        return Double.class;
                    case 10:
                        return Object.class;
                    default:
                        return String.class;
                }
            }
        };
        tableCN = new JTable(modelCN);
        tableCN.setRowHeight(28);
        tableCN.getTableHeader().setFont(tableCN.getTableHeader().getFont().deriveFont(Font.BOLD));
        // attach a sorter so we can support searching and sorting
        sorterCN = new TableRowSorter<>(modelCN);
        tableCN.setRowSorter(sorterCN);
        // name comparator: same Vietnamese collation as main table
        Collator collator = Collator.getInstance(Locale.forLanguageTag("vi-VN"));
        sorterCN.setComparator(2, (o1, o2) -> {
            String s1 = o1 == null ? "" : o1.toString().trim();
            String s2 = o2 == null ? "" : o2.toString().trim();
            String k1 = lastToken(s1).toLowerCase(Locale.forLanguageTag("vi-VN"));
            String k2 = lastToken(s2).toLowerCase(Locale.forLanguageTag("vi-VN"));
            int c = collator.compare(k1, k2);
            if (c != 0)
                return c;
            return collator.compare(s1, s2);
        });
        // class comparator similar to main table
        Pattern classPattern = Pattern.compile("^(\\d+)([^\\d]*?)(\\d*)$");
        sorterCN.setComparator(3, (o1, o2) -> {
            String s1 = o1 == null ? "" : o1.toString().trim();
            String s2 = o2 == null ? "" : o2.toString().trim();
            String t1 = s1.replaceAll("\\s+", "");
            String t2 = s2.replaceAll("\\s+", "");
            Matcher m1 = classPattern.matcher(t1);
            Matcher m2 = classPattern.matcher(t2);
            int grade1 = 0, grade2 = 0, idx1 = 0, idx2 = 0;
            String grp1 = "", grp2 = "";
            if (m1.matches()) {
                try {
                    grade1 = Integer.parseInt(m1.group(1));
                } catch (Exception ex) {
                    grade1 = 0;
                }
                grp1 = m1.group(2) == null ? "" : m1.group(2);
                try {
                    idx1 = (m1.group(3) == null || m1.group(3).isEmpty()) ? 0 : Integer.parseInt(m1.group(3));
                } catch (Exception ex) {
                    idx1 = 0;
                }
            }
            if (m2.matches()) {
                try {
                    grade2 = Integer.parseInt(m2.group(1));
                } catch (Exception ex) {
                    grade2 = 0;
                }
                grp2 = m2.group(2) == null ? "" : m2.group(2);
                try {
                    idx2 = (m2.group(3) == null || m2.group(3).isEmpty()) ? 0 : Integer.parseInt(m2.group(3));
                } catch (Exception ex) {
                    idx2 = 0;
                }
            }
            if (grade1 != grade2)
                return Integer.compare(grade1, grade2);
            int c = collator.compare(grp1.toLowerCase(Locale.forLanguageTag("vi-VN")),
                    grp2.toLowerCase(Locale.forLanguageTag("vi-VN")));
            if (c != 0)
                return c;
            return Integer.compare(idx1, idx2);
        });
        // hide PK column visually
        javax.swing.SwingUtilities.invokeLater(() -> {
            try {
                if (tableCN.getColumnModel().getColumnCount() > 0)
                    tableCN.removeColumn(tableCN.getColumnModel().getColumn(0));
            } catch (Exception ex) {
            }
        });
    }

    private javax.swing.JScrollPane buildChuNhiemScrollPane() {
        if (tableCN == null)
            createChuNhiemPanel();

        // build a small toolbar for the chủ nhiệm tab: search + hk filter + môn filter
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        top.add(new JLabel("Tìm:"));
        top.add(txtSearchCN);
        top.add(new JLabel("Học kỳ:"));
        top.add(cboHKCN);
        top.add(new JLabel("Môn:"));
        top.add(cboMonCN);
        top.add(Box.createHorizontalStrut(8));

        // Wire listeners for filters
        txtSearchCN.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                applyFiltersCN();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                applyFiltersCN();
            }

            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                applyFiltersCN();
            }
        });
        cboHKCN.addActionListener(e -> applyFiltersCN());
        // populate subject filter for CN tab
        try {
            cboMonCN.removeAllItems();
            cboMonCN.addItem("-- Tất cả --");
            java.util.List<MonHocDTO> allMons = monBUS.getAllMon();
            if (allMons != null) {
                for (MonHocDTO m : allMons) {
                    cboMonCN.addItem(m.getTenMon());
                }
            }
            cboMonCN.setSelectedIndex(0);
        } catch (Exception ex) {
            // ignore
        }
        cboMonCN.addActionListener(e -> applyFiltersCN());

        // add a refresh button and a detail button for Chủ nhiệm
        // Refresh will reload the Chủ nhiệm data in-place
        JButton btnRefreshCN = new JButton("Làm mới");
        btnRefreshCN.addActionListener(ev -> {
            try {
                loadChuNhiemData();
            } catch (Exception ex) {
                System.err.println("Lỗi khi làm mới dữ liệu Chủ nhiệm: " + ex.getMessage());
            }
        });

        // detail button to open detailed report for a selected student
        JButton btnDetailCN = new JButton("Bảng điểm chi tiết");
        btnDetailCN.addActionListener(ev -> {
            int sel = tableCN.getSelectedRow();
            if (sel < 0) {
                JOptionPane.showMessageDialog(DiemPanel.this,
                        "Vui lòng chọn 1 học sinh trong danh sách để xem bảng điểm chi tiết.", "Chú ý",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            int modelRow = tableCN.convertRowIndexToModel(sel);
            if (modelRow < 0 || modelRow >= modelCN.getRowCount())
                return;
            Object v = modelCN.getValueAt(modelRow, 1); // MaHS column in model
            int maHS = -1;
            try {
                if (v instanceof Number)
                    maHS = ((Number) v).intValue();
                else
                    maHS = Integer.parseInt(v.toString());
            } catch (Exception ex) {
                maHS = -1;
            }
            if (maHS <= 0) {
                JOptionPane.showMessageDialog(DiemPanel.this, "Mã HS không hợp lệ.", "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            java.awt.Window w = javax.swing.SwingUtilities.getWindowAncestor(DiemPanel.this);
            com.sgu.qlhs.ui.dialogs.BangDiemChiTietDialog dlg = new com.sgu.qlhs.ui.dialogs.BangDiemChiTietDialog(w);
            try {
                // Inject current logged-in user and mark openedFromChuNhiem BEFORE
                // setInitialMaHS so loadBangDiem() has the proper context for
                // subject and hạnh kiểm permission checks.
                try {
                    java.awt.Window ww = javax.swing.SwingUtilities.getWindowAncestor(DiemPanel.this);
                    if (ww instanceof com.sgu.qlhs.ui.MainDashboard) {
                        com.sgu.qlhs.ui.MainDashboard md = (com.sgu.qlhs.ui.MainDashboard) ww;
                        NguoiDungDTO ndLocal = md.getNguoiDung();
                        if (ndLocal != null) {
                            dlg.setInjectedNguoiDung(ndLocal);
                        }
                    }
                } catch (Exception ex2) {
                    // ignore
                }

                dlg.setOpenedFromChuNhiem(true);
                dlg.setInitialMaHS(maHS);
            } catch (Exception ex) {
                // ignore
            }
            dlg.setVisible(true);
        });
        top.add(Box.createHorizontalStrut(8));
        top.add(btnRefreshCN);
        top.add(Box.createHorizontalStrut(8));
        top.add(btnDetailCN);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(top, BorderLayout.NORTH);
        wrapper.add(new JScrollPane(tableCN), BorderLayout.CENTER);
        return new javax.swing.JScrollPane(wrapper);
    }

    private void loadChuNhiemData() {
        if (!isChuNhiem || chuNhiemMaLop <= 0)
            return;
        modelCN.setRowCount(0);
        int maNK = NienKhoaBUS.current();
        try {
            // fetch all diem rows for the class (no user filter) so chủ nhiệm can view
            java.util.List<com.sgu.qlhs.dto.DiemDTO> rows = diemBUS.getDiemFiltered(chuNhiemMaLop, null, null, maNK,
                    null, null);
            currentRowsCN.clear();
            currentRowsCN.addAll(rows);

            // batch fetch hạnh kiểm similar to main loadData
            java.util.Map<Integer, java.util.List<Integer>> hsByHocKy = new java.util.HashMap<>();
            for (var d : rows) {
                hsByHocKy.computeIfAbsent(d.getHocKy(), k -> new java.util.ArrayList<>()).add(d.getMaHS());
            }
            java.util.Map<Integer, java.util.Map<Integer, String>> hkMaps = new java.util.HashMap<>();
            for (var entry : hsByHocKy.entrySet()) {
                int hkVal = entry.getKey();
                java.util.List<Integer> maHsList = entry.getValue();
                try {
                    java.util.Map<Integer, String> map = hanhKiemBUS.getHanhKiemForStudents(maHsList, maNK, hkVal);
                    hkMaps.put(hkVal, map);
                } catch (Exception ex) {
                    hkMaps.put(hkVal, new java.util.HashMap<>());
                }
            }

            // populate rows (keep same logic for LoaiMon)
            for (var d : rows) {
                String hkStr = "";
                var mapForHK = hkMaps.get(d.getHocKy());
                if (mapForHK != null && mapForHK.containsKey(d.getMaHS()))
                    hkStr = mapForHK.get(d.getMaHS());

                if ("DanhGia".equals(d.getLoaiMon())) {
                    modelCN.addRow(
                            new Object[] { d.getMaDiem(), d.getMaHS(), d.getHoTen(), d.getTenLop(), d.getTenMon(),
                                    d.getHocKy(), null, null, null, null, d.getKetQuaDanhGia(), hkStr });
                } else {
                    modelCN.addRow(new Object[] { d.getMaDiem(), d.getMaHS(), d.getHoTen(), d.getTenLop(),
                            d.getTenMon(),
                            d.getHocKy(), d.getDiemMieng(), d.getDiem15p(), d.getDiemGiuaKy(), d.getDiemCuoiKy(),
                            d.getDiemTB(), hkStr });
                }
            }
            // after populating, reapply any CN filters (search / hk) so the view is
            // consistent
            applyFiltersCN();
        } catch (Exception ex) {
            System.err.println("Lỗi khi nạp dữ liệu Lớp chủ nhiệm: " + ex.getMessage());
        }
    }

    private void applyFiltersCN() {
        if (sorterCN == null)
            return;
        java.util.List<RowFilter<Object, Object>> filters = new java.util.ArrayList<>();
        String txt = txtSearchCN.getText();
        if (txt != null && !txt.isBlank()) {
            String pattern = "(?i)" + Pattern.quote(txt);
            filters.add(RowFilter.regexFilter(pattern, 2, 3, 4)); // HoTen, TenLop, TenMon
        }
        int hkSel = cboHKCN.getSelectedIndex();
        if (hkSel > 0) {
            int hkVal = hkSel; // 1 or 2
            filters.add(new RowFilter<Object, Object>() {
                @Override
                public boolean include(Entry<? extends Object, ? extends Object> entry) {
                    Object v = entry.getValue(5); // HocKy column
                    if (v == null)
                        return false;
                    try {
                        int val = Integer.parseInt(v.toString());
                        return val == hkVal;
                    } catch (Exception ex) {
                        return false;
                    }
                }
            });
        }
        // subject filter (TenMon at model index 4)
        String selMonCN = (String) cboMonCN.getSelectedItem();
        if (selMonCN != null && !selMonCN.equals("-- Tất cả --")) {
            filters.add(new RowFilter<Object, Object>() {
                @Override
                public boolean include(Entry<? extends Object, ? extends Object> entry) {
                    Object v = entry.getValue(4);
                    if (v == null)
                        return false;
                    return selMonCN.equals(v.toString());
                }
            });
        }
        if (filters.isEmpty())
            sorterCN.setRowFilter(null);
        else
            sorterCN.setRowFilter(RowFilter.andFilter(filters));
    }

    private static String lastToken(String s) {
        if (s == null || s.isBlank())
            return "";
        String[] parts = s.trim().split("\\s+");
        return parts.length == 0 ? "" : parts[parts.length - 1];
    }
    // (Removed unused helper parseDoubleOrZero) kept parsing logic centralized in
    // dialogs/DAOs where needed.

    /**
     * Refresh Chủ nhiệm tab data if this panel is currently showing Chủ nhiệm
     * data. Public wrapper so external dialogs (e.g. BangDiemChiTietDialog) can
     * request an immediate refresh after related DB changes (like Hạnh kiểm).
     */
    public void refreshChuNhiemIfActive() {
        try {
            if (isChuNhiem && tabbedPane != null) {
                // reload only the chủ nhiệm tab data to avoid flicker for other views
                loadChuNhiemData();
            }
        } catch (Exception ex) {
            // swallow to avoid disturbing caller; log to stderr for debugging
            System.err.println("Lỗi khi refresh Chủ nhiệm data: " + ex.getMessage());
        }
    }

    public void refreshData() {
        System.out.println("DiemPanel: Received refresh request. Reloading data...");
        currentPage = 0; // Reset pagination
        loadData();
    }
}