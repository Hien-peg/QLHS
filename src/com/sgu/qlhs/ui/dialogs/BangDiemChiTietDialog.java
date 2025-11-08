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
import com.sgu.qlhs.bus.HanhKiemBUS;
// IMPORT THÊM MONBUS
import com.sgu.qlhs.bus.MonBUS;
import com.sgu.qlhs.dto.HanhKiemDTO;
import com.sgu.qlhs.bus.PhanCongDayBUS;
import com.sgu.qlhs.dto.NguoiDungDTO;
// THÊM: Import NienKhoaBUS
import com.sgu.qlhs.bus.NienKhoaBUS;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Dialog hiển thị bảng điểm chi tiết của học sinh theo định dạng chính thức
 * (ĐÃ CẬP NHẬT ĐỂ HỖ TRỢ MÔN ĐÁNH GIÁ Đ/KĐ)
 */
public class BangDiemChiTietDialog extends JDialog {
    // ... (Giữ nguyên các biến thành viên) ...
    private final JComboBox<String> cboHocSinh = new JComboBox<>();
    private final JComboBox<String> cboLop = new JComboBox<>();
    private final JComboBox<String> cboHocKy = new JComboBox<>(new String[] { "Học kỳ 1", "Học kỳ 2" });
    private final JComboBox<String> cboNamHoc = new JComboBox<>();
    private java.util.List<Integer> nienKhoaIds = new java.util.ArrayList<>();
    private JPanel pnlBangDiem;
    private DefaultTableModel model;
    private JTable table;
    private boolean tableEditing = false;
    private int currentMaHS = -1;
    private int currentHocKy = -1;
    private int currentMaNK = -1;
    private java.util.List<DiemDTO> currentDiemList = new java.util.ArrayList<>();
    private javax.swing.JTextArea txtNhanXet;
    private String currentNhanXet = "";
    private JComboBox<String> cboHanhKiemEditor;
    private JLabel lblHanhKiemValue;
    private String tenHocSinh = "";
    private String tenTruong = "ĐẠI HỌC SÀI GÒN - SGU";
    private String diaPhuong = "SỞ GD&ĐT HỒ CHÍ MINH";
    private final HocSinhBUS hocSinhBUS = new HocSinhBUS();
    private final com.sgu.qlhs.bus.LopBUS lopBUS = new com.sgu.qlhs.bus.LopBUS();
    private final DiemBUS diemBUS = new DiemBUS();
    private final HanhKiemBUS hanhKiemBUS = new HanhKiemBUS();
    private final PhanCongDayBUS phanCongBUS = new PhanCongDayBUS();
    private final MonBUS monBUS = new MonBUS();
    private java.util.List<Integer> lopIds = new java.util.ArrayList<>();
    private boolean suppressLopAction = false;
    private boolean suppressHocSinhAction = false;
    private boolean isStudentView = false;
    private int loggedInStudentMaHS = -1;
    private boolean isTeacherView = false;
    private JButton btnEdit;
    private JButton btnSave;
    private JButton btnCancel;
    private JButton btnImport;
    private int initialMaLopContext = -1;
    private int initialMaHS = -1;
    private java.util.List<Boolean> rowCanEditList = new java.util.ArrayList<>();

    public BangDiemChiTietDialog(Window owner) {
        super(owner, "Bảng điểm chi tiết học sinh", ModalityType.APPLICATION_MODAL);
        // Make dialog occupy the full available screen area (usable bounds)
        // This makes the dialog appear effectively full-screen on multi-monitor setups
        java.awt.Rectangle screenBounds = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getMaximumWindowBounds();
        setBounds(screenBounds);
        // keep a sensible minimum so resizing down doesn't make layout unusable
        setMinimumSize(new Dimension(800, 600));
        setResizable(true);
        setLocationRelativeTo(owner);
        build();
        // load niên khóa list after UI built
        loadNienKhoa();
    }

    /**
     * Allow external callers to pre-select a class before showing the dialog.
     * Call this after constructing the dialog (or before showing) and it will
     * try to select the matching class in the combo.
     */
    public void setInitialMaLop(int maLop) {
        this.initialMaLopContext = maLop;
        // if the list of lớp is already loaded, try to select it now
        if (lopIds != null && lopIds.size() > 0) {
            int idx = lopIds.indexOf(maLop);
            if (idx >= 0 && idx < cboLop.getItemCount()) {
                suppressLopAction = true;
                cboLop.setSelectedIndex(idx);
                // reload students for that class
                loadHocSinh();
                suppressLopAction = false;
            }
        }
    }

    /**
     * Pre-select a student (MaHS) when opening the dialog. Caller should
     * construct the dialog, optionally call this, then show the dialog.
     */
    public void setInitialMaHS(int maHS) {
        this.initialMaHS = maHS;
        // ensure class and students are loaded so selection can be applied
        try {
            loadLop();
        } catch (Exception ex) {
            // ignore
        }
        try {
            loadHocSinh();
        } catch (Exception ex) {
            // ignore
        }

        if (initialMaHS > 0) {
            String prefix = String.valueOf(initialMaHS) + " - ";
            for (int i = 0; i < cboHocSinh.getItemCount(); i++) {
                Object it = cboHocSinh.getItemAt(i);
                if (it != null && it.toString().startsWith(prefix)) {
                    try {
                        suppressHocSinhAction = true;
                        cboHocSinh.setSelectedIndex(i);
                        suppressHocSinhAction = false;
                    } catch (Exception ex) {
                        suppressHocSinhAction = false;
                    }
                    // load the report for this student immediately
                    loadBangDiem();
                    break;
                }
            }
        }
    }

    private void build() {
        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(new EmptyBorder(16, 16, 16, 16));
        setContentPane(root);

        // ===== THANH CHỌN =====
        // Use two sub-panels so filters stay left and action buttons stay right
        // Use a non-floatable JToolBar so buttons stay visible and don't get wrapped
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.setLayout(new BorderLayout());
        toolbar.setPreferredSize(new Dimension(0, 44));
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.X_AXIS));
        rightPanel.setBorder(new EmptyBorder(4, 4, 4, 4));

        // Lọc theo lớp trước khi chọn học sinh
        leftPanel.add(new JLabel("Lớp:"));
        cboLop.setPreferredSize(new Dimension(160, 25));
        leftPanel.add(cboLop);

        leftPanel.add(Box.createHorizontalStrut(10));
        leftPanel.add(new JLabel("Học sinh:"));
        cboHocSinh.setPreferredSize(new Dimension(200, 25));
        leftPanel.add(cboHocSinh);

        leftPanel.add(Box.createHorizontalStrut(10));
        leftPanel.add(new JLabel("Học kỳ:"));
        leftPanel.add(cboHocKy);

        leftPanel.add(Box.createHorizontalStrut(10));
        leftPanel.add(new JLabel("Năm học:"));
        leftPanel.add(cboNamHoc);

        JButton btnLoad = new JButton("Xem bảng điểm");
        btnEdit = new JButton("Sửa");
        btnSave = new JButton("Lưu");
        btnSave.setEnabled(false);
        btnCancel = new JButton("Hủy");
        btnCancel.setEnabled(false);
        btnImport = new JButton("Nhập CSV");
        JButton btnExport = new JButton("Xuất CSV");
        JButton btnPrint = new JButton("In");
        JButton btnClose = new JButton("Đóng");

        // push buttons to the right
        rightPanel.add(Box.createHorizontalGlue());
        rightPanel.add(btnLoad);
        rightPanel.add(Box.createHorizontalStrut(6));
        rightPanel.add(btnEdit);
        rightPanel.add(Box.createHorizontalStrut(6));
        rightPanel.add(btnSave);
        rightPanel.add(Box.createHorizontalStrut(6));
        rightPanel.add(btnCancel);
        rightPanel.add(Box.createHorizontalStrut(6));
        // Import button: visible only to admin or teachers (further per-row checks
        // will be applied during import). Students won't see it.
        rightPanel.add(btnImport);
        rightPanel.add(Box.createHorizontalStrut(6));
        rightPanel.add(btnExport);
        rightPanel.add(Box.createHorizontalStrut(6));
        rightPanel.add(btnPrint);
        rightPanel.add(Box.createHorizontalStrut(6));
        rightPanel.add(btnClose);

        toolbar.add(leftPanel, BorderLayout.WEST);
        toolbar.add(rightPanel, BorderLayout.EAST);
        root.add(toolbar, BorderLayout.NORTH);

        // --- Determine the current user role and adjust UI accordingly ---
        try {
            java.awt.Window w = javax.swing.SwingUtilities.getWindowAncestor(this);
            if (w instanceof com.sgu.qlhs.ui.MainDashboard) {
                com.sgu.qlhs.ui.MainDashboard md = (com.sgu.qlhs.ui.MainDashboard) w;
                NguoiDungDTO nd = md.getNguoiDung();
                if (nd != null && "hoc_sinh".equalsIgnoreCase(nd.getVaiTro())) {
                    // Student: only viewing permitted; disable edit controls
                    isStudentView = true;
                    loggedInStudentMaHS = nd.getId(); // mapping assumed: NguoiDung.id -> HocSinh.MaHS
                    btnEdit.setEnabled(false);
                    btnSave.setEnabled(false);
                    btnCancel.setEnabled(false);
                    // keep btnExport/btnPrint enabled so student can export/print their own report
                } else if (nd != null && "giao_vien".equalsIgnoreCase(nd.getVaiTro())) {
                    // Teacher: hide or disable filters that are unnecessary when the dialog is
                    // opened from the panel for a specific student. Keep editing controls
                    // available according to permission checks.
                    isTeacherView = true;
                    // hide the class and student selectors from the filter bar to focus
                    // teacher on the selected student and editing area
                    try {
                        cboLop.setVisible(false);
                        cboHocSinh.setVisible(false);
                        // also hide the corresponding labels in the left panel if present
                        java.awt.Container parent = cboLop.getParent();
                        if (parent instanceof JPanel) {
                            Component[] comps = parent.getComponents();
                            for (int i = 0; i < comps.length; i++) {
                                if (comps[i] instanceof JLabel) {
                                    String txt = ((JLabel) comps[i]).getText();
                                    if (txt != null && (txt.contains("Lớp") || txt.contains("Học sinh"))) {
                                        comps[i].setVisible(false);
                                    }
                                }
                            }
                        }
                    } catch (Exception ex) {
                        // non-fatal: if hiding labels fails, continue with combos hidden
                    }
                    // Teachers may be allowed to import; show the button but enable it
                    // only when the loaded dataset contains at least one row they can edit.
                    try {
                        btnImport.setVisible(true);
                        btnImport.setEnabled(false); // per-row enablement happens in loadBangDiem()
                    } catch (Exception ex) {
                    }
                }
            }
            // Admin users: enable import immediately
            try {
                java.awt.Window w2 = javax.swing.SwingUtilities.getWindowAncestor(this);
                if (w2 instanceof com.sgu.qlhs.ui.MainDashboard) {
                    com.sgu.qlhs.ui.MainDashboard md2 = (com.sgu.qlhs.ui.MainDashboard) w2;
                    NguoiDungDTO nd2 = md2.getNguoiDung();
                    if (nd2 != null && ("quan_tri_vien".equalsIgnoreCase(nd2.getVaiTro())
                            || "Admin".equalsIgnoreCase(nd2.getVaiTro()))) {
                        try {
                            btnImport.setVisible(true);
                            btnImport.setEnabled(true);
                        } catch (Exception ex) {
                        }
                    }
                }
            } catch (Exception ex) {
            }
        } catch (Exception ex) {
            // ignore and keep full UI for safety
        }

        // ===== PANEL HIỂN THỊ BẢNG ĐIỂM =====
        pnlBangDiem = new JPanel(new BorderLayout());
        pnlBangDiem.setBackground(Color.WHITE);
        pnlBangDiem.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));

        JScrollPane scrollPane = new JScrollPane(pnlBangDiem);
        scrollPane.getViewport().setBackground(Color.WHITE);
        root.add(scrollPane, BorderLayout.CENTER);

        // ===== SỰ KIỆN =====
        btnLoad.addActionListener(e -> loadBangDiem());
        btnEdit.addActionListener(e -> {
            tableEditing = true;
            btnSave.setEnabled(true);
            btnCancel.setEnabled(true);
            btnEdit.setEnabled(false);
            // refresh table model so isCellEditable takes effect
            table.revalidate();
            table.repaint();
            // allow editing of overall teacher comment if present
            if (txtNhanXet != null) {
                txtNhanXet.setEditable(true);
                txtNhanXet.requestFocusInWindow();
            }
            // allow editing of Hạnh kiểm via inline combobox only when allowed
            if (cboHanhKiemEditor != null) {
                boolean allowHk = false;
                try {
                    com.sgu.qlhs.dto.NguoiDungDTO ndLocal = null;
                    try {
                        java.awt.Window w2 = javax.swing.SwingUtilities.getWindowAncestor(this);
                        if (w2 instanceof com.sgu.qlhs.ui.MainDashboard) {
                            com.sgu.qlhs.ui.MainDashboard md2 = (com.sgu.qlhs.ui.MainDashboard) w2;
                            ndLocal = md2.getNguoiDung();
                        }
                    } catch (Exception ex) {
                    }
                    if (ndLocal != null) {
                        if ("quan_tri_vien".equalsIgnoreCase(ndLocal.getVaiTro())
                                || "Admin".equalsIgnoreCase(ndLocal.getVaiTro())) {
                            allowHk = true;
                        } else if ("giao_vien".equalsIgnoreCase(ndLocal.getVaiTro())) {
                            try {
                                com.sgu.qlhs.bus.ChuNhiemBUS cnBUS = new com.sgu.qlhs.bus.ChuNhiemBUS();
                                var cn = cnBUS.getChuNhiemByGV(ndLocal.getId());
                                if (cn != null && cn.getMaLop() > 0 && currentMaHS > 0) {
                                    com.sgu.qlhs.dto.HocSinhDTO hs = hocSinhBUS.getHocSinhByMaHS(currentMaHS);
                                    if (hs != null && hs.getMaLop() == cn.getMaLop())
                                        allowHk = true;
                                }
                            } catch (Exception ex) {
                                // ignore
                            }
                        }
                    }
                } catch (Exception ex) {
                }

                if (allowHk) {
                    cboHanhKiemEditor.setEnabled(true);
                    cboHanhKiemEditor.setVisible(true);
                    cboHanhKiemEditor.requestFocusInWindow();
                    if (lblHanhKiemValue != null) {
                        lblHanhKiemValue.setVisible(false);
                    }
                } else {
                    // not allowed: keep editor hidden/disabled and keep value label visible
                    cboHanhKiemEditor.setEnabled(false);
                    cboHanhKiemEditor.setVisible(false);
                    if (lblHanhKiemValue != null)
                        lblHanhKiemValue.setVisible(true);
                }
            }
        });
        btnSave.addActionListener(e -> {
            if (saveBangDiemEdits()) {
                btnEdit.setEnabled(true);
                btnSave.setEnabled(false);
                btnCancel.setEnabled(false);
            }
        });
        btnCancel.addActionListener(e -> {
            // revert edits in the table to currentDiemList values
            if (currentDiemList != null && !currentDiemList.isEmpty()) {
                for (int i = 0; i < currentDiemList.size() && i < model.getRowCount(); i++) {
                    DiemDTO dto = currentDiemList.get(i);
                    // THAY ĐỔI: Phục hồi dựa trên LoaiMon
                    if ("DanhGia".equals(dto.getLoaiMon())) {
                        model.setValueAt(null, i, 2);
                        model.setValueAt(null, i, 3);
                        model.setValueAt(null, i, 4);
                        model.setValueAt(null, i, 5);
                        model.setValueAt(dto.getKetQuaDanhGia(), i, 6); // Cột Kết quả
                    } else {
                        model.setValueAt(dto.getDiemMieng(), i, 2);
                        model.setValueAt(dto.getDiem15p(), i, 3);
                        model.setValueAt(dto.getDiemGiuaKy(), i, 4);
                        model.setValueAt(dto.getDiemCuoiKy(), i, 5);
                        // Tính lại TB
                        double tb = Math
                                .round((dto.getDiemMieng() * 0.10 + dto.getDiem15p() * 0.20 + dto.getDiemGiuaKy() * 0.30
                                        + dto.getDiemCuoiKy() * 0.40) * 10.0)
                                / 10.0;
                        model.setValueAt(tb, i, 6); // Cột Kết quả
                    }
                    model.setValueAt(dto.getGhiChu() != null ? dto.getGhiChu() : "", i, 7);
                }
            }
            if (txtNhanXet != null) {
                txtNhanXet.setText(currentNhanXet != null ? currentNhanXet : "");
                txtNhanXet.setEditable(false);
            }
            if (cboHanhKiemEditor != null) {
                try {
                    com.sgu.qlhs.dto.NguoiDungDTO ndLocal = null;
                    try {
                        java.awt.Window w2 = javax.swing.SwingUtilities.getWindowAncestor(this);
                        if (w2 instanceof com.sgu.qlhs.ui.MainDashboard) {
                            com.sgu.qlhs.ui.MainDashboard md2 = (com.sgu.qlhs.ui.MainDashboard) w2;
                            ndLocal = md2.getNguoiDung();
                        }
                    } catch (Exception ex) {
                    }
                    HanhKiemDTO hk = hanhKiemBUS.getHanhKiem(currentMaHS, currentMaNK, currentHocKy, ndLocal);
                    String hkStr = hk != null ? hk.getXepLoai() : null;
                    cboHanhKiemEditor.setSelectedItem(hkStr != null ? hkStr : "Trung bình");
                } catch (Exception ex) {
                    cboHanhKiemEditor.setSelectedItem("Trung bình");
                }
                cboHanhKiemEditor.setEnabled(false);
                cboHanhKiemEditor.setVisible(false);

                if (lblHanhKiemValue != null) {
                    String hkStr = "(chưa có)";
                    try {
                        com.sgu.qlhs.dto.NguoiDungDTO ndLocal2 = null;
                        java.awt.Window w3 = javax.swing.SwingUtilities.getWindowAncestor(this);
                        if (w3 instanceof com.sgu.qlhs.ui.MainDashboard) {
                            com.sgu.qlhs.ui.MainDashboard md3 = (com.sgu.qlhs.ui.MainDashboard) w3;
                            ndLocal2 = md3.getNguoiDung();
                        }
                        HanhKiemDTO hk = hanhKiemBUS.getHanhKiem(currentMaHS, currentMaNK, currentHocKy, ndLocal2);
                        hkStr = hk != null && hk.getXepLoai() != null ? hk.getXepLoai() : "(chưa có)";
                    } catch (Exception ex) {
                    }
                    lblHanhKiemValue.setText(hkStr);
                    lblHanhKiemValue.setVisible(true);
                }
            }

            tableEditing = false;
            btnEdit.setEnabled(true);
            btnSave.setEnabled(false);
            btnCancel.setEnabled(false);
        });

        // Import/Export using Excel (.xlsx) instead of CSV. Uses Apache POI if
        // available.
        btnImport.addActionListener(e -> {
            // Prompt user to choose an .xlsx file to import
            if (currentMaHS == -1) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn một học sinh trước khi nhập dữ liệu.", "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Chọn file Excel (.xlsx) để nhập");
            chooser.setFileFilter(new FileNameExtensionFilter("Excel files", "xlsx"));
            int rc = chooser.showOpenDialog(this);
            if (rc != JFileChooser.APPROVE_OPTION)
                return;
            File f = chooser.getSelectedFile();
            try {
                importXlsx(f);
                loadBangDiem();
            } catch (ClassNotFoundException cnf) {
                JOptionPane.showMessageDialog(this,
                        "Thư viện Apache POI không được tìm thấy. Vui lòng thêm poi-ooxml jars vào thư mục lib/.",
                        "Thiếu thư viện", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi khi nhập file Excel: " + ex.getMessage(), "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
        btnExport.addActionListener(e -> {
            try {
                exportXlsx();
            } catch (ClassNotFoundException cnf) {
                JOptionPane.showMessageDialog(this,
                        "Thư viện Apache POI không được tìm thấy. Vui lòng thêm poi-ooxml jars vào thư mục lib/.",
                        "Thiếu thư viện", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                // Print full stack to console for debugging and show a more informative
                // message to the user (include cause if message is null).
                ex.printStackTrace();
                String msg = ex.getMessage();
                if (msg == null && ex.getCause() != null)
                    msg = ex.getCause().toString();
                if (msg == null)
                    msg = ex.toString();
                JOptionPane.showMessageDialog(this, "Lỗi khi xuất file Excel: " + msg, "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
        btnPrint.addActionListener(e -> printBangDiem());
        btnClose.addActionListener(e -> dispose());

        // Load classes first, then students
        loadLop();
        loadHocSinh();

        // When lớp selection changes, reload students for that lớp (guarded)
        cboLop.addActionListener(e -> {
            if (!suppressLopAction) {
                loadHocSinh();
            }
        });

        // When a student is chosen, try to auto-select the student's class in cboLop
        cboHocSinh.addActionListener(e -> {
            if (suppressHocSinhAction)
                return;
            Object sel = cboHocSinh.getSelectedItem();
            if (sel == null)
                return;
            String s = sel.toString();
            int dash = s.indexOf(" - ");
            int maHS = 0;
            try {
                maHS = Integer.parseInt(dash > 0 ? s.substring(0, dash).trim() : s);
            } catch (Exception ex) {
                return;
            }
            com.sgu.qlhs.dto.HocSinhDTO hs = hocSinhBUS.getHocSinhByMaHS(maHS);
            if (hs != null && hs.getTenLop() != null) {
                // find matching item in cboLop
                for (int i = 0; i < cboLop.getItemCount(); i++) {
                    String item = cboLop.getItemAt(i);
                    if (item != null && item.equals(hs.getTenLop())) {
                        suppressLopAction = true;
                        cboLop.setSelectedIndex(i);
                        suppressLopAction = false;
                        break;
                    }
                }
            }
        });
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

    /** Load lớp options from DB into cboLop and lopIds */
    private void loadLop() {
        cboLop.removeAllItems();
        lopIds.clear();
        // add a "Tất cả" option at index 0
        cboLop.addItem("Tất cả");
        lopIds.add(0);
        try {
            // If current user is teacher, show only classes from PhanCongDay for the
            // selected Niên khóa
            java.awt.Window w = javax.swing.SwingUtilities.getWindowAncestor(this);
            if (w instanceof com.sgu.qlhs.ui.MainDashboard) {
                com.sgu.qlhs.ui.MainDashboard md = (com.sgu.qlhs.ui.MainDashboard) w;
                com.sgu.qlhs.dto.NguoiDungDTO nd = md.getNguoiDung();
                if (nd != null && "giao_vien".equalsIgnoreCase(nd.getVaiTro())) {

                    // === PHẦN SỬA ===
                    int maNK = com.sgu.qlhs.bus.NienKhoaBUS.current();
                    int selNk = cboNamHoc.getSelectedIndex();
                    if (selNk >= 0 && selNk < nienKhoaIds.size())
                        maNK = nienKhoaIds.get(selNk);
                    String namHoc = (new NienKhoaBUS()).getNamHocString(maNK); // Lấy chuỗi năm học

                    int hkIdx = cboHocKy.getSelectedIndex();
                    // Chuyển Integer (0, 1) -> String ("HK1", "HK2")
                    String hkParam = (hkIdx >= 0) ? ("HK" + (hkIdx + 1)) : null;

                    java.util.List<Integer> lopIdsAssigned = phanCongBUS.getDistinctMaLopByGiaoVien(nd.getId(), namHoc,
                            hkParam);
                    // === KẾT THÚC PHẦN SỬA ===

                    java.util.List<com.sgu.qlhs.dto.LopDTO> list = lopBUS.getAllLop();
                    for (com.sgu.qlhs.dto.LopDTO l : list) {
                        if (lopIdsAssigned.contains(l.getMaLop())) {
                            cboLop.addItem(l.getTenLop());
                            lopIds.add(l.getMaLop());
                        }
                    }
                    if (cboLop.getItemCount() > 0)
                        cboLop.setSelectedIndex(0);
                    return;
                }
                // ... (Phần code cho "hoc_sinh" giữ nguyên) ...
                if (nd != null && "hoc_sinh".equalsIgnoreCase(nd.getVaiTro())) {
                    try {
                        int maHS = nd.getId();
                        HocSinhDTO hs = hocSinhBUS.getHocSinhByMaHS(maHS);
                        cboLop.removeAllItems();
                        lopIds.clear();
                        if (hs != null && hs.getTenLop() != null) {
                            cboLop.addItem(hs.getTenLop());
                            // HocSinhDTO does not carry MaLop id; use placeholder 0 and
                            // short-circuit student loading elsewhere
                            lopIds.add(0);
                        } else {
                            cboLop.addItem("(Không xác định)");
                            lopIds.add(0);
                        }
                        cboLop.setSelectedIndex(0);
                        cboLop.setEnabled(false);
                        // also populate the student combo with only this student and disable it
                        cboHocSinh.removeAllItems();
                        String label = maHS + " - " + (hs != null ? hs.getHoTen() : "Học sinh");
                        cboHocSinh.addItem(label);
                        cboHocSinh.setSelectedIndex(0);
                        cboHocSinh.setEnabled(false);
                        return;
                    } catch (Exception ex) {
                        // fallback to default behavior
                    }
                }
            }
            java.util.List<com.sgu.qlhs.dto.LopDTO> list = lopBUS.getAllLop();
            for (com.sgu.qlhs.dto.LopDTO l : list) {
                cboLop.addItem(l.getTenLop());
                lopIds.add(l.getMaLop());
            }
        } catch (Exception ex) {
            // fallback: add an "Tất cả" option
        }
        if (cboLop.getItemCount() > 0)
            cboLop.setSelectedIndex(0);
        // If an initial class context was provided, try to select it now
        if (initialMaLopContext >= 0) {
            int idx = lopIds.indexOf(initialMaLopContext);
            if (idx >= 0 && idx < cboLop.getItemCount()) {
                suppressLopAction = true;
                cboLop.setSelectedIndex(idx);
                // reload students for that class
                loadHocSinh();
                suppressLopAction = false;
            }
        }
    }

    private void loadHocSinh() {
        // Load students from BUS
        cboHocSinh.removeAllItems();
        suppressHocSinhAction = true;
        // If current user is a student, show only that student and return
        try {
            java.awt.Window w = javax.swing.SwingUtilities.getWindowAncestor(this);
            if (w instanceof com.sgu.qlhs.ui.MainDashboard) {
                com.sgu.qlhs.ui.MainDashboard md = (com.sgu.qlhs.ui.MainDashboard) w;
                NguoiDungDTO nd = md.getNguoiDung();
                if (nd != null && "hoc_sinh".equalsIgnoreCase(nd.getVaiTro())) {
                    int maHS = nd.getId();
                    HocSinhDTO hs = hocSinhBUS.getHocSinhByMaHS(maHS);
                    cboHocSinh.removeAllItems();
                    cboHocSinh.addItem(maHS + " - " + (hs != null ? hs.getHoTen() : "Học sinh"));
                    cboHocSinh.setSelectedIndex(0);
                    cboHocSinh.setEnabled(false);
                    suppressHocSinhAction = false;
                    return;
                }
            }
        } catch (Exception ex) {
            // ignore and fall back to normal loading
        }
        java.util.List<HocSinhDTO> list;
        int sel = cboLop.getSelectedIndex();
        if (sel >= 0 && sel < lopIds.size()) {
            int maLop = lopIds.get(sel);
            if (maLop == 0) {
                list = hocSinhBUS.getAllHocSinh();
            } else {
                list = hocSinhBUS.getHocSinhByMaLop(maLop);
            }
        } else {
            list = hocSinhBUS.getAllHocSinh();
        }
        for (HocSinhDTO h : list) {
            // store item as "<MaHS> - <HoTen>" so we can parse ID back
            cboHocSinh.addItem(h.getMaHS() + " - " + h.getHoTen());
        }
        suppressHocSinhAction = false;
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
        // SỬA: Bọc header trong một panel căn giữa
        JPanel headerWrapper = new JPanel(new BorderLayout());
        headerWrapper.setOpaque(false);
        headerWrapper.add(headerPanel, BorderLayout.CENTER);
        headerWrapper.setAlignmentX(Component.CENTER_ALIGNMENT); // Căn giữa khối này
        // (Để header không bị co lại)
        headerWrapper.setMaximumSize(new Dimension(900, headerPanel.getPreferredSize().height));

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
        content.add(headerWrapper); // Thêm panel bọc
        content.add(Box.createVerticalStrut(30));

        // ===== TIÊU ĐỀ (Giữ căn giữa) =====
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
        String[] columns = { "STT", "Tên môn học", "Miệng", "15 Phút", "1 Tiết", "Cuối kỳ", "Kết quả", "Ghi chú" };

        Object sel = cboHocSinh.getSelectedItem();
        if (sel == null) {
            model = new DefaultTableModel(columns, 0);
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
            int hkNum = cboHocKy.getSelectedIndex() + 1;
            int maNK = com.sgu.qlhs.bus.NienKhoaBUS.current();
            int selNk = cboNamHoc.getSelectedIndex();
            if (selNk >= 0 && selNk < nienKhoaIds.size()) {
                maNK = nienKhoaIds.get(selNk);
            }

            com.sgu.qlhs.dto.NguoiDungDTO nd = null;
            // prefer an injected user (if caller provided one), otherwise resolve
            // from the window ancestor
            if (this.injectedNguoiDung != null) {
                nd = this.injectedNguoiDung;
            } else {
                try {
                    java.awt.Window w = javax.swing.SwingUtilities.getWindowAncestor(this);
                    if (w instanceof com.sgu.qlhs.ui.MainDashboard) {
                        com.sgu.qlhs.ui.MainDashboard md = (com.sgu.qlhs.ui.MainDashboard) w;
                        nd = md.getNguoiDung();
                    }
                } catch (Exception ex) {
                }
            }

            // Lấy dữ liệu điểm
            java.util.List<DiemDTO> diemList = diemBUS.getDiemByMaHS(maHS, hkNum, maNK, nd);
            currentMaHS = maHS;
            currentHocKy = hkNum;
            currentMaNK = maNK;
            currentDiemList = diemList;

            // Tính toán quyền sửa
            rowCanEditList.clear();
            boolean anyRowEditable = false;
            for (int i = 0; i < diemList.size(); i++) {
                rowCanEditList.add(Boolean.FALSE);
            }
            try {

                com.sgu.qlhs.dto.NguoiDungDTO ndCheck = nd;
                if (ndCheck != null && "giao_vien".equalsIgnoreCase(ndCheck.getVaiTro())) {
                    for (int i = 0; i < diemList.size(); i++) {
                        DiemDTO d = diemList.get(i);
                        boolean ok = diemBUS.isTeacherAssignedPublic(ndCheck.getId(), maHS, d.getMaMon(), hkNum,
                                maNK);
                        rowCanEditList.set(i, ok);
                        if (ok)
                            anyRowEditable = true;
                    }
                } else if (ndCheck != null && "quan_tri_vien".equalsIgnoreCase(ndCheck.getVaiTro())) {
                    for (int i = 0; i < rowCanEditList.size(); i++)
                        rowCanEditList.set(i, Boolean.TRUE);
                    anyRowEditable = true;
                }
            } catch (Exception ex) {
                for (int i = 0; i < rowCanEditList.size(); i++)
                    rowCanEditList.set(i, Boolean.FALSE);
            }

            // Tạo model
            model = new DefaultTableModel(columns, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    if (!tableEditing)
                        return false;
                    if (row < 0 || row >= rowCanEditList.size())
                        return false;
                    Boolean allowed = rowCanEditList.get(row);
                    if (allowed == null || !allowed.booleanValue())
                        return false;
                    if (row < 0 || row >= currentDiemList.size())
                        return false;
                    DiemDTO dto = currentDiemList.get(row);
                    String loaiMon = dto.getLoaiMon();
                    if ("DanhGia".equals(loaiMon)) {
                        return (column == 6 || column == 7);
                    } else {
                        return (column >= 2 && column <= 5) || (column == 7);
                    }
                }

                @Override
                public Class<?> getColumnClass(int columnIndex) {
                    if (columnIndex == 6)
                        return Object.class;
                    if (columnIndex >= 2 && columnIndex <= 5)
                        return Double.class;
                    return String.class;
                }
            };

            // Điền dữ liệu vào model
            int idx = 1;
            for (DiemDTO d : diemList) {
                String loaiMon = d.getLoaiMon();
                if ("DanhGia".equals(loaiMon)) {
                    model.addRow(new Object[] {
                            String.valueOf(idx++),
                            d.getTenMon(),
                            null, null, null, null,
                            d.getKetQuaDanhGia(),
                            d.getGhiChu() != null ? d.getGhiChu() : ""
                    });
                } else {
                    double mieng = d.getDiemMieng();
                    double p15 = d.getDiem15p();
                    double gk = d.getDiemGiuaKy();
                    double ck = d.getDiemCuoiKy();
                    double tb = Math.round((mieng * 0.10 + p15 * 0.20 + gk * 0.30 + ck * 0.40) * 10.0) / 10.0;
                    model.addRow(new Object[] {
                            String.valueOf(idx++),
                            d.getTenMon(),
                            mieng, p15, gk, ck,
                            tb,
                            d.getGhiChu() != null ? d.getGhiChu() : ""
                    });
                }
            }
            table = new JTable(model);

            // Thêm CellEditor
            JComboBox<String> danhGiaEditor = new JComboBox<>(new String[] { "Đ", "KĐ" });
            table.getColumnModel().getColumn(6).setCellEditor(new DefaultCellEditor(danhGiaEditor));

            boolean canEdit = (!isStudentView) && anyRowEditable;

            boolean canEditHanhKiem = false;
            try {
                if (nd != null) {
                    if ("quan_tri_vien".equalsIgnoreCase(nd.getVaiTro()) || "Admin".equalsIgnoreCase(nd.getVaiTro())) {
                        canEditHanhKiem = true;
                    } else if ("giao_vien".equalsIgnoreCase(nd.getVaiTro())) {
                        // If dialog was opened from Chủ nhiệm tab, allow editing Hạnh kiểm
                        if (openedFromChuNhiem) {
                            canEditHanhKiem = true;
                        } else {
                            try {
                                HocSinhDTO _hs = hocSinhBUS.getHocSinhByMaHS(maHS);
                                if (_hs != null) {
                                    com.sgu.qlhs.bus.ChuNhiemBUS cnBUS = new com.sgu.qlhs.bus.ChuNhiemBUS();
                                    com.sgu.qlhs.dto.ChuNhiemDTO cn = cnBUS.getChuNhiemByGV(nd.getId());
                                    if (cn != null && cn.getMaLop() == _hs.getMaLop()) {
                                        canEditHanhKiem = true;
                                    }
                                }
                            } catch (Exception ex) {
                                // conservative: leave as false on error
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                // ignore and keep canEditHanhKiem = false
            }

            // === SỬA KHỐI HIỂN THỊ HẠNH KIỂM / XẾP HẠNG ===

            // --- 1. Lấy dữ liệu Hạnh kiểm ---
            HanhKiemDTO hk = hanhKiemBUS.getHanhKiem(maHS, maNK, hkNum, nd);
            String hanhKiemStr = hk != null ? hk.getXepLoai() : "(chưa có)";

            // --- 2. Tính toán Xếp hạng, ĐTB, và Học Lực ---
            double myTBHK = 0.0;
            int rank = 0;
            int siSo = 0;
            String hocLucStr = "(chưa có)";

            try {
                HocSinhDTO hs = hocSinhBUS.getHocSinhByMaHS(maHS);
                if (hs != null && hs.getMaLop() != 0) {
                    int maLop = hs.getMaLop();
                    myTBHK = getStudentTBHK(maHS, hkNum, maNK, nd);
                    java.util.List<Double> classScores = getClassTBHKs(maLop, hkNum, maNK);

                    if (myTBHK >= 8.0)
                        hocLucStr = "Giỏi";
                    else if (myTBHK >= 6.5)
                        hocLucStr = "Khá";
                    else if (myTBHK >= 5.0)
                        hocLucStr = "Trung bình";
                    else
                        hocLucStr = "Yếu";

                    if (classScores != null && !classScores.isEmpty()) {
                        classScores.sort(java.util.Comparator.reverseOrder());
                        siSo = classScores.size();
                        for (int i = 0; i < classScores.size(); i++) {
                            if (Math.abs(classScores.get(i) - myTBHK) < 0.001) {
                                rank = i + 1;
                                break;
                            }
                        }
                        if (rank == 0) {
                            for (int i = 0; i < classScores.size(); i++) {
                                if (myTBHK >= classScores.get(i) - 0.001) {
                                    rank = i + 1;
                                    break;
                                }
                            }
                            if (rank == 0)
                                rank = classScores.size() + 1;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // --- 3. Tạo Panel hiển thị (dùng FlowLayout sát lề trái) ---

            // SỬA: Bọc pnlFooterInfo trong một panel căn giữa (tableWrapper)
            // để khối này căn giữa, nhưng nội dung bên trong nó căn trái.
            JPanel footerWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
            footerWrapper.setOpaque(false);
            footerWrapper.setAlignmentX(Component.CENTER_ALIGNMENT); // Căn giữa khối
            // Set kích thước cố định để nó khớp với bảng
            footerWrapper.setPreferredSize(new Dimension(900, 30));
            footerWrapper.setMaximumSize(new Dimension(900, 30));

            JPanel pnlFooterInfo = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            pnlFooterInfo.setBackground(Color.WHITE);
            // Kích thước của panel con này nên vừa đủ nội dung
            pnlFooterInfo.setPreferredSize(new Dimension(900, 30));

            Font infoFont = new Font("Arial", Font.BOLD, 13);

            // 3.1. Hạnh kiểm
            JLabel lblHKLabel = new JLabel("Hạnh kiểm:");
            lblHKLabel.setFont(infoFont);
            lblHKLabel.setForeground(Color.BLACK);
            pnlFooterInfo.add(lblHKLabel);

            lblHanhKiemValue = new JLabel(hanhKiemStr);
            lblHanhKiemValue.setFont(infoFont);
            lblHanhKiemValue.setForeground(Color.BLACK);
            pnlFooterInfo.add(lblHanhKiemValue);

            if (cboHanhKiemEditor == null) {
                cboHanhKiemEditor = new JComboBox<>(new String[] { "Tốt", "Khá", "Trung bình", "Yếu" });
            }
            if (hk != null && hk.getXepLoai() != null) {
                String cur = hk.getXepLoai();
                boolean matched = false;
                for (int i = 0; i < cboHanhKiemEditor.getItemCount(); i++) {
                    if (cboHanhKiemEditor.getItemAt(i).equals(cur)) {
                        cboHanhKiemEditor.setSelectedItem(cur);
                        matched = true;
                        break;
                    }
                }
                if (!matched)
                    cboHanhKiemEditor.setSelectedItem("Trung bình");
            } else {
                cboHanhKiemEditor.setSelectedItem("Trung bình");
            }
            cboHanhKiemEditor.setEnabled(tableEditing);
            cboHanhKiemEditor.setVisible(tableEditing);
            lblHanhKiemValue.setVisible(!tableEditing);
            pnlFooterInfo.add(cboHanhKiemEditor);

            // 3.2. Học Lực
            pnlFooterInfo.add(Box.createHorizontalStrut(20));
            JLabel lblHocLucLabel = new JLabel("Học lực:");
            lblHocLucLabel.setFont(infoFont);
            lblHocLucLabel.setForeground(Color.BLACK);
            pnlFooterInfo.add(lblHocLucLabel);

            JLabel lblHocLucValue = new JLabel(hocLucStr);
            lblHocLucValue.setFont(infoFont);
            lblHocLucValue.setForeground(Color.BLACK);
            pnlFooterInfo.add(lblHocLucValue);

            // 3.3. Xếp hạng
            pnlFooterInfo.add(Box.createHorizontalStrut(20));
            JLabel lblRankLabel = new JLabel("Xếp hạng:");
            lblRankLabel.setFont(infoFont);
            lblRankLabel.setForeground(Color.BLACK);
            pnlFooterInfo.add(lblRankLabel);

            String rankText = String.format("%d / %d (Lớp)", rank, siSo);
            JLabel lblRankValue = new JLabel(rankText);
            lblRankValue.setFont(infoFont);
            lblRankValue.setForeground(Color.BLACK);
            pnlFooterInfo.add(lblRankValue);

            // 3.4. Điểm trung bình
            pnlFooterInfo.add(Box.createHorizontalStrut(20));
            JLabel lblTBLabel = new JLabel("Điểm TB Học kỳ:");
            lblTBLabel.setFont(infoFont);
            lblTBLabel.setForeground(Color.BLACK);
            pnlFooterInfo.add(lblTBLabel);

            String tbText = String.format("%.2f", myTBHK);
            JLabel lblTBValue = new JLabel(tbText);
            lblTBValue.setFont(infoFont);
            lblTBValue.setForeground(Color.BLACK);
            pnlFooterInfo.add(lblTBValue);

            footerWrapper.add(pnlFooterInfo); // Thêm panel FlowLayout(LEFT) vào panel FlowLayout(CENTER)

            content.add(Box.createVerticalStrut(10));
            content.add(footerWrapper); // Thêm panel bọc

            // Cập nhật quyền (giữ nguyên)
            try {
                if (btnEdit != null)
                    // allow Edit when there is any subject-edit permission OR when the
                    // user is allowed to edit Hạnh kiểm (GVCN or admin)
                    btnEdit.setEnabled(canEdit || canEditHanhKiem);
                if (btnImport != null) {
                    boolean adminNow = false;
                    try {
                        java.awt.Window w2 = javax.swing.SwingUtilities.getWindowAncestor(this);
                        if (w2 instanceof com.sgu.qlhs.ui.MainDashboard) {
                            com.sgu.qlhs.ui.MainDashboard md2 = (com.sgu.qlhs.ui.MainDashboard) w2;
                            com.sgu.qlhs.dto.NguoiDungDTO nd2 = md2.getNguoiDung();
                            if (nd2 != null && ("quan_tri_vien".equalsIgnoreCase(nd2.getVaiTro())
                                    || "Admin".equalsIgnoreCase(nd2.getVaiTro())))
                                adminNow = true;
                        }
                    } catch (Exception ex) {
                    }
                    btnImport.setEnabled(canEdit || adminNow);
                }
                // Only hide/disable save/cancel/hanhkiem controls when the user has
                // neither subject-edit rights nor hạnh kiểm edit rights.
                if (!canEdit && !canEditHanhKiem) {
                    if (btnSave != null)
                        btnSave.setEnabled(false);
                    if (btnCancel != null)
                        btnCancel.setEnabled(false);
                    if (cboHanhKiemEditor != null) {
                        cboHanhKiemEditor.setEnabled(false);
                        cboHanhKiemEditor.setVisible(false);
                    }
                    if (lblHanhKiemValue != null)
                        lblHanhKiemValue.setVisible(true);
                }
            } catch (Exception ex) {
            }
        }

        table.setFont(new Font("Arial", Font.PLAIN, 12));
        table.setRowHeight(30);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(240, 240, 240));
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        DefaultTableCellRenderer permissionRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                int modelRow = -1;
                try {
                    modelRow = table.convertRowIndexToModel(row);
                } catch (Exception ex) {
                }
                if (modelRow >= 0 && modelRow < currentDiemList.size()) {
                    DiemDTO dto = currentDiemList.get(modelRow);
                    if ("DanhGia".equals(dto.getLoaiMon()) && column >= 2 && column <= 5) {
                        value = "";
                    }
                }
                Component c = centerRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus,
                        row, column);
                Color editableBg = new Color(225, 255, 225);
                Color normalBg = Color.WHITE;
                Color disabledBg = new Color(245, 245, 245);

                try {
                    boolean canEdit = false;
                    if (modelRow >= 0 && modelRow < rowCanEditList.size()) {
                        Boolean b = rowCanEditList.get(modelRow);
                        canEdit = b != null && b.booleanValue();
                    }
                    if (isSelected) {
                        c.setBackground(table.getSelectionBackground());
                    } else if (canEdit && tableEditing) {
                        c.setBackground(editableBg);
                    } else {
                        c.setBackground(normalBg);
                    }
                    if (c instanceof JComponent) {
                        if (canEdit) {
                            ((JComponent) c).setToolTipText("Hàng này có thể sửa bởi giáo viên được phân công");
                        } else {
                            ((JComponent) c).setToolTipText(null);
                        }
                    }
                } catch (Exception ex) {
                    c.setBackground(normalBg);
                }
                return c;
            }
        };

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(permissionRenderer);
        }

        table.getColumnModel().getColumn(0).setPreferredWidth(40);
        table.getColumnModel().getColumn(1).setPreferredWidth(120);
        table.getColumnModel().getColumn(2).setPreferredWidth(60);
        table.getColumnModel().getColumn(3).setPreferredWidth(100);
        table.getColumnModel().getColumn(4).setPreferredWidth(100);
        table.getColumnModel().getColumn(5).setPreferredWidth(60);
        table.getColumnModel().getColumn(6).setPreferredWidth(60);
        table.getColumnModel().getColumn(7).setPreferredWidth(100);

        table.setShowGrid(true);
        table.setGridColor(Color.BLACK);
        table.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));

        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        tableScroll.setPreferredSize(new Dimension(900, 450));

        // SỬA: Bọc tableScroll trong panel căn giữa
        JPanel tableWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        tableWrapper.setOpaque(false);
        tableWrapper.add(tableScroll);
        tableWrapper.setAlignmentX(Component.CENTER_ALIGNMENT); // Căn giữa khối
        content.add(tableWrapper);

        // ===== NHẬN XÉT CỦA GIÁO VIÊN =====
        String nx = "";
        if (currentMaHS != -1) {
            try {
                com.sgu.qlhs.dto.NguoiDungDTO nd = null;
                try {
                    java.awt.Window w = javax.swing.SwingUtilities.getWindowAncestor(this);
                    if (w instanceof com.sgu.qlhs.ui.MainDashboard) {
                        com.sgu.qlhs.ui.MainDashboard md = (com.sgu.qlhs.ui.MainDashboard) w;
                        nd = md.getNguoiDung();
                    }
                } catch (Exception ex) {
                }
                String fetched = diemBUS.getNhanXet(currentMaHS, currentMaNK, currentHocKy, nd);
                nx = fetched != null ? fetched : "";
            } catch (Exception ex) {
                nx = "";
            }
        }
        currentNhanXet = nx;

        // SỬA: Bọc khối nhận xét trong panel căn giữa
        JPanel commentWrapper = new JPanel();
        commentWrapper.setLayout(new BoxLayout(commentWrapper, BoxLayout.Y_AXIS));
        commentWrapper.setOpaque(false);
        commentWrapper.setAlignmentX(Component.CENTER_ALIGNMENT); // Căn giữa khối
        commentWrapper.setPreferredSize(new Dimension(900, 140)); // Đặt kích thước
        commentWrapper.setMaximumSize(new Dimension(900, 140));

        JLabel lblNhanXet = new JLabel("Nhận xét của giáo viên:");
        lblNhanXet.setFont(new Font("Arial", Font.BOLD, 13));
        lblNhanXet.setAlignmentX(Component.LEFT_ALIGNMENT);
        commentWrapper.add(lblNhanXet);
        commentWrapper.add(Box.createVerticalStrut(6));

        txtNhanXet = new JTextArea(5, 80);
        txtNhanXet.setLineWrap(true);
        txtNhanXet.setWrapStyleWord(true);
        txtNhanXet.setText(currentNhanXet != null ? currentNhanXet : "");
        txtNhanXet.setEditable(tableEditing);
        txtNhanXet.setFont(new Font("Arial", Font.PLAIN, 12));
        JScrollPane spNhanXet = new JScrollPane(txtNhanXet);
        spNhanXet.setAlignmentX(Component.LEFT_ALIGNMENT);

        commentWrapper.add(spNhanXet);

        content.add(Box.createVerticalStrut(10));
        content.add(commentWrapper); // Thêm panel bọc

        pnlBangDiem.add(content, BorderLayout.CENTER);

        pnlBangDiem.revalidate();
        pnlBangDiem.repaint();
    }

    /**
     * Save edits made in the table back to the database. Returns true when saved
     * successfully.
     */
    private boolean saveBangDiemEdits() {
        if (currentMaHS == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một học sinh để lưu.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        try {
            // resolve current user for permission checks: prefer injected user
            com.sgu.qlhs.dto.NguoiDungDTO nd = null;
            if (this.injectedNguoiDung != null) {
                nd = this.injectedNguoiDung;
            } else {
                try {
                    java.awt.Window w = javax.swing.SwingUtilities.getWindowAncestor(this);
                    if (w instanceof com.sgu.qlhs.ui.MainDashboard) {
                        com.sgu.qlhs.ui.MainDashboard md = (com.sgu.qlhs.ui.MainDashboard) w;
                        nd = md.getNguoiDung();
                    }
                } catch (Exception ex) {
                    // ignore
                }
            }
            // validation: check scores for TinhDiem subjects
            java.util.List<String> invalids = new java.util.ArrayList<>();
            for (int i = 0; i < model.getRowCount(); i++) {
                DiemDTO dto = currentDiemList.get(i);
                if ("DanhGia".equals(dto.getLoaiMon()))
                    continue; // Bỏ qua môn đánh giá

                for (int c = 2; c <= 5; c++) { // Chỉ check cột điểm số
                    Object val = model.getValueAt(i, c);
                    String s = val == null ? "" : val.toString().trim();
                    if (s.isEmpty())
                        continue; // allow empty (treated as 0 by parseDoubleSafe)
                    try {
                        double v = Double.parseDouble(s);
                        if (v < 0 || v > 10) {
                            invalids.add(
                                    String.format("Hàng %d (%s): giá trị %.2f ngoài khoảng 0-10", i + 1,
                                            dto.getTenMon(), v));
                        }
                    } catch (NumberFormatException nfe) {
                        invalids.add(String.format("Hàng %d (%s): không phải số", i + 1, dto.getTenMon()));
                    }
                }
            }
            if (!invalids.isEmpty()) {
                StringBuilder msg = new StringBuilder();
                msg.append("Dữ liệu điểm không hợp lệ:\n");
                for (String it : invalids)
                    msg.append(it).append('\n');
                JOptionPane.showMessageDialog(this, msg.toString(), "Lỗi dữ liệu", JOptionPane.ERROR_MESSAGE);
                return false;
            }

            // iterate rows and persist updated scores
            int failed = 0;
            for (int i = 0; i < model.getRowCount(); i++) {
                // get maMon from the loaded currentDiemList (model shows name but DTO holds id)
                DiemDTO dto = currentDiemList.get(i);
                int maMonId = dto.getMaMon();
                if (maMonId <= 0)
                    continue;

                String ghiChu = model.getValueAt(i, 7) != null ? model.getValueAt(i, 7).toString() : "";
                boolean ok;

                if ("DanhGia".equals(dto.getLoaiMon())) {
                    // Lấy kết quả Đ/KĐ từ cột 6
                    String ketQua = model.getValueAt(i, 6) != null ? model.getValueAt(i, 6).toString() : null;
                    // pass ghiChu first, then ketQua as expected by DiemBUS
                    ok = diemBUS.saveOrUpdateDiem(currentMaHS, maMonId, currentHocKy, currentMaNK,
                            null, null, null, null, ghiChu, ketQua, nd);
                } else {
                    // Lấy điểm số từ cột 2-5
                    Double mieng = parseDoubleSafe(model.getValueAt(i, 2));
                    Double p15 = parseDoubleSafe(model.getValueAt(i, 3));
                    Double giuaky = parseDoubleSafe(model.getValueAt(i, 4));
                    Double cuoiky = parseDoubleSafe(model.getValueAt(i, 5));

                    // For numeric-score subjects ketQuaDanhGia is null; pass ghiChu first
                    ok = diemBUS.saveOrUpdateDiem(currentMaHS, maMonId, currentHocKy, currentMaNK,
                            mieng, p15, giuaky, cuoiky, ghiChu, null, nd);
                }

                if (!ok)
                    failed++;
            }

            // persist Hạnh kiểm if the inline editor was shown/used
            try {
                if (cboHanhKiemEditor != null && cboHanhKiemEditor.isVisible()) {
                    // double-check permission: only Admin or GVCN (homeroom) may save Hạnh kiểm
                    com.sgu.qlhs.dto.NguoiDungDTO ndForHk = null;
                    try {
                        java.awt.Window w = javax.swing.SwingUtilities.getWindowAncestor(this);
                        if (w instanceof com.sgu.qlhs.ui.MainDashboard) {
                            com.sgu.qlhs.ui.MainDashboard md = (com.sgu.qlhs.ui.MainDashboard) w;
                            ndForHk = md.getNguoiDung();
                        }
                    } catch (Exception ex) {
                    }

                    boolean allowedHkSave = false;
                    if (ndForHk != null) {
                        if ("quan_tri_vien".equalsIgnoreCase(ndForHk.getVaiTro())
                                || "Admin".equalsIgnoreCase(ndForHk.getVaiTro())) {
                            allowedHkSave = true;
                        } else if ("giao_vien".equalsIgnoreCase(ndForHk.getVaiTro())) {
                            try {
                                HocSinhDTO hsForHk = hocSinhBUS.getHocSinhByMaHS(currentMaHS);
                                if (hsForHk != null) {
                                    com.sgu.qlhs.dto.ChuNhiemDTO cn2 = new com.sgu.qlhs.bus.ChuNhiemBUS()
                                            .getChuNhiemByGV(ndForHk.getId());
                                    if (cn2 != null && cn2.getMaLop() == hsForHk.getMaLop()) {
                                        allowedHkSave = true;
                                    }
                                }
                            } catch (Exception ex) {
                                allowedHkSave = false;
                            }
                        }
                    }

                    if (!allowedHkSave) {
                        JOptionPane.showMessageDialog(this,
                                "Bạn không có quyền chỉnh sửa Hạnh kiểm. Chỉ giáo viên chủ nhiệm hoặc quản trị viên mới có quyền này.",
                                "Không có quyền", JOptionPane.WARNING_MESSAGE);
                    } else {
                        String chosen = (String) cboHanhKiemEditor.getSelectedItem();
                        if (chosen == null)
                            chosen = "Trung bình";
                        HanhKiemDTO newHk = new HanhKiemDTO(currentMaHS, currentMaNK, currentHocKy, chosen, "");
                        hanhKiemBUS.saveOrUpdate(newHk);
                        // disable editor after saving (combobox shows saved value) and update label
                        cboHanhKiemEditor.setEnabled(false);
                        cboHanhKiemEditor.setVisible(false);
                        if (lblHanhKiemValue != null) {
                            lblHanhKiemValue.setText(chosen);
                            lblHanhKiemValue.setVisible(true);
                        }
                        // Ask the main DiemPanel (if present) to refresh its Chủ nhiệm data
                        try {
                            java.awt.Window w = javax.swing.SwingUtilities.getWindowAncestor(this);
                            if (w instanceof com.sgu.qlhs.ui.MainDashboard) {
                                com.sgu.qlhs.ui.MainDashboard md = (com.sgu.qlhs.ui.MainDashboard) w;
                                // attempt to find DiemPanel in the component tree and refresh
                                com.sgu.qlhs.ui.panels.DiemPanel dp = findDiemPanel(md.getContentPane());
                                if (dp != null) {
                                    dp.refreshChuNhiemIfActive();
                                }
                            }
                        } catch (Exception ex) {
                            // non-fatal - ignore
                        }
                    }
                }
            } catch (Exception ex) {
                System.err.println("Lỗi khi lưu hạnh kiểm: " + ex.getMessage());
            }

            // persist overall teacher comment (nhận xét) if present
            try {
                if (txtNhanXet != null) {
                    String nxText = txtNhanXet.getText();
                    boolean okNx = diemBUS.saveNhanXet(currentMaHS, currentMaNK, currentHocKy,
                            nxText != null ? nxText : "", nd);
                    if (!okNx)
                        System.err.println("Không có quyền lưu nhận xét cho HS=" + currentMaHS);
                }
            } catch (Exception ex) {
                System.err.println("Lỗi khi lưu nhận xét: " + ex.getMessage());
            }
            if (failed > 0) {
                JOptionPane.showMessageDialog(this, "Một số mục không được lưu do thiếu quyền.", "Chú ý",
                        JOptionPane.WARNING_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Lưu bảng điểm thành công.", "Thành công",
                        JOptionPane.INFORMATION_MESSAGE);
            }
            tableEditing = false;
            loadBangDiem();
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Đã xảy ra lỗi khi lưu: " + ex.getMessage(), "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private double parseDoubleSafe(Object o) {
        if (o == null)
            return 0.0; // Trả về 0.0 cho null
        try {
            if (o instanceof Number)
                return ((Number) o).doubleValue();
            String s = o.toString().trim();
            if (s.isEmpty())
                return 0.0; // Trả về 0.0 cho chuỗi rỗng
            return Double.parseDouble(s);
        } catch (Exception ex) {
            return 0.0;
        }
    }

    // Recursively search for the DiemPanel instance inside a container
    private com.sgu.qlhs.ui.panels.DiemPanel findDiemPanel(java.awt.Container root) {
        if (root == null)
            return null;
        for (java.awt.Component c : root.getComponents()) {
            if (c instanceof com.sgu.qlhs.ui.panels.DiemPanel)
                return (com.sgu.qlhs.ui.panels.DiemPanel) c;
            if (c instanceof java.awt.Container) {
                com.sgu.qlhs.ui.panels.DiemPanel found = findDiemPanel((java.awt.Container) c);
                if (found != null)
                    return found;
            }
        }
        return null;
    }

    // (Chủ nhiệm view already scopes rows to the teacher's class). When true we
    // relax the hạnh kiểm permission check so the chủ nhiệm may edit hạnh kiểm
    private boolean openedFromChuNhiem = false;
    private NguoiDungDTO injectedNguoiDung = null;

    public void setInjectedNguoiDung(NguoiDungDTO nd) {
        this.injectedNguoiDung = nd;
    }

    public void setOpenedFromChuNhiem(boolean v) {
        this.openedFromChuNhiem = v;
    }

    /** Export current table to CSV file chosen by user. */
    private void exportXlsx() throws Exception {
        if (model == null || model.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Không có dữ liệu để xuất.");
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Lưu Excel (.xlsx)");
        chooser.setFileFilter(new FileNameExtensionFilter("Excel files", "xlsx"));
        int rc = chooser.showSaveDialog(this);
        if (rc != JFileChooser.APPROVE_OPTION)
            return;
        File f = chooser.getSelectedFile();
        if (!f.getName().toLowerCase().endsWith(".xlsx")) {
            f = new File(f.getParentFile(), f.getName() + ".xlsx");
        }

        // Use reflection to call Apache POI if available. This avoids compile-time
        // dependency on POI jars. If POI classes are missing, ClassNotFoundException
        // will be thrown and caller handles it.
        Class<?> wbClass = Class.forName("org.apache.poi.xssf.usermodel.XSSFWorkbook");
        Object wb = wbClass.getConstructor().newInstance();
        java.io.FileOutputStream fos = null;
        try {
            fos = new java.io.FileOutputStream(f);
            java.lang.reflect.Method createSheet = wbClass.getMethod("createSheet", String.class);
            Object sheet = createSheet.invoke(wb, "Bảng điểm");

            Class<?> sheetClass = sheet.getClass();
            java.lang.reflect.Method createRow = sheetClass.getMethod("createRow", int.class);
            java.lang.reflect.Method autoSizeCol = sheetClass.getMethod("autoSizeColumn", int.class);

            // header
            Object hdr = createRow.invoke(sheet, 0);
            Class<?> rowClass = hdr.getClass();
            java.lang.reflect.Method createCell = rowClass.getMethod("createCell", int.class);
            java.lang.reflect.Method setCellValueStr = null;
            java.lang.reflect.Method setCellValueNum = null;
            // attempt to find setCellValue methods on cell
            Object tmpCell = createCell.invoke(hdr, 0);
            Class<?> cellClass = tmpCell.getClass();
            try {
                setCellValueStr = cellClass.getMethod("setCellValue", String.class);
            } catch (NoSuchMethodException ex) {
            }
            try {
                setCellValueNum = cellClass.getMethod("setCellValue", double.class);
            } catch (NoSuchMethodException ex) {
            }

            for (int c = 0; c < model.getColumnCount(); c++) {
                Object cell = createCell.invoke(hdr, c);
                if (setCellValueStr != null)
                    setCellValueStr.invoke(cell, model.getColumnName(c));
                else
                    cell.getClass().getMethod("setCellValue", String.class).invoke(cell, model.getColumnName(c));
            }

            // rows
            for (int r = 0; r < model.getRowCount(); r++) {
                Object prow = createRow.invoke(sheet, r + 1);
                for (int c = 0; c < model.getColumnCount(); c++) {
                    Object cell = createCell.invoke(prow, c);
                    Object v = model.getValueAt(r, c);
                    if (v == null) {
                        if (setCellValueStr != null)
                            setCellValueStr.invoke(cell, "");
                    } else if (v instanceof Number) {
                        if (setCellValueNum != null)
                            setCellValueNum.invoke(cell, ((Number) v).doubleValue());
                        else if (setCellValueStr != null)
                            setCellValueStr.invoke(cell, v.toString());
                    } else {
                        if (setCellValueStr != null)
                            setCellValueStr.invoke(cell, v.toString());
                    }
                }
            }

            // autosize
            for (int c = 0; c < model.getColumnCount(); c++)
                autoSizeCol.invoke(sheet, c);

            // write
            java.lang.reflect.Method write = wbClass.getMethod("write", java.io.OutputStream.class);
            write.invoke(wb, fos);
            JOptionPane.showMessageDialog(this, "Xuất Excel thành công: " + f.getAbsolutePath());
        } finally {
            try {
                if (fos != null)
                    fos.close();
            } catch (Exception ex) {
            }
            try {
                wbClass.getMethod("close").invoke(wb);
            } catch (Exception ex) {
            }
        }
    }

    /** Import data from an .xlsx file (Apache POI required). */
    private void importXlsx(File f) throws Exception {
        if (f == null || !f.exists())
            throw new java.io.FileNotFoundException("File không tồn tại");

        // Build name->id map for subjects
        java.util.List<com.sgu.qlhs.dto.MonHocDTO> allMons = monBUS.getAllMon();
        java.util.Map<String, Integer> monByName = new java.util.HashMap<>();
        java.util.Map<Integer, com.sgu.qlhs.dto.MonHocDTO> monById = new java.util.HashMap<>();
        for (com.sgu.qlhs.dto.MonHocDTO m : allMons) {
            if (m.getTenMon() != null)
                monByName.put(m.getTenMon().trim(), m.getMaMon());
            monById.put(m.getMaMon(), m);
        }

        int saved = 0, skippedNoPerm = 0, unmapped = 0, errors = 0;

        // Use reflection to read .xlsx with Apache POI if available. This avoids
        // compile-time dependency on POI jars. If POI classes are not present,
        // Class.forName will throw and caller will surface a friendly message.
        Class<?> wbClass = Class.forName("org.apache.poi.xssf.usermodel.XSSFWorkbook");
        java.io.FileInputStream fis = null;
        Object wb = null;
        try {
            fis = new java.io.FileInputStream(f);
            java.lang.reflect.Constructor<?> ctor = wbClass.getConstructor(java.io.InputStream.class);
            wb = ctor.newInstance(fis);
            java.lang.reflect.Method getSheetAt = wbClass.getMethod("getSheetAt", int.class);
            Object sheet = getSheetAt.invoke(wb, 0);
            if (sheet == null)
                throw new Exception("Sheet rỗng trong file Excel");

            // resolve current user
            com.sgu.qlhs.dto.NguoiDungDTO nd = null;
            try {
                java.awt.Window w2 = javax.swing.SwingUtilities.getWindowAncestor(this);
                if (w2 instanceof com.sgu.qlhs.ui.MainDashboard) {
                    com.sgu.qlhs.ui.MainDashboard md2 = (com.sgu.qlhs.ui.MainDashboard) w2;
                    nd = md2.getNguoiDung();
                }
            } catch (Exception ex) {
            }

            java.lang.reflect.Method iteratorMethod = sheet.getClass().getMethod("iterator");
            Object it = iteratorMethod.invoke(sheet);
            java.lang.reflect.Method hasNext = it.getClass().getMethod("hasNext");
            java.lang.reflect.Method next = it.getClass().getMethod("next");

            if (!((Boolean) hasNext.invoke(it)))
                throw new Exception("File Excel không có header");
            // skip header
            next.invoke(it);

            while ((Boolean) hasNext.invoke(it)) {
                Object row = next.invoke(it);
                try {
                    java.lang.reflect.Method getCell = row.getClass().getMethod("getCell", int.class);
                    Object cTen = getCell.invoke(row, 1);
                    String tenMon = cTen == null ? "" : cTen.toString().trim();
                    if (tenMon.isEmpty()) {
                        continue; // skip empty rows
                    }
                    Integer maMon = monByName.get(tenMon);
                    if (maMon == null) {
                        unmapped++;
                        continue;
                    }
                    com.sgu.qlhs.dto.MonHocDTO mon = monById.get(maMon);
                    boolean ok = false;
                    if (mon != null && "DanhGia".equalsIgnoreCase(mon.getLoaiMon())) {
                        Object cKet = getCell.invoke(row, 6);
                        String ketQua = cKet == null ? "" : cKet.toString().trim();
                        Object cGhi = getCell.invoke(row, 7);
                        String ghiChu = cGhi == null ? "" : cGhi.toString().trim();
                        ok = diemBUS.saveOrUpdateDiem(currentMaHS, maMon, currentHocKy, currentMaNK,
                                null, null, null, null, ghiChu, ketQua.isEmpty() ? null : ketQua, nd);
                    } else {
                        Double mieng = null, p15 = null, gk = null, ck = null;
                        try {
                            Object cm = getCell.invoke(row, 2);
                            mieng = (cm == null) ? null : Double.valueOf(cm.toString());
                        } catch (Exception ex) {
                        }
                        try {
                            Object cp = getCell.invoke(row, 3);
                            p15 = (cp == null) ? null : Double.valueOf(cp.toString());
                        } catch (Exception ex) {
                        }
                        try {
                            Object cg = getCell.invoke(row, 4);
                            gk = (cg == null) ? null : Double.valueOf(cg.toString());
                        } catch (Exception ex) {
                        }
                        try {
                            Object cc = getCell.invoke(row, 5);
                            ck = (cc == null) ? null : Double.valueOf(cc.toString());
                        } catch (Exception ex) {
                        }
                        Object cGhi = getCell.invoke(row, 7);
                        String ghiChu = cGhi == null ? "" : cGhi.toString().trim();
                        ok = diemBUS.saveOrUpdateDiem(currentMaHS, maMon, currentHocKy, currentMaNK,
                                mieng, p15, gk, ck, ghiChu, null, nd);
                    }

                    if (ok)
                        saved++;
                    else
                        skippedNoPerm++;
                } catch (Exception ex) {
                    errors++;
                }
            }
        } finally {
            try {
                if (fis != null)
                    fis.close();
            } catch (Exception ex) {
            }
            try {
                if (wb != null)
                    wbClass.getMethod("close").invoke(wb);
            } catch (Exception ex) {
            }
        }

        StringBuilder msg = new StringBuilder();
        msg.append("Kết quả nhập:\n");
        msg.append("Đã lưu: ").append(saved).append('\n');
        if (unmapped > 0)
            msg.append("Bị bỏ qua (môn không khớp): ").append(unmapped).append('\n');
        if (skippedNoPerm > 0)
            msg.append("Bị bỏ qua (không có quyền): ").append(skippedNoPerm).append('\n');
        if (errors > 0)
            msg.append("Lỗi: ").append(errors).append('\n');
        JOptionPane.showMessageDialog(this, msg.toString());
    }

    private void printBangDiem() {
        // Build a print-friendly copy of the displayed content so layout and fonts
        // are optimized for PDF/print output.
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setJobName("Bảng điểm - " + tenHocSinh);

        // Create print panel with fixed width for consistent scaling
        int panelWidth = 800;
        JPanel printPanel = new JPanel();
        printPanel.setBackground(Color.WHITE);
        printPanel.setLayout(new BoxLayout(printPanel, BoxLayout.Y_AXIS));
        printPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        printPanel.setPreferredSize(new Dimension(panelWidth, 1000));

        // Header
        JLabel hdrLeft = new JLabel(diaPhuong);
        hdrLeft.setFont(new Font("Serif", Font.PLAIN, 12));
        JLabel hdrSchool = new JLabel(tenTruong, JLabel.CENTER);
        hdrSchool.setFont(new Font("Serif", Font.BOLD, 14));
        JLabel hdrRight = new JLabel(new SimpleDateFormat("'Ngày' dd 'tháng' MM 'năm' yyyy").format(new Date()),
                JLabel.RIGHT);
        hdrRight.setFont(new Font("Serif", Font.PLAIN, 12));

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setBackground(Color.WHITE);
        topRow.add(hdrLeft, BorderLayout.WEST);
        topRow.add(hdrSchool, BorderLayout.CENTER);
        topRow.add(hdrRight, BorderLayout.EAST);
        printPanel.add(topRow);
        printPanel.add(Box.createVerticalStrut(8));

        JLabel title = new JLabel("BẢNG ĐIỂM HỌC SINH", JLabel.CENTER);
        title.setFont(new Font("Serif", Font.BOLD, 16));
        printPanel.add(title);
        printPanel.add(Box.createVerticalStrut(12));

        // Student / period info
        JPanel infoRow = new JPanel(new GridLayout(2, 2, 8, 4));
        infoRow.setBackground(Color.WHITE);
        infoRow.add(new JLabel("Học sinh: "));
        infoRow.add(new JLabel(tenHocSinh != null ? tenHocSinh : ""));
        infoRow.add(new JLabel("Học kỳ: "));
        String hkLabel = (cboHocKy.getSelectedIndex() >= 0) ? cboHocKy.getSelectedItem().toString() : "";
        String nyLabel = (cboNamHoc.getSelectedIndex() >= 0) ? cboNamHoc.getSelectedItem().toString() : "";
        infoRow.add(new JLabel(hkLabel + " - " + nyLabel));
        printPanel.add(infoRow);
        printPanel.add(Box.createVerticalStrut(10));

        // Build a print-friendly table copy from the current model
        DefaultTableModel printModel = new DefaultTableModel();
        // copy column names
        for (int c = 0; c < model.getColumnCount(); c++) {
            printModel.addColumn(model.getColumnName(c));
        }
        // copy rows
        for (int r = 0; r < model.getRowCount(); r++) {
            Object[] row = new Object[model.getColumnCount()];
            for (int c = 0; c < model.getColumnCount(); c++) {
                row[c] = model.getValueAt(r, c);
            }
            printModel.addRow(row);
        }

        JTable printTable = new JTable(printModel);
        printTable.setFont(new Font("Serif", Font.PLAIN, 12));
        printTable.setRowHeight(24);
        printTable.getTableHeader().setFont(new Font("Serif", Font.BOLD, 12));
        printTable.setShowGrid(true);
        printTable.setGridColor(Color.GRAY);
        printTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        // Tidy column widths (relative)
        if (printTable.getColumnCount() >= 8) {
            printTable.getColumnModel().getColumn(0).setPreferredWidth(40);
            printTable.getColumnModel().getColumn(1).setPreferredWidth(260);
            printTable.getColumnModel().getColumn(2).setPreferredWidth(60);
            printTable.getColumnModel().getColumn(3).setPreferredWidth(60);
            printTable.getColumnModel().getColumn(4).setPreferredWidth(60);
            printTable.getColumnModel().getColumn(5).setPreferredWidth(60);
            printTable.getColumnModel().getColumn(6).setPreferredWidth(60);
            printTable.getColumnModel().getColumn(7).setPreferredWidth(200);
        }

        JScrollPane sp = new JScrollPane(printTable);
        sp.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        sp.setPreferredSize(new Dimension(panelWidth - 40,
                Math.min(400, printTable.getRowCount() * printTable.getRowHeight() + 30)));
        printPanel.add(sp);
        printPanel.add(Box.createVerticalStrut(12));

        // Hạnh kiểm and teacher comment (permission-aware)
        // Prefer injected user for print permission context if provided
        com.sgu.qlhs.dto.NguoiDungDTO ndForPrint = this.injectedNguoiDung;
        if (ndForPrint == null) {
            try {
                java.awt.Window w = javax.swing.SwingUtilities.getWindowAncestor(this);
                if (w instanceof com.sgu.qlhs.ui.MainDashboard) {
                    com.sgu.qlhs.ui.MainDashboard md = (com.sgu.qlhs.ui.MainDashboard) w;
                    ndForPrint = md.getNguoiDung();
                }
            } catch (Exception ex) {
                // ignore
            }
        }
        HanhKiemDTO hk = hanhKiemBUS.getHanhKiem(currentMaHS, currentMaNK, currentHocKy, ndForPrint);
        String hkStr = hk != null ? hk.getXepLoai() : "(chưa có)";
        JPanel bottomInfo = new JPanel(new BorderLayout());
        bottomInfo.setBackground(Color.WHITE);
        bottomInfo.add(new JLabel("Hạnh kiểm: " + hkStr), BorderLayout.WEST);
        printPanel.add(bottomInfo);
        printPanel.add(Box.createVerticalStrut(8));

        JTextArea printComment = new JTextArea(6, 60);
        printComment.setLineWrap(true);
        printComment.setWrapStyleWord(true);
        printComment.setEditable(false);
        printComment.setFont(new Font("Serif", Font.ITALIC, 12));
        String nx = "";
        try {
            nx = diemBUS.getNhanXet(currentMaHS, currentMaNK, currentHocKy, ndForPrint);
            if (nx == null)
                nx = "";
        } catch (Exception ex) {
            nx = "";
        }
        printComment.setText(nx);
        printComment.setBorder(BorderFactory.createTitledBorder("Nhận xét của giáo viên"));
        JScrollPane spComment = new JScrollPane(printComment);
        spComment.setPreferredSize(new Dimension(panelWidth - 40, 140));
        printPanel.add(spComment);

        final JPanel headerPanelPrint = new JPanel();
        headerPanelPrint.setBackground(Color.WHITE);
        headerPanelPrint.setLayout(new BoxLayout(headerPanelPrint, BoxLayout.Y_AXIS));
        headerPanelPrint.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        headerPanelPrint.add(topRow);
        headerPanelPrint.add(title);
        headerPanelPrint.add(Box.createVerticalStrut(6));
        headerPanelPrint.add(infoRow);

        final JPanel bodyPanel = new JPanel();
        bodyPanel.setBackground(Color.WHITE);
        bodyPanel.setLayout(new BoxLayout(bodyPanel, BoxLayout.Y_AXIS));
        bodyPanel.add(Box.createVerticalStrut(4));
        bodyPanel.add(sp);
        bodyPanel.add(Box.createVerticalStrut(8));
        bodyPanel.add(bottomInfo);
        bodyPanel.add(Box.createVerticalStrut(6));
        bodyPanel.add(spComment);

        job.setPrintable(new Printable() {
            @Override
            public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
                Graphics2D g2 = (Graphics2D) graphics;
                g2.setColor(Color.black);

                double imgW = pageFormat.getImageableWidth();
                double imgH = pageFormat.getImageableHeight();

                // compute preferred sizes
                headerPanelPrint.doLayout();
                bodyPanel.doLayout();
                Dimension headerPref = headerPanelPrint.getPreferredSize();
                Dimension bodyPref = bodyPanel.getPreferredSize();

                double px = Math.max(headerPref.getWidth(), bodyPref.getWidth());
                if (px <= 0)
                    px = panelWidth;

                double scale = imgW / px;

                double scaledHeaderH = headerPref.getHeight() * scale;
                double availableHForBody = imgH - scaledHeaderH;
                if (availableHForBody <= 0)
                    availableHForBody = imgH; // fallback

                double totalBodyScaled = bodyPref.getHeight() * scale;
                int totalPages = (int) Math.max(1, Math.ceil(totalBodyScaled / availableHForBody));

                if (pageIndex >= totalPages) {
                    return NO_SUCH_PAGE;
                }

                // move to imageable area and scale
                g2.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
                g2.scale(scale, scale);

                // print header at top of page (untranslated in body coordinates)
                headerPanelPrint.printAll(g2);

                // translate to beginning of body content, then offset by page index
                g2.translate(0, headerPref.getHeight());
                double yOffsetBody = (pageIndex * (availableHForBody / scale));
                g2.translate(0, -yOffsetBody);

                // print body; only visible region will be within the page clip
                bodyPanel.printAll(g2);

                // footer: draw page number in device coords (after scaling)
                try {
                    g2.scale(1.0 / scale, 1.0 / scale); // revert scaling for footer in device px
                    String footer = String.format("Trang %d / %d", pageIndex + 1, totalPages);
                    Font f = new Font("Serif", Font.PLAIN, 10);
                    g2.setFont(f);
                    FontMetrics fm = g2.getFontMetrics(f);
                    int fw = fm.stringWidth(footer);
                    double fx = pageFormat.getImageableX() + pageFormat.getImageableWidth() - fw - 10;
                    double fy = pageFormat.getImageableY() + pageFormat.getImageableHeight() - 10;
                    g2.drawString(footer, (float) fx, (float) fy);
                } catch (Exception ex) {
                    // ignore footer errors
                }

                return PAGE_EXISTS;
            }
        });

        boolean doPrint = job.printDialog();
        if (doPrint) {
            try {
                job.print();
                JOptionPane.showMessageDialog(this, "In bảng điểm thành công!", "Thành công",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (PrinterException ex) {
                JOptionPane.showMessageDialog(this, "Lỗi khi in: " + ex.getMessage(), "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private java.util.List<Double> getClassTBHKs(int maLop, int hocKy, int maNK) {
        java.util.List<Double> dsDiemTBHK = new java.util.ArrayList<>();

        java.util.List<HocSinhDTO> dsHS = hocSinhBUS.getHocSinhByMaLop(maLop);
        if (dsHS == null || dsHS.isEmpty()) {
            return dsDiemTBHK;
        }

        java.util.List<DiemDTO> allScoresInClass = diemBUS.getDiemFiltered(maLop, null, hocKy, maNK, null, null);

        java.util.Map<Integer, java.util.List<DiemDTO>> diemTheoHS = allScoresInClass.stream()
                .collect(java.util.stream.Collectors.groupingBy(DiemDTO::getMaHS));

        for (HocSinhDTO hs : dsHS) {
            java.util.List<DiemDTO> diemCuaHS = diemTheoHS.get(hs.getMaHS());
            if (diemCuaHS == null || diemCuaHS.isEmpty())
                continue;

            double tongDiem = 0;
            int soMon = 0;
            for (DiemDTO d : diemCuaHS) {
                if ("TinhDiem".equals(d.getLoaiMon())) {
                    tongDiem += d.getDiemTB();
                    soMon++;
                }
            }
            if (soMon > 0)
                dsDiemTBHK.add(tongDiem / soMon);
        }
        return dsDiemTBHK;
    }

    private double getStudentTBHK(int maHS, int hocKy, int maNK, NguoiDungDTO user) {
        java.util.List<DiemDTO> diemCuaHS = diemBUS.getDiemByMaHS(maHS, hocKy, maNK, user);
        if (diemCuaHS == null || diemCuaHS.isEmpty())
            return 0.0;

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
}