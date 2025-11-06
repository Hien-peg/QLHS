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

        // ⚡ Tách riêng listener của Export ra:
        // Import listener (CSV -> bảng điểm). Permission: admin always allowed;
        // teachers will have per-row permission checks applied when saving.
        btnImport.addActionListener(e -> {
            // Generate a sample CSV template and save it to the user's Downloads folder
            try {
                java.util.List<com.sgu.qlhs.dto.MonHocDTO> allMons = monBUS.getAllMon();
                String userHome = System.getProperty("user.home");
                File downloads = new File(userHome, "Downloads");
                if (!downloads.exists() || !downloads.isDirectory())
                    downloads = new File(userHome);
                String namePart = (currentMaHS > 0) ? String.valueOf(currentMaHS) : "mau";
                String ts = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                File sample = new File(downloads, "bangdiem_mau_" + namePart + "_" + ts + ".csv");
                try (java.io.FileWriter fw = new java.io.FileWriter(sample)) {
                    // header
                    fw.write("STT,Tên môn học,Miệng,15 Phút,1 Tiết,Cuối kỳ,Kết quả,Ghi chú");
                    fw.write(System.lineSeparator());
                    int idx = 1;
                    if (allMons != null) {
                        for (com.sgu.qlhs.dto.MonHocDTO m : allMons) {
                            String ten = m.getTenMon() != null ? m.getTenMon() : "";
                            // quote if needed
                            if (ten.contains(",") || ten.contains("\"")) {
                                ten = "\"" + ten.replace("\"", "\"\"") + "\"";
                            }
                            java.util.List<String> parts = java.util.Arrays.asList(String.valueOf(idx), ten, "", "", "",
                                    "", "", "");
                            fw.write(String.join(",", parts));
                            fw.write(System.lineSeparator());
                            idx++;
                        }
                    }
                }
                int opt = JOptionPane.showConfirmDialog(this,
                        "Mẫu CSV đã được lưu tại:\n" + sample.getAbsolutePath()
                                + "\nBạn muốn chọn file để nhập ngay bây giờ?",
                        "Mẫu CSV", JOptionPane.YES_NO_OPTION);
                if (opt != JOptionPane.YES_OPTION) {
                    // user chose not to continue to import now
                    return;
                }
            } catch (Exception ex) {
                // if template generation fails, continue to import flow but notify user
                try {
                    JOptionPane.showMessageDialog(this, "Không thể tạo mẫu CSV: " + ex.getMessage());
                } catch (Exception ignored) {
                }
            }

            if (model == null || model.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "Không có dữ liệu để nhập.");
                return;
            }
            if (currentMaHS == -1) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn một học sinh trước khi nhập dữ liệu.");
                return;
            }
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Chọn file CSV để nhập");
            chooser.setFileFilter(new FileNameExtensionFilter("CSV files", "csv"));
            int rc = chooser.showOpenDialog(this);
            if (rc != JFileChooser.APPROVE_OPTION)
                return;
            File f = chooser.getSelectedFile();
            // build name->id map for subjects
            java.util.List<com.sgu.qlhs.dto.MonHocDTO> allMons = monBUS.getAllMon();
            java.util.Map<String, Integer> monByName = new java.util.HashMap<>();
            java.util.Map<Integer, com.sgu.qlhs.dto.MonHocDTO> monById = new java.util.HashMap<>();
            for (com.sgu.qlhs.dto.MonHocDTO m : allMons) {
                if (m.getTenMon() != null)
                    monByName.put(m.getTenMon().trim(), m.getMaMon());
                monById.put(m.getMaMon(), m);
            }

            int saved = 0, skippedNoPerm = 0, unmapped = 0, errors = 0;
            try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(f))) {
                String header = br.readLine();
                if (header == null) {
                    JOptionPane.showMessageDialog(this, "File CSV rỗng hoặc không hợp lệ.");
                    return;
                }
                String line;
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
                boolean isAdmin = nd != null && ("quan_tri_vien".equalsIgnoreCase(nd.getVaiTro())
                        || "Admin".equalsIgnoreCase(nd.getVaiTro()));

                while ((line = br.readLine()) != null) {
                    if (line.trim().isEmpty())
                        continue;
                    // naive CSV split (export writes simple comma-separated values)
                    String[] cols = line.split(",");
                    if (cols.length < 8) {
                        errors++;
                        continue;
                    }
                    String tenMon = cols[1].trim();
                    Integer maMon = monByName.get(tenMon);
                    if (maMon == null) {
                        unmapped++;
                        continue;
                    }
                    com.sgu.qlhs.dto.MonHocDTO mon = monById.get(maMon);
                    try {
                        boolean ok;
                        if (mon != null && "DanhGia".equalsIgnoreCase(mon.getLoaiMon())) {
                            String ketQua = cols[6].trim();
                            String ghiChu = cols.length > 7 ? cols[7].trim() : "";
                            // Note: DiemBUS expects (ghiChu, ketQuaDanhGia) in that order for the full
                            // saveOrUpdateDiem signature. Ensure we pass ghiChu first.
                            ok = diemBUS.saveOrUpdateDiem(currentMaHS, maMon, currentHocKy, currentMaNK,
                                    null, null, null, null, ghiChu, ketQua.isEmpty() ? null : ketQua, nd);
                        } else {
                            Double mieng = parseDoubleSafe(cols[2].trim());
                            Double p15 = parseDoubleSafe(cols[3].trim());
                            Double gk = parseDoubleSafe(cols[4].trim());
                            Double ck = parseDoubleSafe(cols[5].trim());
                            String ghiChu = cols.length > 7 ? cols[7].trim() : "";
                            // For numeric-score subjects, ketQuaDanhGia is null; pass ghiChu first.
                            ok = diemBUS.saveOrUpdateDiem(currentMaHS, maMon, currentHocKy, currentMaNK,
                                    mieng, p15, gk, ck, ghiChu, null, nd);
                        }
                        if (ok) {
                            saved++;
                        } else {
                            // likely permission issue
                            skippedNoPerm++;
                        }
                    } catch (Exception ex) {
                        errors++;
                    }
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi khi đọc file: " + ex.getMessage());
                return;
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
            // refresh view
            loadBangDiem();
        });
        btnExport.addActionListener(e -> exportCsv());
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
        // THAY ĐỔI: Đổi tên cột "TBHK" -> "Kết quả"
        String[] columns = { "STT", "Tên môn học", "Miệng", "15 Phút", "1 Tiết", "Cuối kỳ", "Kết quả", "Ghi chú" };

        // Build table rows from DiemBUS for the selected student and semester
        Object sel = cboHocSinh.getSelectedItem();
        if (sel == null) {
            // nothing selected
            model = new DefaultTableModel(columns, 0); // tạo model rỗng
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

            // resolve current user to enforce read-side rules
            com.sgu.qlhs.dto.NguoiDungDTO nd = null;
            try {
                java.awt.Window w = javax.swing.SwingUtilities.getWindowAncestor(this);
                if (w instanceof com.sgu.qlhs.ui.MainDashboard) {
                    com.sgu.qlhs.ui.MainDashboard md = (com.sgu.qlhs.ui.MainDashboard) w;
                    nd = md.getNguoiDung();
                }
            } catch (Exception ex) {
                // ignore
            }

            // THAY ĐỔI: getDiemByMaHS giờ trả về DTO đã có LoaiMon
            java.util.List<DiemDTO> diemList = diemBUS.getDiemByMaHS(maHS, hkNum, maNK, nd);

            // Build a combined list: include existing Diem rows plus placeholder rows
            // for subjects that don't yet have a Diem entry so a teacher can add new
            // grades. We obtain the full subject list from MonBUS and create placeholder
            // DiemDTOs when needed.
            java.util.List<com.sgu.qlhs.dto.MonHocDTO> allMons = monBUS.getAllMon();
            java.util.Map<Integer, DiemDTO> mapByMon = new java.util.HashMap<>();
            if (diemList != null) {
                for (DiemDTO d : diemList) {
                    mapByMon.put(d.getMaMon(), d);
                }
            }

            java.util.List<DiemDTO> combined = new java.util.ArrayList<>();
            // Ensure we keep the existing Diem rows (so their saved MaDiem/GhiChu etc
            // remain)
            if (diemList != null) {
                combined.addAll(diemList);
            }
            // Add placeholders for missing subjects (those in MonHoc but not in Diem)
            for (com.sgu.qlhs.dto.MonHocDTO m : allMons) {
                if (!mapByMon.containsKey(m.getMaMon())) {
                    DiemDTO placeholder = new DiemDTO();
                    placeholder.setMaDiem(0);
                    placeholder.setMaHS(maHS);
                    placeholder.setMaMon(m.getMaMon());
                    placeholder.setTenMon(m.getTenMon());
                    placeholder.setLoaiMon(m.getLoaiMon());
                    // leave numeric fields at default 0.0 and strings null/empty
                    placeholder.setGhiChu("");
                    combined.add(placeholder);
                }
            }

            // store current context so Save can use it
            currentMaHS = maHS;
            currentHocKy = hkNum;
            currentMaNK = maNK;
            currentDiemList = combined;

            // Precompute per-row edit permission: for each subject row, allow editing
            // only if the logged-in teacher is assigned to that student's class+subject
            rowCanEditList.clear();
            boolean anyRowEditable = false;
            // Initialize all rows to false (will update below)
            for (int i = 0; i < currentDiemList.size(); i++) {
                rowCanEditList.add(Boolean.FALSE);
            }

            try {
                com.sgu.qlhs.dto.NguoiDungDTO ndCheck = null;
                try {
                    java.awt.Window w = javax.swing.SwingUtilities.getWindowAncestor(this);
                    if (w instanceof com.sgu.qlhs.ui.MainDashboard) {
                        com.sgu.qlhs.ui.MainDashboard md = (com.sgu.qlhs.ui.MainDashboard) w;
                        ndCheck = md.getNguoiDung();
                    }
                } catch (Exception ex) {
                }
                if (ndCheck != null && "giao_vien".equalsIgnoreCase(ndCheck.getVaiTro())) {
                    for (int i = 0; i < currentDiemList.size(); i++) {
                        DiemDTO d = currentDiemList.get(i);
                        boolean ok = diemBUS.isTeacherAssignedPublic(ndCheck.getId(), maHS, d.getMaMon(), hkNum,
                                maNK);
                        rowCanEditList.set(i, ok);
                        if (ok)
                            anyRowEditable = true;
                    }
                } else if (ndCheck != null && ("quan_tri_vien".equalsIgnoreCase(ndCheck.getVaiTro())
                        || "Admin".equalsIgnoreCase(ndCheck.getVaiTro()))) {
                    // Admin (may be represented as 'quan_tri_vien' or 'Admin') được sửa tất cả
                    for (int i = 0; i < rowCanEditList.size(); i++)
                        rowCanEditList.set(i, Boolean.TRUE);
                    anyRowEditable = true;
                }
            } catch (Exception ex) {
                // on errors, conservatively disable edits
                for (int i = 0; i < rowCanEditList.size(); i++)
                    rowCanEditList.set(i, Boolean.FALSE);
            }

            int idx = 1;

            // create model now that rowCanEditList is computed
            model = new DefaultTableModel(columns, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    if (!tableEditing)
                        return false;
                    // Lấy quyền edit chung của hàng này
                    if (row < 0 || row >= rowCanEditList.size())
                        return false;
                    Boolean allowed = rowCanEditList.get(row);
                    if (allowed == null || !allowed.booleanValue())
                        return false;

                    // Lấy LoaiMon của hàng
                    if (row < 0 || row >= currentDiemList.size())
                        return false;
                    DiemDTO dto = currentDiemList.get(row);
                    String loaiMon = dto.getLoaiMon();

                    if ("DanhGia".equals(loaiMon)) {
                        // Môn Đánh Giá: chỉ sửa cột "Kết quả" (6) và "Ghi chú" (7)
                        return (column == 6 || column == 7);
                    } else {
                        // Môn Tính Điểm: chỉ sửa cột điểm (2-5) và "Ghi chú" (7)
                        return (column >= 2 && column <= 5) || (column == 7);
                    }
                }

                @Override
                public Class<?> getColumnClass(int columnIndex) {
                    // Cột 6 (Kết quả) giờ có thể là Double (TB) hoặc String (Đ/KĐ)
                    if (columnIndex == 6)
                        return Object.class;
                    if (columnIndex >= 2 && columnIndex <= 5)
                        return Double.class;
                    return String.class;
                }
            };

            // THAY ĐỔI: Lặp và điền dữ liệu dựa trên LoaiMon (dùng currentDiemList)
            for (DiemDTO d : currentDiemList) {
                String loaiMon = d.getLoaiMon();
                boolean isPlaceholder = (d.getMaDiem() == 0);

                if ("DanhGia".equals(loaiMon)) {
                    model.addRow(new Object[] {
                            String.valueOf(idx++),
                            d.getTenMon(),
                            null, // Miệng
                            null, // 15p
                            null, // 1 Tiết
                            null, // Cuối kỳ
                            isPlaceholder ? null : d.getKetQuaDanhGia(), // Kết quả (Đ/KĐ)
                            d.getGhiChu() != null ? d.getGhiChu() : ""
                    });
                } else { // Mặc định là TinhDiem
                    // If placeholder (no saved Diem row), show empty cells instead of 0.0
                    Object mieng = isPlaceholder ? null : d.getDiemMieng();
                    Object p15 = isPlaceholder ? null : d.getDiem15p();
                    Object gk = isPlaceholder ? null : d.getDiemGiuaKy();
                    Object ck = isPlaceholder ? null : d.getDiemCuoiKy();
                    Object tb = null;
                    if (!isPlaceholder) {
                        double dm = d.getDiemMieng();
                        double dp = d.getDiem15p();
                        double dg = d.getDiemGiuaKy();
                        double dc = d.getDiemCuoiKy();
                        tb = Math.round((dm * 0.10 + dp * 0.20 + dg * 0.30 + dc * 0.40) * 10.0) / 10.0;
                    }
                    model.addRow(new Object[] {
                            String.valueOf(idx++),
                            d.getTenMon(),
                            mieng,
                            p15,
                            gk,
                            ck,
                            tb, // Kết quả (TB)
                            d.getGhiChu() != null ? d.getGhiChu() : ""
                    });
                }
            }
            table = new JTable(model);

            // THAY ĐỔI: Thêm CellEditor cho cột "Kết quả" (6)
            JComboBox<String> danhGiaEditor = new JComboBox<>(new String[] { "Đ", "KĐ" });
            table.getColumnModel().getColumn(6).setCellEditor(new DefaultCellEditor(danhGiaEditor));

            // compute whether any edit is allowed for the currently loaded dataset
            boolean canEdit = (!isStudentView) && anyRowEditable;

            // compute whether the current user is allowed to edit Hạnh kiểm
            // Only Admin or the homeroom teacher (Giáo viên chủ nhiệm) of the student's
            // class may edit Hạnh kiểm
            boolean canEditHanhKiem = false;
            try {
                com.sgu.qlhs.dto.NguoiDungDTO ndForHk = null;
                try {
                    java.awt.Window w = javax.swing.SwingUtilities.getWindowAncestor(this);
                    if (w instanceof com.sgu.qlhs.ui.MainDashboard) {
                        com.sgu.qlhs.ui.MainDashboard md = (com.sgu.qlhs.ui.MainDashboard) w;
                        ndForHk = md.getNguoiDung();
                    }
                } catch (Exception ex) {
                }
                if (ndForHk != null) {
                    // Admins may edit
                    if ("quan_tri_vien".equalsIgnoreCase(ndForHk.getVaiTro())
                            || "Admin".equalsIgnoreCase(ndForHk.getVaiTro())) {
                        canEditHanhKiem = true;
                    } else if ("giao_vien".equalsIgnoreCase(ndForHk.getVaiTro())) {
                        // If dialog was opened from the Chủ nhiệm tab we allow editing as
                        // the tab itself is only shown to the chủ nhiệm teacher for that
                        // class. This is a safe, UX-friendly shortcut that avoids failing
                        // when subtle DB schema/assignment differences exist.
                        if (this.openedFromChuNhiem) {
                            canEditHanhKiem = true;
                        } else {
                            // Otherwise check explicitly via ChuNhiemBUS that this teacher is
                            // the homeroom for the student's class.
                            try {
                                HocSinhDTO hsForHk = hocSinhBUS.getHocSinhByMaHS(maHS);
                                if (hsForHk != null) {
                                    com.sgu.qlhs.dto.ChuNhiemDTO cn = new com.sgu.qlhs.bus.ChuNhiemBUS()
                                            .getChuNhiemByGV(ndForHk.getId());
                                    if (cn != null && cn.getMaLop() == hsForHk.getMaLop()) {
                                        canEditHanhKiem = true;
                                    }
                                }
                            } catch (Exception ex) {
                                // ignore and keep canEditHanhKiem = false
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                // conservative default: do not allow editing hạnh kiểm if any error
                canEditHanhKiem = false;
            }

            // ===== Hạnh kiểm =====
            HanhKiemDTO hk = hanhKiemBUS.getHanhKiem(maHS, maNK, hkNum, nd);
            String hanhKiemStr = hk != null ? hk.getXepLoai() : "(chưa có)";
            JLabel lblHanhKiem = new JLabel("Hạnh kiểm: " + hanhKiemStr);
            lblHanhKiem.setFont(new Font("Arial", Font.BOLD, 13));
            lblHanhKiem.setAlignmentX(Component.LEFT_ALIGNMENT);

            // put label + value label + combobox (combobox hidden when not editing)
            JPanel pnlHK = new JPanel(new FlowLayout(FlowLayout.LEFT));
            pnlHK.setBackground(Color.WHITE);
            JLabel lblHKLabel = new JLabel("Hạnh kiểm:");
            pnlHK.add(lblHKLabel);
            pnlHK.add(Box.createHorizontalStrut(6));
            // value label (shown when not in edit mode)
            lblHanhKiemValue = new JLabel(hanhKiemStr);
            lblHanhKiemValue.setFont(new Font("Arial", Font.BOLD, 13));
            pnlHK.add(lblHanhKiemValue);
            pnlHK.add(Box.createHorizontalStrut(10));
            if (cboHanhKiemEditor == null) {
                cboHanhKiemEditor = new JComboBox<>(new String[] { "Tốt", "Khá", "Trung bình", "Yếu" });
            }
            // set current selection from DB
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
            // Hạn chế việc sửa hạnh kiểm chỉ cho GVCN hoặc Admin
            cboHanhKiemEditor.setEnabled(tableEditing && canEditHanhKiem);
            cboHanhKiemEditor.setVisible(tableEditing && canEditHanhKiem);
            // show value label when not editing (or when user not permitted)
            lblHanhKiemValue.setVisible(!(tableEditing && canEditHanhKiem));
            if (!canEditHanhKiem) {
                // provide a helpful tooltip for the user
                lblHanhKiemValue
                        .setToolTipText("Chỉ giáo viên chủ nhiệm hoặc quản trị viên mới được chỉnh sửa Hạnh kiểm.");
            } else {
                lblHanhKiemValue.setToolTipText(null);
            }
            pnlHK.add(cboHanhKiemEditor);
            content.add(Box.createVerticalStrut(10));
            content.add(pnlHK);
            // apply computed permission: enable/disable edit actions for teachers
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
                // ignore
            }
        }

        table.setFont(new Font("Arial", Font.PLAIN, 12));
        table.setRowHeight(30);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(240, 240, 240));

        // Căn giữa các cột and apply permission-aware row highlighting.
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);

        // Renderer that indicates which rows are editable by background color and
        // optional tooltip. It delegates to centerRenderer for alignment and basic
        // rendering, then adjusts background based on rowCanEditList.
        DefaultTableCellRenderer permissionRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {

                // THAY ĐỔI: Hiển thị rỗng cho điểm của môn Đánh Giá
                int modelRow = -1;
                try {
                    modelRow = table.convertRowIndexToModel(row);
                } catch (Exception ex) {
                    /* ignore */ }

                if (modelRow >= 0 && modelRow < currentDiemList.size()) {
                    DiemDTO dto = currentDiemList.get(modelRow);
                    if ("DanhGia".equals(dto.getLoaiMon()) && column >= 2 && column <= 5) {
                        value = ""; // Hiển thị rỗng thay vì "0.0" hoặc "null"
                    }
                }

                Component c = centerRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus,
                        row, column);
                // default backgrounds
                Color editableBg = new Color(225, 255, 225); // light green for editable
                Color normalBg = Color.WHITE;
                Color disabledBg = new Color(245, 245, 245);

                try {
                    // int modelRow = table.convertRowIndexToModel(row);
                    boolean canEdit = false;
                    if (modelRow >= 0 && modelRow < rowCanEditList.size()) {
                        Boolean b = rowCanEditList.get(modelRow);
                        canEdit = b != null && b.booleanValue();
                    }
                    if (isSelected) {
                        // keep selection color when selected
                        c.setBackground(table.getSelectionBackground());
                    } else if (canEdit && tableEditing) { // Chỉ tô màu khi đang ở chế độ Sửa
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

        // Đặt độ rộng cột
        table.getColumnModel().getColumn(0).setPreferredWidth(40); // STT
        table.getColumnModel().getColumn(1).setPreferredWidth(120); // Tên môn
        table.getColumnModel().getColumn(2).setPreferredWidth(60); // Miệng
        table.getColumnModel().getColumn(3).setPreferredWidth(100); // 15 phút
        table.getColumnModel().getColumn(4).setPreferredWidth(100); // 1 tiết
        table.getColumnModel().getColumn(5).setPreferredWidth(60); // Cuối kỳ
        table.getColumnModel().getColumn(6).setPreferredWidth(60); // Kết quả (TB/ĐKĐ)
        table.getColumnModel().getColumn(7).setPreferredWidth(100); // Ghi chú

        // Thêm viền cho bảng
        table.setShowGrid(true);
        table.setGridColor(Color.BLACK);
        table.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));

        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        // widen the table area to better use the larger dialog size
        tableScroll.setPreferredSize(new Dimension(1100, 650));
        content.add(tableScroll);

        // ===== NHẬN XÉT CỦA GIÁO VIÊN =====
        // Load existing nhận xét (per-student, per-NK, per-HK) and show it below the
        // table. Use permission-aware getter.
        String nx = "";
        if (currentMaHS != -1) {
            try {
                // use the same resolved user (nd) if available; attempt to resolve again as
                // fallback
                com.sgu.qlhs.dto.NguoiDungDTO nd = null;
                try {
                    java.awt.Window w = javax.swing.SwingUtilities.getWindowAncestor(this);
                    if (w instanceof com.sgu.qlhs.ui.MainDashboard) {
                        com.sgu.qlhs.ui.MainDashboard md = (com.sgu.qlhs.ui.MainDashboard) w;
                        nd = md.getNguoiDung();
                    }
                } catch (Exception ex) {
                    // ignore
                }
                String fetched = diemBUS.getNhanXet(currentMaHS, currentMaNK, currentHocKy, nd);
                nx = fetched != null ? fetched : "";
            } catch (Exception ex) {
                nx = "";
            }
        }
        currentNhanXet = nx;
        JLabel lblNhanXet = new JLabel("Nhận xét của giáo viên:");
        lblNhanXet.setFont(new Font("Arial", Font.BOLD, 13));
        txtNhanXet = new JTextArea(5, 80);
        txtNhanXet.setLineWrap(true);
        txtNhanXet.setWrapStyleWord(true);
        txtNhanXet.setText(currentNhanXet != null ? currentNhanXet : "");
        txtNhanXet.setEditable(tableEditing);
        txtNhanXet.setFont(new Font("Arial", Font.PLAIN, 12));
        JScrollPane spNhanXet = new JScrollPane(txtNhanXet);
        spNhanXet.setPreferredSize(new Dimension(900, 120));
        content.add(Box.createVerticalStrut(10));
        content.add(lblNhanXet);
        content.add(Box.createVerticalStrut(6));
        content.add(spNhanXet);

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
            // resolve current user for permission checks
            com.sgu.qlhs.dto.NguoiDungDTO nd = null;
            try {
                java.awt.Window w = javax.swing.SwingUtilities.getWindowAncestor(this);
                if (w instanceof com.sgu.qlhs.ui.MainDashboard) {
                    com.sgu.qlhs.ui.MainDashboard md = (com.sgu.qlhs.ui.MainDashboard) w;
                    nd = md.getNguoiDung();
                }
            } catch (Exception ex) {
                // ignore
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

    // Flag set by caller to indicate the dialog was opened from the Chủ nhiệm tab
    // (Chủ nhiệm view already scopes rows to the teacher's class). When true we
    // relax the hạnh kiểm permission check so the chủ nhiệm may edit hạnh kiểm
    // for students in that tab without failing on other assignment checks.
    private boolean openedFromChuNhiem = false;

    public void setOpenedFromChuNhiem(boolean v) {
        this.openedFromChuNhiem = v;
    }

    /** Export current table to CSV file chosen by user. */
    private void exportCsv() {
        if (model == null || model.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Không có dữ liệu để xuất.");
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Lưu CSV");
        chooser.setFileFilter(new FileNameExtensionFilter("CSV files", "csv"));
        int rc = chooser.showSaveDialog(this);
        if (rc != JFileChooser.APPROVE_OPTION)
            return;
        File f = chooser.getSelectedFile();
        if (!f.getName().toLowerCase().endsWith(".csv")) {
            f = new File(f.getParentFile(), f.getName() + ".csv");
        }
        try (FileWriter fw = new FileWriter(f)) {
            // header
            for (int c = 0; c < model.getColumnCount(); c++) {
                fw.write(model.getColumnName(c));
                if (c < model.getColumnCount() - 1)
                    fw.write(",");
            }
            fw.write(System.lineSeparator());
            for (int r = 0; r < model.getRowCount(); r++) {
                for (int c = 0; c < model.getColumnCount(); c++) {
                    Object v = model.getValueAt(r, c);
                    fw.write(v == null ? "" : v.toString());
                    if (c < model.getColumnCount() - 1)
                        fw.write(",");
                }
                fw.write(System.lineSeparator());
            }
            JOptionPane.showMessageDialog(this, "Xuất CSV thành công: " + f.getAbsolutePath());
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Lỗi khi xuất CSV: " + ex.getMessage());
        }
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
        com.sgu.qlhs.dto.NguoiDungDTO ndForPrint = null;
        try {
            java.awt.Window w = javax.swing.SwingUtilities.getWindowAncestor(this);
            if (w instanceof com.sgu.qlhs.ui.MainDashboard) {
                com.sgu.qlhs.ui.MainDashboard md = (com.sgu.qlhs.ui.MainDashboard) w;
                ndForPrint = md.getNguoiDung();
            }
        } catch (Exception ex) {
            // ignore
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

        // Prepare printing with pagination and scaling to page width. Split header and
        // body
        // so header (title, school info, student info) can be printed on every page.
        // We'll build headerPanel and bodyPanel separately above to reuse here.
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
}