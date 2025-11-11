package com.sgu.qlhs.bus;

import com.sgu.qlhs.database.PhanCongDayDAO;
import com.sgu.qlhs.dto.PhanCongDayDTO;

import java.util.ArrayList;
import java.util.List;

public class PhanCongDayBUS {
    private final PhanCongDayDAO dao;

    public PhanCongDayBUS() {
        dao = new PhanCongDayDAO();
    }

    public List<Integer> getDistinctMaLopByGiaoVien(int maGV, String namHoc, String hocKy) {
        try {
            // SỬA: Truyền tham số String
            List<Integer> l = dao.getDistinctMaLopByGiaoVien(maGV, namHoc, hocKy);
            return l != null ? l : new ArrayList<>();
        } catch (Exception ex) {
            System.err.println("Lỗi khi lấy PhanCongDay.MaLop: " + ex.getMessage());
            return new ArrayList<>();
        }
    }

    public List<Integer> getDistinctMaMonByGiaoVien(int maGV, String namHoc, String hocKy) {
        try {
            // SỬA: Truyền tham số String
            List<Integer> l = dao.getDistinctMaMonByGiaoVien(maGV, namHoc, hocKy);
            return l != null ? l : new ArrayList<>();
        } catch (Exception ex) {
            System.err.println("Lỗi khi lấy PhanCongDay.MaMon: " + ex.getMessage());
            return new ArrayList<>();
        }
    }

    public List<Integer> getDistinctMaLopWithChuNhiem(int maGV, String namHoc, String hocKy) {
        java.util.Set<Integer> set = new java.util.LinkedHashSet<>();
        try {
            // Lấy danh sách lớp dạy 
            List<Integer> taught = getDistinctMaLopByGiaoVien(maGV, namHoc, hocKy);
            if (taught != null)
                set.addAll(taught);
        } catch (Exception ex) {
            System.err.println("Lỗi khi lấy lớp dạy: " + ex.getMessage());
        }
        try {
            // Lấy lớp chủ nhiệm
            com.sgu.qlhs.bus.ChuNhiemBUS cnBus = new com.sgu.qlhs.bus.ChuNhiemBUS();
            com.sgu.qlhs.dto.ChuNhiemDTO cn = cnBus.getChuNhiemByGV(maGV);
            
            // Chỉ thêm lớp chủ nhiệm nếu nó chưa có trong danh sách lớp dạy
            if (cn != null && cn.getMaLop() > 0) {
                set.add(cn.getMaLop());
            }
        } catch (Exception ex) {
            System.err.println("Lỗi khi lấy lớp chủ nhiệm: " + ex.getMessage());
        }
        return new ArrayList<>(set);
    }

    // Các hàm CRUD cho module Phân công dạy
    // Lấy toàn bộ danh sách phân công
    public List<PhanCongDayDTO> getAll() {
        return dao.getAll();
    }

    // Thêm mới phân công
    public boolean insert(PhanCongDayDTO dto) {
        if (dto == null)
            return false;

        // Kiểm tra trùng phân công (GV + Lớp + Môn + HK + Năm học)
        if (dao.existsDuplicate(dto.getMaGV(), dto.getMaLop(), dto.getMaMon(), dto.getHocKy(), dto.getNamHoc())) {
            System.err.println("Phân công bị trùng: Giáo viên " + dto.getMaGV() +
                    " đã dạy lớp " + dto.getMaLop() +
                    " môn " + dto.getMaMon() +
                    " trong " + dto.getHocKy() + " - " + dto.getNamHoc());
            return false;
        }

        return dao.insert(dto);
    }

    // Cập nhật phân công
    public boolean update(PhanCongDayDTO dto) {
        if (dto == null)
            return false;
        return dao.update(dto);
    }

    // Xóa (xóa thật — Thời khóa biểu liên quan sẽ bị xóa theo cascade)
    public boolean delete(int maPCD) {
        return dao.delete(maPCD);
    }

    // Tìm theo mã phân công
    public PhanCongDayDTO findById(int maPCD) {
        return dao.findById(maPCD);
    }

    // Lấy danh sách theo lớp (phục vụ lọc)
    public List<PhanCongDayDTO> getByLop(int maLop) {
        List<PhanCongDayDTO> all = dao.getAll();
        List<PhanCongDayDTO> filtered = new ArrayList<>();
        for (PhanCongDayDTO dto : all) {
            if (dto.getMaLop() == maLop)
                filtered.add(dto);
        }
        return filtered;
    }

    // Lấy danh sách theo giáo viên (phục vụ lọc)
    public List<PhanCongDayDTO> getByGV(int maGV) {
        List<PhanCongDayDTO> all = dao.getAll();
        List<PhanCongDayDTO> filtered = new ArrayList<>();
        for (PhanCongDayDTO dto : all) {
            if (dto.getMaGV() == maGV)
                filtered.add(dto);
        }
        return filtered;
    }

    // Lấy danh sách theo học kỳ hoặc năm học
    public List<PhanCongDayDTO> getByHocKyNamHoc(String hocKy, String namHoc) {
        List<PhanCongDayDTO> all = dao.getAll();
        List<PhanCongDayDTO> filtered = new ArrayList<>();
        for (PhanCongDayDTO dto : all) {
            if (dto.getHocKy().equalsIgnoreCase(hocKy) && dto.getNamHoc().equalsIgnoreCase(namHoc))
                filtered.add(dto);
        }
        return filtered;
    }
}