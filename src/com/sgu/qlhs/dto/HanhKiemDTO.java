package com.sgu.qlhs.dto;

public class HanhKiemDTO {
    private int maHS;
    private int maNK;
    private int hocKy;
    private String xepLoai;
    private String ghiChu;

    public HanhKiemDTO() {
    }

    public HanhKiemDTO(int maHS, int maNK, int hocKy, String xepLoai, String ghiChu) {
        this.maHS = maHS;
        this.maNK = maNK;
        this.hocKy = hocKy;
        this.xepLoai = xepLoai;
        this.ghiChu = ghiChu;
    }

    public int getMaHS() {
        return maHS;
    }

    public void setMaHS(int maHS) {
        this.maHS = maHS;
    }

    public int getMaNK() {
        return maNK;
    }

    public void setMaNK(int maNK) {
        this.maNK = maNK;
    }

    public int getHocKy() {
        return hocKy;
    }

    public void setHocKy(int hocKy) {
        this.hocKy = hocKy;
    }

    public String getXepLoai() {
        return xepLoai;
    }

    public void setXepLoai(String xepLoai) {
        this.xepLoai = xepLoai;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }
}
