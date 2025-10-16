package com.sgu.qlhs.ui.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO để tính và quản lý điểm trung bình học kỳ và cả năm
 */
public class DiemHocKyDAO {

    /**
     * Tính điểm trung bình của 1 môn theo học kỳ
     * Công thức: (DiemMieng + Diem15p + DiemGiuaKy*2 + DiemCuoiKy*3) / 7
     */
    private double tinhDiemTBMon(double mieng, double p15, double giuaKy, double cuoiKy) {
        return Math.round((mieng + p15 + giuaKy * 2 + cuoiKy * 3) / 7.0 * 10) / 10.0;
    }

    /**
     * Lấy điểm trung bình của học sinh theo học kỳ
     * Trả về: MaHS, HoTen, [Điểm TB các môn], Điểm TB HK, Xếp loại
     */
    public List<Object[]> getDiemTrungBinhHocKy(int hocKy, int maNK) {
        List<Object[]> result = new ArrayList<>();

        String sql = "SELECT hs.MaHS, hs.HoTen, " +
                "AVG((d.DiemMieng + d.Diem15p + d.DiemGiuaKy*2 + d.DiemCuoiKy*3) / 7.0) as DiemTBHK " +
                "FROM HocSinh hs " +
                "JOIN Diem d ON hs.MaHS = d.MaHS " +
                "WHERE d.HocKy = ? AND d.MaNK = ? " +
                "GROUP BY hs.MaHS, hs.HoTen " +
                "ORDER BY hs.HoTen";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, hocKy);
            pstmt.setInt(2, maNK);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    double diemTB = Math.round(rs.getDouble("DiemTBHK") * 10) / 10.0;
                    Object[] row = new Object[4];
                    row[0] = rs.getInt("MaHS");
                    row[1] = rs.getString("HoTen");
                    row[2] = diemTB;
                    row[3] = xepLoai(diemTB);
                    result.add(row);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tính điểm TB học kỳ: " + e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    /**
     * Lấy điểm chi tiết của học sinh theo học kỳ (có điểm từng môn)
     */
    public List<Object[]> getDiemChiTietHocKy(int maLop, int hocKy, int maNK) {
        List<Object[]> result = new ArrayList<>();

        String sql = "SELECT hs.MaHS, hs.HoTen, mh.TenMon, " +
                "d.DiemMieng, d.Diem15p, d.DiemGiuaKy, d.DiemCuoiKy, " +
                "ROUND((d.DiemMieng + d.Diem15p + d.DiemGiuaKy*2 + d.DiemCuoiKy*3) / 7.0, 1) as DiemTB " +
                "FROM HocSinh hs " +
                "JOIN Diem d ON hs.MaHS = d.MaHS " +
                "JOIN MonHoc mh ON d.MaMon = mh.MaMon " +
                "WHERE hs.MaLop = ? AND d.HocKy = ? AND d.MaNK = ? " +
                "ORDER BY hs.HoTen, mh.TenMon";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, maLop);
            pstmt.setInt(2, hocKy);
            pstmt.setInt(3, maNK);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Object[] row = new Object[8];
                    row[0] = rs.getInt("MaHS");
                    row[1] = rs.getString("HoTen");
                    row[2] = rs.getString("TenMon");
                    row[3] = rs.getDouble("DiemMieng");
                    row[4] = rs.getDouble("Diem15p");
                    row[5] = rs.getDouble("DiemGiuaKy");
                    row[6] = rs.getDouble("DiemCuoiKy");
                    row[7] = rs.getDouble("DiemTB");
                    result.add(row);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy điểm chi tiết học kỳ: " + e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    /**
     * Lấy điểm trung bình cả năm (TB của HK1 và HK2)
     */
    public List<Object[]> getDiemTrungBinhCaNam(int maNK) {
        List<Object[]> result = new ArrayList<>();

        String sql = "SELECT hs.MaHS, hs.HoTen, l.TenLop, " +
                "AVG(CASE WHEN d.HocKy = 1 THEN (d.DiemMieng + d.Diem15p + d.DiemGiuaKy*2 + d.DiemCuoiKy*3) / 7.0 END) as DiemHK1, "
                +
                "AVG(CASE WHEN d.HocKy = 2 THEN (d.DiemMieng + d.Diem15p + d.DiemGiuaKy*2 + d.DiemCuoiKy*3) / 7.0 END) as DiemHK2 "
                +
                "FROM HocSinh hs " +
                "JOIN Lop l ON hs.MaLop = l.MaLop " +
                "JOIN Diem d ON hs.MaHS = d.MaHS " +
                "WHERE d.MaNK = ? " +
                "GROUP BY hs.MaHS, hs.HoTen, l.TenLop " +
                "ORDER BY l.TenLop, hs.HoTen";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, maNK);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    double hk1 = rs.getDouble("DiemHK1");
                    double hk2 = rs.getDouble("DiemHK2");
                    double diemCaNam = Math.round((hk1 + hk2 * 2) / 3.0 * 10) / 10.0;

                    Object[] row = new Object[7];
                    row[0] = rs.getInt("MaHS");
                    row[1] = rs.getString("HoTen");
                    row[2] = rs.getString("TenLop");
                    row[3] = Math.round(hk1 * 10) / 10.0;
                    row[4] = Math.round(hk2 * 10) / 10.0;
                    row[5] = diemCaNam;
                    row[6] = xepLoai(diemCaNam);
                    result.add(row);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tính điểm TB cả năm: " + e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    /**
     * Lấy danh sách lớp
     */
    public List<Object[]> getAllLop() {
        List<Object[]> result = new ArrayList<>();
        String sql = "SELECT MaLop, TenLop, Khoi FROM Lop ORDER BY TenLop";

        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Object[] row = new Object[3];
                row[0] = rs.getInt("MaLop");
                row[1] = rs.getString("TenLop");
                row[2] = rs.getInt("Khoi");
                result.add(row);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy danh sách lớp: " + e.getMessage());
        }

        return result;
    }

    /**
     * Xếp loại học lực
     */
    private String xepLoai(double diemTB) {
        if (diemTB >= 9.0)
            return "Xuất sắc";
        if (diemTB >= 8.0)
            return "Giỏi";
        if (diemTB >= 6.5)
            return "Khá";
        if (diemTB >= 5.0)
            return "Trung bình";
        if (diemTB >= 3.5)
            return "Yếu";
        return "Kém";
    }
}
