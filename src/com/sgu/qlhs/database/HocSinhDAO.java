package com.sgu.qlhs.database;

import com.sgu.qlhs.DatabaseConnection;
import com.sgu.qlhs.dto.HocSinhDTO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HocSinhDAO {

    // Lấy tất cả học sinh kèm tên lớp
    public List<Object[]> getAllHocSinh() {
        List<Object[]> data = new ArrayList<>();
        String sql = """
                    SELECT hs.MaHS, hs.HoTen, hs.NgaySinh, hs.GioiTinh, l.TenLop
                    FROM HocSinh hs
                    JOIN Lop l ON hs.MaLop = l.MaLop
                """;

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            if (conn == null)
                throw new SQLException("Không thể kết nối CSDL!");

            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                data.add(new Object[] {
                        rs.getInt("MaHS"),
                        rs.getString("HoTen"),
                        rs.getDate("NgaySinh") != null ? rs.getDate("NgaySinh").toString() : "",
                        rs.getString("GioiTinh"),
                        rs.getString("TenLop")
                });
            }
        } catch (SQLException e) {
            System.err.println("❌ Lỗi khi truy vấn danh sách học sinh: " + e.getMessage());
        } finally {
            closeQuietly(rs, ps, conn);
        }
        return data;
    }

    // Lấy học sinh theo mã lớp
    public List<Object[]> getHocSinhByMaLop(int maLop) {
        List<Object[]> data = new ArrayList<>();
        String sql = "SELECT MaHS, HoTen, GioiTinh, NgaySinh FROM HocSinh WHERE MaLop = ?";

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            if (conn == null)
                throw new SQLException("Không thể kết nối CSDL!");

            ps = conn.prepareStatement(sql);
            ps.setInt(1, maLop);
            rs = ps.executeQuery();

            while (rs.next()) {
                data.add(new Object[] {
                        rs.getInt("MaHS"),
                        rs.getString("HoTen"),
                        rs.getString("GioiTinh"),
                        rs.getDate("NgaySinh") != null ? rs.getDate("NgaySinh").toString() : ""
                });
            }
        } catch (SQLException e) {
            System.err.println("❌ Lỗi khi truy vấn học sinh theo lớp: " + e.getMessage());
        } finally {
            closeQuietly(rs, ps, conn);
        }
        return data;
    }

    // Lấy học sinh theo mã HS
    public Object[] getHocSinhById(int maHS) {
        String sql = """
                    SELECT hs.MaHS, hs.HoTen, hs.NgaySinh, hs.GioiTinh,
                           hs.DiaChi, hs.SoDienThoai, hs.Email, l.TenLop
                    FROM HocSinh hs
                    JOIN Lop l ON hs.MaLop = l.MaLop
                    WHERE hs.MaHS = ?
                """;

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            if (conn == null)
                throw new SQLException("Không thể kết nối CSDL!");

            ps = conn.prepareStatement(sql);
            ps.setInt(1, maHS);
            rs = ps.executeQuery();

            if (rs.next()) {
                return new Object[] {
                        rs.getInt("MaHS"),
                        rs.getString("HoTen"),
                        rs.getDate("NgaySinh"),
                        rs.getString("GioiTinh"),
                        rs.getString("DiaChi"),
                        rs.getString("SoDienThoai"),
                        rs.getString("Email"),
                        rs.getString("TenLop")
                };
            }

        } catch (SQLException e) {
            System.err.println("❌ Lỗi khi lấy học sinh theo mã: " + e.getMessage());
        } finally {
            closeQuietly(rs, ps, conn);
        }
        return null;
    }

    // Thêm học sinh mới
    public boolean addHocSinh(String hoTen, java.util.Date ngaySinh, String gioiTinh,
            String diaChi, String soDienThoai, String email, int maLop) {
        String sql = """
                    INSERT INTO HocSinh (HoTen, NgaySinh, GioiTinh, DiaChi, SoDienThoai, Email, MaLop)
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = DatabaseConnection.getConnection();
            if (conn == null)
                throw new SQLException("Không thể kết nối CSDL!");

            ps = conn.prepareStatement(sql);
            ps.setString(1, hoTen);
            ps.setDate(2, new java.sql.Date(ngaySinh.getTime()));
            ps.setString(3, gioiTinh);
            ps.setString(4, diaChi);
            ps.setString(5, soDienThoai);
            ps.setString(6, email);
            ps.setInt(7, maLop);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("❌ Lỗi khi thêm học sinh: " + e.getMessage());
        } finally {
            closeQuietly(null, ps, conn);
        }
        return false;
    }

    // Cập nhật học sinh
    public boolean updateHocSinh(int maHS, String hoTen, java.util.Date ngaySinh, String gioiTinh,
            String diaChi, String soDienThoai, String email, int maLop) {
        String sql = """
                    UPDATE HocSinh
                    SET HoTen=?, NgaySinh=?, GioiTinh=?, DiaChi=?, SoDienThoai=?, Email=?, MaLop=?
                    WHERE MaHS=?
                """;

        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = DatabaseConnection.getConnection();
            if (conn == null)
                throw new SQLException("Không thể kết nối CSDL!");

            ps = conn.prepareStatement(sql);
            ps.setString(1, hoTen);
            ps.setDate(2, new java.sql.Date(ngaySinh.getTime()));
            ps.setString(3, gioiTinh);
            ps.setString(4, diaChi);
            ps.setString(5, soDienThoai);
            ps.setString(6, email);
            ps.setInt(7, maLop);
            ps.setInt(8, maHS);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("❌ Lỗi khi cập nhật học sinh: " + e.getMessage());
        } finally {
            closeQuietly(null, ps, conn);
        }
        return false;
    }

    // Xoá học sinh
    public boolean deleteHocSinh(int maHS) {
        String sql = "DELETE FROM HocSinh WHERE MaHS=?";

        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = DatabaseConnection.getConnection();
            if (conn == null)
                throw new SQLException("Không thể kết nối CSDL!");

            ps = conn.prepareStatement(sql);
            ps.setInt(1, maHS);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println(" Lỗi khi xoá học sinh: " + e.getMessage());
        } finally {
            closeQuietly(null, ps, conn);
        }
        return false;
    }

    private void closeQuietly(ResultSet rs, PreparedStatement ps, Connection conn) {
        try {
            if (rs != null)
                rs.close();
        } catch (SQLException ignored) {
        }
        try {
            if (ps != null)
                ps.close();
        } catch (SQLException ignored) {
        }
        try {
            if (conn != null)
                DatabaseConnection.closeConnection(conn);
        } catch (Exception ignored) {
        }
    }
    public HocSinhDTO findByMaND(int maND) {
        String sql = """
            SELECT hs.MaHS, hs.HoTen, hs.NgaySinh, hs.GioiTinh,
                   hs.MaLop, l.TenLop
            FROM HocSinh hs
            JOIN Lop l ON l.MaLop = hs.MaLop
            JOIN TaiKhoan_HocSinh tkhs ON tkhs.MaHS = hs.MaHS
            WHERE tkhs.MaND = ?
            LIMIT 1
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maND);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                HocSinhDTO hs = new HocSinhDTO();
                hs.setMaHS(rs.getInt("MaHS"));
                hs.setHoTen(rs.getString("HoTen"));
                hs.setNgaySinh(rs.getDate("NgaySinh"));
                hs.setGioiTinh(rs.getString("GioiTinh"));
                hs.setMaLop(rs.getInt("MaLop"));
                hs.setTenLop(rs.getString("TenLop"));
                return hs;
            }
        } catch (SQLException e) {
            System.err.println("❌ Lỗi khi lấy học sinh theo MaND: " + e.getMessage());
        }
        return null;
    }
    public HocSinhDTO findByMaHS(int maHS) {
        HocSinhDTO hs = null;
        String sql = """
            SELECT hs.MaHS, hs.HoTen, hs.MaLop, l.TenLop
            FROM HocSinh hs
            LEFT JOIN Lop l ON hs.MaLop = l.MaLop
            WHERE hs.MaHS = ?
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maHS);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                hs = new HocSinhDTO();
                hs.setMaHS(rs.getInt("MaHS"));
                hs.setHoTen(rs.getString("HoTen"));
                hs.setMaLop(rs.getInt("MaLop"));
                hs.setTenLop(rs.getString("TenLop"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return hs;
    }
}
