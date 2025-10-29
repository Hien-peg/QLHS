package com.sgu.qlhs.bus;

import com.sgu.qlhs.dto.GiaoVienDTO;
import com.sgu.qlhs.database.GiaoVienDAO;
import java.util.ArrayList;
import java.util.List;

public class GiaoVienBUS {
    private GiaoVienDAO dao;

    public GiaoVienBUS() {
        dao = new GiaoVienDAO();
    }

    public List<GiaoVienDTO> getAllGiaoVien() {
        List<GiaoVienDTO> list = new ArrayList<>();
        List<Object[]> rows = dao.getAllGiaoVien();
        for (Object[] r : rows) {
            int maGV = (r[0] instanceof Integer) ? (Integer) r[0] : Integer.parseInt(r[0].toString());
            String hoTen = r[1] != null ? r[1].toString() : "";
            String sdt = r.length > 2 && r[2] != null ? r[2].toString() : "";
            String email = r.length > 3 && r[3] != null ? r[3].toString() : "";
            list.add(new GiaoVienDTO(maGV, hoTen, "", sdt, email));
        }
        return list;
    }

    public void saveGiaoVien(String hoTen, String ngaySinh, String gioiTinh, String sdt, String email) {
        dao.insertGiaoVien(hoTen, ngaySinh, gioiTinh, sdt, email);
    }

    public void updateGiaoVien(int maGV, String hoTen, String ngaySinh, String gioiTinh, String sdt, String email) {
        dao.updateGiaoVien(maGV, hoTen, ngaySinh, gioiTinh, sdt, email);
    }

    public void deleteGiaoVien(int maGV) {
        dao.deleteGiaoVien(maGV);
    }

    public GiaoVienDTO getGiaoVienByMa(int maGV) {
        for (GiaoVienDTO g : getAllGiaoVien()) {
            if (g.getMaGV() == maGV)
                return g;
        }
        return null;
    }
}
