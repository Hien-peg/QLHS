package com.sgu.qlhs.dto;

import java.sql.Date;

public class ChuNhiemDTO {
    private int maCN;
    private int maGV;
    private int maLop;
    private Date ngayNhanNhiem;
    private Date ngayKetThuc;

    // Thông tin hiển thị
    private String tenGV;
    private String tenLop;

    public int getMaCN() { return maCN; }
    public void setMaCN(int maCN) { this.maCN = maCN; }

    public int getMaGV() { return maGV; }
    public void setMaGV(int maGV) { this.maGV = maGV; }

    public int getMaLop() { return maLop; }
    public void setMaLop(int maLop) { this.maLop = maLop; }

    public Date getNgayNhanNhiem() { return ngayNhanNhiem; }
    public void setNgayNhanNhiem(Date ngayNhanNhiem) { this.ngayNhanNhiem = ngayNhanNhiem; }

    public Date getNgayKetThuc() { return ngayKetThuc; }
    public void setNgayKetThuc(Date ngayKetThuc) { this.ngayKetThuc = ngayKetThuc; }

    public String getTenGV() { return tenGV; }
    public void setTenGV(String tenGV) { this.tenGV = tenGV; }

    public String getTenLop() { return tenLop; }
    public void setTenLop(String tenLop) { this.tenLop = tenLop; }
}
