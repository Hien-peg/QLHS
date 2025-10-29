package com.sgu.qlhs.dto;

public class GiaoVienDTO {
    private int maGV;
    private String hoTen;
    private String chuyenMon;
    private String soDienThoai;
    private String email;

    public GiaoVienDTO() {
    }

    public GiaoVienDTO(int maGV, String hoTen, String chuyenMon) {
        this.maGV = maGV;
        this.hoTen = hoTen;
        this.chuyenMon = chuyenMon;
        this.soDienThoai = "";
        this.email = "";
    }

    public GiaoVienDTO(int maGV, String hoTen, String chuyenMon, String soDienThoai, String email) {
        this.maGV = maGV;
        this.hoTen = hoTen;
        this.chuyenMon = chuyenMon;
        this.soDienThoai = soDienThoai;
        this.email = email;
    }

    public int getMaGV() {
        return maGV;
    }

    public void setMaGV(int maGV) {
        this.maGV = maGV;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public String getChuyenMon() {
        return chuyenMon;
    }

    public void setChuyenMon(String chuyenMon) {
        this.chuyenMon = chuyenMon;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public void setSoDienThoai(String soDienThoai) {
        this.soDienThoai = soDienThoai;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "GiaoVienDTO{" + "maGV=" + maGV + ", hoTen='" + hoTen + '\'' + ", chuyenMon='" + chuyenMon + '\''
                + ", soDienThoai='" + soDienThoai + '\'' + ", email='" + email + '\'' + "}";
    }
}
