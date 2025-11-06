package com.sgu.qlhs.database;

import com.sgu.qlhs.DatabaseConnection;
import com.sgu.qlhs.dto.ThoiKhoaBieuDTO;
import java.sql.*;
import java.util.*;

public class ThoiKhoaBieuDAO {

    // ===== Lấy tất cả TKB có JOIN =====
    public List<ThoiKhoaBieuDTO> getAll() {
        List<ThoiKhoaBieuDTO> list = new ArrayList<>();
        String sql = """
        	    SELECT tkb.*, pcd.MaGV, pcd.MaLop, pcd.MaPhong, pcd.HocKy, pcd.NamHoc,
        	           gv.HoTen AS TenGV, mon.TenMon, lop.TenLop, phong.TenPhong
        	    FROM ThoiKhoaBieu tkb
        	    JOIN PhanCongDay pcd ON tkb.MaPCD = pcd.MaPCD
        	    JOIN GiaoVien gv ON gv.MaGV = pcd.MaGV
        	    JOIN MonHoc mon ON mon.MaMon = pcd.MaMon
        	    JOIN Lop lop ON lop.MaLop = pcd.MaLop
        	    JOIN PhongHoc phong ON phong.MaPhong = pcd.MaPhong
        	    WHERE tkb.TrangThai = 1 AND pcd.TrangThai = 1
        	    ORDER BY tkb.Thu, tkb.TietBD
        	""";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                ThoiKhoaBieuDTO t = new ThoiKhoaBieuDTO();
                t.setMaTKB(rs.getInt("MaTKB"));
                t.setMaPCD(rs.getInt("MaPCD"));
                t.setThu(rs.getString("Thu"));
                t.setTietBD(rs.getInt("TietBD"));
                t.setTietKT(rs.getInt("TietKT"));
                t.setTrangThai(rs.getInt("TrangThai"));
                t.setNgayTao(rs.getTimestamp("NgayTao"));
                t.setNgayCapNhat(rs.getTimestamp("NgayCapNhat"));
                t.setMaGV(rs.getInt("MaGV"));
                t.setMaLop(rs.getInt("MaLop"));
                t.setMaPhong(rs.getInt("MaPhong"));
                t.setHocKy(rs.getString("HocKy"));
                t.setNamHoc(rs.getString("NamHoc"));
                t.setTenGV(rs.getString("TenGV"));
                t.setTenMon(rs.getString("TenMon"));
                t.setTenLop(rs.getString("TenLop"));
                t.setTenPhong(rs.getString("TenPhong"));
                list.add(t);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ===== Thêm / Sửa / Xóa =====
    public boolean insert(ThoiKhoaBieuDTO dto) {
        String sql = """
            INSERT INTO ThoiKhoaBieu (MaPCD, Thu, TietBD, TietKT, TrangThai, NgayTao, NgayCapNhat)
            VALUES (?, ?, ?, ?, 1, NOW(), NOW())
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, dto.getMaPCD());
            ps.setString(2, dto.getThu());
            ps.setInt(3, dto.getTietBD());
            ps.setInt(4, dto.getTietKT());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean update(ThoiKhoaBieuDTO dto) {
        String sql = """
            UPDATE ThoiKhoaBieu
            SET MaPCD=?, Thu=?, TietBD=?, TietKT=?, TrangThai=?, NgayCapNhat=NOW()
            WHERE MaTKB=?
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, dto.getMaPCD());
            ps.setString(2, dto.getThu());
            ps.setInt(3, dto.getTietBD());
            ps.setInt(4, dto.getTietKT());
            ps.setInt(5, dto.getTrangThai());
            ps.setInt(6, dto.getMaTKB());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(int maTKB) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM ThoiKhoaBieu WHERE MaTKB=?")) {
            ps.setInt(1, maTKB);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean checkConflict(String sql, Object... params) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) ps.setObject(i + 1, params[i]);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ===== Kiểm tra trùng GV / Lớp / Phòng =====
    public boolean isConflict_GiaoVien(int maGV, String thu, int tietBD, int tietKT, String hocKy, String namHoc) {
        String sql = """
            SELECT COUNT(*) 
            FROM ThoiKhoaBieu tkb
            JOIN PhanCongDay pcd ON tkb.MaPCD = pcd.MaPCD
            WHERE pcd.MaGV = ? AND pcd.HocKy = ? AND pcd.NamHoc = ? AND tkb.Thu = ?
              AND ((tkb.TietBD BETWEEN ? AND ?) OR (tkb.TietKT BETWEEN ? AND ?) 
                   OR (? BETWEEN tkb.TietBD AND tkb.TietKT))
        """;
        return checkConflict(sql, maGV, hocKy, namHoc, thu, tietBD, tietKT, tietBD, tietKT, tietBD);
    }

    public boolean isConflict_Lop(int maLop, String thu, int tietBD, int tietKT, String hocKy, String namHoc) {
        String sql = """
            SELECT COUNT(*) 
            FROM ThoiKhoaBieu tkb
            JOIN PhanCongDay pcd ON tkb.MaPCD = pcd.MaPCD
            WHERE pcd.MaLop = ? AND pcd.HocKy = ? AND pcd.NamHoc = ? AND tkb.Thu = ?
              AND ((tkb.TietBD BETWEEN ? AND ?) OR (tkb.TietKT BETWEEN ? AND ?) 
                   OR (? BETWEEN tkb.TietBD AND tkb.TietKT))
        """;
        return checkConflict(sql, maLop, hocKy, namHoc, thu, tietBD, tietKT, tietBD, tietKT, tietBD);
    }

    public boolean isConflict_Phong(int maPhong, String thu, int tietBD, int tietKT, String hocKy, String namHoc) {
        String sql = """
            SELECT COUNT(*) 
            FROM ThoiKhoaBieu tkb
            JOIN PhanCongDay pcd ON tkb.MaPCD = pcd.MaPCD
            WHERE pcd.MaPhong = ? AND pcd.HocKy = ? AND pcd.NamHoc = ? AND tkb.Thu = ?
              AND ((tkb.TietBD BETWEEN ? AND ?) OR (tkb.TietKT BETWEEN ? AND ?) 
                   OR (? BETWEEN tkb.TietBD AND tkb.TietKT))
        """;
        return checkConflict(sql, maPhong, hocKy, namHoc, thu, tietBD, tietKT, tietBD, tietKT, tietBD);
    }

    public List<ThoiKhoaBieuDTO> getByLop(int maLop, String hocKy, String namHoc) {
        List<ThoiKhoaBieuDTO> list = new ArrayList<>();
        String sql = """
            SELECT tkb.*, pcd.MaGV, pcd.MaLop, pcd.MaPhong, pcd.HocKy, pcd.NamHoc,
                   gv.HoTen AS TenGV, mon.TenMon, lop.TenLop, phong.TenPhong
            FROM ThoiKhoaBieu tkb
            JOIN PhanCongDay pcd ON tkb.MaPCD = pcd.MaPCD
            JOIN GiaoVien gv ON gv.MaGV = pcd.MaGV
            JOIN MonHoc mon ON mon.MaMon = pcd.MaMon
            JOIN Lop lop ON lop.MaLop = pcd.MaLop
            JOIN PhongHoc phong ON phong.MaPhong = pcd.MaPhong
            WHERE pcd.MaLop = ? AND pcd.HocKy = ? AND pcd.NamHoc = ? AND tkb.TrangThai = 1 AND pcd.TrangThai = 1
            ORDER BY FIELD(tkb.Thu, 'Thứ 2','Thứ 3','Thứ 4','Thứ 5','Thứ 6','Thứ 7'), tkb.TietBD;
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maLop);
            ps.setString(2, hocKy);
            ps.setString(3, namHoc);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ThoiKhoaBieuDTO t = new ThoiKhoaBieuDTO();
                t.setMaTKB(rs.getInt("MaTKB"));
                t.setMaPCD(rs.getInt("MaPCD"));
                t.setThu(rs.getString("Thu"));
                t.setTietBD(rs.getInt("TietBD"));
                t.setTietKT(rs.getInt("TietKT"));
                t.setTrangThai(rs.getInt("TrangThai"));
                t.setTenMon(rs.getString("TenMon"));
                t.setTenGV(rs.getString("TenGV"));
                t.setTenLop(rs.getString("TenLop"));
                t.setTenPhong(rs.getString("TenPhong"));
                list.add(t);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<ThoiKhoaBieuDTO> getByGiaoVien(int maGV, String hocKy, String namHoc) {
        List<ThoiKhoaBieuDTO> list = new ArrayList<>();
        String sql = """
            SELECT tkb.*, pcd.MaGV, pcd.MaLop, pcd.MaPhong, pcd.HocKy, pcd.NamHoc,
                   gv.HoTen AS TenGV, mon.TenMon, lop.TenLop, phong.TenPhong
            FROM ThoiKhoaBieu tkb
            JOIN PhanCongDay pcd ON tkb.MaPCD = pcd.MaPCD
            JOIN GiaoVien gv ON gv.MaGV = pcd.MaGV
            JOIN MonHoc mon ON mon.MaMon = pcd.MaMon
            JOIN Lop lop ON lop.MaLop = pcd.MaLop
            JOIN PhongHoc phong ON phong.MaPhong = pcd.MaPhong
            WHERE pcd.MaGV = ? AND pcd.HocKy = ? AND pcd.NamHoc = ? AND tkb.TrangThai = 1 AND pcd.TrangThai = 1
            ORDER BY FIELD(tkb.Thu, 'Thứ 2','Thứ 3','Thứ 4','Thứ 5','Thứ 6','Thứ 7'), tkb.TietBD;
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maGV);
            ps.setString(2, hocKy);
            ps.setString(3, namHoc);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ThoiKhoaBieuDTO t = new ThoiKhoaBieuDTO();
                t.setMaTKB(rs.getInt("MaTKB"));
                t.setMaPCD(rs.getInt("MaPCD"));
                t.setThu(rs.getString("Thu"));
                t.setTietBD(rs.getInt("TietBD"));
                t.setTietKT(rs.getInt("TietKT"));
                t.setTrangThai(rs.getInt("TrangThai"));
                t.setTenMon(rs.getString("TenMon"));
                t.setTenGV(rs.getString("TenGV"));
                t.setTenLop(rs.getString("TenLop"));
                t.setTenPhong(rs.getString("TenPhong"));
                list.add(t);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

}
