package com.sgu.qlhs.bus;

import com.sgu.qlhs.database.ThoiKhoaBieuDAO;
import com.sgu.qlhs.dto.ThoiKhoaBieuDTO;
import java.util.List;

public class ThoiKhoaBieuBUS {
    private final ThoiKhoaBieuDAO dao;

    public ThoiKhoaBieuBUS() {
        dao = new ThoiKhoaBieuDAO();
    }

    public List<ThoiKhoaBieuDTO> getAll() {
        return dao.getAll();
    }

    public boolean delete(int maTKB) {
        return dao.delete(maTKB);
    }

    public String addTKB(ThoiKhoaBieuDTO tkb) {
        if (dao.isConflict_Lop(tkb.getMaLop(), tkb.getThu(), tkb.getTietBD(), tkb.getTietKT(), tkb.getHocKy(), tkb.getNamHoc())) {
            return "❌ Trùng tiết trong cùng lớp học!";
        }
        if (dao.isConflict_GiaoVien(tkb.getMaGV(), tkb.getThu(), tkb.getTietBD(), tkb.getTietKT(), tkb.getHocKy(), tkb.getNamHoc())) {
            return "❌ Giáo viên đã có lịch dạy trùng tiết!";
        }
        if (dao.isConflict_Phong(tkb.getMaPhong(), tkb.getThu(), tkb.getTietBD(), tkb.getTietKT(), tkb.getHocKy(), tkb.getNamHoc())) {
            return "❌ Phòng học này đã có lớp khác học cùng thời gian!";
        }

        boolean ok = dao.insert(tkb);
        return ok ? "✅ Thêm thời khóa biểu thành công!" : "❌ Lỗi khi thêm thời khóa biểu!";
    }

    public String updateTKB(ThoiKhoaBieuDTO tkb) {
        if (dao.isConflict_Lop(tkb.getMaLop(), tkb.getThu(), tkb.getTietBD(), tkb.getTietKT(), tkb.getHocKy(), tkb.getNamHoc())) {
            return "❌ Trùng tiết trong cùng lớp học!";
        }
        if (dao.isConflict_GiaoVien(tkb.getMaGV(), tkb.getThu(), tkb.getTietBD(), tkb.getTietKT(), tkb.getHocKy(), tkb.getNamHoc())) {
            return "❌ Giáo viên đã có lịch dạy trùng tiết!";
        }
        if (dao.isConflict_Phong(tkb.getMaPhong(), tkb.getThu(), tkb.getTietBD(), tkb.getTietKT(), tkb.getHocKy(), tkb.getNamHoc())) {
            return "❌ Phòng học này đã có lớp khác học cùng thời gian!";
        }

        boolean ok = dao.update(tkb);
        return ok ? "✅ Cập nhật thời khóa biểu thành công!" : "❌ Lỗi khi cập nhật!";
    }
    /**
     * Lấy thời khóa biểu theo lớp (cho Học sinh / GVCN)
     */
    public List<ThoiKhoaBieuDTO> getByLop(int maLop, String hocKy, String namHoc) {
        return dao.getByLop(maLop, hocKy, namHoc);
    }

    /**
     * Lấy thời khóa biểu theo giáo viên (cho GV bộ môn / GVCN)
     */
    public List<ThoiKhoaBieuDTO> getByGiaoVien(int maGV, String hocKy, String namHoc) {
        return dao.getByGiaoVien(maGV, hocKy, namHoc);
    }

    /**
     * Lấy TKB theo vai trò đăng nhập
     * @param role Vai trò ("Admin", "HocSinh", "GiaoVien", "ChuNhiem")
     * @param maLop Mã lớp (HS hoặc GVCN)
     * @param maGV Mã giáo viên (GV hoặc GVCN)
     * @param hocKy Học kỳ hiện tại
     * @param namHoc Năm học hiện tại
     */
    public List<ThoiKhoaBieuDTO> getByRole(String role, Integer maLop, Integer maGV, String hocKy, String namHoc) {
        switch (role) {
            case "Admin":
                // ✅ Nếu có chọn lớp thì lọc theo lớp, ngược lại hiển thị tất cả
                if (maLop != null)
                    return getByLop(maLop, hocKy, namHoc);
                else
                    return getAll();
            case "HocSinh":
                if (maLop != null)
                    return getByLop(maLop, hocKy, namHoc);
                break;
            case "GiaoVien":
                if (maGV != null)
                    return getByGiaoVien(maGV, hocKy, namHoc);
                break;
            case "ChuNhiem":
                if (maLop != null)
                    return getByLop(maLop, hocKy, namHoc);
                else if (maGV != null)
                    return getByGiaoVien(maGV, hocKy, namHoc);
                break;
        }
        return List.of();
    }

}
