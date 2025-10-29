package com.sgu.qlhs.database;

import com.sgu.qlhs.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HocSinhDAO {

    public List<Object[]> getAllHocSinh() {
        List<Object[]> data = new ArrayList<>();
        String sql = "SELECT hs.MaHS, hs.HoTen, hs.NgaySinh, hs.GioiTinh, l.TenLop FROM HocSinh hs JOIN Lop l ON hs.MaLop = l.MaLop";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Object[] row = new Object[5];
                row[0] = rs.getInt("MaHS");
                row[1] = rs.getString("HoTen");
                row[2] = rs.getDate("NgaySinh").toString();
                row[3] = rs.getString("GioiTinh");
                row[4] = rs.getString("TenLop");
                data.add(row);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi truy vấn dữ liệu học sinh: " + e.getMessage());
            e.printStackTrace();
        }
        return data;
    }

    public List<Object[]> getHocSinhByMaLop(int maLop) {
        List<Object[]> data = new ArrayList<>();
        String sql = "SELECT MaHS, HoTen, GioiTinh, NgaySinh FROM HocSinh WHERE MaLop = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, maLop);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Object[] row = new Object[4];
                    row[0] = rs.getInt("MaHS");
                    row[1] = rs.getString("HoTen");
                    row[2] = rs.getString("GioiTinh");
                    row[3] = rs.getString("NgaySinh");
                    data.add(row);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi truy vấn học sinh theo lớp: " + e.getMessage());
            e.printStackTrace();
        }
        return data;
    }

    public void insertHocSinh(String hoTen, String ngaySinh, String gioiTinh, int maLop, String sdt, String email) {
        String sql = "INSERT INTO HocSinh (HoTen, NgaySinh, GioiTinh, MaLop, SoDienThoai, Email) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, hoTen);
            pstmt.setString(2, ngaySinh);
            pstmt.setString(3, gioiTinh);
            pstmt.setInt(4, maLop);
            pstmt.setString(5, sdt);
            pstmt.setString(6, email);
            pstmt.executeUpdate();
            System.out.println("Thêm học sinh thành công");
        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm học sinh: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void updateHocSinh(int maHS, String hoTen, String ngaySinh, String gioiTinh, int maLop, String sdt,
            String email) {
        String sql = "UPDATE HocSinh SET HoTen = ?, NgaySinh = ?, GioiTinh = ?, MaLop = ?, SoDienThoai = ?, Email = ? WHERE MaHS = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, hoTen);
            pstmt.setString(2, ngaySinh);
            pstmt.setString(3, gioiTinh);
            pstmt.setInt(4, maLop);
            pstmt.setString(5, sdt);
            pstmt.setString(6, email);
            pstmt.setInt(7, maHS);
            pstmt.executeUpdate();
            System.out.println("Cập nhật học sinh thành công");
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật học sinh: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void deleteHocSinh(int maHS) {
        String sql = "DELETE FROM HocSinh WHERE MaHS = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, maHS);
            pstmt.executeUpdate();
            System.out.println("Xóa học sinh thành công");
        } catch (SQLException e) {
            System.err.println("Lỗi khi xóa học sinh: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
