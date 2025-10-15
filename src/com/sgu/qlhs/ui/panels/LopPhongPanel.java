package com.sgu.qlhs.ui.panels;

import com.sgu.qlhs.ui.components.RoundedPanel;
import com.sgu.qlhs.ui.model.LopTableModel;
import com.sgu.qlhs.ui.model.PhongTableModel;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import static com.sgu.qlhs.ui.MainDashboard.*;

public class LopPhongPanel extends JPanel {
    public LopPhongPanel() {
        setLayout(new BorderLayout());
        setOpaque(false);

        var outer = new RoundedPanel(18, CARD_BG, CARD_BORDER);
        outer.setLayout(new BorderLayout());

        var lbl = new JLabel("Lớp / Phòng");
        lbl.setBorder(new EmptyBorder(12, 16, 8, 16));
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 18f));
        outer.add(lbl, BorderLayout.NORTH);

        var tabs = new JTabbedPane();
        tabs.setOpaque(false);

        // ===== TAB LỚP =====
        var lopPanel = new JPanel(new BorderLayout());
        lopPanel.setOpaque(false);

        LopTableModel lopModel = new LopTableModel();
        JTable tblLop = new JTable(lopModel);

        // Thanh công cụ
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        JButton btnView = new JButton("Xem chi tiết");
        bar.add(btnView);
        lopPanel.add(bar, BorderLayout.NORTH);

        lopPanel.add(new JScrollPane(tblLop), BorderLayout.CENTER);

        // Sự kiện xem chi tiết
        btnView.addActionListener(e -> {
            int row = tblLop.getSelectedRow();
            if (row >= 0) {
                int modelRow = tblLop.convertRowIndexToModel(row);
                int maLop = lopModel.getMaLop(modelRow);
                String tenLop = lopModel.getTenLop(modelRow);
                List<Object[]> dsHocSinh = lopModel.getHocSinhByLop(maLop);

                new ChiTietLopDialog(SwingUtilities.getWindowAncestor(this), maLop, tenLop, dsHocSinh)
                        .setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn một lớp để xem chi tiết!");
            }
        });

        // ===== TAB PHÒNG =====
        var phongPanel = new JPanel(new BorderLayout());
        phongPanel.setOpaque(false);
        phongPanel.add(new JScrollPane(new JTable(new PhongTableModel())), BorderLayout.CENTER);

        tabs.addTab("Lớp", lopPanel);
        tabs.addTab("Phòng", phongPanel);

        outer.add(tabs, BorderLayout.CENTER);
        add(outer, BorderLayout.CENTER);
    }

    // ===================== DIALOG CHI TIẾT LỚP =====================
    private static class ChiTietLopDialog extends JDialog {
        private final DefaultTableModel model =
                new DefaultTableModel(new Object[]{"Mã HS", "Họ tên", "Giới tính", "Ngày sinh"}, 0);

        public ChiTietLopDialog(Window owner, int maLop, String tenLop, List<Object[]> dsHocSinh) {
            super(owner, "Chi tiết lớp: " + tenLop, ModalityType.APPLICATION_MODAL);
            setMinimumSize(new Dimension(600, 400));
            setLocationRelativeTo(owner);
            buildUI(tenLop, dsHocSinh);
        }

        private void buildUI(String tenLop, List<Object[]> dsHocSinh) {
            var root = new JPanel(new BorderLayout(12, 12));
            root.setBorder(new EmptyBorder(16, 16, 16, 16));
            setContentPane(root);

            JLabel lblTitle = new JLabel("Danh sách học sinh lớp " + tenLop, JLabel.CENTER);
            lblTitle.setFont(lblTitle.getFont().deriveFont(Font.BOLD, 18f));
            root.add(lblTitle, BorderLayout.NORTH);

            var tbl = new JTable(model);
            root.add(new JScrollPane(tbl), BorderLayout.CENTER);

            // Nạp dữ liệu học sinh
            for (Object[] hs : dsHocSinh) {
                model.addRow(hs);
            }
        }
    }
}
