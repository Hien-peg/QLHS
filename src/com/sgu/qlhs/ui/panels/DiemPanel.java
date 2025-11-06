/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sgu.qlhs.ui.panels;

/**
 *
 * @author minho
 */
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
import com.sgu.qlhs.bus.HocSinhBUS;
// THÊM: Import DiemDTO
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
// import java.util.Vector; (unused)
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
    // SỬA: Dùng DiemDTO
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
        // SỬA: Thêm cột "Kết quả" (sau Cuối kỳ)
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

        // sort Họ tên by the last token (last name) using Vietnamese collation
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

        // sort Lớp like: 6A1,6A2,6B3,7A1,7B2,7C3 by grade number, then letter(s), then
        // trailing number
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

        // hide the MaDiem column (kept in model for PK operations)
        outer.add(new JScrollPane(table), BorderLayout.CENTER);
        // After adding to viewport, remove the first column from view
        // (it remains accessible in the TableModel)
        javax.swing.SwingUtilities.invokeLater(() -> {
            if (table.getColumnModel().getColumnCount() > 0) {
                try {
                    table.removeColumn(table.getColumnModel().getColumn(0));
                } catch (Exception ex) {
                    // ignore if already removed
                }
            }
        });

        // pagination controls (added to outer so they appear under the table)
        var pager = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 8));
        pager.add(btnPrev);
        pager.add(lblPageInfo);
        pager.add(btnNext);
        btnPrev.setEnabled(false);
        btnNext.setEnabled(false);
        outer.add(pager, BorderLayout.SOUTH);

        // Use a tabbed pane so we can add the "Bộ môn" view and conditionally a
        // "Lớp chủ nhiệm" view for homeroom teachers.
        tabbedPane.addTab("Điểm bộ môn", outer);
        add(tabbedPane, BorderLayout.CENTER);

        // Prepare Chủ nhiệm panel (constructed but added later when we know the
        // user is a chủ nhiệm)
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

        // Open detailed grade dialog for selected student (or for current student when
        // logged-in as student)
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
                dlg.setInitialMaHS(targetMaHS);
            } catch (Exception ex) {
                // ignore selection failure; dialog will still allow manual selection if
                // permitted
            }
            dlg.setVisible(true);
        });
        // quick-search on type
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

        // add popup menu for edit/delete (listeners attached later after resolving
        // user)
        var popup = new JPopupMenu();
        var miEdit = new JMenuItem("Sửa");
        var miDelete = new JMenuItem("Xóa");
        popup.add(miEdit);
        popup.add(miDelete);

        // Right-click listener: enable/disable popup items based on current user's
        // permissions. This avoids showing edit/delete for rows the teacher is not
        // assigned to (defense-in-depth; DB-level checks still apply).
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                maybeShowPopup(e);
            }

            @Override
            public void mouseReleased(java.awt.event.MouseEvent e) {
                maybeShowPopup(e);
            }

            private void maybeShowPopup(java.awt.event.MouseEvent e) {
                if (!e.isPopupTrigger() && !javax.swing.SwingUtilities.isRightMouseButton(e))
                    return;
                int viewRow = table.rowAtPoint(e.getPoint());
                if (viewRow >= 0) {
                    try {
                        table.setRowSelectionInterval(viewRow, viewRow);
                    } catch (Exception ex) {
                        // ignore
                    }
                } else {
                    table.clearSelection();
                }

                // determine permission for the current selection
                boolean allowEdit = false;
                boolean allowDelete = false;
                int[] sel = table.getSelectedRows();
                if (sel != null && sel.length > 0) {
                    // resolve current user
                    NguoiDungDTO ndLocal = null;
                    try {
                        java.awt.Window w = javax.swing.SwingUtilities.getWindowAncestor(DiemPanel.this);
                        if (w instanceof com.sgu.qlhs.ui.MainDashboard) {
                            com.sgu.qlhs.ui.MainDashboard md = (com.sgu.qlhs.ui.MainDashboard) w;
                            ndLocal = md.getNguoiDung();
                        }
                    } catch (Exception ex) {
                        // ignore
                    }
                    int maNKLocal = NienKhoaBUS.current();

                    // default: only admins can edit/delete everything
                    if (ndLocal == null || "quan_tri_vien".equalsIgnoreCase(ndLocal.getVaiTro())) {
                        allowEdit = true;
                        allowDelete = true;
                    } else if ("giao_vien".equalsIgnoreCase(ndLocal.getVaiTro())) {
                        // For edit: only require the single selected row to be assigned to
                        // this teacher for that subject.
                        if (sel.length == 1) {
                            int v = sel[0];
                            int modelRow = table.convertRowIndexToModel(v);
                            if (modelRow >= 0 && modelRow < currentRows.size()) {
                                var dto = currentRows.get(modelRow);
                                try {
                                    allowEdit = diemBUS.isTeacherAssignedPublic(ndLocal.getId(), dto.getMaHS(),
                                            dto.getMaMon(), dto.getHocKy(), maNKLocal);
                                } catch (Exception ex) {
                                    allowEdit = false;
                                }
                            }
                        }
                        // For delete: require every selected row to be permitted
                        boolean allOk = true;
                        for (int v : sel) {
                            int modelRow = table.convertRowIndexToModel(v);
                            if (modelRow < 0 || modelRow >= currentRows.size()) {
                                allOk = false;
                                break;
                            }
                            var dto = currentRows.get(modelRow);
                            try {
                                if (!diemBUS.isTeacherAssignedPublic(ndLocal.getId(), dto.getMaHS(), dto.getMaMon(),
                                        dto.getHocKy(), maNKLocal)) {
                                    allOk = false;
                                    break;
                                }
                            } catch (Exception ex) {
                                allOk = false;
                                break;
                            }
                        }
                        allowDelete = allOk;
                    }
                }

                miEdit.setEnabled(allowEdit);
                miDelete.setEnabled(allowDelete);
            }
        });

        // Defer resolving the logged-in user until this panel is added to a window
        // (MainDashboard). When constructed inside MainDashboard, getWindowAncestor
        // may be null during ctor; use a hierarchy listener to perform one-time
        // adjustment once ancestor is available.
        this.addHierarchyListener(evt -> {
            if (userContextResolved)
                return;
            java.awt.Window w = javax.swing.SwingUtilities.getWindowAncestor(this);
            if (w instanceof com.sgu.qlhs.ui.MainDashboard) {
                com.sgu.qlhs.ui.MainDashboard md = (com.sgu.qlhs.ui.MainDashboard) w;
                NguoiDungDTO nd = md.getNguoiDung();
                try {
                    if (nd != null && "hoc_sinh".equalsIgnoreCase(nd.getVaiTro())) {
                        // student: restrict to own class/student and disable editing/search
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
                        // ensure no popup for students
                        table.setComponentPopupMenu(null);
                    } else {
                        // non-student: attach popup and wire actions
                        table.setComponentPopupMenu(popup);
                        miDelete.addActionListener(ev -> doDeleteSelectedRows());
                        miEdit.addActionListener(ev -> doEditSelectedRow());
                    }

                    // Additionally detect if this teacher is a Chủ nhiệm; if so,
                    // prepare the Chủ nhiệm tab (read-only class view).
                    try {
                        if (nd != null && "giao_vien".equalsIgnoreCase(nd.getVaiTro())) {
                            ChuNhiemBUS cnBUS = new ChuNhiemBUS();
                            ChuNhiemDTO cn = cnBUS.getChuNhiemByGV(nd.getId());
                            if (cn != null && cn.getMaLop() > 0) {
                                isChuNhiem = true;
                                chuNhiemMaLop = cn.getMaLop();
                                // add the tab if not already added
                                if (tabbedPane.indexOfTab("Lớp chủ nhiệm") == -1) {
                                    tabbedPane.addTab("Lớp chủ nhiệm", buildChuNhiemScrollPane());
                                }
                                // load class data into the read-only table
                                loadChuNhiemData();
                            }
                        }
                    } catch (Exception ex) {
                        // ignore failure to detect chủ nhiệm
                    }
                } catch (Exception ex) {
                    // ignore any failures while resolving user context
                }

                // reload filters and data using the resolved context
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
        try {
            java.awt.Window w = javax.swing.SwingUtilities.getWindowAncestor(this);
            if (w instanceof com.sgu.qlhs.ui.MainDashboard) {
                com.sgu.qlhs.ui.MainDashboard md = (com.sgu.qlhs.ui.MainDashboard) w;
                NguoiDungDTO nd = md.getNguoiDung();
                if (nd != null && "giao_vien".equalsIgnoreCase(nd.getVaiTro())) {

                    // === PHẦN SỬA ===
                    int maNK = NienKhoaBUS.current();
                    String namHoc = (new NienKhoaBUS()).getNamHocString(maNK); // Lấy chuỗi năm học
                    int hkIdx = cboHK.getSelectedIndex();
                    // Chuyển Integer (1) sang String ("HK1")
                    String hkParam = hkIdx > 0 ? ("HK" + hkIdx) : null;

                    java.util.List<Integer> lopIds = phanCongBUS.getDistinctMaLopByGiaoVien(nd.getId(), namHoc,
                            hkParam);
                    // === KẾT THÚC PHẦN SỬA ===

                    lopList = new java.util.ArrayList<>();
                    for (Integer ml : lopIds) {
                        var l = lopBUS.getLopByMa(ml);
                        if (l != null) {
                            lopList.add(l);
                            cboLop.addItem(l.getTenLop());
                        }
                    }
                    // For users with role 'giao_vien' (subject teachers / chủ nhiệm),
                    // we DO NOT expose the "-- Tất cả --" option to avoid expensive
                    // cross-class queries at startup. For other roles (admin), the
                    // combo will include the all-option in the fallback below.
                    // (So: no insertion of "-- Tất cả --" here.)
                    if (cboLop.getItemCount() > 0)
                        cboLop.setSelectedIndex(0);
                    return;
                }
                // ... (phần hoc_sinh giữ nguyên) ...
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
                    return;
                }
            }
        } catch (Exception ex) {
            System.err.println("Lỗi lấy lớp theo giáo viên: " + ex.getMessage());
        }

        // fallback: show all classes
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

                    // === PHẦN SỬA ===
                    int maNK = NienKhoaBUS.current();
                    String namHoc = (new NienKhoaBUS()).getNamHocString(maNK); // Lấy chuỗi năm học
                    int hkIdx = cboHK.getSelectedIndex();
                    // Chuyển Integer (1) sang String ("HK1")
                    String hkParam = hkIdx > 0 ? ("HK" + hkIdx) : null;

                    java.util.List<Integer> monIds = phanCongBUS.getDistinctMaMonByGiaoVien(nd.getId(), namHoc,
                            hkParam);
                    // === KẾT THÚC PHẦN SỬA ===

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
                // ... (phần hoc_sinh giữ nguyên) ...
                if (nd != null && "hoc_sinh".equalsIgnoreCase(nd.getVaiTro())) {
                    cboMon.removeAllItems();
                    cboMon.addItem("-- Tất cả --");
                    cboMon.setSelectedIndex(0);
                    cboMon.setEnabled(false);
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
        Integer maLop = null;
        Integer maMon = null;
        Integer hocKy = null;
        int lopIdx = cboLop.getSelectedIndex();
        // cboLop may or may not contain a leading "-- Tất cả --" item depending on
        // user role (chu nhiem hides it). Handle both mappings:
        boolean lopHasAll = cboLop.getItemCount() > 0 && "-- Tất cả --".equals(cboLop.getItemAt(0));
        if (lopList != null && !lopList.isEmpty()) {
            if (lopHasAll) {
                if (lopIdx > 0 && lopIdx <= lopList.size()) {
                    maLop = lopList.get(lopIdx - 1).getMaLop();
                }
            } else {
                if (lopIdx >= 0 && lopIdx < lopList.size()) {
                    maLop = lopList.get(lopIdx).getMaLop();
                }
            }
        }
        int monIdx = cboMon.getSelectedIndex();
        if (monIdx > 0 && monList != null && monIdx <= monList.size()) { // Thêm kiểm tra null
            maMon = monList.get(monIdx - 1).getMaMon();
        }
        int hkIdx = cboHK.getSelectedIndex();
        if (hkIdx > 0) {
            hocKy = hkIdx; // 1 or 2
        }

        // pagination: fetch pageSize+1 rows to detect next page
        List<com.sgu.qlhs.dto.DiemDTO> rows;
        boolean hasNext = false;
        if (isStudentView) {
            // student view: only fetch this student's records (may be both HK1/HK2)
            // Use permission-aware reads (pass the logged-in user) to enforce
            // defense-in-depth
            rows = new java.util.ArrayList<>();
            if (hocKy != null && hocKy > 0) {
                rows.addAll(diemBUS.getDiemByMaHS(currentStudentMaHS, hocKy, maNK, nd));
            } else {
                rows.addAll(diemBUS.getDiemByMaHS(currentStudentMaHS, 1, maNK, nd));
                rows.addAll(diemBUS.getDiemByMaHS(currentStudentMaHS, 2, maNK, nd));
            }
            // no pagination for single student view
            hasNext = false;
        } else {
            int fetchSize = pageSize + 1;
            java.util.List<com.sgu.qlhs.dto.DiemDTO> fetched = new java.util.ArrayList<>();
            // when a teacher is logged-in, make sure we collect rows from ALL classes
            // the teacher is assigned to when the user selected "-- Tất cả --" for
            // lớp. This avoids a case where the DAO/filtering returns only one class.
            if (nd != null && "giao_vien".equalsIgnoreCase(nd.getVaiTro())) {
                if (maLop == null) {
                    // collect all class IDs assigned to this teacher for the current
                    // niên khóa/học kỳ
                    int maNKcur = maNK;
                    String namHoc = (new NienKhoaBUS()).getNamHocString(maNKcur);
                    int hkIdxLocal = cboHK.getSelectedIndex();
                    String hkParamLocal = hkIdxLocal > 0 ? ("HK" + hkIdxLocal) : null;
                    java.util.List<Integer> assignedLops = phanCongBUS.getDistinctMaLopByGiaoVien(nd.getId(),
                            namHoc, hkParamLocal);
                    // iterate and fetch per-class (no server-side pagination here; keep it
                    // simple and aggregate)
                    // If teacher has no direct teaching assignments but is also a chủ nhiệm,
                    // ensure we don't accidentally drop results -- however, chủ nhiệm-only
                    // class should not add editable môn rows; we still gather by assigned
                    // classes (subjects) below. Also dedupe class list.
                    java.util.Set<Integer> lopSet = new java.util.LinkedHashSet<>(assignedLops);
                    try {
                        ChuNhiemBUS cnBUS = new ChuNhiemBUS();
                        ChuNhiemDTO cn = cnBUS.getChuNhiemByGV(nd.getId());
                        if (cn != null && cn.getMaLop() > 0)
                            lopSet.add(cn.getMaLop());
                    } catch (Exception ex) {
                        // ignore
                    }
                    // Use optimized DAO/Bus call that fetches all classes in one query and
                    // applies teacher-user filtering in the BUS wrapper.
                    try {
                        java.util.List<Integer> lopList = new java.util.ArrayList<>(lopSet);
                        fetched = diemBUS.getDiemFilteredForUserByMaLopList(lopList, maMon, hocKy, maNKcur, null, null,
                                nd);
                    } catch (Exception ex) {
                        // fallback to per-class fetch if optimized call fails
                        for (Integer ml : lopSet) {
                            try {
                                java.util.List<com.sgu.qlhs.dto.DiemDTO> part = diemBUS.getDiemFiltered(ml, maMon,
                                        hocKy, maNKcur, null, null);
                                if (part != null && !part.isEmpty())
                                    fetched.addAll(part);
                            } catch (Exception ex2) {
                                // ignore fetch failure for a particular class
                            }
                        }
                        // keep defensive filtering in case fallback returned unfiltered rows
                        try {
                            java.util.List<com.sgu.qlhs.dto.DiemDTO> filteredByAssign = new java.util.ArrayList<>();
                            for (com.sgu.qlhs.dto.DiemDTO d : fetched) {
                                try {
                                    if (diemBUS.isTeacherAssignedPublic(nd.getId(), d.getMaHS(), d.getMaMon(),
                                            d.getHocKy(),
                                            maNKcur))
                                        filteredByAssign.add(d);
                                } catch (Exception ex3) {
                                    // skip on error
                                }
                            }
                            fetched = filteredByAssign;
                        } catch (Exception ex3) {
                            // ignore filtering errors
                        }
                    }
                } else {
                    // specific class selected: use permission-aware fetch
                    fetched = diemBUS.getDiemFilteredForUser(maLop, maMon, hocKy, maNK, fetchSize,
                            currentPage * pageSize, nd);
                }
            } else {
                fetched = diemBUS.getDiemFiltered(maLop, maMon, hocKy, maNK, fetchSize, currentPage * pageSize);
            }
            rows = fetched;
            hasNext = rows.size() > pageSize;
            if (hasNext) {
                rows = new java.util.ArrayList<>(rows.subList(0, pageSize));
            }
        }
        currentRows.clear();
        currentRows.addAll(rows);

        // subject filter
        String selMon = (String) cboMon.getSelectedItem();
        boolean filterMon = selMon != null && !selMon.equals("-- Tất cả --");

        int hkFilter = cboHK.getSelectedIndex(); // 0 == all, 1 == HK1, 2 == HK2

        // Prepare batch fetch: group rows by HocKy and collect MaHS per group
        java.util.Map<Integer, java.util.List<Integer>> hsByHocKy = new java.util.HashMap<>();
        for (var d : rows) {
            if (filterMon && !d.getTenMon().equals(selMon))
                continue;
            if (hkFilter > 0 && d.getHocKy() != hkFilter)
                continue;
            hsByHocKy.computeIfAbsent(d.getHocKy(), k -> new java.util.ArrayList<>()).add(d.getMaHS());
        }

        // Fetch Hạnh kiểm per HocKy in batch
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

        // SỬA: Lặp và thêm hàng dựa trên LoaiMon
        for (var d : rows) {
            // Lọc filter (nếu có)
            if (filterMon && !d.getTenMon().equals(selMon))
                continue;
            if (hkFilter > 0 && d.getHocKy() != hkFilter)
                continue;

            String hkStr = "";
            var mapForHK = hkMaps.get(d.getHocKy());
            if (mapForHK != null && mapForHK.containsKey(d.getMaHS()))
                hkStr = mapForHK.get(d.getMaHS());

            // Kiểm tra LoaiMon từ DTO (đã được BUS nạp)
            if ("DanhGia".equals(d.getLoaiMon())) {
                model.addRow(new Object[] {
                        d.getMaDiem(), d.getMaHS(), d.getHoTen(), d.getTenLop(), d.getTenMon(),
                        d.getHocKy(),
                        null, // Miệng
                        null, // 15p
                        null, // Giữa kỳ
                        null, // Cuối kỳ
                        d.getKetQuaDanhGia(), // Kết quả (Đ/KĐ)
                        hkStr // Hạnh kiểm
                });
            } else { // Môn TinhDiem
                model.addRow(new Object[] {
                        d.getMaDiem(), d.getMaHS(), d.getHoTen(), d.getTenLop(), d.getTenMon(),
                        d.getHocKy(),
                        d.getDiemMieng(),
                        d.getDiem15p(),
                        d.getDiemGiuaKy(),
                        d.getDiemCuoiKy(),
                        d.getDiemTB(), // Kết quả (Điểm TB)
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
        java.util.List<Integer> models = new java.util.ArrayList<>();
        for (int v : sel) {
            models.add(table.convertRowIndexToModel(v));
        }
        models.sort(java.util.Collections.reverseOrder());
        int maNK = NienKhoaBUS.current();

        // SỬA: Lấy modelRow từ danh sách đã sắp xếp
        for (int modelRowIndex : models) {
            if (modelRowIndex < 0 || modelRowIndex >= currentRows.size())
                continue;
            var dto = currentRows.get(modelRowIndex);
            try {
                // resolve current user from MainDashboard (if available) to enforce server-side
                // checks
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

                boolean ok = diemBUS.deleteDiem(dto.getMaHS(), dto.getMaMon(), dto.getHocKy(), maNK, nd);
                if (!ok) {
                    JOptionPane.showMessageDialog(this, "Bạn không có quyền xóa điểm này.", "Không có quyền",
                            JOptionPane.WARNING_MESSAGE);
                    continue; // Bỏ qua hàng này nếu không xóa được
                }
                // Chỉ xóa khỏi model/currentRows NẾU xóa DB thành công
                model.removeRow(modelRowIndex);
                currentRows.remove(modelRowIndex);

            } catch (Exception ex) {
                System.err.println("Lỗi khi xóa: " + ex.getMessage());
            }

        }
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

        // add a detail button for Chủ nhiệm to open detailed report for a selected
        // student
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
                dlg.setInitialMaHS(maHS);
            } catch (Exception ex) {
                // ignore
            }
            dlg.setVisible(true);
        });
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
}