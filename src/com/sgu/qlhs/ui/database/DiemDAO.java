package com.sgu.qlhs.ui.database;

import com.sgu.qlhs.ui.database.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DiemDAO {
    
    public List<Object[]> getAllDiem() {
        List<Object[]> data = new ArrayList<>();
        String sql = "SELECT d.MaDiem, hs.HoTen, mh.TenMon, d.HocKy, d.DiemMieng, d.Diem15p, d.DiemGiuaKy, d.DiemCuoiKy " +
                     "FROM Diem d " +
                     "JOIN HocSinh hs ON d.MaHS = hs.MaHS " +
                     "JOIN MonHoc mh ON d.MaMon = mh.MaMon";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                Object[] row = new Object[8];
                row[0] = rs.getInt("MaDiem");
                row[1] = rs.getString("HoTen");
                row[2] = rs.getString("TenMon");
                row[3] = rs.getInt("HocKy");
                row[4] = rs.getDouble("DiemMieng");
                row[5] = rs.getDouble("Diem15p");
                row[6] = rs.getDouble("DiemGiuaKy");
                row[7] = rs.getDouble("DiemCuoiKy");
                data.add(row);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi truy vấn dữ liệu điểm: " + e.getMessage());
        }
        return data;
    }

    public void insertDiem(int maHS, int maMon, int hocKy, int maNK, double mieng, double p15, double gk, double ck) {
        String sql = "INSERT INTO Diem (MaHS, MaMon, HocKy, MaNK, DiemMieng, Diem15p, DiemGiuaKy, DiemCuoiKy) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, maHS);
            pstmt.setInt(2, maMon);
            pstmt.setInt(3, hocKy);
            pstmt.setInt(4, maNK);
            pstmt.setDouble(5, mieng);
            pstmt.setDouble(6, p15);
            pstmt.setDouble(7, gk);
            pstmt.setDouble(8, ck);
            
            pstmt.executeUpdate();
            System.out.println("Thêm điểm thành công!");
        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm điểm: " + e.getMessage());
        }
    }
}