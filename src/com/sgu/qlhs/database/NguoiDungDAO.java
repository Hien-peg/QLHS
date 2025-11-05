package com.sgu.qlhs.database;

import com.sgu.qlhs.dto.NguoiDungDTO;
import java.sql.*;

public class NguoiDungDAO {

    private Connection conn;

    public NguoiDungDAO(Connection conn) {
        this.conn = conn;
    }

    public NguoiDungDTO dangNhap(String tenDangNhap, String matKhau) throws SQLException {
        String sql = """
            SELECT MaTK, TenDangNhap, MatKhau, VaiTro
            FROM TaiKhoan
            WHERE TenDangNhap = ? AND MatKhau = ?
        """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tenDangNhap);
            stmt.setString(2, matKhau);

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) return null;

                // === Lấy thông tin cơ bản ===
                int maTK = rs.getInt("MaTK");
                String vaiTroRaw = rs.getString("VaiTro");
                String role = chuanHoaVaiTro(vaiTroRaw);
                String displayName = tenDangNhap;
                int mappedId = maTK; // mặc định (Admin)

                // === Phân nhánh theo vai trò ===
                switch (role) {
                    case "giao_vien" -> {
                        // Lấy MaGV theo MaTK
                        String q1 = "SELECT MaGV FROM TaiKhoan_GiaoVien WHERE MaTK = ?";
                        try (PreparedStatement ps = conn.prepareStatement(q1)) {
                            ps.setInt(1, maTK);
                            ResultSet r = ps.executeQuery();
                            if (r.next()) mappedId = r.getInt("MaGV");
                        }

                        // Lấy tên giáo viên
                        String q2 = "SELECT HoTen FROM GiaoVien WHERE MaGV = ?";
                        try (PreparedStatement ps = conn.prepareStatement(q2)) {
                            ps.setInt(1, mappedId);
                            ResultSet r = ps.executeQuery();
                            if (r.next()) displayName = r.getString("HoTen");
                        }
                    }

                    case "hoc_sinh" -> {
                        // Lấy MaHS theo MaTK
                        String q1 = "SELECT MaHS FROM TaiKhoan_HocSinh WHERE MaTK = ?";
                        try (PreparedStatement ps = conn.prepareStatement(q1)) {
                            ps.setInt(1, maTK);
                            ResultSet r = ps.executeQuery();
                            if (r.next()) mappedId = r.getInt("MaHS");
                        }

                        // Lấy tên học sinh
                        String q2 = "SELECT HoTen FROM HocSinh WHERE MaHS = ?";
                        try (PreparedStatement ps = conn.prepareStatement(q2)) {
                            ps.setInt(1, mappedId);
                            ResultSet r = ps.executeQuery();
                            if (r.next()) displayName = r.getString("HoTen");
                        }
                    }

                    case "quan_tri_vien" -> {
                        displayName = "Quản trị viên";
                    }
                }

                // === Trả về DTO (id là mã thực: MaGV / MaHS / MaTK) ===
                return new NguoiDungDTO(mappedId, tenDangNhap, matKhau, displayName, role);
            }
        }
    }

    // === Chuẩn hóa vai trò (Admin, GiaoVien, HocSinh) ===
    private String chuanHoaVaiTro(String roleRaw) {
        if (roleRaw == null) return "";
        String r = roleRaw.trim().toLowerCase();
        if (r.contains("admin") || r.contains("quan_tri")) return "quan_tri_vien";
        if (r.contains("giao") || r.contains("gv")) return "giao_vien";
        if (r.contains("hoc") || r.contains("hs")) return "hoc_sinh";
        return r;
    }
}
