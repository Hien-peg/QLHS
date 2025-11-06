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
            // SỬA LỖI: Tạo DTO và gán dữ liệu
            HocSinhDTO hs = new HocSinhDTO();
            hs.setMaHS(parseInt(r[0]));
            hs.setHoTen(str(r[1]));
            try {
                // Xử lý Ngày sinh an toàn
                hs.setNgaySinh(java.sql.Date.valueOf(str(r[2])));
            } catch (Exception e) {
                hs.setNgaySinh(null);
            }
            hs.setGioiTinh(str(r[3]));
            hs.setTenLop((r.length > 4) ? str(r[4]) : "");
            list.add(hs); // Thêm DTO đã có dữ liệu
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
            // SỬA LỖI: Tạo DTO và gán dữ liệu
            HocSinhDTO hs = new HocSinhDTO();
            hs.setMaHS(parseInt(r[0]));
            hs.setHoTen(str(r[1]));
            hs.setGioiTinh(str(r[2]));
            try {
                // Xử lý Ngày sinh an toàn
                hs.setNgaySinh(java.sql.Date.valueOf(str(r[3])));
            } catch (Exception e) {
                hs.setNgaySinh(null);
            }
            hs.setMaLop(maLop);
            list.add(hs); // Thêm DTO đã có dữ liệu
        }
        return list;
    }

    /**
     * Thêm học sinh mới
     */
    public boolean saveHocSinh(String hoTen, java.util.Date ngaySinh, String gioiTinh,
            String diaChi, String sdt, String email, int maLop,
            String ph1HoTen, String ph1MQH, String ph1Sdt, String ph1Email,
            String ph2HoTen, String ph2MQH, String ph2Sdt, String ph2Email) {

    	return dao.addHocSinh(hoTen, ngaySinh, gioiTinh, diaChi, sdt, email, maLop,
           ph1HoTen, ph1MQH, ph1Sdt, ph1Email,
           ph2HoTen, ph2MQH, ph2Sdt, ph2Email);
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
    public boolean deleteHocSinh(int maHS) {
        return dao.deleteHocSinh(maHS); // Trả về kết quả của DAO
    }

    /**
     * Tìm học sinh theo mã HS
     */
    public HocSinhDTO getHocSinhByMaHS(int maHS) {
        // SỬA LỖI: Gọi hàm getByMaHS (bên dưới) để truy vấn DAO trực tiếp
        // thay vì lặp qua hàm getAllHocSinh() đã bị hỏng.
        return getByMaHS(maHS);
    }

    /**
     * Tìm học sinh theo tài khoản đăng nhập (MaND)
     * Dùng khi học sinh đăng nhập để xem thời khóa biểu
     */
    public HocSinhDTO getByMaND(int maND) {
        return dao.findByMaND(maND);
    }

    // =================== Tiện ích nội bộ ===================
    private int parseInt(Object o) {
        if (o == null) return 0;
        return (o instanceof Integer) ? (Integer) o : Integer.parseInt(o.toString());
    }

    private String str(Object o) {
        return (o == null) ? "" : o.toString();
    }
    
    // Hàm này gọi DAO đúng, được giữ lại
    public HocSinhDTO getByMaHS(int maHS) {
        return dao.findByMaHS(maHS);
    }
}