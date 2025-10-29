-- === KHỞI TẠO DB ===
CREATE DATABASE IF NOT EXISTS QLHS_New CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE QLHS_New;

-- === BẢNG DANH MỤC / THỰC THỂ CHÍNH ===
CREATE TABLE IF NOT EXISTS PhongHoc (
    MaPhong INT PRIMARY KEY AUTO_INCREMENT,
    TenPhong VARCHAR(50),
    LoaiPhong VARCHAR(50),
    SucChua INT,
    ViTri VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS Lop (
    MaLop INT PRIMARY KEY AUTO_INCREMENT,
    TenLop VARCHAR(50),
    Khoi INT,
    MaPhong INT NULL,
    CONSTRAINT fk_lop_phong FOREIGN KEY (MaPhong) REFERENCES PhongHoc (MaPhong) ON DELETE SET NULL ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS NienKhoa (
    MaNK INT PRIMARY KEY AUTO_INCREMENT,
    NamBatDau INT NOT NULL,
    NamKetThuc INT NOT NULL
);

CREATE TABLE IF NOT EXISTS GiaoVien (
    MaGV INT PRIMARY KEY AUTO_INCREMENT,
    HoTen VARCHAR(100),
    NgaySinh DATE,
    GioiTinh VARCHAR(10),
    SoDienThoai VARCHAR(15),
    Email VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS MonHoc (
    MaMon INT PRIMARY KEY AUTO_INCREMENT,
    TenMon VARCHAR(100),
    SoTiet INT,
    GhiChu TEXT DEFAULT NULL
);

CREATE TABLE IF NOT EXISTS HocSinh (
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
CREATE TABLE IF NOT EXISTS PhuHuynh (
    MaPH INT PRIMARY KEY AUTO_INCREMENT,
    HoTen VARCHAR(100),
    SoDienThoai VARCHAR(15),
    Email VARCHAR(100),
    DiaChi VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS HocSinh_PhuHuynh (
    MaHS INT,
    MaPH INT,
    QuanHe VARCHAR(50),
    PRIMARY KEY (MaHS, MaPH),
    CONSTRAINT fk_hsph_hs FOREIGN KEY (MaHS) REFERENCES HocSinh (MaHS) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_hsph_ph FOREIGN KEY (MaPH) REFERENCES PhuHuynh (MaPH) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS DanhGia (
    MaDG INT PRIMARY KEY AUTO_INCREMENT,
    MaHS INT,
    Loai VARCHAR(50), -- 'KhenThuong' / 'KyLuat'
    TieuDe VARCHAR(100),
    NoiDung TEXT,
    NgayApDung DATE,
    CONSTRAINT fk_dg_hs FOREIGN KEY (MaHS) REFERENCES HocSinh (MaHS) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS PhanCongDay (
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

CREATE TABLE IF NOT EXISTS ChuNhiem (
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

CREATE TABLE IF NOT EXISTS ThoiKhoaBieu (
    MaTKB INT PRIMARY KEY AUTO_INCREMENT,
    MaPC INT,
    MaPhong INT,
    ThuTrongTuan VARCHAR(10),
    TietHoc INT,
    CONSTRAINT fk_tkb_pc FOREIGN KEY (MaPC) REFERENCES PhanCongDay (MaPC) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_tkb_phong FOREIGN KEY (MaPhong) REFERENCES PhongHoc (MaPhong) ON DELETE CASCADE ON UPDATE CASCADE
);

-- === BẢNG ĐIỂM (GỘP) + CỘT SINH ===
CREATE TABLE IF NOT EXISTS Diem (
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
CREATE TABLE IF NOT EXISTS TrongSoNam (
    MaNK INT PRIMARY KEY,
    wHK1 DECIMAL(4, 3) NOT NULL,
    wHK2 DECIMAL(4, 3) NOT NULL,
    CONSTRAINT fk_ts_nk FOREIGN KEY (MaNK) REFERENCES NienKhoa (MaNK) ON DELETE CASCADE ON UPDATE CASCADE
);

-- BACKFILL: gán mặc định 40/60 cho TẤT CẢ niên khóa đã tồn tại mà chưa có trọng số
INSERT INTO
    TrongSoNam (MaNK, wHK1, wHK2)
SELECT nk.MaNK, 0.4, 0.6
FROM NienKhoa nk
    LEFT JOIN TrongSoNam ts ON ts.MaNK = nk.MaNK
WHERE
    ts.MaNK IS NULL;

-- TRIGGER: mọi niên khóa mới thêm vào sẽ tự có 40/60
DELIMITER / /

CREATE TRIGGER trg_NienKhoa_DefaultTrongSo
AFTER INSERT ON NienKhoa
FOR EACH ROW
BEGIN
  INSERT INTO TrongSoNam (MaNK, wHK1, wHK2)
  VALUES (NEW.MaNK, 0.4, 0.6)
  ON DUPLICATE KEY UPDATE wHK1 = VALUES(wHK1), wHK2 = VALUES(wHK2);
END//

DELIMITER;

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
    (4, '12A1', 12, 301);

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
        'Phạm Văn C',
        '0901111222',
        'c@gv.example.com',
        '1980-05-20',
        'Nam'
    ),
    (
        102,
        'Lê Thị D',
        '0902222333',
        'd@gv.example.com',
        '1985-11-15',
        'Nữ'
    ),
    (
        103,
        'Nguyễn Hữu E',
        '0903333444',
        'e@gv.example.com',
        '1978-02-28',
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
        'Nguyễn Văn A',
        '2007-09-01',
        'Nam',
        1,
        '123 Đường ABC, Quận 1',
        '0123456789',
        'nguyenvana@example.com'
    ),
    (
        2,
        'Trần Thị B',
        '2007-12-11',
        'Nữ',
        1,
        '456 Đường XYZ, Quận 2',
        '0987654321',
        'tranthib@example.com'
    ),
    (
        3,
        'Phạm Minh C',
        '2007-05-10',
        'Nam',
        2,
        '789 Đường DEF, Quận 3',
        '0901234567',
        'phamminhc@example.com'
    ),
    (
        4,
        'Lê Thu D',
        '2007-03-22',
        'Nữ',
        3,
        '111 Đường GHI, Quận 4',
        '0912345678',
        'lethud@example.com'
    ),
    (
        5,
        'Đỗ Hải E',
        '2006-10-09',
        'Khác',
        4,
        '222 Đường JKL, Quận 5',
        '0923456789',
        'dohaie@example.com'
    );

INSERT INTO
    MonHoc (MaMon, TenMon, SoTiet)
VALUES (101, 'Toán', 90),
    (102, 'Văn', 90),
    (103, 'Anh', 90);

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
    );

INSERT INTO
    PhuHuynh (
        HoTen,
        SoDienThoai,
        Email,
        DiaChi
    )
VALUES (
        'Bà Nguyễn Thị X',
        '0899999000',
        'phx@example.com',
        'Địa chỉ A'
    ),
    (
        'Ông Trần Văn Y',
        '0899999111',
        'phy@example.com',
        'Địa chỉ B'
    );

INSERT INTO
    HocSinh_PhuHuynh (MaHS, MaPH, QuanHe)
VALUES (1, 1, 'Mẹ'),
    (2, 2, 'Bố');

INSERT INTO
    ChuNhiem (
        MaGV,
        MaLop,
        MaNK,
        NgayNhanNhiem
    )
VALUES (101, 1, 1, '2024-08-01');

INSERT INTO
    PhanCongDay (
        MaGV,
        MaMon,
        MaLop,
        MaNK,
        HocKy
    )
VALUES (101, 101, 1, 1, 1),
    (102, 102, 1, 1, 1);

INSERT INTO
    ThoiKhoaBieu (
        MaPC,
        MaPhong,
        ThuTrongTuan,
        TietHoc
    )
VALUES (1, 101, 'Thứ 2', 1),
    (2, 102, 'Thứ 3', 2);

/* =========================
1) BỔ SUNG PHÒNG HỌC
========================= */
INSERT INTO
    PhongHoc (
        MaPhong,
        TenPhong,
        LoaiPhong,
        SucChua,
        ViTri
    )
VALUES (
        103,
        'P103',
        'Lý thuyết',
        45,
        'Tầng 1'
    ),
    (
        104,
        'P104',
        'Lý thuyết',
        45,
        'Tầng 1'
    ),
    (
        105,
        'P105',
        'Lý thuyết',
        45,
        'Tầng 1'
    ),
    (
        203,
        'P203',
        'Lý thuyết',
        45,
        'Tầng 2'
    ),
    (
        204,
        'P204',
        'Lý thuyết',
        45,
        'Tầng 2'
    ),
    (
        302,
        'P302',
        'Lý thuyết',
        45,
        'Tầng 3'
    ),
    (
        303,
        'P303',
        'Lý thuyết',
        45,
        'Tầng 3'
    );

/* =========================
2) BỔ SUNG LỚP THCS (6–9)
========================= */
INSERT INTO
    Lop (MaLop, TenLop, Khoi, MaPhong)
VALUES (5, '6A1', 6, 103),
    (6, '6A2', 6, 104),
    (7, '7A1', 7, 105),
    (8, '7A2', 7, 201), -- dùng phòng sẵn có
    (9, '8A1', 8, 203),
    (10, '8A2', 8, 202), -- P202 thí nghiệm cũng tạm dùng
    (11, '9A1', 9, 302),
    (12, '9A2', 9, 303);

/* =======================================
3) BỔ SUNG MÔN THCS (đủ nhóm phổ biến)
======================================= */
-- Đã có: (101 Toán), (102 Văn), (103 Anh)
INSERT INTO
    MonHoc (MaMon, TenMon, SoTiet, GhiChu)
VALUES (104, 'Vật lý', 70, NULL),
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
    ),
    (113, 'Âm nhạc', 35, NULL),
    (114, 'Mỹ thuật', 35, NULL);

/* =========================
4) BỔ SUNG GIÁO VIÊN
========================= */
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
        104,
        'Trần Quốc Lý',
        '0904444555',
        'ly@gv.example.com',
        '1982-09-09',
        'Nam'
    ), -- Vật lý
    (
        105,
        'Bùi Thanh Hóa',
        '0905555666',
        'hoa@gv.example.com',
        '1987-03-12',
        'Nữ'
    ), -- Hóa
    (
        106,
        'Đặng Mai Sinh',
        '0906666777',
        'sinh@gv.example.com',
        '1986-07-30',
        'Nữ'
    ), -- Sinh
    (
        107,
        'Phan Việt Sử',
        '0907777888',
        'su@gv.example.com',
        '1979-01-18',
        'Nam'
    ), -- Lịch sử
    (
        108,
        'Ngô Hải Địa',
        '0908888999',
        'dia@gv.example.com',
        '1983-04-22',
        'Nam'
    ), -- Địa lý
    (
        109,
        'Võ Thu GDCD',
        '0910000111',
        'gdcd@gv.example.com',
        '1984-12-05',
        'Nữ'
    ), -- GDCD
    (
        110,
        'Hồ Minh IT',
        '0911111222',
        'it@gv.example.com',
        '1990-02-10',
        'Nam'
    ), -- Tin
    (
        111,
        'Đoàn Công Nghệ',
        '0912222333',
        'cn@gv.example.com',
        '1985-05-15',
        'Nam'
    ), -- Công nghệ
    (
        112,
        'Lý An Thể',
        '0913333444',
        'td@gv.example.com',
        '1981-08-08',
        'Nam'
    ), -- Thể dục
    (
        113,
        'Tạ Bảo Nhạc',
        '0914444555',
        'amnhac@gv.example.com',
        '1991-11-11',
        'Nữ'
    ), -- Âm nhạc
    (
        114,
        'La Mỹ Thuật',
        '0915555666',
        'mythuat@gv.example.com',
        '1989-06-06',
        'Nữ'
    );
-- Mỹ thuật

/* =========================
5) BỔ SUNG HỌC SINH  (6–9)
========================= */
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
        6,
        'Ngô Minh K',
        '2012-05-01',
        'Nam',
        5,
        'Q1',
        '0916000001',
        'k6a1@example.com'
    ),
    (
        7,
        'Phạm Gia L',
        '2012-08-12',
        'Nữ',
        5,
        'Q1',
        '0916000002',
        'l6a1@example.com'
    ),
    (
        8,
        'Lê Thành M',
        '2012-02-20',
        'Nam',
        6,
        'Q2',
        '0916000003',
        'm6a2@example.com'
    ),
    (
        9,
        'Đinh Nhật N',
        '2011-10-10',
        'Nam',
        7,
        'Q3',
        '0916000004',
        'n7a1@example.com'
    ),
    (
        10,
        'Trần Thu O',
        '2011-12-09',
        'Nữ',
        7,
        'Q3',
        '0916000005',
        'o7a1@example.com'
    ),
    (
        11,
        'Vũ Quốc P',
        '2011-07-22',
        'Nam',
        8,
        'Q4',
        '0916000006',
        'p7a2@example.com'
    ),
    (
        12,
        'Hoàng Mai Q',
        '2010-04-14',
        'Nữ',
        9,
        'Q5',
        '0916000007',
        'q8a1@example.com'
    ),
    (
        13,
        'Đào Bảo R',
        '2010-09-19',
        'Nam',
        10,
        'Q5',
        '0916000008',
        'r8a2@example.com'
    ),
    (
        14,
        'Nguyễn Thái S',
        '2009-03-03',
        'Nam',
        11,
        'Q6',
        '0916000009',
        's9a1@example.com'
    ),
    (
        15,
        'Phan Mỹ T',
        '2009-06-25',
        'Nữ',
        12,
        'Q6',
        '0916000010',
        't9a2@example.com'
    );

/* =========================
6) CHỦ NHIỆM CHO LỚP 6–9
========================= */
INSERT INTO
    ChuNhiem (
        MaGV,
        MaLop,
        MaNK,
        NgayNhanNhiem
    )
VALUES (101, 5, 1, '2024-08-01'), -- 6A1
    (102, 6, 1, '2024-08-01'), -- 6A2
    (103, 7, 1, '2024-08-01'), -- 7A1
    (104, 8, 1, '2024-08-01'), -- 7A2
    (105, 9, 1, '2024-08-01'), -- 8A1
    (106, 10, 1, '2024-08-01'), -- 8A2
    (107, 11, 1, '2024-08-01'), -- 9A1
    (108, 12, 1, '2024-08-01');
-- 9A2

/* =========================
7) PHÂN CÔNG DẠY (một số môn chính)
========================= */
-- Cho 6A1 (MaLop=5), 6A2 (6)
INSERT INTO
    PhanCongDay (
        MaGV,
        MaMon,
        MaLop,
        MaNK,
        HocKy
    )
VALUES (101, 101, 5, 1, 1),
    (102, 102, 5, 1, 1),
    (103, 103, 5, 1, 1),
    (104, 104, 5, 1, 1),
    (106, 106, 5, 1, 1),
    (110, 110, 5, 1, 1),
    (101, 101, 6, 1, 1),
    (102, 102, 6, 1, 1),
    (103, 103, 6, 1, 1),
    (104, 104, 6, 1, 1),
    (106, 106, 6, 1, 1),
    (110, 110, 6, 1, 1),
    (101, 101, 5, 1, 2),
    (102, 102, 5, 1, 2),
    (103, 103, 5, 1, 2),
    (104, 104, 5, 1, 2),
    (106, 106, 5, 1, 2),
    (110, 110, 5, 1, 2),
    (101, 101, 6, 1, 2),
    (102, 102, 6, 1, 2),
    (103, 103, 6, 1, 2),
    (104, 104, 6, 1, 2),
    (106, 106, 6, 1, 2),
    (110, 110, 6, 1, 2);

-- Cho 7A1 (7), 7A2 (8) – thêm Lịch sử/Địa lý
INSERT INTO
    PhanCongDay (
        MaGV,
        MaMon,
        MaLop,
        MaNK,
        HocKy
    )
VALUES (101, 101, 7, 1, 1),
    (102, 102, 7, 1, 1),
    (103, 103, 7, 1, 1),
    (104, 104, 7, 1, 1),
    (106, 106, 7, 1, 1),
    (107, 107, 7, 1, 1),
    (108, 108, 7, 1, 1),
    (101, 101, 8, 1, 1),
    (102, 102, 8, 1, 1),
    (103, 103, 8, 1, 1),
    (104, 104, 8, 1, 1),
    (106, 106, 8, 1, 1),
    (107, 107, 8, 1, 1),
    (108, 108, 8, 1, 1),
    (101, 101, 7, 1, 2),
    (102, 102, 7, 1, 2),
    (103, 103, 7, 1, 2),
    (104, 104, 7, 1, 2),
    (106, 106, 7, 1, 2),
    (107, 107, 7, 1, 2),
    (108, 108, 7, 1, 2),
    (101, 101, 8, 1, 2),
    (102, 102, 8, 1, 2),
    (103, 103, 8, 1, 2),
    (104, 104, 8, 1, 2),
    (106, 106, 8, 1, 2),
    (107, 107, 8, 1, 2),
    (108, 108, 8, 1, 2);

-- Cho 8A1 (9), 8A2 (10) – thêm Hóa từ khối 8
INSERT INTO
    PhanCongDay (
        MaGV,
        MaMon,
        MaLop,
        MaNK,
        HocKy
    )
VALUES (101, 101, 9, 1, 1),
    (102, 102, 9, 1, 1),
    (103, 103, 9, 1, 1),
    (104, 104, 9, 1, 1),
    (105, 105, 9, 1, 1),
    (106, 106, 9, 1, 1),
    (101, 101, 10, 1, 1),
    (102, 102, 10, 1, 1),
    (103, 103, 10, 1, 1),
    (104, 104, 10, 1, 1),
    (105, 105, 10, 1, 1),
    (106, 106, 10, 1, 1),
    (101, 101, 9, 1, 2),
    (102, 102, 9, 1, 2),
    (103, 103, 9, 1, 2),
    (104, 104, 9, 1, 2),
    (105, 105, 9, 1, 2),
    (106, 106, 9, 1, 2),
    (101, 101, 10, 1, 2),
    (102, 102, 10, 1, 2),
    (103, 103, 10, 1, 2),
    (104, 104, 10, 1, 2),
    (105, 105, 10, 1, 2),
    (106, 106, 10, 1, 2);

-- Cho 9A1 (11), 9A2 (12)
INSERT INTO
    PhanCongDay (
        MaGV,
        MaMon,
        MaLop,
        MaNK,
        HocKy
    )
VALUES (101, 101, 11, 1, 1),
    (102, 102, 11, 1, 1),
    (103, 103, 11, 1, 1),
    (104, 104, 11, 1, 1),
    (105, 105, 11, 1, 1),
    (106, 106, 11, 1, 1),
    (101, 101, 12, 1, 1),
    (102, 102, 12, 1, 1),
    (103, 103, 12, 1, 1),
    (104, 104, 12, 1, 1),
    (105, 105, 12, 1, 1),
    (106, 106, 12, 1, 1),
    (101, 101, 11, 1, 2),
    (102, 102, 11, 1, 2),
    (103, 103, 11, 1, 2),
    (104, 104, 11, 1, 2),
    (105, 105, 11, 1, 2),
    (106, 106, 11, 1, 2),
    (101, 101, 12, 1, 2),
    (102, 102, 12, 1, 2),
    (103, 103, 12, 1, 2),
    (104, 104, 12, 1, 2),
    (105, 105, 12, 1, 2),
    (106, 106, 12, 1, 2);

/* =========================
8) THỜI KHÓA BIỂU MẪU
========================= */
-- Một vài dòng minh họa cho 6A1/6A2 HK1
INSERT INTO
    ThoiKhoaBieu (
        MaPC,
        MaPhong,
        ThuTrongTuan,
        TietHoc
    )
VALUES (
        (
            SELECT MaPC
            FROM PhanCongDay
            WHERE
                MaLop = 5
                AND MaMon = 101
                AND HocKy = 1
            LIMIT 1
        ),
        103,
        'Thứ 2',
        1
    ),
    (
        (
            SELECT MaPC
            FROM PhanCongDay
            WHERE
                MaLop = 5
                AND MaMon = 102
                AND HocKy = 1
            LIMIT 1
        ),
        103,
        'Thứ 2',
        2
    ),
    (
        (
            SELECT MaPC
            FROM PhanCongDay
            WHERE
                MaLop = 6
                AND MaMon = 101
                AND HocKy = 1
            LIMIT 1
        ),
        104,
        'Thứ 3',
        1
    ),
    (
        (
            SELECT MaPC
            FROM PhanCongDay
            WHERE
                MaLop = 6
                AND MaMon = 103
                AND HocKy = 1
            LIMIT 1
        ),
        104,
        'Thứ 3',
        2
    );

/* =========================
9) ĐIỂM MẪU HK1 & HK2 (để test view)
========================= */
-- 6A1 (MaLop=5): HS 6,7 | Môn: Toán(101), Văn(102), Anh(103), Lý(104), Sinh(106)
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
        6,
        101,
        1,
        1,
        7.5,
        7.0,
        7.0,
        8.0
    ),
    (
        6,
        102,
        1,
        1,
        8.0,
        7.5,
        7.5,
        8.0
    ),
    (
        6,
        103,
        1,
        1,
        7.0,
        7.0,
        7.5,
        8.0
    ),
    (
        6,
        104,
        1,
        1,
        6.5,
        7.0,
        7.0,
        7.5
    ),
    (
        6,
        106,
        1,
        1,
        8.5,
        8.0,
        8.0,
        8.5
    ),
    (
        7,
        101,
        1,
        1,
        8.0,
        8.0,
        7.5,
        8.5
    ),
    (
        7,
        102,
        1,
        1,
        7.5,
        7.0,
        7.0,
        7.5
    ),
    (
        7,
        103,
        1,
        1,
        8.0,
        8.0,
        7.5,
        8.0
    ),
    (
        7,
        104,
        1,
        1,
        7.0,
        7.0,
        7.0,
        7.5
    ),
    (
        7,
        106,
        1,
        1,
        8.5,
        8.0,
        8.0,
        9.0
    ),

-- HK2 cùng HS/môn
(
    6,
    101,
    2,
    1,
    8.0,
    7.5,
    7.5,
    8.5
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
    6,
    103,
    2,
    1,
    7.5,
    7.5,
    7.5,
    8.0
),
(
    6,
    104,
    2,
    1,
    7.0,
    7.0,
    7.5,
    8.0
),
(
    6,
    106,
    2,
    1,
    8.5,
    8.5,
    8.0,
    9.0
),
(
    7,
    101,
    2,
    1,
    8.5,
    8.0,
    8.0,
    9.0
),
(
    7,
    102,
    2,
    1,
    7.5,
    7.5,
    7.5,
    8.0
),
(
    7,
    103,
    2,
    1,
    8.0,
    8.0,
    8.0,
    8.5
),
(
    7,
    104,
    2,
    1,
    7.5,
    7.0,
    7.5,
    8.0
),
(
    7,
    106,
    2,
    1,
    8.5,
    8.5,
    8.5,
    9.0
);

-- 9A1 (MaLop=11): HS 14 | Môn: Toán(101), Văn(102), Anh(103), Lý(104), Hóa(105), Sinh(106)
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
        14,
        101,
        1,
        1,
        7.5,
        7.5,
        7.0,
        8.0
    ),
    (
        14,
        102,
        1,
        1,
        7.0,
        7.0,
        7.0,
        7.5
    ),
    (
        14,
        103,
        1,
        1,
        8.0,
        8.0,
        7.5,
        8.5
    ),
    (
        14,
        104,
        1,
        1,
        7.5,
        7.0,
        7.0,
        7.5
    ),
    (
        14,
        105,
        1,
        1,
        7.0,
        7.0,
        7.0,
        7.5
    ),
    (
        14,
        106,
        1,
        1,
        8.0,
        8.0,
        8.0,
        8.5
    ),
    (
        14,
        101,
        2,
        1,
        8.0,
        8.0,
        7.5,
        8.5
    ),
    (
        14,
        102,
        2,
        1,
        7.5,
        7.5,
        7.5,
        8.0
    ),
    (
        14,
        103,
        2,
        1,
        8.0,
        8.0,
        8.0,
        8.5
    ),
    (
        14,
        104,
        2,
        1,
        7.5,
        7.5,
        7.5,
        8.0
    ),
    (
        14,
        105,
        2,
        1,
        7.0,
        7.5,
        7.5,
        8.0
    ),
    (
        14,
        106,
        2,
        1,
        8.5,
        8.0,
        8.0,
        9.0
    );

-- 8A2 (MaLop=10): HS 13 | Môn: Toán, Văn, Anh, Lý, Hóa, Sinh
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
        13,
        101,
        1,
        1,
        8.0,
        7.5,
        7.5,
        8.0
    ),
    (
        13,
        102,
        1,
        1,
        7.0,
        7.0,
        7.0,
        7.5
    ),
    (
        13,
        103,
        1,
        1,
        7.5,
        7.5,
        7.5,
        8.0
    ),
    (
        13,
        104,
        1,
        1,
        7.0,
        7.0,
        7.0,
        7.5
    ),
    (
        13,
        105,
        1,
        1,
        7.0,
        7.0,
        7.0,
        7.5
    ),
    (
        13,
        106,
        1,
        1,
        8.0,
        8.0,
        7.5,
        8.5
    ),
    (
        13,
        101,
        2,
        1,
        8.5,
        8.0,
        8.0,
        8.5
    ),
    (
        13,
        102,
        2,
        1,
        7.5,
        7.5,
        7.5,
        8.0
    ),
    (
        13,
        103,
        2,
        1,
        7.5,
        8.0,
        8.0,
        8.5
    ),
    (
        13,
        104,
        2,
        1,
        7.5,
        7.5,
        7.5,
        8.0
    ),
    (
        13,
        105,
        2,
        1,
        7.5,
        7.5,
        7.5,
        8.0
    ),
    (
        13,
        106,
        2,
        1,
        8.0,
        8.0,
        8.0,
        8.5
    );

/* =========================
10) PHỤ HUYNH LIÊN KẾT (mẫu)
========================= */
INSERT INTO
    PhuHuynh (
        HoTen,
        SoDienThoai,
        Email,
        DiaChi
    )
VALUES (
        'Bà Võ Thị U',
        '0917000001',
        'u_ph@example.com',
        'Q1'
    ),
    (
        'Ông Lý Văn V',
        '0917000002',
        'v_ph@example.com',
        'Q3'
    );

INSERT INTO
    HocSinh_PhuHuynh (MaHS, MaPH, QuanHe)
VALUES (6, 3, 'Mẹ'), -- giả sử MaPH=3,4 tiếp nối 1,2 đã có
    (7, 4, 'Bố');

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