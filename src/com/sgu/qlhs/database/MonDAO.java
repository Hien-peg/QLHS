package com.sgu.qlhs.database;

import com.sgu.qlhs.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MonDAO {

    public List<Object[]> getAllMon() {
        List<Object[]> data = new ArrayList<>();
        String sql = "SELECT MaMon, TenMon, SoTiet, GhiChu FROM MonHoc";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Object[] row = new Object[4];
                row[0] = rs.getInt("MaMon");
                row[1] = rs.getString("TenMon");
                row[2] = rs.getInt("SoTiet");
                row[3] = rs.getString("GhiChu");
                data.add(row);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi truy vấn dữ liệu môn: " + e.getMessage());
        }
        return data;
    }

    public void insertMon(String tenMon, int soTiet, String ghiChu) {
        String sql = "INSERT INTO MonHoc (TenMon, SoTiet, GhiChu) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, tenMon);
            pstmt.setInt(2, soTiet);
            pstmt.setString(3, ghiChu);
            pstmt.executeUpdate();
            System.out.println("Thêm môn thành công!");
        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm môn: " + e.getMessage());
        }
    }

    public void updateMon(int maMon, String tenMon, int soTiet, String ghiChu) {
        String sql = "UPDATE MonHoc SET TenMon = ?, SoTiet = ?, GhiChu = ? WHERE MaMon = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, tenMon);
            pstmt.setInt(2, soTiet);
            pstmt.setString(3, ghiChu);
            pstmt.setInt(4, maMon);
            pstmt.executeUpdate();
            System.out.println("Cập nhật môn thành công!");
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật môn: " + e.getMessage());
        }
    }

    public void deleteMon(int maMon) {
        String sql = "DELETE FROM MonHoc WHERE MaMon = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, maMon);
            pstmt.executeUpdate();
            System.out.println("Xóa môn thành công!");
        } catch (SQLException e) {
            System.err.println("Lỗi khi xóa môn: " + e.getMessage());
        }
    }
}
