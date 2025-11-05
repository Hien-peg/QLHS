package com.sgu.qlhs.dto;

public class MonHocDTO {
    private int maMon;
    private String tenMon;
    private int soTiet;
    private String ghiChu;
    private String loaiMon; 

    public MonHocDTO() {
    }

    public MonHocDTO(int maMon, String tenMon) {
        this.maMon = maMon;
        this.tenMon = tenMon;
    }

    public MonHocDTO(int maMon, String tenMon, int soTiet, String ghiChu, String loaiMon) {
        this.maMon = maMon;
        this.tenMon = tenMon;
        this.soTiet = soTiet;
        this.ghiChu = ghiChu;
        this.loaiMon = loaiMon; 
    }

    public int getMaMon() {
        return maMon;
    }

    public void setMaMon(int maMon) {
        this.maMon = maMon;
    }

    public String getTenMon() {
        return tenMon;
    }

    public int getSoTiet() {
        return soTiet;
    }

    public void setSoTiet(int soTiet) {
        this.soTiet = soTiet;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    public void setTenMon(String tenMon) {
        this.tenMon = tenMon;
    }

    public String getLoaiMon() {
        return loaiMon;
    }

    public void setLoaiMon(String loaiMon) {
        this.loaiMon = loaiMon;
    }


    @Override
    public String toString() {
        return tenMon; 
    }
}