package com.sgu.qlhs.bus;

import com.sgu.qlhs.dto.PhongDTO;
import com.sgu.qlhs.database.PhongDAO;
import java.util.ArrayList;
import java.util.List;

public class PhongBUS {
    private PhongDAO dao;

    public PhongBUS() {
        dao = new PhongDAO();
    }

    public List<PhongDTO> getAllPhong() {
        List<PhongDTO> list = new ArrayList<>();
        List<Object[]> rows = dao.getAllPhong();
        for (Object[] r : rows) {
            int maPhong = (r[0] instanceof Integer) ? (Integer) r[0] : Integer.parseInt(r[0].toString());
            String tenPhong = r[1] != null ? r[1].toString() : "";
            String loai = r.length > 2 && r[2] != null ? r[2].toString() : "";
            int suc = r.length > 3 && r[3] != null
                    ? (r[3] instanceof Integer ? (Integer) r[3] : Integer.parseInt(r[3].toString()))
                    : 0;
            String viTri = r.length > 4 && r[4] != null ? r[4].toString() : "";
            list.add(new PhongDTO(maPhong, tenPhong, loai, suc, viTri));
        }
        return list;
    }

    public void savePhong(String tenPhong, String loaiPhong, int sucChua, String viTri) {
        dao.insertPhong(tenPhong, loaiPhong, sucChua, viTri);
    }

    public void updatePhong(int maPhong, String tenPhong, String loaiPhong, int sucChua, String viTri) {
        dao.updatePhong(maPhong, tenPhong, loaiPhong, sucChua, viTri);
    }

    public void deletePhong(int maPhong) {
        dao.deletePhong(maPhong);
    }

    public PhongDTO getPhongByMa(int maPhong) {
        List<PhongDTO> all = getAllPhong();
        for (PhongDTO p : all) {
            if (p.getMaPhong() == maPhong)
                return p;
        }
        return null;
    }
}
