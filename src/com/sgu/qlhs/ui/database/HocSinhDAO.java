package com.sgu.qlhs.ui.database;

import com.sgu.qlhs.ui.database.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HocSinhDAO {
    
    // Phương thức để lấy tất cả học sinh
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

    // Phương thức để lấy học sinh theo mã lớp
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
}