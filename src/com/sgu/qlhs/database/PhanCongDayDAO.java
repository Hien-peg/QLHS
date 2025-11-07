package com.sgu.qlhs.database;

import com.sgu.qlhs.DatabaseConnection;
import com.sgu.qlhs.dto.PhanCongDayDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PhanCongDayDAO {

    public List<Integer> getDistinctMaLopByGiaoVien(int maGV, String namHoc, String hocKy) throws Exception {
        String sql = "SELECT DISTINCT pc.MaLop FROM PhanCongDay pc WHERE pc.MaGV = ? AND pc.TrangThai = 1";
        if (namHoc != null && !namHoc.isEmpty())
            sql += " AND pc.NamHoc = ?";
        if (hocKy != null && !hocKy.isEmpty())
            sql += " AND pc.HocKy = ?";
        sql += " ORDER BY pc.MaLop";

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            int idx = 1;
            ps.setInt(idx++, maGV);
            if (namHoc != null && !namHoc.isEmpty())
                ps.setString(idx++, namHoc);
            if (hocKy != null && !hocKy.isEmpty())
                ps.setString(idx++, hocKy);

            try (ResultSet rs = ps.executeQuery()) {
                List<Integer> out = new ArrayList<>();
                while (rs.next()) {
                    out.add(rs.getInt(1));
                }
                return out;
            }
        }
    }

    public List<Integer> getDistinctMaMonByGiaoVien(int maGV, String namHoc, String hocKy) throws Exception {
        String sql = "SELECT DISTINCT pc.MaMon FROM PhanCongDay pc WHERE pc.MaGV = ? AND pc.TrangThai = 1";
        if (namHoc != null && !namHoc.isEmpty())
            sql += " AND pc.NamHoc = ?";
        if (hocKy != null && !hocKy.isEmpty())
            sql += " AND pc.HocKy = ?";
        sql += " ORDER BY pc.MaMon";

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            int idx = 1;
            ps.setInt(idx++, maGV);
            if (namHoc != null && !namHoc.isEmpty())
                ps.setString(idx++, namHoc);
            if (hocKy != null && !hocKy.isEmpty())
                ps.setString(idx++, hocKy);

            try (ResultSet rs = ps.executeQuery()) {
                List<Integer> out = new ArrayList<>();
                while (rs.next()) {
                    out.add(rs.getInt(1));
                }
                return out;
            }
        }
    }

    public List<PhanCongDayDTO> getAll() {
        List<PhanCongDayDTO> list = new ArrayList<>();
        String sql = """
                    SELECT pcd.*, gv.HoTen AS TenGV, mon.TenMon, lop.TenLop, phong.TenPhong
                    FROM PhanCongDay pcd
                    JOIN GiaoVien gv ON gv.MaGV = pcd.MaGV
                    JOIN MonHoc mon ON mon.MaMon = pcd.MaMon
                    JOIN Lop lop ON lop.MaLop = pcd.MaLop
                    JOIN PhongHoc phong ON phong.MaPhong = pcd.MaPhong
                    WHERE pcd.TrangThai = 1
                    ORDER BY pcd.MaPCD ASC
                """;

        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                PhanCongDayDTO dto = new PhanCongDayDTO();
                dto.setMaPCD(rs.getInt("MaPCD"));
                dto.setMaGV(rs.getInt("MaGV"));
                dto.setMaMon(rs.getInt("MaMon"));
                dto.setMaLop(rs.getInt("MaLop"));
                dto.setMaPhong(rs.getInt("MaPhong"));
                dto.setHocKy(rs.getString("HocKy"));
                dto.setNamHoc(rs.getString("NamHoc"));
                dto.setTrangThai(rs.getInt("TrangThai"));
                dto.setNgayTao(rs.getTimestamp("NgayTao"));
                dto.setNgayCapNhat(rs.getTimestamp("NgayCapNhat"));

                dto.setTenGV(rs.getString("TenGV"));
                dto.setTenMon(rs.getString("TenMon"));
                dto.setTenLop(rs.getString("TenLop"));
                dto.setTenPhong(rs.getString("TenPhong"));

                list.add(dto);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ===== Thêm mới =====
    public boolean insert(PhanCongDayDTO dto) {
        String sql = """
                    INSERT INTO PhanCongDay (MaGV, MaLop, MaMon, MaPhong, HocKy, NamHoc, TrangThai, NgayTao, NgayCapNhat)
                    VALUES (?, ?, ?, ?, ?, ?, 1, NOW(), NOW())
                """;
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, dto.getMaGV());
            ps.setInt(2, dto.getMaLop());
            ps.setInt(3, dto.getMaMon());
            ps.setInt(4, dto.getMaPhong());
            ps.setString(5, dto.getHocKy());
            ps.setString(6, dto.getNamHoc());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ===== Cập nhật =====
    public boolean update(PhanCongDayDTO dto) {
        String sql = """
                    UPDATE PhanCongDay
                    SET MaGV=?, MaLop=?, MaMon=?, MaPhong=?, HocKy=?, NamHoc=?, TrangThai=?, NgayCapNhat=NOW()
                    WHERE MaPCD=?
                """;
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, dto.getMaGV());
            ps.setInt(2, dto.getMaLop());
            ps.setInt(3, dto.getMaMon());
            ps.setInt(4, dto.getMaPhong());
            ps.setString(5, dto.getHocKy());
            ps.setString(6, dto.getNamHoc());
            ps.setInt(7, dto.getTrangThai() == 0 ? 1 : dto.getTrangThai());
            ps.setInt(8, dto.getMaPCD());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ===== Xóa (Delete) =====
    public boolean delete(int maPCD) {
        String sql = "UPDATE PhanCongDay SET TrangThai = 0 WHERE MaPCD = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maPCD);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public PhanCongDayDTO findById(int maPCD) {
        String sql = """
                    SELECT pcd.*, gv.HoTen AS TenGV, mon.TenMon, lop.TenLop, phong.TenPhong
                    FROM PhanCongDay pcd
                    JOIN GiaoVien gv ON gv.MaGV = pcd.MaGV
                    JOIN MonHoc mon ON mon.MaMon = pcd.MaMon
                    JOIN Lop lop ON lop.MaLop = pcd.MaLop
                    JOIN PhongHoc phong ON phong.MaPhong = pcd.MaPhong
                    WHERE pcd.MaPCD = ? AND pcd.TrangThai = 1
                """;
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maPCD);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                PhanCongDayDTO dto = new PhanCongDayDTO();
                dto.setMaPCD(rs.getInt("MaPCD"));
                dto.setMaGV(rs.getInt("MaGV"));
                dto.setMaMon(rs.getInt("MaMon"));
                dto.setMaLop(rs.getInt("MaLop"));
                dto.setMaPhong(rs.getInt("MaPhong"));
                dto.setHocKy(rs.getString("HocKy"));
                dto.setNamHoc(rs.getString("NamHoc"));
                dto.setTrangThai(rs.getInt("TrangThai"));
                dto.setTenGV(rs.getString("TenGV"));
                dto.setTenMon(rs.getString("TenMon"));
                dto.setTenLop(rs.getString("TenLop"));
                dto.setTenPhong(rs.getString("TenPhong"));
                return dto;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ===== Kiểm tra trùng =====
    public boolean existsDuplicate(int maGV, int maLop, int maMon, String hocKy, String namHoc) {
        String sql = """
                    SELECT COUNT(*) FROM PhanCongDay
                    WHERE MaGV = ? AND MaLop = ? AND MaMon = ? AND HocKy = ? AND NamHoc = ? AND TrangThai = 1
                """;
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maGV);
            ps.setInt(2, maLop);
            ps.setInt(3, maMon);
            ps.setString(4, hocKy);
            ps.setString(5, namHoc);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return rs.getInt(1) > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}