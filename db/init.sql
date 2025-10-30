-- === KHỞI TẠO DB ===
CREATE DATABASE QLHS CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE QLHS;

-- === BẢNG DANH MỤC / THỰC THỂ CHÍNH ===
CREATE TABLE PhongHoc (
    MaPhong INT PRIMARY KEY AUTO_INCREMENT,
    TenPhong VARCHAR(50),
    LoaiPhong VARCHAR(50),
    SucChua INT,
    ViTri VARCHAR(100)
);

CREATE TABLE Lop (
    MaLop INT PRIMARY KEY AUTO_INCREMENT,
    TenLop VARCHAR(50),
    Khoi INT,
    MaPhong INT NULL,
    CONSTRAINT fk_lop_phong FOREIGN KEY (MaPhong) REFERENCES PhongHoc (MaPhong) ON DELETE SET NULL ON UPDATE CASCADE
);

CREATE TABLE NienKhoa (
    MaNK INT PRIMARY KEY AUTO_INCREMENT,
    NamBatDau INT NOT NULL,
    NamKetThuc INT NOT NULL
);

CREATE TABLE GiaoVien (
    MaGV INT PRIMARY KEY AUTO_INCREMENT,
    HoTen VARCHAR(100),
    NgaySinh DATE,
    GioiTinh VARCHAR(10),
    SoDienThoai VARCHAR(15),
    Email VARCHAR(100)
);

CREATE TABLE MonHoc (
    MaMon INT PRIMARY KEY AUTO_INCREMENT,
    TenMon VARCHAR(100),
    SoTiet INT,
    GhiChu TEXT DEFAULT NULL
);

CREATE TABLE HocSinh (
    MaHS INT PRIMARY KEY AUTO_INCREMENT,
    HoTen VARCHAR(100),
    NgaySinh DATE,
    GioiTinh VARCHAR(10),
    DiaChi VARCHAR(255),
    SoDienThoai VARCHAR(15),
    Email VARCHAR(100),
    MaLop INT NULL,
    CONSTRAINT fk_hs_lop FOREIGN KEY (MaLop) REFERENCES Lop (MaLop) ON DELETE SET NULL ON UPDATE CASCADE
);

-- === QUAN HỆ / NGHIỆP VỤ ===
CREATE TABLE PhuHuynh (
    MaPH INT PRIMARY KEY AUTO_INCREMENT,
    HoTen VARCHAR(100),
    SoDienThoai VARCHAR(15),
    Email VARCHAR(100),
    DiaChi VARCHAR(255)
);

CREATE TABLE HocSinh_PhuHuynh (
    MaHS INT,
    MaPH INT,
    QuanHe VARCHAR(50),
    PRIMARY KEY (MaHS, MaPH),
    CONSTRAINT fk_hsph_hs FOREIGN KEY (MaHS) REFERENCES HocSinh (MaHS) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_hsph_ph FOREIGN KEY (MaPH) REFERENCES PhuHuynh (MaPH) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE DanhGia (
    MaDG INT PRIMARY KEY AUTO_INCREMENT,
    MaHS INT,
    Loai VARCHAR(50), -- 'KhenThuong' / 'KyLuat'
    TieuDe VARCHAR(100),
    NoiDung TEXT,
    NgayApDung DATE,
    CONSTRAINT fk_dg_hs FOREIGN KEY (MaHS) REFERENCES HocSinh (MaHS) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE PhanCongDay (
    MaPC INT PRIMARY KEY AUTO_INCREMENT,
    MaGV INT,
    MaMon INT,
    MaLop INT,
    MaNK INT,
    HocKy INT,
    CONSTRAINT fk_pc_gv FOREIGN KEY (MaGV) REFERENCES GiaoVien (MaGV) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_pc_mon FOREIGN KEY (MaMon) REFERENCES MonHoc (MaMon) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_pc_lop FOREIGN KEY (MaLop) REFERENCES Lop (MaLop) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_pc_nk FOREIGN KEY (MaNK) REFERENCES NienKhoa (MaNK) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE ChuNhiem (
    MaCN INT PRIMARY KEY AUTO_INCREMENT,
    MaGV INT,
    MaLop INT,
    MaNK INT,
    NgayNhanNhiem DATE,
    NgayKetThuc DATE,
    CONSTRAINT fk_cn_gv FOREIGN KEY (MaGV) REFERENCES GiaoVien (MaGV) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_cn_lop FOREIGN KEY (MaLop) REFERENCES Lop (MaLop) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_cn_nk FOREIGN KEY (MaNK) REFERENCES NienKhoa (MaNK) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE ThoiKhoaBieu (
    MaTKB INT PRIMARY KEY AUTO_INCREMENT, -- Mã thời khóa biểu
    MaLop INT NOT NULL, -- Lớp áp dụng (khóa ngoại -> Lop)
    MaGV INT NOT NULL, -- Giáo viên dạy (khóa ngoại -> GiaoVien)
    MaMon INT NOT NULL, -- Môn học (khóa ngoại -> MonHoc)
    MaPhong INT NOT NULL, -- Phòng học (khóa ngoại -> PhongHoc)
    HocKy VARCHAR(10) NOT NULL, -- Học kỳ (VD: HK1, HK2)
    NamHoc VARCHAR(9) NOT NULL, -- Năm học (VD: 2024-2025)
    ThuTrongTuan VARCHAR(10) NOT NULL, -- Thứ trong tuần (VD: Thứ 2, Thứ 3,...)
    TietBatDau INT NOT NULL, -- Tiết bắt đầu (VD: 1)
    TietKetThuc INT NOT NULL, -- Tiết kết thúc (VD: 3)
    TrangThai TINYINT DEFAULT 1, -- 1: hoạt động, 0: xóa mềm
    NgayTao DATETIME DEFAULT CURRENT_TIMESTAMP, -- Ngày tạo bản ghi
    NgayCapNhat DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (MaLop) REFERENCES Lop (MaLop) ON UPDATE CASCADE,
    FOREIGN KEY (MaGV) REFERENCES GiaoVien (MaGV) ON UPDATE CASCADE,
    FOREIGN KEY (MaMon) REFERENCES MonHoc (MaMon) ON UPDATE CASCADE,
    FOREIGN KEY (MaPhong) REFERENCES PhongHoc (MaPhong) ON UPDATE CASCADE
);

-- === BẢNG ĐIỂM (GỘP) + CỘT SINH ===
CREATE TABLE Diem (
    MaDiem INT PRIMARY KEY AUTO_INCREMENT,
    MaHS INT,
    MaMon INT,
    HocKy INT,
    MaNK INT,
    DiemMieng DECIMAL(3, 1),
    Diem15p DECIMAL(3, 1),
    DiemGiuaKy DECIMAL(3, 1),
    DiemCuoiKy DECIMAL(3, 1),
    CONSTRAINT fk_diem_hs FOREIGN KEY (MaHS) REFERENCES HocSinh (MaHS) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_diem_mon FOREIGN KEY (MaMon) REFERENCES MonHoc (MaMon) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_diem_nk FOREIGN KEY (MaNK) REFERENCES NienKhoa (MaNK) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT uq_diem UNIQUE (MaHS, MaMon, HocKy, MaNK)
);

-- Cột sinh: TB & Xếp loại (theo trọng số thành phần 10/20/30/40)
ALTER TABLE Diem
ADD COLUMN DiemTB DECIMAL(3, 1) GENERATED ALWAYS AS (
    CASE
        WHEN DiemMieng IS NULL
        OR Diem15p IS NULL
        OR DiemGiuaKy IS NULL
        OR DiemCuoiKy IS NULL THEN NULL
        ELSE ROUND(
            DiemMieng * 0.10 + Diem15p * 0.20 + DiemGiuaKy * 0.30 + DiemCuoiKy * 0.40,
            1
        )
    END
) STORED,
ADD COLUMN XepLoai VARCHAR(20) GENERATED ALWAYS AS (
    CASE
        WHEN DiemTB IS NULL THEN NULL
        WHEN DiemTB >= 8.0 THEN 'Giỏi'
        WHEN DiemTB >= 6.5 THEN 'Khá'
        WHEN DiemTB >= 5.0 THEN 'Trung bình'
        ELSE 'Yếu'
    END
) VIRTUAL;

-- === TRỌNG SỐ NĂM (KHÔNG DÙNG BIẾN PHIÊN) ===
CREATE TABLE TrongSoNam (
    MaNK INT PRIMARY KEY,
    wHK1 DECIMAL(4, 3) NOT NULL,
    wHK2 DECIMAL(4, 3) NOT NULL,
    CONSTRAINT fk_ts_nk FOREIGN KEY (MaNK) REFERENCES NienKhoa (MaNK) ON DELETE CASCADE ON UPDATE CASCADE
);

-- TRIGGER: mọi niên khóa mới thêm vào sẽ tự có 40/60
DELIMITER /
/

CREATE TRIGGER trg_NienKhoa_DefaultTrongSo
AFTER INSERT ON NienKhoa
FOR EACH ROW
BEGIN
  INSERT INTO TrongSoNam (MaNK, wHK1, wHK2)
  VALUES (NEW.MaNK, 0.4, 0.6)
  ON DUPLICATE KEY UPDATE wHK1 = VALUES(wHK1), wHK2 = VALUES(wHK2);
END
/
/

DELIMITER;

-- =================================================================================================================================================
-- === DỮ LIỆU MẪU (ĐÃ LỌC CHO CẤP 3 VÀ THAY TÊN A, B, C...) ===
-- =================================================================================================================================================

-- === DỮ LIỆU MẪU NGẮN GỌN ===
INSERT INTO
    PhongHoc (
        MaPhong,
        TenPhong,
        LoaiPhong,
        SucChua,
        ViTri
    )
VALUES (
        101,
        'P101',
        'Lý thuyết',
        45,
        'Tầng 1'
    ),
    (
        102,
        'P102',
        'Lý thuyết',
        45,
        'Tầng 1'
    ),
    (
        201,
        'P201',
        'Lý thuyết',
        45,
        'Tầng 2'
    ),
    (
        301,
        'P301',
        'Lý thuyết',
        45,
        'Tầng 3'
    ),
    (
        202,
        'P202',
        'Thí nghiệm',
        30,
        'Tầng 2'
    );

INSERT INTO
    Lop (MaLop, TenLop, Khoi, MaPhong)
VALUES (1, '10A1', 10, 101),
    (2, '10A2', 10, 102),
    (3, '11A1', 11, 201),
    (4, '12A1', 12, 301),
    (5, '10A3', 10, 102),
    (6, '11A2', 11, 201),
    (7, '12A2', 12, 301);

-- *** ĐÃ SỬA LẠI TÊN GIÁO VIÊN CHO THỰC TẾ ***
INSERT INTO
    GiaoVien (
        MaGV,
        HoTen,
        SoDienThoai,
        Email,
        NgaySinh,
        GioiTinh
    )
VALUES (
        101,
        'Trần Quốc Tuấn',
        '0901111222',
        'tuantq@gv.example.com',
        '1980-05-20',
        'Nam'
    ),
    (
        102,
        'Lê Thị Kim Ngân',
        '0902222333',
        'nganltk@gv.example.com',
        '1985-11-15',
        'Nữ'
    ),
    (
        103,
        'Nguyễn Hữu Thắng',
        '0903333444',
        'thangnh@gv.example.com',
        '1978-02-28',
        'Nam'
    ),
    (
        104,
        'Hoàng Minh Dũng',
        '0904444555',
        'dunghm@gv.example.com',
        '1982-09-09',
        'Nam'
    ),
    (
        105,
        'Bùi Thanh Mai',
        '0905555666',
        'maibt@gv.example.com',
        '1987-03-12',
        'Nữ'
    ),
    (
        106,
        'Đặng Mai Lan',
        '0906666777',
        'landm@gv.example.com',
        '1986-07-30',
        'Nữ'
    ),
    (
        107,
        'Phan Việt Anh',
        '0907777888',
        'anhpv@gv.example.com',
        '1979-01-18',
        'Nam'
    ),
    (
        108,
        'Ngô Hải Long',
        '0908888999',
        'longnh@gv.example.com',
        '1983-04-22',
        'Nam'
    ),
    (
        109,
        'Võ Thu Thảo',
        '0910000111',
        'thaovt@gv.example.com',
        '1984-12-05',
        'Nữ'
    ),
    (
        110,
        'Hồ Minh Quân',
        '0911111222',
        'quanhm@gv.example.com',
        '1990-02-10',
        'Nam'
    ),
    (
        111,
        'Đoàn Công Thành',
        '0912222333',
        'thanhdc@gv.example.com',
        '1985-05-15',
        'Nam'
    ),
    (
        112,
        'Lý Anh Dũng',
        '0913333444',
        'dungla@gv.example.com',
        '1981-08-08',
        'Nam'
    ),
    (
        113,
        'Nguyễn Đức Quang',
        '0914444777',
        'quangnd@gv.example.com',
        '1988-02-10',
        'Nam'
    ),
    (
        114,
        'Phạm Thị Hòa',
        '0915555888',
        'hoapt@gv.example.com',
        '1989-06-12',
        'Nữ'
    ),
    (
        115,
        'Trần Văn Lực',
        '0916666999',
        'luctv@gv.example.com',
        '1984-03-15',
        'Nam'
    );

INSERT INTO
    HocSinh (
        MaHS,
        HoTen,
        NgaySinh,
        GioiTinh,
        MaLop,
        DiaChi,
        SoDienThoai,
        Email
    )
VALUES (
        1,
        'Bùi Tấn Trường',
        '2007-09-01',
        'Nam',
        1,
        '123 Đường ABC, Quận 1',
        '0123456789',
        'truongbt@example.com'
    ),
    (
        2,
        'Trần Thùy Linh',
        '2007-12-11',
        'Nữ',
        1,
        '456 Đường XYZ, Quận 2',
        '0987654321',
        'linhtt@example.com'
    ),
    (
        3,
        'Phạm Minh Khang',
        '2007-05-10',
        'Nam',
        2,
        '789 Đường DEF, Quận 3',
        '0901234567',
        'khangpm@example.com'
    ),
    (
        4,
        'Lê Thu Thảo',
        '2007-03-22',
        'Nữ',
        3,
        '111 Đường GHI, Quận 4',
        '0912345678',
        'thaolt@example.com'
    ),
    (
        5,
        'Đỗ Hải Phong',
        '2006-10-09',
        'Khác',
        4,
        '222 Đường JKL, Quận 5',
        '0923456789',
        'phongdh@example.com'
    ),
    (
        6,
        'Phạm Gia Hân',
        '2007-04-20',
        'Nữ',
        5,
        'Q1',
        '0933333001',
        'hanpg@example.com'
    ),
    (
        7,
        'Ngô Nhật Quang',
        '2007-09-15',
        'Nam',
        5,
        'Q2',
        '0933333002',
        'quangnn@example.com'
    ),
    (
        8,
        'Vũ Hoàng Khang',
        '2006-10-10',
        'Nam',
        6,
        'Q3',
        '0933333003',
        'khangvh@example.com'
    ),
    (
        9,
        'Trần Minh Khôi',
        '2005-12-12',
        'Nam',
        7,
        'Q5',
        '0933333004',
        'khoitm@example.com'
    );

INSERT INTO
    MonHoc (MaMon, TenMon, SoTiet, GhiChu)
VALUES (101, 'Toán', 90, NULL),
    (102, 'Văn', 90, NULL),
    (103, 'Anh', 90, NULL),
    (104, 'Vật lý', 70, NULL),
    (
        105,
        'Hóa học',
        70,
        'Bắt đầu từ khối 8'
    ),
    (106, 'Sinh học', 70, NULL),
    (107, 'Lịch sử', 70, NULL),
    (108, 'Địa lý', 70, NULL),
    (
        109,
        'Giáo dục công dân',
        52,
        'GDCD'
    ),
    (110, 'Tin học', 70, NULL),
    (111, 'Công nghệ', 70, NULL),
    (
        112,
        'Thể dục',
        70,
        'Giáo dục thể chất'
    );

INSERT INTO
    NienKhoa (MaNK, NamBatDau, NamKetThuc)
VALUES (1, 2024, 2025);

INSERT INTO
    Diem (
        MaHS,
        MaMon,
        HocKy,
        MaNK,
        DiemMieng,
        Diem15p,
        DiemGiuaKy,
        DiemCuoiKy
    )
VALUES (
        1,
        101,
        1,
        1,
        8.5,
        7.0,
        8.0,
        8.5
    ),
    (
        2,
        102,
        1,
        1,
        7.5,
        8.0,
        8.0,
        8.5
    ),
    (
        3,
        103,
        2,
        1,
        8.0,
        8.0,
        8.0,
        9.0
    ),
    -- 10A3 (MaLop=5), học sinh 6 và 7
    (
        6,
        101,
        1,
        1,
        8.0,
        8.0,
        7.5,
        8.5
    ),
    (
        6,
        102,
        1,
        1,
        7.5,
        8.0,
        8.0,
        8.0
    ),
    (
        7,
        101,
        1,
        1,
        7.0,
        7.0,
        7.0,
        7.5
    ),
    (
        7,
        102,
        1,
        1,
        8.5,
        8.0,
        8.0,
        8.5
    ),
    (
        6,
        101,
        2,
        1,
        8.5,
        8.0,
        8.0,
        9.0
    ),
    (
        6,
        102,
        2,
        1,
        8.0,
        8.0,
        8.0,
        8.5
    ),
    (
        7,
        101,
        2,
        1,
        7.5,
        7.0,
        7.5,
        8.0
    ),
    (
        7,
        102,
        2,
        1,
        8.0,
        8.0,
        8.0,
        8.5
    ),
    -- 11A2 (MaLop=6), học sinh 8
    (
        8,
        101,
        1,
        1,
        7.5,
        7.0,
        7.0,
        8.0
    ),
    (
        8,
        102,
        1,
        1,
        8.0,
        8.0,
        8.0,
        8.5
    ),
    (
        8,
        103,
        1,
        1,
        7.0,
        7.0,
        7.5,
        8.0
    ),
    (
        8,
        101,
        2,
        1,
        8.0,
        7.5,
        8.0,
        8.5
    ),
    (
        8,
        102,
        2,
        1,
        8.5,
        8.0,
        8.5,
        9.0
    ),
    (
        8,
        103,
        2,
        1,
        7.5,
        7.0,
        7.5,
        8.0
    ),
    -- 12A2 (MaLop=7), học sinh 9
    (
        9,
        101,
        1,
        1,
        7.5,
        8.0,
        8.0,
        8.5
    ),
    (
        9,
        102,
        1,
        1,
        8.0,
        8.0,
        8.0,
        8.5
    ),
    (
        9,
        103,
        1,
        1,
        7.0,
        7.5,
        7.5,
        8.0
    ),
    (
        9,
        101,
        2,
        1,
        8.5,
        8.5,
        8.0,
        9.0
    ),
    (
        9,
        102,
        2,
        1,
        8.0,
        8.0,
        8.0,
        8.5
    ),
    (
        9,
        103,
        2,
        1,
        8.0,
        7.5,
        8.0,
        8.5
    );

INSERT INTO
    PhuHuynh (
        HoTen,
        SoDienThoai,
        Email,
        DiaChi
    )
VALUES (
        'Bà Nguyễn Thị Lan',
        '0899999000',
        'phlan@example.com',
        'Địa chỉ A'
    ),
    (
        'Ông Trần Văn Hùng',
        '0899999111',
        'phhung@example.com',
        'Địa chỉ B'
    ),
    (
        'Bà Lê Thị Xuân',
        '0935555001',
        'xuanlt@example.com',
        'Q1'
    ),
    (
        'Ông Phan Văn Tài',
        '0935555002',
        'taipv@example.com',
        'Q2'
    ),
    (
        'Bà Nguyễn Mỹ An',
        '0935555003',
        'anmy@example.com',
        'Q3'
    );

INSERT INTO
    HocSinh_PhuHuynh (MaHS, MaPH, QuanHe)
VALUES (1, 1, 'Mẹ'),
    (2, 2, 'Bố'),
    (6, 3, 'Mẹ'),
    (7, 4, 'Bố'),
    (8, 5, 'Mẹ');

INSERT INTO
    ChuNhiem (
        MaGV,
        MaLop,
        MaNK,
        NgayNhanNhiem
    )
VALUES (101, 1, 1, '2024-08-01'),
    (113, 5, 1, '2024-08-01'),
    (114, 6, 1, '2024-08-01'),
    (115, 7, 1, '2024-08-01');

INSERT INTO
    PhanCongDay (
        MaGV,
        MaMon,
        MaLop,
        MaNK,
        HocKy
    )
VALUES (101, 101, 1, 1, 1),
    (102, 102, 1, 1, 1),
    (101, 101, 5, 1, 1),
    (102, 102, 5, 1, 1),
    (103, 103, 5, 1, 1),
    (101, 101, 6, 1, 1),
    (102, 102, 6, 1, 1),
    (103, 103, 6, 1, 1),
    (101, 101, 7, 1, 1),
    (102, 102, 7, 1, 1),
    (103, 103, 7, 1, 1);

INSERT INTO
    ThoiKhoaBieu (
        MaLop,
        MaGV,
        MaMon,
        MaPhong,
        HocKy,
        NamHoc,
        ThuTrongTuan,
        TietBatDau,
        TietKetThuc
    )
VALUES
    -- HK I
    -- 10A1 (MaLop=1)
    (
        1,
        101,
        101,
        101,
        'HK1',
        '2024-2025',
        'Thứ 2',
        1,
        2
    ), -- Toán
    (
        1,
        102,
        102,
        101,
        'HK1',
        '2024-2025',
        'Thứ 2',
        3,
        4
    ), -- Văn
    (
        1,
        103,
        103,
        101,
        'HK1',
        '2024-2025',
        'Thứ 3',
        1,
        2
    ), -- Anh
    (
        1,
        104,
        104,
        101,
        'HK1',
        '2024-2025',
        'Thứ 3',
        3,
        4
    ), -- Vật lý
    (
        1,
        106,
        106,
        101,
        'HK1',
        '2024-2025',
        'Thứ 4',
        1,
        2
    ), -- Sinh
    -- 10A2 (MaLop=2)
    (
        2,
        101,
        101,
        102,
        'HK1',
        '2024-2025',
        'Thứ 2',
        1,
        2
    ), -- Toán
    (
        2,
        102,
        102,
        102,
        'HK1',
        '2024-2025',
        'Thứ 2',
        3,
        4
    ), -- Văn
    (
        2,
        103,
        103,
        102,
        'HK1',
        '2024-2025',
        'Thứ 3',
        1,
        2
    ), -- Anh
    (
        2,
        104,
        104,
        102,
        'HK1',
        '2024-2025',
        'Thứ 3',
        3,
        4
    ), -- Vật lý
    (
        2,
        105,
        105,
        102,
        'HK1',
        '2024-2025',
        'Thứ 4',
        1,
        2
    ), -- Hóa
    -- 11A1 (MaLop=3)
    (
        3,
        101,
        101,
        201,
        'HK1',
        '2024-2025',
        'Thứ 2',
        1,
        2
    ), -- Toán
    (
        3,
        102,
        102,
        201,
        'HK1',
        '2024-2025',
        'Thứ 2',
        3,
        4
    ), -- Văn
    (
        3,
        103,
        103,
        201,
        'HK1',
        '2024-2025',
        'Thứ 3',
        1,
        2
    ), -- Anh
    (
        3,
        104,
        104,
        201,
        'HK1',
        '2024-2025',
        'Thứ 3',
        3,
        4
    ), -- Vật lý
    (
        3,
        105,
        105,
        201,
        'HK1',
        '2024-2025',
        'Thứ 4',
        1,
        2
    ), -- Hóa

-- HK II
-- 10A1 (MaLop=1)
(
    1,
    101,
    101,
    101,
    'HK2',
    '2024-2025',
    'Thứ 2',
    1,
    2
),
(
    1,
    102,
    102,
    101,
    'HK2',
    '2024-2025',
    'Thứ 3',
    3,
    4
),
(
    1,
    103,
    103,
    101,
    'HK2',
    '2024-2025',
    'Thứ 4',
    1,
    2
),

-- 10A2 (MaLop=2)
(
    2,
    104,
    104,
    102,
    'HK2',
    '2024-2025',
    'Thứ 2',
    1,
    2
),
(
    2,
    105,
    105,
    102,
    'HK2',
    '2024-2025',
    'Thứ 3',
    3,
    4
),
(
    2,
    103,
    103,
    102,
    'HK2',
    '2024-2025',
    'Thứ 4',
    1,
    2
),

-- 11A1 (MaLop=3)
(
    3,
    101,
    101,
    201,
    'HK2',
    '2024-2025',
    'Thứ 2',
    1,
    2
),
(
    3,
    102,
    102,
    201,
    'HK2',
    '2024-2025',
    'Thứ 3',
    3,
    4
),
(
    3,
    103,
    103,
    201,
    'HK2',
    '2024-2025',
    'Thứ 4',
    1,
    2
);

-- === VIEW PHỤC VỤ HIỂN THỊ/BÁO CÁO ===

-- Điểm từng môn theo HK (từ Diem)
CREATE OR REPLACE VIEW v_DiemMon_HK AS
SELECT
    d.MaDiem,
    d.MaHS,
    hs.HoTen AS TenHS,
    d.MaMon,
    m.TenMon,
    d.HocKy,
    d.MaNK,
    d.DiemMieng,
    d.Diem15p,
    d.DiemGiuaKy,
    d.DiemCuoiKy,
    d.DiemTB,
    d.XepLoai
FROM
    Diem d
    JOIN HocSinh hs ON hs.MaHS = d.MaHS
    JOIN MonHoc m ON m.MaMon = d.MaMon;

-- TB học kỳ (trung bình các môn theo HS)
CREATE OR REPLACE VIEW v_TBHocKy_TheoHS AS
SELECT
    d.MaHS,
    hs.HoTen AS TenHS,
    d.MaNK,
    d.HocKy,
    ROUND(AVG(d.DiemTB), 1) AS DiemTBHK,
    CASE
        WHEN AVG(d.DiemTB) IS NULL THEN NULL
        WHEN AVG(d.DiemTB) >= 8.0 THEN 'Giỏi'
        WHEN AVG(d.DiemTB) >= 6.5 THEN 'Khá'
        WHEN AVG(d.DiemTB) >= 5.0 THEN 'Trung bình'
        ELSE 'Yếu'
    END AS XepLoaiHK
FROM Diem d
    JOIN HocSinh hs ON hs.MaHS = d.MaHS
GROUP BY
    d.MaHS,
    hs.HoTen,
    d.MaNK,
    d.HocKy;

-- TB cả năm theo TỪNG MÔN (kết hợp HK1/HK2 theo trọng số trong bảng TrongSoNam)
CREATE OR REPLACE VIEW v_DiemMon_CaNam AS
SELECT
    hk1.MaHS,
    hs.HoTen AS TenHS,
    hk1.MaMon,
    m.TenMon,
    hk1.MaNK,
    hk1.DiemTB AS TB_HK1,
    hk2.DiemTB AS TB_HK2,
    ROUND(
        CASE
            WHEN hk1.DiemTB IS NOT NULL
            AND hk2.DiemTB IS NOT NULL THEN hk1.DiemTB * COALESCE(ts.wHK1, 0.4) + hk2.DiemTB * COALESCE(ts.wHK2, 0.6)
            WHEN hk1.DiemTB IS NOT NULL THEN hk1.DiemTB
            WHEN hk2.DiemTB IS NOT NULL THEN hk2.DiemTB
            ELSE NULL
        END,
        1
    ) AS DiemTBCaNam,
    CASE
        WHEN (
            hk1.DiemTB IS NULL
            AND hk2.DiemTB IS NULL
        ) THEN NULL
        WHEN (
            (
                hk1.DiemTB IS NOT NULL
                AND hk2.DiemTB IS NULL
                AND hk1.DiemTB >= 8.0
            )
            OR (
                hk1.DiemTB IS NULL
                AND hk2.DiemTB IS NOT NULL
                AND hk2.DiemTB >= 8.0
            )
            OR (
                hk1.DiemTB IS NOT NULL
                AND hk2.DiemTB IS NOT NULL
                AND (
                    hk1.DiemTB * COALESCE(ts.wHK1, 0.4) + hk2.DiemTB * COALESCE(ts.wHK2, 0.6)
                ) >= 8.0
            )
        ) THEN 'Giỏi'
        WHEN (
            (
                hk1.DiemTB IS NOT NULL
                AND hk2.DiemTB IS NULL
                AND hk1.DiemTB >= 6.5
            )
            OR (
                hk1.DiemTB IS NULL
                AND hk2.DiemTB IS NOT NULL
                AND hk2.DiemTB >= 6.5
            )
            OR (
                hk1.DiemTB IS NOT NULL
                AND hk2.DiemTB IS NOT NULL
                AND (
                    hk1.DiemTB * COALESCE(ts.wHK1, 0.4) + hk2.DiemTB * COALESCE(ts.wHK2, 0.6)
                ) >= 6.5
            )
        ) THEN 'Khá'
        WHEN (
            (
                hk1.DiemTB IS NOT NULL
                AND hk2.DiemTB IS NULL
                AND hk1.DiemTB >= 5.0
            )
            OR (
                hk1.DiemTB IS NULL
                AND hk2.DiemTB IS NOT NULL
                AND hk2.DiemTB >= 5.0
            )
            OR (
                hk1.DiemTB IS NOT NULL
                AND hk2.DiemTB IS NOT NULL
                AND (
                    hk1.DiemTB * COALESCE(ts.wHK1, 0.4) + hk2.DiemTB * COALESCE(ts.wHK2, 0.6)
                ) >= 5.0
            )
        ) THEN 'Trung bình'
        ELSE 'Yếu'
    END AS XepLoaiCaNam
FROM (
        SELECT MaHS, MaMon, MaNK, DiemTB
        FROM Diem
        WHERE
            HocKy = 1
    ) hk1
    LEFT JOIN (
        SELECT MaHS, MaMon, MaNK, DiemTB
        FROM Diem
        WHERE
            HocKy = 2
    ) hk2 ON hk1.MaHS = hk2.MaHS
    AND hk1.MaMon = hk2.MaMon
    AND hk1.MaNK = hk2.MaNK
    LEFT JOIN HocSinh hs ON hs.MaHS = hk1.MaHS
    LEFT JOIN MonHoc m ON m.MaMon = hk1.MaMon
    LEFT JOIN TrongSoNam ts ON ts.MaNK = hk1.MaNK
UNION
SELECT
    hk2.MaHS,
    hs.HoTen AS TenHS,
    hk2.MaMon,
    m.TenMon,
    hk2.MaNK,
    NULL AS TB_HK1,
    hk2.DiemTB AS TB_HK2,
    ROUND(hk2.DiemTB, 1) AS DiemTBCaNam,
    CASE
        WHEN hk2.DiemTB IS NULL THEN NULL
        WHEN hk2.DiemTB >= 8.0 THEN 'Giỏi'
        WHEN hk2.DiemTB >= 6.5 THEN 'Khá'
        WHEN hk2.DiemTB >= 5.0 THEN 'Trung bình'
        ELSE 'Yếu'
    END AS XepLoaiCaNam
FROM (
        SELECT MaHS, MaMon, MaNK, DiemTB
        FROM Diem
        WHERE
            HocKy = 2
    ) hk2
    LEFT JOIN (
        SELECT MaHS, MaMon, MaNK, DiemTB
        FROM Diem
        WHERE
            HocKy = 1
    ) hk1 ON hk2.MaHS = hk1.MaHS
    AND hk2.MaMon = hk1.MaMon
    AND hk2.MaNK = hk1.MaNK
    LEFT JOIN HocSinh hs ON hs.MaHS = hk2.MaHS
    LEFT JOIN MonHoc m ON m.MaMon = hk2.MaMon
    LEFT JOIN TrongSoNam ts ON ts.MaNK = hk2.MaNK
WHERE
    hk1.MaHS IS NULL;

-- TB cả năm theo HỌC SINH (trung bình các môn)
CREATE OR REPLACE VIEW v_TBCaNam_TheoHS AS
SELECT
    MaHS,
    TenHS,
    MaNK,
    ROUND(AVG(DiemTBCaNam), 1) AS DiemTBCaNam,
    CASE
        WHEN AVG(DiemTBCaNam) IS NULL THEN NULL
        WHEN AVG(DiemTBCaNam) >= 8.0 THEN 'Giỏi'
        WHEN AVG(DiemTBCaNam) >= 6.5 THEN 'Khá'
        WHEN AVG(DiemTBCaNam) >= 5.0 THEN 'Trung bình'
        ELSE 'Yếu'
    END AS XepLoaiCaNam
FROM v_DiemMon_CaNam
GROUP BY
    MaHS,
    TenHS,
    MaNK;

-- === INDEX GỢI Ý (tăng tốc truy vấn) ===
CREATE INDEX idx_diem_hs_mon_hk_nk ON Diem (MaHS, MaMon, HocKy, MaNK);

CREATE INDEX idx_diem_hs_nk_hk ON Diem (MaHS, MaNK, HocKy);