package com.sgu.qlhs.database;

import com.sgu.qlhs.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GiaoVienDAO {

    public List<Object[]> getAllGiaoVien() {
        List<Object[]> data = new ArrayList<>();
        String sql = "SELECT MaGV, HoTen, SoDienThoai, Email FROM GiaoVien";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Object[] row = new Object[4];
                row[0] = rs.getInt("MaGV");
                row[1] = rs.getString("HoTen");
                row[2] = rs.getString("SoDienThoai");
                row[3] = rs.getString("Email");
                data.add(row);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi truy vấn dữ liệu giáo viên: " + e.getMessage());
        }
        return data;
    }

    public void insertGiaoVien(String hoTen, String ngaySinh, String gioiTinh, String sdt, String email) {
        String sql = "INSERT INTO GiaoVien (HoTen, NgaySinh, GioiTinh, SoDienThoai, Email) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, hoTen);
            pstmt.setString(2, ngaySinh);
            pstmt.setString(3, gioiTinh);
            pstmt.setString(4, sdt);
            pstmt.setString(5, email);

            pstmt.executeUpdate();
            System.out.println("Thêm giáo viên thành công!");
        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm giáo viên: " + e.getMessage());
        }
    }

    public void updateGiaoVien(int maGV, String hoTen, String ngaySinh, String gioiTinh, String sdt, String email) {
        String sql = "UPDATE GiaoVien SET HoTen = ?, NgaySinh = ?, GioiTinh = ?, SoDienThoai = ?, Email = ? WHERE MaGV = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, hoTen);
            pstmt.setString(2, ngaySinh);
            pstmt.setString(3, gioiTinh);
            pstmt.setString(4, sdt);
            pstmt.setString(5, email);
            pstmt.setInt(6, maGV);
            pstmt.executeUpdate();
            System.out.println("Cập nhật giáo viên thành công!");
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật giáo viên: " + e.getMessage());
        }
    }

    public void deleteGiaoVien(int maGV) {
        String sql = "DELETE FROM GiaoVien WHERE MaGV = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, maGV);
            pstmt.executeUpdate();
            System.out.println("Xóa giáo viên thành công!");
        } catch (SQLException e) {
            System.err.println("Lỗi khi xóa giáo viên: " + e.getMessage());
        }
    }
}
