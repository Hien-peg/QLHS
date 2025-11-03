package com.sgu.qlhs.bus;

import com.sgu.qlhs.database.NienKhoaDAO;

/**
 * Thin BUS for NienKhoa helper functions.
 */
public class NienKhoaBUS {
    private final NienKhoaDAO dao;

    public NienKhoaBUS() {
        dao = new NienKhoaDAO();
    }

    /**
     * Return the latest MaNK from DB (or 1 if none).
     */
    public int getCurrentMaNK() {
        return dao.getLatestMaNK();
    }

    /**
     * Convenience static accessor that returns current MaNK or 1 on error.
     */
    public static int current() {
        try {
            return new NienKhoaBUS().getCurrentMaNK();
        } catch (Exception ex) {
            return 1;
        }
    }

    /**
     * === MỚI: Thêm hàm này ===
     * Lấy chuỗi NamHoc (vd: "2024-2025") từ MaNK
     */
    public String getNamHocString(int maNK) {
        String s = dao.getNamHocStringByMaNK(maNK);
        // Fallback nếu không tìm thấy
        return (s != null) ? s : "2024-2025";
    }

    /**
     * === MỚI: Thêm hàm này ===
     * Lấy chuỗi NamHoc cho niên khóa HIỆN TẠI
     */
    public static String currentNamHoc() {
        try {
            NienKhoaBUS bus = new NienKhoaBUS();
            int maNK = bus.getCurrentMaNK();
            return bus.getNamHocString(maNK);
        } catch (Exception ex) {
            return "2024-2025"; // Fallback
        }
    }
}