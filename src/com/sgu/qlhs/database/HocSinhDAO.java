package com.sgu.qlhs.database;

import com.sgu.qlhs.DatabaseConnection;
import com.sgu.qlhs.dto.HocSinhDTO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HocSinhDAO {

    public List<Object[]> getAllHocSinh() {
        List<Object[]> data = new ArrayList<>();
        String sql = """
                    SELECT hs.MaHS, hs.HoTen, hs.NgaySinh, hs.GioiTinh, l.TenLop
                    FROM HocSinh hs
                    JOIN Lop l ON hs.MaLop = l.MaLop
                    WHERE hs.TrangThai = 1
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

    public List<Object[]> getHocSinhByMaLop(int maLop) {
        List<Object[]> data = new ArrayList<>();
        String sql = "SELECT MaHS, HoTen, GioiTinh, NgaySinh FROM HocSinh WHERE MaLop = ? AND TrangThai = 1";

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

    public Object[] getHocSinhById(int maHS) {
        String sql = """
                    SELECT hs.MaHS, hs.HoTen, hs.NgaySinh, hs.GioiTinh,
                           hs.DiaChi, hs.SoDienThoai, hs.Email, l.TenLop
                    FROM HocSinh hs
                    JOIN Lop l ON hs.MaLop = l.MaLop
                    WHERE hs.MaHS = ? AND hs.TrangThai = 1
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
    
    // Thêm học sinh mới (và 2 phụ huynh)
    public boolean addHocSinh(String hoTen, java.util.Date ngaySinh, String gioiTinh,
            String diaChi, String soDienThoai, String email, int maLop,
            String ph1HoTen, String ph1MQH, String ph1Sdt, String ph1Email,
            String ph2HoTen, String ph2MQH, String ph2Sdt, String ph2Email) {

        String sqlInsertHS = """
            INSERT INTO HocSinh (HoTen, NgaySinh, GioiTinh, DiaChi, SoDienThoai, Email, MaLop, TrangThai)
            VALUES (?, ?, ?, ?, ?, ?, ?, 1)
        """;
        
        // INSERT vào PhuHuynh (không có MaHS)
        String sqlInsertPH = """
            INSERT INTO PhuHuynh (HoTen, SoDienThoai, Email, DiaChi, TrangThai)
            VALUES (?, ?, ?, ?, 1)
        """;
        
        // INSERT vào bảng liên kết HocSinh_PhuHuynh
        String sqlInsertLink = """
            INSERT INTO HocSinh_PhuHuynh (MaHS, MaPH, QuanHe)
            VALUES (?, ?, ?)
        """;
        
        Connection conn = null;
        PreparedStatement psHS = null;
        PreparedStatement psPH = null;
        PreparedStatement psLink = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            if (conn == null) throw new SQLException("Không thể kết nối CSDL!");

            // === BẮT ĐẦU TRANSACTION ===
            conn.setAutoCommit(false);
            
            // 1. Thêm Học Sinh (Lấy MaHS)
            psHS = conn.prepareStatement(sqlInsertHS, Statement.RETURN_GENERATED_KEYS);
            psHS.setString(1, hoTen);
            psHS.setDate(2, new java.sql.Date(ngaySinh.getTime()));
            psHS.setString(3, gioiTinh);
            psHS.setString(4, diaChi);
            psHS.setString(5, soDienThoai);
            psHS.setString(6, email);
            psHS.setInt(7, maLop);

            int affected = psHS.executeUpdate();
            if (affected == 0) throw new SQLException("Thêm học sinh thất bại, không có dòng nào được thêm.");

            int maHS = -1;
            try (ResultSet rsGenKeys = psHS.getGeneratedKeys()) {
                if (rsGenKeys.next()) {
                    maHS = rsGenKeys.getInt(1);
                } else {
                    throw new SQLException("Không thể lấy MaHS vừa tạo.");
                }
            }
            
            psPH = conn.prepareStatement(sqlInsertPH, Statement.RETURN_GENERATED_KEYS);
            psLink = conn.prepareStatement(sqlInsertLink);

            // 2. Xử lý Phụ Huynh 1 (Nếu có tên)
            if (ph1HoTen != null && !ph1HoTen.trim().isEmpty()) {
                // 2a. INSERT vào PhuHuynh (Lấy MaPH1)
                psPH.setString(1, ph1HoTen);
                psPH.setString(2, ph1Sdt);
                psPH.setString(3, ph1Email);
                psPH.setString(4, diaChi); // Dùng chung địa chỉ với học sinh
                psPH.executeUpdate();
                
                int maPH1 = -1;
                try (ResultSet rsPH1 = psPH.getGeneratedKeys()) {
                    if (rsPH1.next()) maPH1 = rsPH1.getInt(1);
                }
                
                // 2b. INSERT vào HocSinh_PhuHuynh (Liên kết MaHS và MaPH1)
                if (maPH1 != -1) {
                    psLink.setInt(1, maHS);
                    psLink.setInt(2, maPH1);
                    psLink.setString(3, ph1MQH);
                    psLink.executeUpdate();
                }
            }

            // 3. Xử lý Phụ Huynh 2 (Nếu có tên)
            if (ph2HoTen != null && !ph2HoTen.trim().isEmpty()) {
                // 3a. INSERT vào PhuHuynh (Lấy MaPH2)
                psPH.setString(1, ph2HoTen);
                psPH.setString(2, ph2Sdt);
                psPH.setString(3, ph2Email);
                psPH.setString(4, diaChi); // Dùng chung địa chỉ với học sinh
                psPH.executeUpdate();
                
                int maPH2 = -1;
                try (ResultSet rsPH2 = psPH.getGeneratedKeys()) {
                    if (rsPH2.next()) maPH2 = rsPH2.getInt(1);
                }
                
                // 3b. INSERT vào HocSinh_PhuHuynh (Liên kết MaHS và MaPH2)
                if (maPH2 != -1) {
                    psLink.setInt(1, maHS);
                    psLink.setInt(2, maPH2);
                    psLink.setString(3, ph2MQH);
                    psLink.executeUpdate();
                }
            }

            // === HOÀN TẤT TRANSACTION ===
            conn.commit(); 
            return true;

        } catch (SQLException e) {
            System.err.println("❌ Lỗi khi thêm học sinh và phụ huynh (Transaction): " + e.getMessage());
            try {
                if(conn != null) conn.rollback(); // Hủy bỏ
            } catch (SQLException ex) {
                System.err.println("❌ Lỗi khi rollback: " + ex.getMessage());
            }
            return false;
        } finally {
            try {
                if (conn != null) conn.setAutoCommit(true); // Trả lại auto commit
            } catch (SQLException ex) {}
            
            // Đóng tất cả PreparedStatement và Connection
            closeQuietly(null, psHS, null);
            closeQuietly(null, psPH, null);
            closeQuietly(null, psLink, conn);
        }
    }


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

    public boolean deleteHocSinh(int maHS) {
        String sqlHS = "UPDATE HocSinh SET TrangThai = 0 WHERE MaHS = ?";
        
        String sqlPH = """
            UPDATE PhuHuynh ph
            JOIN HocSinh_PhuHuynh hsph ON ph.MaPH = hsph.MaPH
            SET ph.TrangThai = 0
            WHERE hsph.MaHS = ?
        """;

        Connection conn = null;
        PreparedStatement psHS = null;
        PreparedStatement psPH = null;

        try {
            conn = DatabaseConnection.getConnection();
            if (conn == null)
                throw new SQLException("Không thể kết nối CSDL!");

            conn.setAutoCommit(false); 

            psHS = conn.prepareStatement(sqlHS);
            psHS.setInt(1, maHS);
            int hsRowsAffected = psHS.executeUpdate();

            if (hsRowsAffected == 0) {
                 conn.rollback(); 
                 return false;
            }

            psPH = conn.prepareStatement(sqlPH);
            psPH.setInt(1, maHS);
            psPH.executeUpdate(); 
            
            conn.commit(); 
            return true;

        } catch (SQLException e) {
            System.err.println(" Lỗi khi xoá mềm học sinh và phụ huynh: " + e.getMessage());
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException exRollback) {
                 System.err.println(" Lỗi khi rollback: " + exRollback.getMessage());
            }
            return false;
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                }
            } catch (SQLException exAutoCommit) {
                 System.err.println(" Lỗi khi reset AutoCommit: " + exAutoCommit.getMessage());
            }
            closeQuietly(null, psHS, null);
            closeQuietly(null, psPH, conn);
        }
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
            WHERE tkhs.MaND = ? AND hs.TrangThai = 1
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
            WHERE hs.MaHS = ? AND hs.TrangThai = 1
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