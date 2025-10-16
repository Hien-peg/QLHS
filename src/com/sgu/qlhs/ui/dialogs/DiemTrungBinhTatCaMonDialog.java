package com.sgu.qlhs.ui.dialogs;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class DiemTrungBinhTatCaMonDialog extends JDialog {
    private final JComboBox<String> cboLop = new JComboBox<>(new String[] { "10A1", "10A2", "11A1", "12A1" });
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

    // Danh sách môn tương ứng các cột 2..7
    private static final String[] MONS = { "Toán", "Văn", "Anh", "Lý", "Hóa", "Sinh" };
    // Lưu điểm HK1/HK2 theo: Map<MãHS, Map<Môn, ĐiểmTBMonHK>>
    private final java.util.Map<String, java.util.Map<String, Double>> hk1 = new java.util.HashMap<>();
    private final java.util.Map<String, java.util.Map<String, Double>> hk2 = new java.util.HashMap<>();
    private boolean loading = false; // tránh cập nhật map khi đang nạp dữ liệu vào bảng

    public DiemTrungBinhTatCaMonDialog(Window owner) {
        super(owner, "Tính điểm trung bình tất cả các môn", ModalityType.APPLICATION_MODAL);
        setMinimumSize(new Dimension(1000, 550));
        setLocationRelativeTo(owner);
        build();
        pack();
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

        // === Dữ liệu mẫu ===
        model.addRow(new Object[] { "HS01", "Nguyễn Văn A", 8.0, 7.5, 8.2, 7.8, 8.0, 7.9, null, null });
        model.addRow(new Object[] { "HS02", "Trần Thị B", 9.0, 8.5, 8.0, 8.5, 9.0, 8.8, null, null });
        model.addRow(new Object[] { "HS03", "Lê Văn C", 6.8, 7.2, 6.5, 7.0, 6.8, 7.1, null, null });

        // Mặc định coi dữ liệu khởi tạo là điểm TB môn của HK1
        snapshotCurrentTableTo(hk1);

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
                    String mon = MONS[c - 2];
                    Double val = model.getValueAt(r, c) == null ? null
                            : ((Number) model.getValueAt(r, c)).doubleValue();
                    setScore(target, ma, mon, val);
                }
            }
        });

        // Chuyển học kỳ -> nạp lại dữ liệu cột môn tương ứng
        cboHK.addActionListener(ev -> {
            if (ev == null)
                return;
            reloadTableByHKSelection();
        });
        reloadTableByHKSelection(); // nạp lần đầu

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
                    String mon = MONS[c - 2];
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
            // TODO: Ghi hk1/hk2 xuống DB theo lớp + học kỳ
            JOptionPane.showMessageDialog(this, "(Demo) Đã lưu tạm thời trong bộ nhớ.");
            dispose();
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
            for (int i = 0; i < MONS.length; i++) {
                Object v = model.getValueAt(r, 2 + i);
                Double d = (v instanceof Number) ? ((Number) v).doubleValue() : null;
                if (d != null)
                    setScore(store, ma, MONS[i], d);
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
                    for (int i = 0; i < MONS.length; i++) {
                        Double d = getScore(src, ma, MONS[i]);
                        model.setValueAt(d, r, 2 + i);
                    }
                } else { // Cả năm -> hiển thị TB môn năm = TB(HK1,HK2)
                    String ma = String.valueOf(model.getValueAt(r, 0));
                    for (int i = 0; i < MONS.length; i++) {
                        Double d1 = getScore(hk1, ma, MONS[i]);
                        Double d2 = getScore(hk2, ma, MONS[i]);
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
}
