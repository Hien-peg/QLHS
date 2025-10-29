package com.sgu.qlhs.bus;

import com.sgu.qlhs.dto.DiemDTO;
import com.sgu.qlhs.database.DiemDAO;
import java.util.ArrayList;
import java.util.List;

public class DiemBUS {
    private DiemDAO dao;

    public DiemBUS() {
        dao = new DiemDAO();
    }

    public List<DiemDTO> getAllDiem() {
        List<DiemDTO> list = new ArrayList<>();
        List<Object[]> rows = dao.getAllDiem();
        for (Object[] r : rows) {
            int maHS = (r[0] instanceof Integer) ? (Integer) r[0] : Integer.parseInt(r[0].toString());
            String hoTen = r[1] != null ? r[1].toString() : "";
            String tenMon = r[2] != null ? r[2].toString() : "";
            int hocKy = (r[3] instanceof Integer) ? (Integer) r[3] : Integer.parseInt(r[3].toString());
            double mieng = r[4] != null ? Double.parseDouble(r[4].toString()) : 0.0;
            double p15 = r[5] != null ? Double.parseDouble(r[5].toString()) : 0.0;
            double gk = r[6] != null ? Double.parseDouble(r[6].toString()) : 0.0;
            double ck = r[7] != null ? Double.parseDouble(r[7].toString()) : 0.0;
            DiemDTO dto = new DiemDTO();
            dto.setMaHS(maHS);
            dto.setHoTen(hoTen);
            dto.setTenMon(tenMon);
            dto.setHocKy(hocKy);
            dto.setDiemMieng(mieng);
            dto.setDiem15p(p15);
            dto.setDiemGiuaKy(gk);
            dto.setDiemCuoiKy(ck);
            list.add(dto);
        }
        return list;
    }

    public List<DiemDTO> getDiemByLopHocKy(int maLop, int hocKy, int maNK) {
        List<DiemDTO> list = new ArrayList<>();
        List<Object[]> rows = dao.getDiemByLopHocKy(maLop, hocKy, maNK);
        for (Object[] r : rows) {
            int maHS = (r[0] instanceof Integer) ? (Integer) r[0] : Integer.parseInt(r[0].toString());
            String hoTen = r[1] != null ? r[1].toString() : "";
            // r[2] TenLop ignored here
            int maMon = (r[3] instanceof Integer) ? (Integer) r[3] : Integer.parseInt(r[3].toString());
            String tenMon = r[4] != null ? r[4].toString() : "";
            double mieng = r[5] != null ? Double.parseDouble(r[5].toString()) : 0.0;
            double p15 = r[6] != null ? Double.parseDouble(r[6].toString()) : 0.0;
            double gk = r[7] != null ? Double.parseDouble(r[7].toString()) : 0.0;
            double ck = r[8] != null ? Double.parseDouble(r[8].toString()) : 0.0;
            list.add(new DiemDTO(maHS, hoTen, maMon, tenMon, mieng, p15, gk, ck));
        }
        return list;
    }

    public List<DiemDTO> getDiemByMaHS(int maHS, int hocKy, int maNK) {
        List<DiemDTO> list = new ArrayList<>();
        List<Object[]> rows = dao.getDiemByMaHS(maHS, hocKy, maNK);
        for (Object[] r : rows) {
            int maDiem = (r[0] instanceof Integer) ? (Integer) r[0] : Integer.parseInt(r[0].toString());
            String tenMon = r[1] != null ? r[1].toString() : "";
            double mieng = r[2] != null ? Double.parseDouble(r[2].toString()) : 0.0;
            double p15 = r[3] != null ? Double.parseDouble(r[3].toString()) : 0.0;
            double gk = r[4] != null ? Double.parseDouble(r[4].toString()) : 0.0;
            double ck = r[5] != null ? Double.parseDouble(r[5].toString()) : 0.0;
            DiemDTO d = new DiemDTO();
            d.setMaDiem(maDiem);
            d.setTenMon(tenMon);
            d.setDiemMieng(mieng);
            d.setDiem15p(p15);
            d.setDiemGiuaKy(gk);
            d.setDiemCuoiKy(ck);
            list.add(d);
        }
        return list;
    }

    // Thin write facade that delegates to DAO. Keeps Presentation layer unaware of
    // DAO.
    public void saveDiem(int maHS, int maMon, int hocKy, int maNK, double mieng, double p15, double gk, double ck) {
        dao.insertDiem(maHS, maMon, hocKy, maNK, mieng, p15, gk, ck);
    }

    /**
     * Insert or update a diem row. Delegates to DAO.upsertDiem.
     */
    public void saveOrUpdateDiem(int maHS, int maMon, int hocKy, int maNK, double mieng, double p15, double gk,
            double ck) {
        dao.upsertDiem(maHS, maMon, hocKy, maNK, mieng, p15, gk, ck);
    }
}
