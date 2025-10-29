package com.sgu.qlhs.ui.dialogs;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import com.sgu.qlhs.bus.LopBUS;
import com.sgu.qlhs.bus.HocSinhBUS;
import com.sgu.qlhs.bus.DiemBUS;
import com.sgu.qlhs.bus.MonBUS;
import com.sgu.qlhs.dto.LopDTO;
import com.sgu.qlhs.dto.HocSinhDTO;
import com.sgu.qlhs.dto.DiemDTO;

public class DiemTrungBinhTatCaMonDialog extends JDialog {
    private final JComboBox<String> cboLop = new JComboBox<>();
    private final JComboBox<String> cboHK = new JComboBox<>(new String[] { "HK1", "HK2", "Cả năm" });
    // Cột 0..1: mã, họ tên | 2..7: 6 môn | 8: TB | 9: Xếp loại
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[] { "Mã HS", "Họ tên", "Toán", "Văn", "Anh", "Lý", "Hóa", "Sinh", "Trung bình", "Xếp loại" },
            0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            // Cho sửa điểm môn ở HK1/HK2, không cho sửa khi chọn "Cả năm"
            Object sel = cboHK.getSelectedItem();
            boolean editableHK = (sel != null && ("HK1".equals(sel) || "HK2".equals(sel)));
            return editableHK && c >= 2 && c <= 7;
        }

        @Override
        public Class<?> getColumnClass(int c) {
            return (c >= 2 && c <= 8) ? Double.class : String.class;
        }
    };

    // Danh sách môn tương ứng các cột 2..7 (loaded from DB when possible)
    private String[] mons = new String[] { "Toán", "Văn", "Anh", "Lý", "Hóa", "Sinh" };
    // Lưu điểm HK1/HK2 theo: Map<MãHS, Map<Môn, ĐiểmTBMonHK>>
    private final java.util.Map<String, java.util.Map<String, Double>> hk1 = new java.util.HashMap<>();
    private final java.util.Map<String, java.util.Map<String, Double>> hk2 = new java.util.HashMap<>();
    private boolean loading = false; // tránh cập nhật map khi đang nạp dữ liệu vào bảng

    public DiemTrungBinhTatCaMonDialog(Window owner) {
        super(owner, "Tính điểm trung bình tất cả các môn", ModalityType.APPLICATION_MODAL);
        setMinimumSize(new Dimension(1000, 550));
        setLocationRelativeTo(owner);
        initBuses();
        loadMonData();
        build();
        loadLopData();
        pack();
    }

    private LopBUS lopBUS;
    private HocSinhBUS hocSinhBUS;
    private DiemBUS diemBUS;
    private MonBUS monBUS;
    private java.util.List<LopDTO> lops = new java.util.ArrayList<>();

    private void initBuses() {
        lopBUS = new LopBUS();
        hocSinhBUS = new HocSinhBUS();
        diemBUS = new DiemBUS();
        monBUS = new MonBUS();
    }

    private void loadMonData() {
        try {
            java.util.List<com.sgu.qlhs.dto.MonHocDTO> list = new java.util.ArrayList<>();
            if (monBUS == null) {
                monBUS = new MonBUS();
            }
            list = monBUS.getAllMon();
            if (list != null && list.size() >= 1) {
                // take up to 6 subject names to match table columns
                int n = Math.min(6, list.size());
                String[] arr = new String[6];
                for (int i = 0; i < 6; i++) {
                    if (i < n)
                        arr[i] = list.get(i).getTenMon();
                    else
                        arr[i] = mons[i];
                }
                mons = arr;
                // update table column headers to reflect subject names
                String[] cols = new String[10];
                cols[0] = "Mã HS";
                cols[1] = "Họ tên";
                for (int i = 0; i < 6; i++)
                    cols[2 + i] = mons[i];
                cols[8] = "Trung bình";
                cols[9] = "Xếp loại";
                model.setColumnIdentifiers(cols);
            }
        } catch (Exception ex) {
            // keep defaults if DB access fails
        }
    }

    private void build() {
        var root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(new EmptyBorder(16, 16, 16, 16));
        setContentPane(root);

        // ===== Thanh lọc trên cùng =====
        var bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        bar.add(new JLabel("Lớp:"));
        bar.add(cboLop);
        bar.add(new JLabel("Học kỳ:"));
        bar.add(cboHK);
        var btnCalc = new JButton("Tính điểm TB tất cả môn");
        bar.add(btnCalc);
        root.add(bar, BorderLayout.NORTH);

        // ===== Bảng điểm =====
        var tbl = new JTable(model);
        tbl.setRowHeight(26);
        root.add(new JScrollPane(tbl), BorderLayout.CENTER);

        // initially empty; will load when class selected

        // Theo dõi chỉnh sửa bảng để ghi vào map HK tương ứng
        model.addTableModelListener(e -> {
            if (loading)
                return; // bỏ qua khi đang fill dữ liệu
            int c = e.getColumn();
            if (c >= 2 && c <= 7) {
                Object sel = cboHK.getSelectedItem();
                if (sel == null)
                    return;
                if (!"HK1".equals(sel) && !"HK2".equals(sel))
                    return; // chỉ ghi khi là HK1/HK2
                java.util.Map<String, java.util.Map<String, Double>> target = "HK1".equals(sel) ? hk1 : hk2;
                int r0 = e.getFirstRow();
                int r1 = e.getLastRow();
                for (int r = r0; r <= r1; r++) {
                    String ma = String.valueOf(model.getValueAt(r, 0));
                    String mon = mons[c - 2];
                    Double val = model.getValueAt(r, c) == null ? null
                            : ((Number) model.getValueAt(r, c)).doubleValue();
                    setScore(target, ma, mon, val);
                }
            }
        });

        // class/hk selection
        cboLop.addActionListener((java.awt.event.ActionEvent __) -> {
            if (__ == null) {
            }
            reloadForSelection();
        });
        cboHK.addActionListener((java.awt.event.ActionEvent __) -> {
            if (__ == null) {
            }
            reloadTableByHKSelection();
        });

        // ===== Nút tính toán =====
        btnCalc.addActionListener(e -> {
            if (e == null)
                return; // tránh cảnh báo tham số không dùng
            Object sel = cboHK.getSelectedItem();
            for (int r = 0; r < model.getRowCount(); r++) {
                String ma = String.valueOf(model.getValueAt(r, 0));
                double tong = 0;
                int dem = 0;
                for (int c = 2; c <= 7; c++) {
                    String mon = mons[c - 2];
                    double d;
                    if ("Cả năm".equals(sel)) {
                        Double d1 = getScore(hk1, ma, mon);
                        Double d2 = getScore(hk2, ma, mon);
                        // TB môn năm = trung bình HK1 & HK2 (nếu có)
                        int cnt = 0;
                        double sum = 0;
                        if (d1 != null) {
                            sum += d1;
                            cnt++;
                        }
                        if (d2 != null) {
                            sum += d2;
                            cnt++;
                        }
                        d = (cnt > 0) ? (sum / cnt) : 0.0;
                    } else {
                        // HK cụ thể: lấy trực tiếp giá trị hiển thị (đã là TB môn của HK đó)
                        d = val(model.getValueAt(r, c));
                    }
                    tong += d;
                    dem++;
                }
                double tb = dem > 0 ? round1(tong / dem) : 0;
                model.setValueAt(tb, r, 8);
                model.setValueAt(xepLoai(tb), r, 9);
            }
        });

        // ===== Nút lưu + đóng =====
        var btnPane = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        var btnClose = new JButton("Đóng");
        var btnSave = new JButton("Lưu kết quả");
        btnSave.addActionListener(e -> {
            if (e == null)
                return; // tránh cảnh báo tham số không dùng
            // Persist hk1/hk2 to DB per student+semester using DiemBUS.upsert
            Object sel = cboHK.getSelectedItem();
            // If user is viewing a specific HK, snapshot visible table into that HK store
            if ("HK1".equals(sel))
                snapshotCurrentTableTo(hk1);
            else if ("HK2".equals(sel))
                snapshotCurrentTableTo(hk2);

            // Build subject name -> maMon map
            java.util.Map<String, Integer> monMap = new java.util.HashMap<>();
            try {
                for (var m : monBUS.getAllMon())
                    monMap.put(m.getTenMon(), m.getMaMon());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi khi tải danh sách môn: " + ex.getMessage());
                return;
            }

            int saved = 0;
            int maNK = com.sgu.qlhs.bus.NienKhoaBUS.current();

            // Save HK1
            for (var entry : hk1.entrySet()) {
                String maHSs = entry.getKey();
                int maHS;
                try {
                    maHS = Integer.parseInt(maHSs);
                } catch (NumberFormatException ex) {
                    continue;
                }
                for (var e2 : entry.getValue().entrySet()) {
                    String tenMon = e2.getKey();
                    Double val = e2.getValue();
                    if (val == null)
                        continue;
                    Integer maMon = monMap.get(tenMon);
                    if (maMon == null)
                        continue; // unknown subject name
                    try {
                        // Persist HK TB by populating all components with the same TB
                        // This ensures the DB-generated DiemTB equals the TB value the user entered.
                        double tbv = val.doubleValue();
                        diemBUS.saveOrUpdateDiem(maHS, maMon, 1, maNK, tbv, tbv, tbv, tbv);
                        saved++;
                    } catch (Exception ex) {
                        System.err.println("Lỗi khi lưu điểm HK1: " + ex.getMessage());
                    }
                }
            }

            // Save HK2
            for (var entry : hk2.entrySet()) {
                String maHSs = entry.getKey();
                int maHS;
                try {
                    maHS = Integer.parseInt(maHSs);
                } catch (NumberFormatException ex) {
                    continue;
                }
                for (var e2 : entry.getValue().entrySet()) {
                    String tenMon = e2.getKey();
                    Double val = e2.getValue();
                    if (val == null)
                        continue;
                    Integer maMon = monMap.get(tenMon);
                    if (maMon == null)
                        continue;
                    try {
                        double tbv = val.doubleValue();
                        diemBUS.saveOrUpdateDiem(maHS, maMon, 2, maNK, tbv, tbv, tbv, tbv);
                        saved++;
                    } catch (Exception ex) {
                        System.err.println("Lỗi khi lưu điểm HK2: " + ex.getMessage());
                    }
                }
            }

            JOptionPane.showMessageDialog(this, "Đã lưu " + saved + " mục điểm vào cơ sở dữ liệu.");
        });
        btnClose.addActionListener(e -> {
            if (e == null)
                return;
            dispose();
        });
        btnPane.add(btnClose);
        btnPane.add(btnSave);
        root.add(btnPane, BorderLayout.SOUTH);
    }

    // ===== Hàm hỗ trợ =====
    private static double val(Object o) {
        return o == null ? 0.0 : ((Number) o).doubleValue();
    }

    private static double round1(double x) {
        return Math.round(x * 10.0) / 10.0;
    }

    private static String xepLoai(double tb) {
        if (tb >= 8.0)
            return "Giỏi";
        if (tb >= 6.5)
            return "Khá";
        if (tb >= 5.0)
            return "Trung bình";
        return "Yếu";
    }

    private static Double getScore(java.util.Map<String, java.util.Map<String, Double>> store, String ma, String mon) {
        var m = store.get(ma);
        if (m == null)
            return null;
        return m.get(mon);
    }

    private static void setScore(java.util.Map<String, java.util.Map<String, Double>> store, String ma, String mon,
            Double val) {
        java.util.Map<String, Double> m = store.get(ma);
        if (m == null) {
            m = new java.util.HashMap<>();
            store.put(ma, m);
        }
        if (val == null)
            m.remove(mon);
        else
            m.put(mon, val);
    }

    private void snapshotCurrentTableTo(java.util.Map<String, java.util.Map<String, Double>> store) {
        for (int r = 0; r < model.getRowCount(); r++) {
            String ma = String.valueOf(model.getValueAt(r, 0));
            for (int i = 0; i < mons.length; i++) {
                Object v = model.getValueAt(r, 2 + i);
                Double d = (v instanceof Number) ? ((Number) v).doubleValue() : null;
                if (d != null)
                    setScore(store, ma, mons[i], d);
            }
        }
    }

    private void reloadTableByHKSelection() {
        Object sel = cboHK.getSelectedItem();
        loading = true;
        try {
            // reset TB & Xếp loại khi đổi học kỳ
            for (int r = 0; r < model.getRowCount(); r++) {
                if ("HK1".equals(sel) || "HK2".equals(sel)) {
                    var src = "HK1".equals(sel) ? hk1 : hk2;
                    String ma = String.valueOf(model.getValueAt(r, 0));
                    for (int i = 0; i < mons.length; i++) {
                        Double d = getScore(src, ma, mons[i]);
                        model.setValueAt(d, r, 2 + i);
                    }
                } else { // Cả năm -> hiển thị TB môn năm = TB(HK1,HK2)
                    String ma = String.valueOf(model.getValueAt(r, 0));
                    for (int i = 0; i < mons.length; i++) {
                        Double d1 = getScore(hk1, ma, mons[i]);
                        Double d2 = getScore(hk2, ma, mons[i]);
                        int cnt = 0;
                        double sum = 0;
                        if (d1 != null) {
                            sum += d1;
                            cnt++;
                        }
                        if (d2 != null) {
                            sum += d2;
                            cnt++;
                        }
                        Double dn = (cnt > 0) ? round1(sum / cnt) : null;
                        model.setValueAt(dn, r, 2 + i);
                    }
                }
                model.setValueAt(null, r, 8);
                model.setValueAt(null, r, 9);
            }
        } finally {
            loading = false;
        }
        // Làm mới quyền edit theo HK
        model.fireTableStructureChanged();
    }

    private void loadLopData() {
        lops = lopBUS.getAllLop();
        cboLop.removeAllItems();
        cboLop.addItem("-- Chọn lớp --");
        for (LopDTO l : lops)
            cboLop.addItem(l.getTenLop());
    }

    private void reloadForSelection() {
        int idx = cboLop.getSelectedIndex();
        hk1.clear();
        hk2.clear();
        model.setRowCount(0);
        if (idx <= 0)
            return;
        LopDTO sel = lops.get(idx - 1);
        int maLop = sel.getMaLop();
        // load students
        java.util.List<HocSinhDTO> students = hocSinhBUS.getHocSinhByMaLop(maLop);
        for (HocSinhDTO hs : students) {
            model.addRow(new Object[] { hs.getMaHS(), hs.getHoTen(), null, null, null, null, null, null, null, null });
            // load HK1
            java.util.List<DiemDTO> d1 = diemBUS.getDiemByMaHS(hs.getMaHS(), 1, com.sgu.qlhs.bus.NienKhoaBUS.current());
            for (DiemDTO d : d1) {
                // compute TB for the subject from components
                double tb = round1(d.getDiemMieng() * 0.10 + d.getDiem15p() * 0.20 + d.getDiemGiuaKy() * 0.30
                        + d.getDiemCuoiKy() * 0.40);
                setScore(hk1, String.valueOf(hs.getMaHS()), d.getTenMon(), tb);
            }
            // load HK2
            java.util.List<DiemDTO> d2 = diemBUS.getDiemByMaHS(hs.getMaHS(), 2, com.sgu.qlhs.bus.NienKhoaBUS.current());
            for (DiemDTO d : d2) {
                double tb = round1(d.getDiemMieng() * 0.10 + d.getDiem15p() * 0.20 + d.getDiemGiuaKy() * 0.30
                        + d.getDiemCuoiKy() * 0.40);
                setScore(hk2, String.valueOf(hs.getMaHS()), d.getTenMon(), tb);
            }
        }
        // refresh view
        reloadTableByHKSelection();
    }
}
