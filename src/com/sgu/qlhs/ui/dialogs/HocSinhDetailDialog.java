package com.sgu.qlhs.ui.dialogs;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class HocSinhDetailDialog extends JDialog {

    public HocSinhDetailDialog(Window owner, Object[] hocSinhData) {
        super(owner, "Chi tiết học sinh", ModalityType.APPLICATION_MODAL);
        setSize(600, 700);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(12, 12));
        buildDetail(hocSinhData);
    }

    private void buildDetail(Object[] hocSinhData) {
        var mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(16, 16, 16, 16));

        // Fake data nếu không truyền đủ
        if (hocSinhData == null || hocSinhData.length < 14) {
            hocSinhData = new Object[]{
                "HS001", "Nguyễn Văn A", "15/08/2007", "Nam", "10A1",
                "0987654321", "nguyenvana@example.com",
                "Nguyễn Văn B", "Cha", "0912345678", "nguyenvanb@example.com",
                "Trần Thị C", "Mẹ", "0922334455", "tranthic@example.com"
            };
        }

        // ===== Thông tin học sinh =====
        var pnlHS = new JPanel(new GridLayout(0, 2, 10, 10));
        pnlHS.setBorder(BorderFactory.createTitledBorder("Thông tin học sinh"));

        String[] hsLabels = {"Mã HS:", "Họ tên:", "Ngày sinh:", "Giới tính:", "Lớp:", "SĐT:", "Email:"};

        for (int i = 0; i < hsLabels.length; i++) {
            pnlHS.add(new JLabel(hsLabels[i]));
            JTextField txt = new JTextField(String.valueOf(hocSinhData[i]));
            txt.setEditable(false);
            pnlHS.add(txt);
        }

        // ===== Phụ huynh 1 =====
        var pnlPh1 = new JPanel(new GridLayout(0, 2, 10, 10));
        pnlPh1.setBorder(BorderFactory.createTitledBorder("Phụ huynh 1"));
        String[] ph1Labels = {"Họ tên:", "Mối quan hệ:", "SĐT:", "Email:"};

        for (int i = 0; i < ph1Labels.length; i++) {
            pnlPh1.add(new JLabel(ph1Labels[i]));
            JTextField txt = new JTextField(String.valueOf(hocSinhData[7 + i]));
            txt.setEditable(false);
            pnlPh1.add(txt);
        }

        // ===== Phụ huynh 2 =====
        var pnlPh2 = new JPanel(new GridLayout(0, 2, 10, 10));
        pnlPh2.setBorder(BorderFactory.createTitledBorder("Phụ huynh 2"));
        String[] ph2Labels = {"Họ tên:", "Mối quan hệ:", "SĐT:", "Email:"};

        for (int i = 0; i < ph2Labels.length; i++) {
            pnlPh2.add(new JLabel(ph2Labels[i]));
            JTextField txt = new JTextField(String.valueOf(hocSinhData[11 + i]));
            txt.setEditable(false);
            pnlPh2.add(txt);
        }

        // ===== Thêm vào panel chính =====
        mainPanel.add(pnlHS);
        mainPanel.add(Box.createVerticalStrut(12));
        mainPanel.add(pnlPh1);
        mainPanel.add(Box.createVerticalStrut(12));
        mainPanel.add(pnlPh2);

        add(new JScrollPane(mainPanel), BorderLayout.CENTER);

        // ===== Nút đóng =====
        var pnlBtn = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnClose = new JButton("Đóng");
        btnClose.addActionListener(e -> dispose());
        pnlBtn.add(btnClose);
        add(pnlBtn, BorderLayout.SOUTH);
    }
}
