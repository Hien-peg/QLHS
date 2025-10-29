package com.sgu.qlhs.bus;

import com.sgu.qlhs.dto.LopDTO;
import com.sgu.qlhs.database.LopDAO;
import java.util.ArrayList;
import java.util.List;

public class LopBUS {
    private LopDAO dao;

    public LopBUS() {
        dao = new LopDAO();
    }

    public List<LopDTO> getAllLop() {
        List<LopDTO> list = new ArrayList<>();
        List<Object[]> rows = dao.getAllLop();
        for (Object[] r : rows) {
            int maLop = (r[0] instanceof Integer) ? (Integer) r[0] : Integer.parseInt(r[0].toString());
            String tenLop = r[1] != null ? r[1].toString() : "";
            int khoi = (r[2] instanceof Integer) ? (Integer) r[2] : Integer.parseInt(r[2].toString());
            String tenPhong = r[3] != null ? r[3].toString() : "";
            list.add(new LopDTO(maLop, tenLop, khoi, tenPhong));
        }
        return list;
    }

    // write facades
    public void saveLop(String tenLop, int khoi, int maPhong) {
        dao.insertLop(tenLop, khoi, maPhong);
    }

    public void updateLop(int maLop, String tenLop, int khoi, int maPhong) {
        dao.updateLop(maLop, tenLop, khoi, maPhong);
    }

    public void deleteLop(int maLop) {
        dao.deleteLop(maLop);
    }

    public LopDTO getLopByMa(int maLop) {
        for (LopDTO l : getAllLop()) {
            if (l.getMaLop() == maLop)
                return l;
        }
        return null;
    }
}
