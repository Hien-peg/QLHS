package com.sgu.qlhs.database;

import com.sgu.qlhs.dto.PhuHuynhDTO;
import com.sgu.qlhs.DatabaseConnection;
import java.sql.*;
import java.util.*;

public class PhuHuynhDAO {
    private Connection conn;

    public PhuHuynhDAO() {
        conn = DatabaseConnection.getConnection();
    }

    public List<PhuHuynhDTO> getByHocSinh(int maHS) {
        List<PhuHuynhDTO> list = new ArrayList<>();
        String sql = """
            SELECT ph.*, hp.QuanHe
            FROM HocSinh_PhuHuynh hp
            JOIN PhuHuynh ph ON ph.MaPH = hp.MaPH
            WHERE hp.MaHS = ?
        """;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, maHS);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                PhuHuynhDTO ph = new PhuHuynhDTO();
                ph.setMaPH(rs.getInt("MaPH"));
                ph.setHoTen(rs.getString("HoTen"));
                ph.setSoDienThoai(rs.getString("SoDienThoai"));
                ph.setEmail(rs.getString("Email"));
                ph.setDiaChi(rs.getString("DiaChi"));
                ph.setQuanHe(rs.getString("QuanHe"));
                list.add(ph);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
