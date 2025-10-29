package com.sgu.qlhs.dto;

public class PhongDTO {
    private int maPhong;
    private String tenPhong;
    private String loaiPhong;
    private int sucChua;
    private String viTri;

    public PhongDTO() {
    }

    public PhongDTO(int maPhong, String tenPhong) {
        this.maPhong = maPhong;
        this.tenPhong = tenPhong;
        this.loaiPhong = "";
        this.sucChua = 0;
        this.viTri = "";
    }

    public PhongDTO(int maPhong, String tenPhong, String loaiPhong, int sucChua, String viTri) {
        this.maPhong = maPhong;
        this.tenPhong = tenPhong;
        this.loaiPhong = loaiPhong;
        this.sucChua = sucChua;
        this.viTri = viTri;
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

    public String getLoaiPhong() {
        return loaiPhong;
    }

    public void setLoaiPhong(String loaiPhong) {
        this.loaiPhong = loaiPhong;
    }

    public int getSucChua() {
        return sucChua;
    }

    public void setSucChua(int sucChua) {
        this.sucChua = sucChua;
    }

    public String getViTri() {
        return viTri;
    }

    public void setViTri(String viTri) {
        this.viTri = viTri;
    }

    @Override
    public String toString() {
        return "PhongDTO{" + "maPhong=" + maPhong + ", tenPhong='" + tenPhong + '\'' + ", loaiPhong='" + loaiPhong
                + '\'' + ", sucChua=" + sucChua + ", viTri='" + viTri + '\'' + '}';
    }
}
