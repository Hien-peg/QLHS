package com.sgu.qlhs.dto;

public class MonHocDTO {
    private int maMon;
    private String tenMon;
    private int soTiet;
    private String ghiChu;

    public MonHocDTO() {
    }

    public MonHocDTO(int maMon, String tenMon) {
        this.maMon = maMon;
        this.tenMon = tenMon;
    }

    public MonHocDTO(int maMon, String tenMon, int soTiet, String ghiChu) {
        this.maMon = maMon;
        this.tenMon = tenMon;
        this.soTiet = soTiet;
        this.ghiChu = ghiChu;
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

    @Override
    public String toString() {
        return "MonHocDTO{" + "maMon=" + maMon + ", tenMon='" + tenMon + '\'' + '}';
    }
}
