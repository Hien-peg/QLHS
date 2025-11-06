package com.sgu.qlhs.dto;

public class PhuHuynhDTO {
    private int maPH;
    private String hoTen;
    private String soDienThoai;
    private String email;
    private String diaChi;
    private String quanHe; // "Cha", "Mẹ", "Giám hộ"...

    public int getMaPH() { return maPH; }
    public void setMaPH(int maPH) { this.maPH = maPH; }

    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }

    public String getSoDienThoai() { return soDienThoai; }
    public void setSoDienThoai(String soDienThoai) { this.soDienThoai = soDienThoai; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDiaChi() { return diaChi; }
    public void setDiaChi(String diaChi) { this.diaChi = diaChi; }

    public String getQuanHe() { return quanHe; }
    public void setQuanHe(String quanHe) { this.quanHe = quanHe; }
}