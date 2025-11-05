package com.sgu.qlhs.database;

import com.sgu.qlhs.DatabaseConnection;
import com.sgu.qlhs.dto.ChuNhiemDTO;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ChuNhiemDAO {
    public List<ChuNhiemDTO> getAll() {
        List<ChuNhiemDTO> list = new ArrayList<>();
        String sql = """
            SELECT cn.*, gv.HoTen AS TenGV, lop.TenLop
            FROM ChuNhiem cn
            JOIN GiaoVien gv ON gv.MaGV = cn.MaGV
            JOIN Lop lop ON lop.MaLop = cn.MaLop
            WHERE cn.TrangThai = 1
            ORDER BY cn.MaCN DESC
        """;
        try (Connection c = DatabaseConnection.getConnection();
             Statement s = c.createStatement();
             ResultSet rs = s.executeQuery(sql)) {

            while (rs.next()) {
                ChuNhiemDTO dto = new ChuNhiemDTO();
                dto.setMaCN(rs.getInt("MaCN"));
                dto.setMaGV(rs.getInt("MaGV"));
                dto.setMaLop(rs.getInt("MaLop"));
                dto.setTenGV(rs.getString("TenGV"));
                dto.setTenLop(rs.getString("TenLop"));
                dto.setNgayNhanNhiem(rs.getDate("NgayNhanNhiem"));
                dto.setNgayKetThuc(rs.getDate("NgayKetThuc"));
                list.add(dto);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // === Thêm mới ===
    public boolean insert(ChuNhiemDTO dto) {
        String sql = "INSERT INTO ChuNhiem (MaGV, MaLop, NgayNhanNhiem, NgayKetThuc) VALUES (?,?,?,?)";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, dto.getMaGV());
            ps.setInt(2, dto.getMaLop());
            ps.setDate(3, dto.getNgayNhanNhiem());
            ps.setDate(4, dto.getNgayKetThuc());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // === Sửa ===
    public boolean update(ChuNhiemDTO dto) {
        String sql = "UPDATE ChuNhiem SET MaGV=?, MaLop=?, NgayNhanNhiem=?, NgayKetThuc=? WHERE MaCN=?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, dto.getMaGV());
            ps.setInt(2, dto.getMaLop());
            ps.setDate(3, dto.getNgayNhanNhiem());
            ps.setDate(4, dto.getNgayKetThuc());
            ps.setInt(5, dto.getMaCN());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(int maCN) {
        String sql = "UPDATE ChuNhiem SET TrangThai = 0 WHERE MaCN = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, maCN);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // === Kiểm tra trùng giáo viên ===
    public boolean existsSameGiaoVien(int maGV, Integer maCN) {
        String sql = "SELECT COUNT(*) FROM ChuNhiem WHERE MaGV = ?" +
                     (maCN != null ? " AND MaCN <> ?" : "");
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, maGV);
            if (maCN != null) ps.setInt(2, maCN);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // === Kiểm tra trùng lớp ===
    public boolean existsSameLop(int maLop, Integer maCN) {
        String sql = "SELECT COUNT(*) FROM ChuNhiem WHERE MaLop = ?" +
                     (maCN != null ? " AND MaCN <> ?" : "");
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, maLop);
            if (maCN != null) ps.setInt(2, maCN);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    // ===== Kiểm tra GVCN theo MaGV (dùng khi đăng nhập) =====
    public ChuNhiemDTO getChuNhiemByGV(int maGV) {
        String sql = """
            SELECT cn.*, gv.HoTen AS TenGV, lop.TenLop
            FROM ChuNhiem cn
            JOIN GiaoVien gv ON gv.MaGV = cn.MaGV
            JOIN Lop lop ON lop.MaLop = cn.MaLop
            WHERE cn.MaGV = ? AND cn.TrangThai = 1
            ORDER BY cn.NgayNhanNhiem DESC
            LIMIT 1
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maGV);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                ChuNhiemDTO cn = new ChuNhiemDTO();
                cn.setMaCN(rs.getInt("MaCN"));
                cn.setMaGV(rs.getInt("MaGV"));
                cn.setMaLop(rs.getInt("MaLop"));
                cn.setNgayNhanNhiem(rs.getDate("NgayNhanNhiem"));
                cn.setNgayKetThuc(rs.getDate("NgayKetThuc"));
                cn.setTenGV(rs.getString("TenGV"));
                cn.setTenLop(rs.getString("TenLop"));
                return cn;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
