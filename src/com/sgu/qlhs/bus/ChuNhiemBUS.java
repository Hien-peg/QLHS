package com.sgu.qlhs.bus;

import com.sgu.qlhs.database.ChuNhiemDAO;
import com.sgu.qlhs.dto.ChuNhiemDTO;
import java.util.List;

public class ChuNhiemBUS {
    private final ChuNhiemDAO dao = new ChuNhiemDAO();

    public List<ChuNhiemDTO> getAll() {
        return dao.getAll();
    }

    public String insert(ChuNhiemDTO dto) {
        // Kiểm tra trùng giáo viên
        if (dao.existsSameGiaoVien(dto.getMaGV(), null))
            return "❌ Giáo viên này đã làm chủ nhiệm lớp khác!";
        // Kiểm tra trùng lớp
        if (dao.existsSameLop(dto.getMaLop(), null))
            return "❌ Lớp này đã có giáo viên chủ nhiệm!";
        return dao.insert(dto) ? "✅ Thêm thành công!" : "❌ Thêm thất bại!";
    }

    public String update(ChuNhiemDTO dto) {
        if (dao.existsSameGiaoVien(dto.getMaGV(), dto.getMaCN()))
            return "❌ Giáo viên này đã làm chủ nhiệm lớp khác!";
        if (dao.existsSameLop(dto.getMaLop(), dto.getMaCN()))
            return "❌ Lớp này đã có giáo viên chủ nhiệm!";
        return dao.update(dto) ? "✅ Cập nhật thành công!" : "❌ Cập nhật thất bại!";
    }

    public boolean delete(int maCN) {
        return dao.delete(maCN);
    }

    public ChuNhiemDTO getChuNhiemByGV(int maGV) {
        return dao.getChuNhiemByGV(maGV);
    }
}
