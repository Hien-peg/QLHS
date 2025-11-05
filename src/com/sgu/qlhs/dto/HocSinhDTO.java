package com.sgu.qlhs.dto;

import java.sql.Date;

public class HocSinhDTO {
    private int maHS;
    private String hoTen;
    private Date ngaySinh;
    private String gioiTinh;

    // Bổ sung theo init/DB
    private int maLop;
    private int maND;

    // Tuỳ anh vẫn muốn hiển thị tên lớp:
    private String tenLop;

    // ===== Getter/Setter =====
    public int getMaHS() { return maHS; }
    public void setMaHS(int maHS) { this.maHS = maHS; }

    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }

    public Date getNgaySinh() { return ngaySinh; }
    public void setNgaySinh(Date ngaySinh) { this.ngaySinh = ngaySinh; }

    public String getGioiTinh() { return gioiTinh; }
    public void setGioiTinh(String gioiTinh) { this.gioiTinh = gioiTinh; }

    public int getMaLop() { return maLop; }
    public void setMaLop(int maLop) { this.maLop = maLop; }

    public int getMaND() { return maND; }
    public void setMaND(int maND) { this.maND = maND; }

    public String getTenLop() { return tenLop; }
    public void setTenLop(String tenLop) { this.tenLop = tenLop; }
}
