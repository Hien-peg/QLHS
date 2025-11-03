package com.sgu.qlhs.dto;

import java.sql.Timestamp;

public class ThoiKhoaBieuDTO {
    private int maTKB;
    private int maPCD;
    private String thu;
    private int tietBD;
    private int tietKT;
    private int trangThai;
    private Timestamp ngayTao;
    private Timestamp ngayCapNhat;

    // JOIN từ PhanCongDay
    private int maGV;
    private int maLop;
    private int maPhong;
    private String hocKy;
    private String namHoc;

    // Thông tin hiển thị
    private String tenGV;
    private String tenMon;
    private String tenLop;
    private String tenPhong;

    // ===== Getter & Setter =====
    public int getMaTKB() { return maTKB; }
    public void setMaTKB(int maTKB) { this.maTKB = maTKB; }

    public int getMaPCD() { return maPCD; }
    public void setMaPCD(int maPCD) { this.maPCD = maPCD; }

    public String getThu() { return thu; }
    public void setThu(String thu) { this.thu = thu; }

    public int getTietBD() { return tietBD; }
    public void setTietBD(int tietBD) { this.tietBD = tietBD; }

    public int getTietKT() { return tietKT; }
    public void setTietKT(int tietKT) { this.tietKT = tietKT; }

    public int getTrangThai() { return trangThai; }
    public void setTrangThai(int trangThai) { this.trangThai = trangThai; }

    public Timestamp getNgayTao() { return ngayTao; }
    public void setNgayTao(Timestamp ngayTao) { this.ngayTao = ngayTao; }

    public Timestamp getNgayCapNhat() { return ngayCapNhat; }
    public void setNgayCapNhat(Timestamp ngayCapNhat) { this.ngayCapNhat = ngayCapNhat; }

    public int getMaGV() { return maGV; }
    public void setMaGV(int maGV) { this.maGV = maGV; }

    public int getMaLop() { return maLop; }
    public void setMaLop(int maLop) { this.maLop = maLop; }

    public int getMaPhong() { return maPhong; }
    public void setMaPhong(int maPhong) { this.maPhong = maPhong; }

    public String getHocKy() { return hocKy; }
    public void setHocKy(String hocKy) { this.hocKy = hocKy; }

    public String getNamHoc() { return namHoc; }
    public void setNamHoc(String namHoc) { this.namHoc = namHoc; }

    public String getTenGV() { return tenGV; }
    public void setTenGV(String tenGV) { this.tenGV = tenGV; }

    public String getTenMon() { return tenMon; }
    public void setTenMon(String tenMon) { this.tenMon = tenMon; }

    public String getTenLop() { return tenLop; }
    public void setTenLop(String tenLop) { this.tenLop = tenLop; }

    public String getTenPhong() { return tenPhong; }
    public void setTenPhong(String tenPhong) { this.tenPhong = tenPhong; }
}
