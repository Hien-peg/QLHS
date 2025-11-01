package com.sgu.qlhs.database;

import com.sgu.qlhs.DatabaseConnection;
import com.sgu.qlhs.dto.HanhKiemDTO;
import java.sql.*;

public class HanhKiemDAO {

    public HanhKiemDTO getHanhKiem(int maHS, int maNK, int hocKy) {
        String sql = "SELECT MaHS, MaNK, HocKy, XepLoai, GhiChu FROM HanhKiem WHERE MaHS = ? AND MaNK = ? AND HocKy = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maHS);
            ps.setInt(2, maNK);
            ps.setInt(3, hocKy);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new HanhKiemDTO(rs.getInt("MaHS"), rs.getInt("MaNK"), rs.getInt("HocKy"),
                            rs.getString("XepLoai"), rs.getString("GhiChu"));
                }
            }
        } catch (SQLException ex) {
            System.err.println("Lỗi HanhKiemDAO.getHanhKiem: " + ex.getMessage());
        }
        return null;
    }

    /**
     * Batch fetch: returns a map MaHS -> XepLoai for the given students (maHSList)
     * for the given niên khóa and học kỳ. Missing entries are absent from the map.
     */
    public java.util.Map<Integer, String> getHanhKiemForStudents(java.util.List<Integer> maHSList, int maNK,
            int hocKy) {
        java.util.Map<Integer, String> result = new java.util.HashMap<>();
        if (maHSList == null || maHSList.isEmpty())
            return result;

        // Build placeholders
        StringBuilder in = new StringBuilder();
        for (int i = 0; i < maHSList.size(); i++) {
            if (i > 0)
                in.append(',');
            in.append('?');
        }
        String sql = "SELECT MaHS, XepLoai FROM HanhKiem WHERE MaNK = ? AND HocKy = ? AND MaHS IN (" + in + ")";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            int idx = 1;
            ps.setInt(idx++, maNK);
            ps.setInt(idx++, hocKy);
            for (Integer id : maHSList) {
                ps.setInt(idx++, id);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.put(rs.getInt("MaHS"), rs.getString("XepLoai"));
                }
            }
        } catch (SQLException ex) {
            System.err.println("Lỗi HanhKiemDAO.getHanhKiemForStudents: " + ex.getMessage());
        }
        return result;
    }

    // Insert or update (upsert) based on unique key (MaHS, MaNK, HocKy)
    public boolean upsertHanhKiem(HanhKiemDTO hk) {
        String sql = "INSERT INTO HanhKiem (MaHS, MaNK, HocKy, XepLoai, GhiChu) VALUES (?, ?, ?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE XepLoai = VALUES(XepLoai), GhiChu = VALUES(GhiChu)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, hk.getMaHS());
            ps.setInt(2, hk.getMaNK());
            ps.setInt(3, hk.getHocKy());
            ps.setString(4, hk.getXepLoai());
            ps.setString(5, hk.getGhiChu());
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            System.err.println("Lỗi HanhKiemDAO.upsertHanhKiem: " + ex.getMessage());
        }
        return false;
    }

    public boolean deleteHanhKiem(int maHS, int maNK, int hocKy) {
        String sql = "DELETE FROM HanhKiem WHERE MaHS = ? AND MaNK = ? AND HocKy = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maHS);
            ps.setInt(2, maNK);
            ps.setInt(3, hocKy);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            System.err.println("Lỗi HanhKiemDAO.deleteHanhKiem: " + ex.getMessage());
        }
        return false;
    }
}
