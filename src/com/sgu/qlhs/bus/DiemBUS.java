package com.sgu.qlhs.bus;

import com.sgu.qlhs.dto.DiemDTO;
import com.sgu.qlhs.dto.NguoiDungDTO;
import com.sgu.qlhs.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.sgu.qlhs.database.DiemDAO;
import com.sgu.qlhs.bus.HocSinhBUS;
import com.sgu.qlhs.dto.HocSinhDTO;
import java.util.ArrayList;
import java.util.List;

public class DiemBUS {
    private DiemDAO dao;

    public DiemBUS() {
        dao = new DiemDAO();
    }

    // helper bus for enriching diem rows with student info when querying by MaHS
    private final HocSinhBUS hocSinhBUS = new HocSinhBUS();

    public List<DiemDTO> getAllDiem() {
        List<DiemDTO> list = new ArrayList<>();
        // Đọc 10 cột
        List<Object[]> rows = dao.getAllDiem();
        for (Object[] r : rows) {
            int maHS = (r[0] instanceof Integer) ? (Integer) r[0] : Integer.parseInt(r[0].toString());
            String hoTen = r[1] != null ? r[1].toString() : "";
            String tenMon = r[2] != null ? r[2].toString() : "";
            String loaiMon = r[3] != null ? r[3].toString() : ""; // Thêm
            int hocKy = (r[4] instanceof Integer) ? (Integer) r[4] : Integer.parseInt(r[4].toString()); // Sửa index
            double mieng = r[5] != null ? Double.parseDouble(r[5].toString()) : 0.0; // Sửa index
            double p15 = r[6] != null ? Double.parseDouble(r[6].toString()) : 0.0; // Sửa index
            double gk = r[7] != null ? Double.parseDouble(r[7].toString()) : 0.0; // Sửa index
            double ck = r[8] != null ? Double.parseDouble(r[8].toString()) : 0.0; // Sửa index
            String ketQuaDanhGia = r[9] != null ? r[9].toString() : null; // Thêm

            DiemDTO dto = new DiemDTO();
            dto.setMaHS(maHS);
            dto.setHoTen(hoTen);
            dto.setTenMon(tenMon);
            dto.setLoaiMon(loaiMon); // Thêm
            dto.setHocKy(hocKy);
            dto.setDiemMieng(mieng);
            dto.setDiem15p(p15);
            dto.setDiemGiuaKy(gk);
            dto.setDiemCuoiKy(ck);
            dto.setKetQuaDanhGia(ketQuaDanhGia); // Thêm
            list.add(dto);
        }
        return list;
    }

    public List<DiemDTO> getDiemByLopHocKy(int maLop, int hocKy, int maNK) {
        List<DiemDTO> list = new ArrayList<>();
        // Đọc 11 cột
        List<Object[]> rows = dao.getDiemByLopHocKy(maLop, hocKy, maNK);
        for (Object[] r : rows) {
            int maHS = (r[0] instanceof Integer) ? (Integer) r[0] : Integer.parseInt(r[0].toString());
            String hoTen = r[1] != null ? r[1].toString() : "";
            // r[2] TenLop ignored here
            int maMon = (r[3] instanceof Integer) ? (Integer) r[3] : Integer.parseInt(r[3].toString());
            String tenMon = r[4] != null ? r[4].toString() : "";
            String loaiMon = r[5] != null ? r[5].toString() : ""; // Thêm
            double mieng = r[6] != null ? Double.parseDouble(r[6].toString()) : 0.0; // Sửa index
            double p15 = r[7] != null ? Double.parseDouble(r[7].toString()) : 0.0; // Sửa index
            double gk = r[8] != null ? Double.parseDouble(r[8].toString()) : 0.0; // Sửa index
            double ck = r[9] != null ? Double.parseDouble(r[9].toString()) : 0.0; // Sửa index
            String ketQuaDanhGia = r[10] != null ? r[10].toString() : null; // Thêm

            DiemDTO dto = new DiemDTO(maHS, hoTen, maMon, tenMon, mieng, p15, gk, ck);
            dto.setLoaiMon(loaiMon);
            dto.setKetQuaDanhGia(ketQuaDanhGia);
            list.add(dto);
        }
        return list;
    }

    public List<DiemDTO> getDiemByMaHS(int maHS, int hocKy, int maNK) {
        List<DiemDTO> list = new ArrayList<>();
        // === SỬA LỖI: Đọc 11 cột ===
        List<Object[]> rows = dao.getDiemByMaHS(maHS, hocKy, maNK);
        HocSinhDTO hsInfo = null;
        try {
            hsInfo = hocSinhBUS.getHocSinhByMaHS(maHS);
        } catch (Exception ex) {
            // ignore
        }
        String hoTen = hsInfo != null ? hsInfo.getHoTen() : "";
        String tenLop = hsInfo != null ? hsInfo.getTenLop() : "";
        for (Object[] r : rows) {
            int maDiem = (r[0] instanceof Integer) ? (Integer) r[0] : Integer.parseInt(r[0].toString());
            int maMon = (r[1] instanceof Integer) ? (Integer) r[1] : Integer.parseInt(r[1].toString());
            String tenMon = r[2] != null ? r[2].toString() : "";
            String loaiMon = r[3] != null ? r[3].toString() : "";
            double mieng = r[4] != null ? Double.parseDouble(r[4].toString()) : 0.0;
            double p15 = r[5] != null ? Double.parseDouble(r[5].toString()) : 0.0;
            double gk = r[6] != null ? Double.parseDouble(r[6].toString()) : 0.0;
            double ck = r[7] != null ? Double.parseDouble(r[7].toString()) : 0.0;
            String ghiChu = r[8] != null ? r[8].toString() : "";
            String ketQuaDanhGia = r[9] != null ? r[9].toString() : null;
            double diemTB = (r[10] != null) ? Double.parseDouble(r[10].toString()) : 0.0;

            DiemDTO d = new DiemDTO();
            d.setMaDiem(maDiem);
            d.setMaHS(maHS);
            d.setHoTen(hoTen);
            d.setTenLop(tenLop);
            d.setHocKy(hocKy);
            d.setMaMon(maMon);
d.setTenMon(tenMon);
            d.setLoaiMon(loaiMon);
            d.setDiemMieng(mieng);
            d.setDiem15p(p15);
            d.setDiemGiuaKy(gk);
            d.setDiemCuoiKy(ck);
            d.setGhiChu(ghiChu);
            d.setKetQuaDanhGia(ketQuaDanhGia);
            d.setDiemTB(diemTB); // Thêm dòng này
            list.add(d);
        }
        return list;
    }

    public List<DiemDTO> getDiemByMaHS(int maHS, int hocKy, int maNK, NguoiDungDTO user) {
        if (user != null && "hoc_sinh".equalsIgnoreCase(user.getVaiTro())) {
            if (user.getId() != maHS) {
                return new ArrayList<>();
            }
        }
        return getDiemByMaHS(maHS, hocKy, maNK);
    }

    public String getNhanXet(int maHS, int maNK, int hocKy) {
        return dao.getNhanXet(maHS, maNK, hocKy);
    }

    public String getNhanXet(int maHS, int maNK, int hocKy, NguoiDungDTO user) {
        if (user != null && "hoc_sinh".equalsIgnoreCase(user.getVaiTro())) {
            if (user.getId() != maHS)
                return null;
        }
        return getNhanXet(maHS, maNK, hocKy);
    }

    public void saveNhanXet(int maHS, int maNK, int hocKy, String ghiChu) {
        dao.upsertNhanXet(maHS, maNK, hocKy, ghiChu);
    }

    public boolean saveNhanXet(int maHS, int maNK, int hocKy, String ghiChu, NguoiDungDTO user) {
        if (user != null && "giao_vien".equalsIgnoreCase(user.getVaiTro())) {
            try {

                boolean allowed = false;
                try {
                    if (isTeacherAssigned(user.getId(), maHS, null, hocKy, maNK))
                        allowed = true;
                } catch (Exception ex) {
                    // ignore inner failure and try chu nhiem fallback
                }
                if (!allowed) {
                    try {
                        com.sgu.qlhs.bus.HocSinhBUS hsBUS = new com.sgu.qlhs.bus.HocSinhBUS();
                        com.sgu.qlhs.dto.HocSinhDTO hs = hsBUS.getHocSinhByMaHS(maHS);
                        if (hs != null) {
                            com.sgu.qlhs.bus.ChuNhiemBUS cnBUS = new com.sgu.qlhs.bus.ChuNhiemBUS();
                            com.sgu.qlhs.dto.ChuNhiemDTO cn = cnBUS.getChuNhiemByGV(user.getId());
                            if (cn != null && cn.getMaLop() == hs.getMaLop())
                                allowed = true;
                        }
                    } catch (Exception ex) {
                        // ignore
                    }
                }
                if (!allowed)
                    return false;
            } catch (Exception ex) {
                System.err.println("Lỗi kiểm tra quyền trước khi lưu nhận xét: " + ex.getMessage());
                return false;
            }
        }
        try {
            dao.upsertNhanXet(maHS, maNK, hocKy, ghiChu);
            return true;
        } catch (Exception ex) {
            System.err.println("Lỗi khi lưu nhận xét: " + ex.getMessage());
            return false;
        }
    }

    // === PHẦN SỬA LỖI (NẠP CHỒNG HÀM) ===

    // HÀM CŨ (cho DiemTinhXepLoaiDialog và DiemTrungBinhTatCaMonDialog)
    public boolean saveOrUpdateDiem(int maHS, int maMon, int hocKy, int maNK,
            double mieng, double p15, double gk, double ck, NguoiDungDTO user) {
        // Gọi hàm mới, truyền null cho ketQuaDanhGia
        return this.saveOrUpdateDiem(maHS, maMon, hocKy, maNK, mieng, p15, gk, ck, null, null, user);
    }

    // HÀM MỚI (cho DiemNhapDialog)
    public boolean saveOrUpdateDiem(int maHS, int maMon, int hocKy, int maNK,
            Double mieng, Double p15, Double gk, Double ck,
            String ketQuaDanhGia, NguoiDungDTO user) {
        
        if (user != null && "giao_vien".equalsIgnoreCase(user.getVaiTro())) {
            try {
                // 1. Kiểm tra quyền cơ bản (phải được phân công)
                if (!isTeacherAssigned(user.getId(), maHS, maMon, hocKy, maNK)) {
                    System.err.println("GV (ID=" + user.getId() + ") bị từ chối lưu điểm (Không được phân công).");
                    return false; 
                }
                
                // 2. KIỂM TRA QUYỀN SỬA: GV không được SỬA điểm đã có
                boolean diemDaTonTai = dao.checkDiemExists(maHS, maMon, hocKy, maNK);
                if (diemDaTonTai) {
                    // Nếu điểm đã tồn tại, GV không được SỬA (ghi đè)
                    System.err.println("GV (ID=" + user.getId() + ") bị từ chối SỬA điểm đã tồn tại cho HS=" + maHS + ", Mon=" + maMon);
                    return false; // Cấm SỬA
                }
                // Nếu điểm chưa tồn tại (GV đang THÊM MỚI), thì tiếp tục

            } catch (Exception ex) {
                System.err.println("Lỗi kiểm tra quyền trước khi lưu điểm (ghi chú): " + ex.getMessage());
                return false;
            }
        }

        try {
            // Bỏ các dòng (mieng != null) ? mieng : 0.0;
            // Truyền thẳng mieng, p15, gk, ck (là các đối tượng Double) xuống DAO
            dao.upsertDiemWithNote(maHS, maMon, hocKy, maNK, mieng, p15, gk, ck, null, ketQuaDanhGia);
            // === KẾT THÚC SỬA ===
            return true;
        } catch (Exception ex) {
            System.err.println("Lỗi khi lưu điểm (with note): " + ex.getMessage());
            return false;
        }
    }

 // HÀM MỚI ĐẦY ĐỦ (cho BangDiemChiTietDialog)
    public boolean saveOrUpdateDiem(int maHS, int maMon, int hocKy, int maNK,
            Double mieng, Double p15, Double gk, Double ck,
            String ghiChu, String ketQuaDanhGia, NguoiDungDTO user) {
        
        if (user != null && "giao_vien".equalsIgnoreCase(user.getVaiTro())) {
            try {
                // 1. Kiểm tra quyền cơ bản (phải được phân công)
                if (!isTeacherAssigned(user.getId(), maHS, maMon, hocKy, maNK)) {
                    System.err.println("GV (ID=" + user.getId() + ") bị từ chối lưu điểm (Không được phân công).");
                    return false; 
                }
                
                // 2. KIỂM TRA QUYỀN SỬA: GV không được SỬA điểm đã có
                boolean diemDaTonTai = dao.checkDiemExists(maHS, maMon, hocKy, maNK);
                if (diemDaTonTai) {
                    // Nếu điểm đã tồn tại, GV không được SỬA (ghi đè)
                    System.err.println("GV (ID=" + user.getId() + ") bị từ chối SỬA điểm đã tồn tại cho HS=" + maHS + ", Mon=" + maMon);
                    return false; // Cấm SỬA
                }
                // Nếu điểm chưa tồn tại (GV đang THÊM MỚI), thì tiếp tục

            } catch (Exception ex) {
                System.err.println("Lỗi kiểm tra quyền trước khi lưu điểm (ghi chú): " + ex.getMessage());
                return false;
            }
        }
        
        // Logic lưu (dành cho Admin hoặc GV thêm mới)
        try {
            // Truyền thẳng mieng, p15, gk, ck (là các đối tượng Double) xuống DAO
            dao.upsertDiemWithNote(maHS, maMon, hocKy, maNK, mieng, p15, gk, ck, ghiChu, ketQuaDanhGia);
            return true;
        } catch (Exception ex) {
            System.err.println("Lỗi khi lưu điểm (with note): " + ex.getMessage());
            return false;
        }
    }

    public void deleteDiem(int maHS, int maMon, int hocKy, int maNK) {
        dao.deleteDiem(maHS, maMon, hocKy, maNK);
    }

    public boolean deleteDiem(int maHS, int maMon, int hocKy, int maNK, NguoiDungDTO user) {
        // CHỈ ADMIN MỚI ĐƯỢC XÓA
        if (user == null || !("quan_tri_vien".equalsIgnoreCase(user.getVaiTro()) || "admin".equalsIgnoreCase(user.getVaiTro()))) {
             System.err.println("Bị từ chối XÓA điểm (Không phải Admin).");
             return false; // Không phải Admin, cấm xóa
        }
        
        // Logic cho Admin
        try {
            dao.deleteDiem(maHS, maMon, hocKy, maNK);
            return true;
        } catch (Exception ex) {
            System.err.println("Lỗi khi xóa điểm: " + ex.getMessage());
            return false;
        }
    }
    
    private boolean isTeacherAssigned(int maGV, int maHS, Integer maMon, int hocKyInt, int maNK) throws SQLException {

        // 1. Chuyển đổi int HocKy (1) -> String HocKy ("HK1")
        String hocKyString = "HK" + hocKyInt;

        // 2. Lấy NamHoc string (ví dụ "2024-2025")
        NienKhoaBUS nkBUS = new NienKhoaBUS();
        String namHocString = null;
        try {
            namHocString = nkBUS.getNamHocString(maNK);
        } catch (Exception ex) {
            namHocString = null;
        }
        
        // Nếu không tìm thấy năm học string, không thể kiểm tra quyền
        if (namHocString == null) {
            System.err.println("[DBG] isTeacherAssigned: Không tìm thấy NamHoc String cho MaNK=" + maNK);
            return false;
        }

        // 3. Xây dựng câu truy vấn (Chiến lược B - Dùng String)
        // Bảng PhanCongDay dùng NamHoc (VARCHAR) và HocKy (VARCHAR)
        String sqlB = "SELECT COUNT(*) AS cnt FROM PhanCongDay pc JOIN HocSinh hs ON hs.MaLop = pc.MaLop "
                + "WHERE pc.MaGV = ? AND pc.NamHoc = ? AND pc.HocKy = ? AND hs.MaHS = ?";
        
        if (maMon != null) {
            sqlB += " AND pc.MaMon = ?";
        }

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sqlB)) {
            int idx = 1;
            ps.setInt(idx++, maGV);
            ps.setString(idx++, namHocString);
            ps.setString(idx++, hocKyString);
            ps.setInt(idx++, maHS);
            if (maMon != null) {
                ps.setInt(idx++, maMon);
            }
            
            // Debugging (có thể xóa sau)
            // System.out.println("[DBG] isTeacherAssigned: maGV=" + maGV + ", NamHoc='" + namHocString + "', hocKy='" + hocKyString + "', maHS=" + maHS + (maMon != null ? ", maMon=" + maMon : ""));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int cnt = rs.getInt("cnt");
                    // if (cnt > 0) System.out.println("[DBG] => Quyền được cấp (Count=" + cnt + ")");
                    return (cnt > 0);
                }
            }
        } catch (SQLException e) {
            System.err.println("[DBG] Strategy B (String) threw SQLException: " + e.getMessage());
            throw e; // Ném lỗi ra ngoài để transaction (nếu có) rollback
        }

        // System.out.println("[DBG] => Không có quyền");
        return false;
    }
    // =================================================================
    // === KẾT THÚC SỬA LỖI KIỂM TRA QUYỀN ===
    // =================================================================


    public List<DiemDTO> getDiemFiltered(Integer maLop, Integer maMon, Integer hocKy, Integer maNK,
            Integer limit, Integer offset) {
        return dao.getDiemFiltered(maLop, maMon, hocKy, maNK, limit, offset);
    }

    public List<DiemDTO> getDiemFilteredByMaLopList(java.util.List<Integer> maLops, Integer maMon, Integer hocKy,
            Integer maNK, Integer limit, Integer offset) {
        return dao.getDiemFilteredByMaLopList(maLops, maMon, hocKy, maNK, limit, offset);
    }

    public List<DiemDTO> getDiemFilteredForUser(Integer maLop, Integer maMon, Integer hocKy, Integer maNK,
            Integer limit, Integer offset, NguoiDungDTO user) {
        List<DiemDTO> all = getDiemFiltered(maLop, maMon, hocKy, maNK, limit, offset);
        if (user == null || !"giao_vien".equalsIgnoreCase(user.getVaiTro()))
            return all;
        java.util.List<DiemDTO> filtered = new java.util.ArrayList<>();
        for (DiemDTO d : all) {
            try {
                if (isTeacherAssignedPublic(user.getId(), d.getMaHS(), d.getMaMon(), d.getHocKy(), maNK)) {
                    filtered.add(d);
                }
            } catch (Exception ex) {
                // on error, be conservative and skip the row
            }
        }
        return filtered;
    }

    /**
     * Version that accepts a list of MaLop and returns rows then filters them
     * according to the provided user (teacher semantics). If user is null or not
     * a teacher the raw results are returned.
     */
    public List<DiemDTO> getDiemFilteredForUserByMaLopList(java.util.List<Integer> maLops, Integer maMon,
            Integer hocKy, Integer maNK, Integer limit, Integer offset, NguoiDungDTO user) {
        List<DiemDTO> all = getDiemFilteredByMaLopList(maLops, maMon, hocKy, maNK, limit, offset);
        if (user == null || !"giao_vien".equalsIgnoreCase(user.getVaiTro()))
            return all;
        java.util.List<DiemDTO> filtered = new java.util.ArrayList<>();
        for (DiemDTO d : all) {
            try {
                if (isTeacherAssignedPublic(user.getId(), d.getMaHS(), d.getMaMon(), d.getHocKy(), maNK)) {
                    filtered.add(d);
                }
            } catch (Exception ex) {
                // on error, be conservative and skip the row
            }
        }
        return filtered;
    }

    public boolean isTeacherAssignedPublic(int maGV, int maHS, Integer maMon, int hocKy, int maNK) {
        try {
            return isTeacherAssigned(maGV, maHS, maMon, hocKy, maNK);
        } catch (SQLException ex) {
            System.err.println("Lỗi khi kiểm tra phân công: " + ex.getMessage());
            return false;
        }
    }
}