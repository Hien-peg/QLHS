package com.sgu.qlhs.database;

import com.sgu.qlhs.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LopDAO {

    public List<Object[]> getAllLop() {
        List<Object[]> data = new ArrayList<>();
        // THAY ĐỔI: Thêm l.MaPhong vào câu truy vấn
        String sql = "SELECT l.MaLop, l.TenLop, l.Khoi, l.MaPhong, ph.TenPhong " +
                "FROM Lop l LEFT JOIN PhongHoc ph ON l.MaPhong = ph.MaPhong";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                // THAY ĐỔI: Object[] giờ có 5 phần tử
                Object[] row = new Object[5];
                row[0] = rs.getInt("MaLop");
                row[1] = rs.getString("TenLop");
                row[2] = rs.getInt("Khoi");
                row[3] = rs.getInt("MaPhong"); // <--- Thêm MaPhong
                row[4] = rs.getString("TenPhong");
                data.add(row);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi truy vấn dữ liệu lớp: " + e.getMessage());
        }
        return data;
    }

    public void insertLop(String tenLop, int khoi, int maPhong) {
        String sql = "INSERT INTO Lop (TenLop, Khoi, MaPhong) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, tenLop);
            pstmt.setInt(2, khoi);
            pstmt.setInt(3, maPhong);

            pstmt.executeUpdate();
            System.out.println("Thêm lớp thành công!");
        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm lớp: " + e.getMessage());
        }
    }

    public void updateLop(int maLop, String tenLop, int khoi, int maPhong) {
        String sql = "UPDATE Lop SET TenLop = ?, Khoi = ?, MaPhong = ? WHERE MaLop = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, tenLop);
            pstmt.setInt(2, khoi);
            pstmt.setInt(3, maPhong);
            pstmt.setInt(4, maLop);
            pstmt.executeUpdate();
            System.out.println("Cập nhật lớp thành công!");
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật lớp: " + e.getMessage());
        }
    }

    public void deleteLop(int maLop) {
        String sql = "DELETE FROM Lop WHERE MaLop = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, maLop);
            pstmt.executeUpdate();
            System.out.println("Xóa lớp thành công!");
        } catch (SQLException e) {
            System.err.println("Lỗi khi xóa lớp: " + e.getMessage());
        }
    }
}
