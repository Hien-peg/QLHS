package com.sgu.qlhs.bus;

import com.sgu.qlhs.dto.HocSinhDTO;
import com.sgu.qlhs.database.HocSinhDAO;
import java.util.ArrayList;
import java.util.List;

public class HocSinhBUS {
    private HocSinhDAO dao;

    public HocSinhBUS() {
        dao = new HocSinhDAO();
    }

    public List<HocSinhDTO> getAllHocSinh() {
        List<HocSinhDTO> list = new ArrayList<>();
        List<Object[]> rows = dao.getAllHocSinh();
        for (Object[] r : rows) {
            int maHS = (r[0] instanceof Integer) ? (Integer) r[0] : Integer.parseInt(r[0].toString());
            String hoTen = r[1] != null ? r[1].toString() : "";
            String ngaySinh = r[2] != null ? r[2].toString() : "";
            String gioiTinh = r[3] != null ? r[3].toString() : "";
            String tenLop = r.length > 4 && r[4] != null ? r[4].toString() : "";
            list.add(new HocSinhDTO(maHS, hoTen, ngaySinh, gioiTinh, tenLop));
        }
        return list;
    }

    public List<HocSinhDTO> getHocSinhByMaLop(int maLop) {
        List<HocSinhDTO> list = new ArrayList<>();
        List<Object[]> rows = dao.getHocSinhByMaLop(maLop);
        for (Object[] r : rows) {
            int maHS = (r[0] instanceof Integer) ? (Integer) r[0] : Integer.parseInt(r[0].toString());
            String hoTen = r[1] != null ? r[1].toString() : "";
            String gioiTinh = r[2] != null ? r[2].toString() : "";
            String ngaySinh = r[3] != null ? r[3].toString() : "";
            list.add(new HocSinhDTO(maHS, hoTen, ngaySinh, gioiTinh, null));
        }
        return list;
    }

    // Write facades (match DAO signatures)
    public void saveHocSinh(String hoTen, java.util.Date ngaySinh, String gioiTinh, String diaChi, String sdt,
            String email, int maLop) {
        dao.addHocSinh(hoTen, ngaySinh, gioiTinh, diaChi, sdt, email, maLop);
    }

    public void updateHocSinh(int maHS, String hoTen, java.util.Date ngaySinh, String gioiTinh, String diaChi,
            String sdt, String email, int maLop) {
        dao.updateHocSinh(maHS, hoTen, ngaySinh, gioiTinh, diaChi, sdt, email, maLop);
    }

    public void deleteHocSinh(int maHS) {
        dao.deleteHocSinh(maHS);
    }

    public HocSinhDTO getHocSinhByMaHS(int maHS) {
        for (HocSinhDTO h : getAllHocSinh()) {
            if (h.getMaHS() == maHS)
                return h;
        }
        return null;
    }
}
