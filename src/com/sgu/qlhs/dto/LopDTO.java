package com.sgu.qlhs.dto;

public class LopDTO {
    private int maLop;
    private String tenLop;
    private int khoi;
    private int maPhong;
    private String tenPhong;

    public LopDTO() {
    }

    public LopDTO(int maLop, String tenLop, int khoi, int maPhong, String tenPhong) {
        this.maLop = maLop;
        this.tenLop = tenLop;
        this.khoi = khoi;
        this.maPhong = maPhong;
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

    public int getMaPhong() {
        return maPhong;
    }

    public void setMaPhong(int maPhong) {
        this.maPhong = maPhong;
    }

    public String getTenPhong() {
        return tenPhong;
    }

    public void setTenPhong(String tenPhong) {
        this.tenPhong = tenPhong;
    }


    @Override
    public String toString() {
        return tenLop;
    }
}