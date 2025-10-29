package com.sgu.qlhs.dto;

public class HocSinhDTO {
    private int maHS;
    private String hoTen;
    private String ngaySinh;
    private String gioiTinh;
    private String tenLop;

    public HocSinhDTO() {
    }

    public HocSinhDTO(int maHS, String hoTen, String ngaySinh, String gioiTinh, String tenLop) {
        this.maHS = maHS;
        this.hoTen = hoTen;
        this.ngaySinh = ngaySinh;
        this.gioiTinh = gioiTinh;
        this.tenLop = tenLop;
    }

    public int getMaHS() {
        return maHS;
    }

    public void setMaHS(int maHS) {
        this.maHS = maHS;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public String getNgaySinh() {
        return ngaySinh;
    }

    public void setNgaySinh(String ngaySinh) {
        this.ngaySinh = ngaySinh;
    }

    public String getGioiTinh() {
        return gioiTinh;
    }

    public void setGioiTinh(String gioiTinh) {
        this.gioiTinh = gioiTinh;
    }

    public String getTenLop() {
        return tenLop;
    }

    public void setTenLop(String tenLop) {
        this.tenLop = tenLop;
    }

    @Override
    public String toString() {
        return "HocSinhDTO{" + "maHS=" + maHS + ", hoTen='" + hoTen + '\'' + ", ngaySinh='" + ngaySinh + '\''
                + ", gioiTinh='" + gioiTinh + '\'' + ", tenLop='" + tenLop + '\'' + '}';
    }
}
