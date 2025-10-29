package com.sgu.qlhs.dto;

public class LopDTO {
    private int maLop;
    private String tenLop;
    private int khoi;
    private String tenPhong;

    public LopDTO() {
    }

    public LopDTO(int maLop, String tenLop, int khoi, String tenPhong) {
        this.maLop = maLop;
        this.tenLop = tenLop;
        this.khoi = khoi;
        this.tenPhong = tenPhong;
    }

    public int getMaLop() {
        return maLop;
    }

    public void setMaLop(int maLop) {
        this.maLop = maLop;
    }

    public String getTenLop() {
        return tenLop;
    }

    public void setTenLop(String tenLop) {
        this.tenLop = tenLop;
    }

    public int getKhoi() {
        return khoi;
    }

    public void setKhoi(int khoi) {
        this.khoi = khoi;
    }

    public String getTenPhong() {
        return tenPhong;
    }

    public void setTenPhong(String tenPhong) {
        this.tenPhong = tenPhong;
    }

    @Override
    public String toString() {
        return "LopDTO{" + "maLop=" + maLop + ", tenLop='" + tenLop + '\'' + ", khoi=" + khoi + ", tenPhong='"
                + tenPhong + '\'' + '}';
    }
}
