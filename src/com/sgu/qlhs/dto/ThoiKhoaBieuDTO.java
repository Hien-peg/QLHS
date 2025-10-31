package com.sgu.qlhs.dto;

public class ThoiKhoaBieuDTO {
    private int maTKB;
    private int maLop;
    private int maGV;
    private int maMon;
    private int maPhong;
    private String hocKy;
    private String namHoc;
    private String thuTrongTuan;
    private int tietBatDau;
    private int tietKetThuc;
    private int trangThai;
    private String tenMon;
    private String tenGV;
    private String tenPhong;




    public ThoiKhoaBieuDTO() {}

    public ThoiKhoaBieuDTO(int maTKB, int maLop, int maGV, int maMon, int maPhong,
                           String hocKy, String namHoc, String thuTrongTuan,
                           int tietBatDau, int tietKetThuc, int trangThai) {
        this.maTKB = maTKB;
        this.maLop = maLop;
        this.maGV = maGV;
        this.maMon = maMon;
        this.maPhong = maPhong;
        this.hocKy = hocKy;
        this.namHoc = namHoc;
        this.thuTrongTuan = thuTrongTuan;
        this.tietBatDau = tietBatDau;
        this.tietKetThuc = tietKetThuc;
        this.trangThai = trangThai;
    }

    // ===== Getter / Setter =====
    public int getMaTKB() { return maTKB; }
    public void setMaTKB(int maTKB) { this.maTKB = maTKB; }

    public int getMaLop() { return maLop; }
    public void setMaLop(int maLop) { this.maLop = maLop; }

    public int getMaGV() { return maGV; }
    public void setMaGV(int maGV) { this.maGV = maGV; }

    public int getMaMon() { return maMon; }
    public void setMaMon(int maMon) { this.maMon = maMon; }

    public int getMaPhong() { return maPhong; }
    public void setMaPhong(int maPhong) { this.maPhong = maPhong; }

    public String getHocKy() { return hocKy; }
    public void setHocKy(String hocKy) { this.hocKy = hocKy; }

    public String getNamHoc() { return namHoc; }
    public void setNamHoc(String namHoc) { this.namHoc = namHoc; }

    public String getThuTrongTuan() { return thuTrongTuan; }
    public void setThuTrongTuan(String thuTrongTuan) { this.thuTrongTuan = thuTrongTuan; }

    public int getTietBatDau() { return tietBatDau; }
    public void setTietBatDau(int tietBatDau) { this.tietBatDau = tietBatDau; }

    public int getTietKetThuc() { return tietKetThuc; }
    public void setTietKetThuc(int tietKetThuc) { this.tietKetThuc = tietKetThuc; }

    public int getTrangThai() { return trangThai; }
    public void setTrangThai(int trangThai) { this.trangThai = trangThai; }
    // Getter & Setter
    public String getTenMon() { return tenMon; }
    public void setTenMon(String tenMon) { this.tenMon = tenMon; }

    public String getTenGV() { return tenGV; }
    public void setTenGV(String tenGV) { this.tenGV = tenGV; }

    public String getTenPhong() { return tenPhong; }
    public void setTenPhong(String tenPhong) { this.tenPhong = tenPhong; }
}
