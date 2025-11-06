package com.sgu.qlhs.ui.dialogs;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import com.sgu.qlhs.bus.PhuHuynhBUS;
import com.sgu.qlhs.dto.PhuHuynhDTO;
import java.util.List;
// ==================

public class HocSinhDetailDialog extends JDialog {

    // === THÊM BUS ===
    private PhuHuynhBUS phuHuynhBUS;
    // ================

    public HocSinhDetailDialog(Window owner, Object[] hocSinhData) {
        super(owner, "Chi tiết học sinh", ModalityType.APPLICATION_MODAL);
        
        // === KHỞI TẠO BUS ===
        this.phuHuynhBUS = new PhuHuynhBUS();
        // ======================
        
        setSize(600, 700);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(12, 12));
        buildDetail(hocSinhData);
    }

    private void buildDetail(Object[] hocSinhData) {
        var mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(16, 16, 16, 16));

        // ===== Thông tin học sinh =====
        var pnlHS = new JPanel(new GridLayout(0, 2, 10, 10));
        pnlHS.setBorder(BorderFactory.createTitledBorder("Thông tin học sinh"));

        // Dữ liệu học sinh cơ bản (từ HocSinhTableModel có 5 cột)
        String[] hsLabels = {"Mã HS:", "Họ tên:", "Ngày sinh:", "Giới tính:", "Lớp:"};
        int maHS = 0; // Sẽ lấy từ hocSinhData[0]

        for (int i = 0; i < hsLabels.length; i++) {
            pnlHS.add(new JLabel(hsLabels[i]));
            String value = (hocSinhData != null && i < hocSinhData.length && hocSinhData[i] != null) 
                            ? String.valueOf(hocSinhData[i]) : "";
            
            if (i == 0) { // Lấy MaHS
                try {
                    maHS = Integer.parseInt(value);
                } catch (NumberFormatException e) {
                    maHS = 0; // Không tìm thấy mã HS
                }
            }
            
            pnlHS.add(createReadOnlyField(value));
        }

        // (Thông tin SĐT, Email của học sinh không có trong HocSinhTableModel,
        
        // ===== TẢI DỮ LIỆU PHỤ HUYNH THẬT =====
        List<PhuHuynhDTO> phuHuynhList = phuHuynhBUS.getByHocSinh(maHS);
        
        // Gán Phụ huynh 1
        PhuHuynhDTO ph1 = (phuHuynhList.size() > 0) ? phuHuynhList.get(0) : new PhuHuynhDTO();
        // Gán Phụ huynh 2
        PhuHuynhDTO ph2 = (phuHuynhList.size() > 1) ? phuHuynhList.get(1) : new PhuHuynhDTO();
        // ======================================


        // ===== Phụ huynh 1 =====
        var pnlPh1 = new JPanel(new GridLayout(0, 2, 10, 10));
        pnlPh1.setBorder(BorderFactory.createTitledBorder("Phụ huynh 1"));
        String[] phLabels = {"Họ tên:", "Mối quan hệ:", "SĐT:", "Email:", "Địa chỉ:"};

        pnlPh1.add(new JLabel(phLabels[0]));
        pnlPh1.add(createReadOnlyField(ph1.getHoTen()));
        pnlPh1.add(new JLabel(phLabels[1]));
        pnlPh1.add(createReadOnlyField(ph1.getQuanHe()));
        pnlPh1.add(new JLabel(phLabels[2]));
        pnlPh1.add(createReadOnlyField(ph1.getSoDienThoai()));
        pnlPh1.add(new JLabel(phLabels[3]));
        pnlPh1.add(createReadOnlyField(ph1.getEmail()));
        pnlPh1.add(new JLabel(phLabels[4]));
        pnlPh1.add(createReadOnlyField(ph1.getDiaChi()));


        // ===== Phụ huynh 2 =====
        var pnlPh2 = new JPanel(new GridLayout(0, 2, 10, 10));
        pnlPh2.setBorder(BorderFactory.createTitledBorder("Phụ huynh 2"));

        pnlPh2.add(new JLabel(phLabels[0]));
        pnlPh2.add(createReadOnlyField(ph2.getHoTen()));
        pnlPh2.add(new JLabel(phLabels[1]));
        pnlPh2.add(createReadOnlyField(ph2.getQuanHe()));
        pnlPh2.add(new JLabel(phLabels[2]));
        pnlPh2.add(createReadOnlyField(ph2.getSoDienThoai()));
        pnlPh2.add(new JLabel(phLabels[3]));
        pnlPh2.add(createReadOnlyField(ph2.getEmail()));
        pnlPh2.add(new JLabel(phLabels[4]));
        pnlPh2.add(createReadOnlyField(ph2.getDiaChi()));

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
    
    /**
     * Hàm hỗ trợ tạo JTextField không cho chỉnh sửa
     */
    private JTextField createReadOnlyField(String text) {
        JTextField txt = new JTextField(text != null ? text : "");
        txt.setEditable(false);
        txt.setBackground(Color.WHITE); // Đảm bảo nền trắng
        return txt;
    }
}