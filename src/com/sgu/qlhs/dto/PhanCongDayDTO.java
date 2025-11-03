package com.sgu.qlhs.dto;

import java.util.Date;

public class PhanCongDayDTO {
    private int maPCD;          // Khóa chính
    private int maGV;           // Giáo viên dạy
    private int maLop;          // Lớp được dạy
    private int maMon;          // Môn học
    private int maPhong;        // Phòng học
    private String hocKy;       // Học kỳ (VD: HK1, HK2)
    private String namHoc;      // Năm học (VD: 2024-2025)
    private int trangThai;      // 1 = hoạt động, 0 = xóa mềm
    private Date ngayTao;
    private Date ngayCapNhat;

    // ====== Thông tin hiển thị (phục vụ UI) ======
    private String tenGV;
    private String tenMon;
    private String tenLop;
    private String tenPhong;

    // ====== Constructors ======
    public PhanCongDayDTO() {}

    public PhanCongDayDTO(int maPCD, int maGV, int maLop, int maMon, int maPhong,
                          String hocKy, String namHoc, int trangThai,
                          Date ngayTao, Date ngayCapNhat) {
        this.maPCD = maPCD;
        this.maGV = maGV;
        this.maLop = maLop;
        this.maMon = maMon;
        this.maPhong = maPhong;
        this.hocKy = hocKy;
        this.namHoc = namHoc;
        this.trangThai = trangThai;
        this.ngayTao = ngayTao;
        this.ngayCapNhat = ngayCapNhat;
    }

    // ====== Getter / Setter ======
    public int getMaPCD() { return maPCD; }
    public void setMaPCD(int maPCD) { this.maPCD = maPCD; }

    public int getMaGV() { return maGV; }
    public void setMaGV(int maGV) { this.maGV = maGV; }

    public int getMaLop() { return maLop; }
    public void setMaLop(int maLop) { this.maLop = maLop; }

    public int getMaMon() { return maMon; }
    public void setMaMon(int maMon) { this.maMon = maMon; }

    public int getMaPhong() { return maPhong; }
    public void setMaPhong(int maPhong) { this.maPhong = maPhong; }

    public String getHocKy() { return hocKy; }
    public void setHocKy(String hocKy) { this.hocKy = hocKy; }

    public String getNamHoc() { return namHoc; }
    public void setNamHoc(String namHoc) { this.namHoc = namHoc; }

    public int getTrangThai() { return trangThai; }
    public void setTrangThai(int trangThai) { this.trangThai = trangThai; }

    public Date getNgayTao() { return ngayTao; }
    public void setNgayTao(Date ngayTao) { this.ngayTao = ngayTao; }

    public Date getNgayCapNhat() { return ngayCapNhat; }
    public void setNgayCapNhat(Date ngayCapNhat) { this.ngayCapNhat = ngayCapNhat; }

    // ====== Các trường hiển thị ======
    public String getTenGV() { return tenGV; }
    public void setTenGV(String tenGV) { this.tenGV = tenGV; }

    public String getTenMon() { return tenMon; }
    public void setTenMon(String tenMon) { this.tenMon = tenMon; }

    public String getTenLop() { return tenLop; }
    public void setTenLop(String tenLop) { this.tenLop = tenLop; }

    public String getTenPhong() { return tenPhong; }
    public void setTenPhong(String tenPhong) { this.tenPhong = tenPhong; }

    @Override
    public String toString() {
        return "PhanCongDayDTO{" +
                "maPCD=" + maPCD +
                ", maGV=" + maGV +
                ", maLop=" + maLop +
                ", maMon=" + maMon +
                ", maPhong=" + maPhong +
                ", hocKy='" + hocKy + '\'' +
                ", namHoc='" + namHoc + '\'' +
                ", trangThai=" + trangThai +
                '}';
    }
}
