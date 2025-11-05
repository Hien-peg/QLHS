package com.sgu.qlhs.bus;

import com.sgu.qlhs.dto.HocSinhDTO;
import com.sgu.qlhs.database.HocSinhDAO;
import java.util.ArrayList;
import java.util.List;

public class HocSinhBUS {
    private HocSinhDAO dao;

    public HocSinhBUS() {
        dao = new HocSinhDAO();
    }

    /**
     * Lấy toàn bộ học sinh (JOIN với tên lớp)
     */
    public List<HocSinhDTO> getAllHocSinh() {
        List<HocSinhDTO> list = new ArrayList<>();
        List<Object[]> rows = dao.getAllHocSinh();

        for (Object[] r : rows) {
            int maHS = parseInt(r[0]);
            String hoTen = str(r[1]);
            String ngaySinh = str(r[2]);
            String gioiTinh = str(r[3]);
            String tenLop = (r.length > 4) ? str(r[4]) : "";
            HocSinhDTO hs = new HocSinhDTO();
            hs.setMaHS(maHS);
            hs.setHoTen(hoTen);
            try {
                if (!ngaySinh.isEmpty()) {
                    java.sql.Date d = java.sql.Date.valueOf(ngaySinh);
                    hs.setNgaySinh(d);
                }
            } catch (Exception ex) {
                // ignore parse errors
            }
            hs.setGioiTinh(gioiTinh);
            hs.setTenLop(tenLop);
            list.add(hs);
        }

        return list;
    }

    /**
     * Lấy danh sách học sinh theo mã lớp
     */
    public List<HocSinhDTO> getHocSinhByMaLop(int maLop) {
        List<HocSinhDTO> list = new ArrayList<>();
        List<Object[]> rows = dao.getHocSinhByMaLop(maLop);

        for (Object[] r : rows) {
            int maHS = parseInt(r[0]);
            String hoTen = str(r[1]);
            String gioiTinh = str(r[2]);
            String ngaySinh = str(r[3]);
            HocSinhDTO hs = new HocSinhDTO();
            hs.setMaHS(maHS);
            hs.setHoTen(hoTen);
            hs.setGioiTinh(gioiTinh);
            try {
                if (!ngaySinh.isEmpty()) {
                    java.sql.Date d = java.sql.Date.valueOf(ngaySinh);
                    hs.setNgaySinh(d);
                }
            } catch (Exception ex) {
                // ignore
            }
            hs.setMaLop(maLop);
            list.add(hs);
        }

        return list;
    }

    /**
     * Thêm học sinh mới
     */
    public void saveHocSinh(String hoTen, java.util.Date ngaySinh, String gioiTinh,
            String diaChi, String sdt, String email, int maLop) {
        dao.addHocSinh(hoTen, ngaySinh, gioiTinh, diaChi, sdt, email, maLop);
    }

    /**
     * Cập nhật học sinh
     */
    public void updateHocSinh(int maHS, String hoTen, java.util.Date ngaySinh, String gioiTinh,
            String diaChi, String sdt, String email, int maLop) {
        dao.updateHocSinh(maHS, hoTen, ngaySinh, gioiTinh, diaChi, sdt, email, maLop);
    }

    /**
     * Xóa học sinh
     */
    public void deleteHocSinh(int maHS) {
        dao.deleteHocSinh(maHS);
    }

    /**
     * Tìm học sinh theo mã HS
     */
    public HocSinhDTO getHocSinhByMaHS(int maHS) {
        for (HocSinhDTO h : getAllHocSinh()) {
            if (h.getMaHS() == maHS)
                return h;
        }
        return null;
    }

    /**
     * 🔍 Tìm học sinh theo tài khoản đăng nhập (MaND)
     * → Dùng khi học sinh đăng nhập để xem thời khóa biểu
     */
    public HocSinhDTO getByMaND(int maND) {
        return dao.findByMaND(maND);
    }

    // =================== Tiện ích nội bộ ===================
    private int parseInt(Object o) {
        if (o == null)
            return 0;
        return (o instanceof Integer) ? (Integer) o : Integer.parseInt(o.toString());
    }

    private String str(Object o) {
        return (o == null) ? "" : o.toString();
    }

    public HocSinhDTO getByMaHS(int maHS) {
        return dao.findByMaHS(maHS);
    }
}
