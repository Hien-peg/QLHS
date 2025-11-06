package com.sgu.qlhs.ui.panels;

import com.sgu.qlhs.ui.components.RoundedPanel;
import com.sgu.qlhs.bus.LopBUS;
import com.sgu.qlhs.bus.PhanCongDayBUS;
import com.sgu.qlhs.bus.NienKhoaBUS;
import com.sgu.qlhs.bus.ChuNhiemBUS;
import com.sgu.qlhs.bus.HocSinhBUS;
import com.sgu.qlhs.dto.HocSinhDTO;
import com.sgu.qlhs.dto.LopDTO;
import com.sgu.qlhs.dto.NguoiDungDTO;
import com.sgu.qlhs.ui.model.HocSinhTableModel;
import com.sgu.qlhs.ui.dialogs.HocSinhDetailDialog;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.RowFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.event.DocumentEvent;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.regex.Pattern;

import static com.sgu.qlhs.ui.MainDashboard.*;

public class HocSinhPanel extends JPanel {
    // Default single-view components (used when not chủ nhiệm)
    private JTable tblHS;
    private TableRowSorter<TableModel> hsSorter;
    private JTextField txtHsSearch;
    private JComboBox<String> cboHsLop, cboHsGioiTinh;
    // mapping of cboHsLop items to MaLop when populated for a teacher
    private final java.util.List<Integer> cboLopMaList = new java.util.ArrayList<>();

    // Components for two-tab view when user is chủ nhiệm
    private JTabbedPane hsTabbedPane;
    // Homeroom tab
    private JTable tblHomeroom;
    private DefaultTableModel homModel;
    private TableRowSorter<TableModel> homSorter;
    private JTextField txtHomSearch;
    private JComboBox<String> cboHomLop;
    private final java.util.List<Integer> homLopMaList = new java.util.ArrayList<>();
    // Taught-classes tab
    private JTable tblTaught;
    private DefaultTableModel taughtModel;
    private TableRowSorter<TableModel> taughtSorter;
    private JTextField txtTaughtSearch;
    private JComboBox<String> cboTaughtLop;
    private final java.util.List<Integer> taughtLopMaList = new java.util.ArrayList<>();
    private JPanel innerWrap; // made a field so we can swap its contents later
    private boolean userContextResolved = false;

    public HocSinhPanel() {
        setLayout(new BorderLayout());
        setOpaque(false);

        var outer = new RoundedPanel(18, CARD_BG, CARD_BORDER);
        outer.setLayout(new BorderLayout());
        var lbl = new JLabel("Học sinh");
        lbl.setBorder(new EmptyBorder(12, 16, 8, 16));
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 18f));
        outer.add(lbl, BorderLayout.NORTH);

        innerWrap = new JPanel(new BorderLayout());
        innerWrap.setOpaque(false);
        innerWrap.setBorder(new EmptyBorder(8, 12, 12, 12));

        // By default show the single-table filter view. If this panel is later
        // attached to the MainDashboard and the user is a chủ nhiệm we will swap
        // the content to the two-tab view in the hierarchy listener below.
        innerWrap.add(buildFilter(), BorderLayout.NORTH);
        innerWrap.add(buildTable(), BorderLayout.CENTER);
        outer.add(innerWrap, BorderLayout.CENTER);

        // Defer detecting the logged-in user until the panel is added to a
        // window. When constructed inside MainDashboard, getWindowAncestor may be
        // null during ctor; use a hierarchy listener to perform one-time
        // adjustment once ancestor is available.
        this.addHierarchyListener(evt -> {
            if (userContextResolved)
                return;
            java.awt.Window w = javax.swing.SwingUtilities.getWindowAncestor(this);
            if (w instanceof com.sgu.qlhs.ui.MainDashboard) {
                com.sgu.qlhs.ui.MainDashboard md = (com.sgu.qlhs.ui.MainDashboard) w;
                NguoiDungDTO nd = md.getNguoiDung();
                try {
                    if (nd != null && "giao_vien".equalsIgnoreCase(nd.getVaiTro())) {
                        ChuNhiemBUS cnBus = new ChuNhiemBUS();
                        var cn = cnBus.getChuNhiemByGV(nd.getId());
                        if (cn != null && cn.getMaLop() > 0) {
                            // replace innerWrap contents with the tabbed UI
                            innerWrap.removeAll();
                            innerWrap.add(buildTabbedHsPanel(nd.getId(), cn.getMaLop()), BorderLayout.CENTER);
                            innerWrap.revalidate();
                            innerWrap.repaint();
                        }
                    }
                } catch (Exception ex) {
                    // ignore and keep default single view
                }
                userContextResolved = true;
            }
        });

        add(outer, BorderLayout.CENTER);

        // apply filters initially (after table created)
        SwingUtilities.invokeLater(this::applyHsFilters);
    }

    private JComponent buildFilter() {
        var filter = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        filter.setOpaque(false);

        txtHsSearch = new JTextField(18);
        txtHsSearch.setBorder(BorderFactory.createTitledBorder("Từ khóa"));

        cboHsLop = new JComboBox<>();
        cboHsLop.setBorder(BorderFactory.createTitledBorder("Lớp"));

        try {
            java.awt.Window w = SwingUtilities.getWindowAncestor(this);
            if (w instanceof com.sgu.qlhs.ui.MainDashboard) {
                com.sgu.qlhs.ui.MainDashboard md = (com.sgu.qlhs.ui.MainDashboard) w;
                NguoiDungDTO nd = md.getNguoiDung();
                LopBUS lopBUS = new LopBUS();
                PhanCongDayBUS pc = new PhanCongDayBUS();

                if (nd != null && "giao_vien".equalsIgnoreCase(nd.getVaiTro())) {
                    // For teachers: do NOT show a global "Tất cả"; show only their classes.
                    // If the teacher is also a chủ nhiệm, include their homeroom class as
                    // well (union).
                    String namHoc = NienKhoaBUS.currentNamHoc();
                    java.util.List<Integer> lopIds = new java.util.ArrayList<>();
                    try {
                        // get union of taught classes + chu nhiem if any
                        lopIds = pc.getDistinctMaLopWithChuNhiem(nd.getId(), namHoc, null);
                    } catch (Exception ex) {
                        // fallback: get only taught classes
                        lopIds = pc.getDistinctMaLopByGiaoVien(nd.getId(), namHoc, null);
                    }

                    // populate combo and mapping list
                    cboLopMaList.clear();
                    cboHsLop.removeAllItems();
                    for (Integer ml : lopIds) {
                        com.sgu.qlhs.dto.LopDTO l = lopBUS.getLopByMa(ml);
                        if (l != null) {
                            cboHsLop.addItem(l.getTenLop());
                            cboLopMaList.add(l.getMaLop());
                        }
                    }
                    if (cboHsLop.getItemCount() > 0)
                        cboHsLop.setSelectedIndex(0);
                } else {
                    // Non-teacher (admin or others): keep a default "Tất cả" + sample
                    cboHsLop.addItem("Tất cả");
                    cboHsLop.addItem("10A1");
                    cboHsLop.addItem("10A2");
                    cboHsLop.addItem("11A1");
                    cboHsLop.addItem("12A1");
                }
            } else {
                cboHsLop.addItem("Tất cả");
                cboHsLop.addItem("10A1");
                cboHsLop.addItem("10A2");
                cboHsLop.addItem("11A1");
                cboHsLop.addItem("12A1");
            }
        } catch (Exception ex) {
            cboHsLop.addItem("Tất cả");
        }

        // ... (Phần còn lại của hàm buildFilter() giữ nguyên) ...
        // Only keep the two realistic options for gender in this app: Nam and Nữ
        cboHsGioiTinh = new JComboBox<>(new String[] { "Tất cả", "Nam", "Nữ" });
        cboHsGioiTinh.setBorder(BorderFactory.createTitledBorder("Giới tính"));

        var btnClear = new JButton("Xóa lọc");
        var btnDetail = new JButton("Chi tiết");

        filter.add(txtHsSearch);
        filter.add(cboHsLop);
        filter.add(cboHsGioiTinh);
        filter.add(btnClear);
        filter.add(btnDetail);

        // --- Sự kiện lọc ---
        txtHsSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                applyHsFilters();
            }

            public void removeUpdate(DocumentEvent e) {
                applyHsFilters();
            }

            public void changedUpdate(DocumentEvent e) {
                applyHsFilters();
            }
        });

        cboHsLop.addActionListener(e -> applyHsFilters());
        cboHsGioiTinh.addActionListener(e -> applyHsFilters());

        btnClear.addActionListener(e -> {
            txtHsSearch.setText("");
            cboHsLop.setSelectedIndex(0);
            cboHsGioiTinh.setSelectedIndex(0);
            applyHsFilters();
        });

        // --- Nút Chi tiết ---
        btnDetail.addActionListener(e -> showDetailDialog());

        return filter;
    }

    private JComponent buildTable() {
        // Default single-table that shows all students or filtered per combo
        tblHS = new JTable(new HocSinhTableModel());
        tblHS.setAutoCreateRowSorter(true);
        hsSorter = new TableRowSorter<>(tblHS.getModel());
        tblHS.setRowSorter(hsSorter);

        // Double-click vào dòng để mở chi tiết
        tblHS.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && tblHS.getSelectedRow() != -1) {
                    showDetailDialog();
                }
            }
        });

        return new JScrollPane(tblHS);
    }

    // Build the tabbed panel for chủ nhiệm: two tabs (Lớp chủ nhiệm, Lớp dạy)
    private JComponent buildTabbedHsPanel(int maGV, int chuNhiemMaLop) {
        hsTabbedPane = new JTabbedPane();
        hsTabbedPane.addTab("Lớp chủ nhiệm", buildHomeroomPanel(maGV, chuNhiemMaLop));
        hsTabbedPane.addTab("Lớp dạy", buildTaughtPanel(maGV, chuNhiemMaLop));
        return hsTabbedPane;
    }

    private JComponent buildHomeroomPanel(int maGV, int maLop) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        // top: search and detail
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        top.setOpaque(false);
        txtHomSearch = new JTextField(18);
        txtHomSearch.setBorder(BorderFactory.createTitledBorder("Từ khóa"));
        JButton btnDetailHom = new JButton("Chi tiết");
        JLabel lblTaughtTop = new JLabel();
        lblTaughtTop.setBorder(new EmptyBorder(6, 6, 6, 6));
        top.add(txtHomSearch);
        top.add(btnDetailHom);
        top.add(lblTaughtTop);

        // table
        homModel = new DefaultTableModel(new Object[] { "Mã", "Họ tên", "Ngày sinh", "Giới tính", "Lớp" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblHomeroom = new JTable(homModel);
        tblHomeroom.setAutoCreateRowSorter(true);
        homSorter = new TableRowSorter<>(tblHomeroom.getModel());
        tblHomeroom.setRowSorter(homSorter);
        tblHomeroom.setRowHeight(24);

        // double-click -> open detail for the selected row
        tblHomeroom.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && tblHomeroom.getSelectedRow() != -1) {
                    int row = tblHomeroom.getSelectedRow();
                    int modelRow = tblHomeroom.convertRowIndexToModel(row);
                    Object[] data = new Object[homModel.getColumnCount()];
                    for (int i = 0; i < homModel.getColumnCount(); i++)
                        data[i] = homModel.getValueAt(modelRow, i);
                    new HocSinhDetailDialog(SwingUtilities.getWindowAncestor(HocSinhPanel.this), data).setVisible(true);
                }
            }
        });

        btnDetailHom.addActionListener(e -> {
            int row = tblHomeroom.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(HocSinhPanel.this, "Vui lòng chọn một học sinh để xem chi tiết!");
                return;
            }
            int modelRow = tblHomeroom.convertRowIndexToModel(row);
            Object[] data = new Object[homModel.getColumnCount()];
            for (int i = 0; i < homModel.getColumnCount(); i++)
                data[i] = homModel.getValueAt(modelRow, i);
            new HocSinhDetailDialog(SwingUtilities.getWindowAncestor(HocSinhPanel.this), data).setVisible(true);
        });

        // load students
        loadStudentsIntoModel(homModel, maLop);

        // show homeroom class name so the chủ nhiệm knows which class they manage
        try {
            LopBUS lopBUS = new LopBUS();
            LopDTO lop = lopBUS.getLopByMa(maLop);
            if (lop != null) {
                lblTaughtTop.setText("Lớp chủ nhiệm: " + lop.getTenLop());
            } else {
                lblTaughtTop.setText("");
            }
        } catch (Exception ex) {
            lblTaughtTop.setText("");
        }

        // search filter listener
        txtHomSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                applyHomFilter();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                applyHomFilter();
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                applyHomFilter();
            }
        });

        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(tblHomeroom), BorderLayout.CENTER);
        panel.add(new JScrollPane(tblHomeroom), BorderLayout.CENTER);

        // If the 'Lớp' column is empty for every row, hide that column to save space.
        javax.swing.SwingUtilities.invokeLater(() -> {
            try {
                boolean allEmpty = true;
                int rowCount = homModel.getRowCount();
                for (int r = 0; r < rowCount; r++) {
                    Object v = homModel.getValueAt(r, 4); // Lớp column index
                    if (v != null && !v.toString().isBlank()) {
                        allEmpty = false;
                        break;
                    }
                }
                if (allEmpty) {
                    // hide the column visually (keep in model)
                    if (tblHomeroom.getColumnModel().getColumnCount() > 4) {
                        try {
                            tblHomeroom.removeColumn(tblHomeroom.getColumnModel().getColumn(4));
                        } catch (Exception ex) {
                            // ignore
                        }
                    }
                }
            } catch (Exception ex) {
                // ignore
            }
        });
        return panel;
    }

    private void applyHomFilter() {
        if (homSorter == null)
            return;
        String kw = txtHomSearch.getText().trim();
        if (kw.isEmpty())
            homSorter.setRowFilter(null);
        else
            homSorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(kw)));
    }

    private JComponent buildTaughtPanel(int maGV, int chuNhiemMaLop) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        top.setOpaque(false);
        txtTaughtSearch = new JTextField(18);
        txtTaughtSearch.setBorder(BorderFactory.createTitledBorder("Từ khóa"));
        cboTaughtLop = new JComboBox<>();
        cboTaughtLop.setBorder(BorderFactory.createTitledBorder("Lớp"));
        JButton btnDetailTaught = new JButton("Chi tiết");
        top.add(txtTaughtSearch);
        top.add(cboTaughtLop);
        top.add(btnDetailTaught);

        taughtModel = new DefaultTableModel(new Object[] { "Mã", "Họ tên", "Ngày sinh", "Giới tính", "Lớp" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblTaught = new JTable(taughtModel);
        tblTaught.setAutoCreateRowSorter(true);
        taughtSorter = new TableRowSorter<>(tblTaught.getModel());
        tblTaught.setRowSorter(taughtSorter);
        tblTaught.setRowHeight(24);

        // populate class list for taught classes (exclude homeroom to avoid dup)
        populateTaughtClassCombo(maGV, chuNhiemMaLop);

        cboTaughtLop.addActionListener(e -> {
            int idx = cboTaughtLop.getSelectedIndex();
            if (idx >= 0 && idx < taughtLopMaList.size()) {
                int maLop = taughtLopMaList.get(idx);
                loadStudentsIntoModel(taughtModel, maLop);
            } else {
                taughtModel.setRowCount(0);
            }
        });

        // double-click -> open detail for the selected row
        tblTaught.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && tblTaught.getSelectedRow() != -1) {
                    int row = tblTaught.getSelectedRow();
                    int modelRow = tblTaught.convertRowIndexToModel(row);
                    Object[] data = new Object[taughtModel.getColumnCount()];
                    for (int i = 0; i < taughtModel.getColumnCount(); i++)
                        data[i] = taughtModel.getValueAt(modelRow, i);
                    new HocSinhDetailDialog(SwingUtilities.getWindowAncestor(HocSinhPanel.this), data).setVisible(true);
                }
            }
        });

        btnDetailTaught.addActionListener(e -> {
            int row = tblTaught.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(HocSinhPanel.this, "Vui lòng chọn một học sinh để xem chi tiết!");
                return;
            }
            int modelRow = tblTaught.convertRowIndexToModel(row);
            Object[] data = new Object[taughtModel.getColumnCount()];
            for (int i = 0; i < taughtModel.getColumnCount(); i++)
                data[i] = taughtModel.getValueAt(modelRow, i);
            new HocSinhDetailDialog(SwingUtilities.getWindowAncestor(HocSinhPanel.this), data).setVisible(true);
        });

        txtTaughtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                applyTaughtFilter();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                applyTaughtFilter();
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                applyTaughtFilter();
            }
        });

        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(tblTaught), BorderLayout.CENTER);
        return panel;
    }

    private void applyTaughtFilter() {
        if (taughtSorter == null)
            return;
        String kw = txtTaughtSearch.getText().trim();
        if (kw.isEmpty())
            taughtSorter.setRowFilter(null);
        else
            taughtSorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(kw)));
    }

    private void populateTaughtClassCombo(int maGV, int chuNhiemMaLop) {
        taughtLopMaList.clear();
        cboTaughtLop.removeAllItems();
        try {
            String namHoc = NienKhoaBUS.currentNamHoc();
            PhanCongDayBUS pc = new PhanCongDayBUS();
            java.util.List<Integer> ids = pc.getDistinctMaLopByGiaoVien(maGV, namHoc, null);
            for (Integer ml : ids) {
                if (ml == null)
                    continue;
                if (chuNhiemMaLop > 0 && ml == chuNhiemMaLop)
                    continue; // skip homeroom
                LopDTO l = new LopBUS().getLopByMa(ml);
                if (l != null) {
                    cboTaughtLop.addItem(l.getTenLop());
                    taughtLopMaList.add(l.getMaLop());
                }
            }
            if (cboTaughtLop.getItemCount() > 0)
                cboTaughtLop.setSelectedIndex(0);
        } catch (Exception ex) {
            System.err.println("Lỗi nạp lớp dạy: " + ex.getMessage());
        }
    }

    private void loadStudentsIntoModel(DefaultTableModel model, int maLop) {
        model.setRowCount(0);
        try {
            HocSinhBUS hsBus = new HocSinhBUS();
            java.util.List<HocSinhDTO> list = hsBus.getHocSinhByMaLop(maLop);
            if (list != null) {
                for (HocSinhDTO hs : list) {
                    model.addRow(new Object[] { hs.getMaHS(), hs.getHoTen(), hs.getNgaySinh(), hs.getGioiTinh(),
                            hs.getTenLop() });
                }
            }
        } catch (Exception ex) {
            System.err.println("Lỗi khi nạp học sinh cho lớp " + maLop + ": " + ex.getMessage());
        }
    }

    private void showDetailDialog() {
        int row = tblHS.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một học sinh để xem chi tiết!");
            return;
        }

        int modelRow = tblHS.convertRowIndexToModel(row);
        TableModel model = tblHS.getModel();

        // Lấy toàn bộ dữ liệu của dòng hiện tại
        Object[] hocSinhData = new Object[model.getColumnCount()];
        for (int i = 0; i < model.getColumnCount(); i++) {
            hocSinhData[i] = model.getValueAt(modelRow, i);
        }

        // Mở dialog chi tiết
        new HocSinhDetailDialog(
                SwingUtilities.getWindowAncestor(this),
                hocSinhData).setVisible(true);
    }

    private void applyHsFilters() {
        if (hsSorter == null)
            return;

        var filters = new ArrayList<RowFilter<TableModel, Object>>();

        // --- Lọc theo từ khóa ---
        String kw = txtHsSearch.getText().trim();
        if (!kw.isEmpty()) {
            filters.add(RowFilter.regexFilter("(?i)" + Pattern.quote(kw)));
        }

        // --- Lọc theo lớp ---
        Object selectedLopObj = cboHsLop.getSelectedItem();
        if (selectedLopObj != null) {
            String lop = String.valueOf(selectedLopObj);
            // If the combo contains a global "Tất cả" item (admins), keep its behavior.
            // For teachers the combo is populated only with their classes (no "Tất cả").
            if (!"Tất cả".equals(lop)) {
                filters.add(RowFilter.regexFilter("^" + Pattern.quote(lop) + "$", 4));
            }
        }

        // --- Lọc theo giới tính ---
        Object selectedGtObj = cboHsGioiTinh.getSelectedItem();
        if (selectedGtObj != null) {
            String gt = String.valueOf(selectedGtObj);
            if (!"Tất cả".equals(gt)) {
                filters.add(RowFilter.regexFilter("^" + Pattern.quote(gt) + "$", 3));
            }
        }

        hsSorter.setRowFilter(filters.isEmpty() ? null : RowFilter.andFilter(filters));
    }
}
