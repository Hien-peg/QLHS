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
}
