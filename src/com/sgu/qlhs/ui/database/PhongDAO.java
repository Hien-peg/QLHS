package com.sgu.qlhs.ui.database;

import com.sgu.qlhs.ui.database.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PhongDAO {

    public List<Object[]> getAllPhong() {
        List<Object[]> data = new ArrayList<>();
        String sql = "SELECT MaPhong, TenPhong, LoaiPhong, SucChua, ViTri FROM PhongHoc";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                Object[] row = new Object[5];
                row[0] = rs.getInt("MaPhong");
                row[1] = rs.getString("TenPhong");
                row[2] = rs.getString("LoaiPhong");
                row[3] = rs.getInt("SucChua");
                row[4] = rs.getString("ViTri");
                data.add(row);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi truy vấn dữ liệu phòng học: " + e.getMessage());
        }
        return data;
    }

    public void insertPhong(String tenPhong, String loaiPhong, int sucChua, String viTri) {
        String sql = "INSERT INTO PhongHoc (TenPhong, LoaiPhong, SucChua, ViTri) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, tenPhong);
            pstmt.setString(2, loaiPhong);
            pstmt.setInt(3, sucChua);
            pstmt.setString(4, viTri);
            
            pstmt.executeUpdate();
            System.out.println("Thêm phòng học thành công!");
        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm phòng học: " + e.getMessage());
        }
    }
}