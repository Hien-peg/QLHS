package com.sgu.qlhs.database;

import com.sgu.qlhs.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Simple DAO to read NienKhoa information.
 */
public class NienKhoaDAO {

    /**
     * Return the latest MaNK (highest MaNK) or 1 if none found.
     */
    public int getLatestMaNK() {
        String sql = "SELECT MaNK FROM NienKhoa ORDER BY MaNK DESC LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("MaNK");
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi truy vấn NienKhoa: " + e.getMessage());
            e.printStackTrace();
        }
        return 1;
    }
}
