package com.sgu.qlhs.bus;

import com.sgu.qlhs.database.DiemHocKyDAO;
import java.util.List;

public class DiemHocKyBUS {
    private DiemHocKyDAO dao;

    public DiemHocKyBUS() {
        dao = new DiemHocKyDAO();
    }

    public List<Object[]> getDiemTrungBinhHocKy(int hocKy, int maNK) {
        return dao.getDiemTrungBinhHocKy(hocKy, maNK);
    }

    public List<Object[]> getDiemChiTietHocKy(int maLop, int hocKy, int maNK) {
        return dao.getDiemChiTietHocKy(maLop, hocKy, maNK);
    }

    public List<Object[]> getDiemTrungBinhCaNam(int maNK) {
        return dao.getDiemTrungBinhCaNam(maNK);
    }

    public List<Object[]> getAllLop() {
        return dao.getAllLop();
    }
}
