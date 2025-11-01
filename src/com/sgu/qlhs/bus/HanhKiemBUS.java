package com.sgu.qlhs.bus;

import com.sgu.qlhs.database.HanhKiemDAO;
import com.sgu.qlhs.dto.HanhKiemDTO;

public class HanhKiemBUS {
    private HanhKiemDAO dao;

    public HanhKiemBUS() {
        dao = new HanhKiemDAO();
    }

    public HanhKiemDTO getHanhKiem(int maHS, int maNK, int hocKy) {
        return dao.getHanhKiem(maHS, maNK, hocKy);
    }

    public boolean saveOrUpdate(HanhKiemDTO hk) {
        return dao.upsertHanhKiem(hk);
    }

    public boolean deleteHanhKiem(int maHS, int maNK, int hocKy) {
        return dao.deleteHanhKiem(maHS, maNK, hocKy);
    }

    public java.util.Map<Integer, String> getHanhKiemForStudents(java.util.List<Integer> maHSList, int maNK,
            int hocKy) {
        return dao.getHanhKiemForStudents(maHSList, maNK, hocKy);
    }
}
