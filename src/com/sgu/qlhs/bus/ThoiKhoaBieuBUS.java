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
}
