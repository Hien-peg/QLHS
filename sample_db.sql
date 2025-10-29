-- Sample SQL for QLHS testing (MySQL)
-- Drop existing tables if present
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS Diem;

DROP TABLE IF EXISTS HocSinh;

DROP TABLE IF EXISTS MonHoc;

DROP TABLE IF EXISTS Lop;

DROP TABLE IF EXISTS PhongHoc;

DROP TABLE IF EXISTS NienKhoa;

SET FOREIGN_KEY_CHECKS = 1;

-- Niên khóa
CREATE TABLE NienKhoa (
    MaNK INT AUTO_INCREMENT PRIMARY KEY,
    TenNK VARCHAR(50) NOT NULL
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- Phòng học (optional)
CREATE TABLE PhongHoc (
    MaPhong INT AUTO_INCREMENT PRIMARY KEY,
    TenPhong VARCHAR(50),
    LoaiPhong VARCHAR(50),
    SucChua INT,
    ViTri VARCHAR(100)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- Lớp
CREATE TABLE Lop (
    MaLop INT AUTO_INCREMENT PRIMARY KEY,
    TenLop VARCHAR(50) NOT NULL,
    Khoi INT,
    MaPhong INT,
    FOREIGN KEY (MaPhong) REFERENCES PhongHoc (MaPhong)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- Môn học
CREATE TABLE MonHoc (
    MaMon INT AUTO_INCREMENT PRIMARY KEY,
    TenMon VARCHAR(100) NOT NULL,
    SoTiet INT DEFAULT 0
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- Học sinh
CREATE TABLE HocSinh (
    MaHS INT AUTO_INCREMENT PRIMARY KEY,
    HoTen VARCHAR(150) NOT NULL,
    NgaySinh DATE,
    GioiTinh VARCHAR(10),
    MaLop INT,
    FOREIGN KEY (MaLop) REFERENCES Lop (MaLop)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- Điểm
CREATE TABLE Diem (
    MaDiem INT AUTO_INCREMENT PRIMARY KEY,
    MaHS INT NOT NULL,
    MaMon INT NOT NULL,
    HocKy INT NOT NULL,
    MaNK INT NOT NULL,
    DiemMieng DOUBLE DEFAULT 0,
    Diem15p DOUBLE DEFAULT 0,
    DiemGiuaKy DOUBLE DEFAULT 0,
    DiemCuoiKy DOUBLE DEFAULT 0,
    FOREIGN KEY (MaHS) REFERENCES HocSinh (MaHS),
    FOREIGN KEY (MaMon) REFERENCES MonHoc (MaMon),
    FOREIGN KEY (MaNK) REFERENCES NienKhoa (MaNK)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- Sample data
INSERT INTO NienKhoa (TenNK) VALUES ('2024-2025'), ('2023-2024');

INSERT INTO
    PhongHoc (
        TenPhong,
        LoaiPhong,
        SucChua,
        ViTri
    )
VALUES (
        'P101',
        'Lý thuyết',
        45,
        'Tầng 1'
    );

INSERT INTO
    Lop (TenLop, Khoi, MaPhong)
VALUES ('10A1', 10, 1),
    ('10A2', 10, 1);

INSERT INTO
    MonHoc (TenMon, SoTiet)
VALUES ('Toán', 140),
    ('Văn', 140),
    ('Anh', 140),
    ('Lý', 140),
    ('Hóa', 140),
    ('Sinh', 140);

-- Học sinh
INSERT INTO
    HocSinh (
        HoTen,
        NgaySinh,
        GioiTinh,
        MaLop
    )
VALUES (
        'Nguyễn Văn A',
        '2008-05-12',
        'Nam',
        1
    ),
    (
        'Trần Thị B',
        '2008-08-03',
        'Nữ',
        1
    ),
    (
        'Lê Văn C',
        '2008-11-21',
        'Nam',
        2
    );

-- Điểm sample (MaNK = 1 corresponds to 2024-2025)
-- Học kỳ 1
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
        1,
        1,
        1,
        8.0,
        7.5,
        8.0,
        7.0
    ),
    (
        1,
        2,
        1,
        1,
        7.0,
        6.5,
        7.5,
        6.0
    ),
    (
        2,
        1,
        1,
        1,
        9.0,
        8.5,
        9.0,
        8.0
    ),
    (
        2,
        3,
        1,
        1,
        8.5,
        8.0,
        8.0,
        8.5
    ),
    (
        3,
        1,
        1,
        1,
        6.0,
        5.5,
        6.5,
        6.0
    );

-- Thêm điểm HK1 cho Lý(4), Hóa(5), Sinh(6)
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
        4,
        1,
        1,
        7.5,
        7.0,
        7.5,
        7.0
    ),
    (
        1,
        5,
        1,
        1,
        6.5,
        6.0,
        6.5,
        6.0
    ),
    (
        1,
        6,
        1,
        1,
        7.0,
        6.5,
        7.0,
        6.5
    ),
    (
        2,
        4,
        1,
        1,
        8.0,
        8.0,
        8.5,
        8.0
    ),
    (
        2,
        5,
        1,
        1,
        7.5,
        7.0,
        7.5,
        7.0
    ),
    (
        3,
        4,
        1,
        1,
        6.0,
        5.5,
        6.0,
        5.5
    );

-- Học kỳ 2
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
        1,
        2,
        1,
        8.5,
        8.0,
        8.5,
        8.0
    ),
    (
        1,
        2,
        2,
        1,
        7.5,
        7.0,
        7.0,
        7.5
    ),
    (
        2,
        1,
        2,
        1,
        9.0,
        9.0,
        8.5,
        9.0
    ),
    (
        2,
        3,
        2,
        1,
        8.0,
        8.0,
        8.5,
        8.0
    ),
    (
        3,
        1,
        2,
        1,
        6.5,
        6.0,
        6.5,
        6.0
    );

-- Thêm điểm HK2 cho Lý(4), Hóa(5), Sinh(6)
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
        4,
        2,
        1,
        8.0,
        7.5,
        8.0,
        7.5
    ),
    (
        1,
        5,
        2,
        1,
        7.0,
        6.5,
        7.0,
        6.5
    ),
    (
        1,
        6,
        2,
        1,
        7.5,
        7.0,
        7.5,
        7.0
    ),
    (
        2,
        4,
        2,
        1,
        9.0,
        8.5,
        9.0,
        8.5
    ),
    (
        2,
        5,
        2,
        1,
        8.0,
        8.0,
        8.0,
        8.0
    ),
    (
        3,
        4,
        2,
        1,
        6.5,
        6.0,
        6.5,
        6.0
    );

-- End of sample

-- Tips: to load the file into MySQL:
-- mysql -u root -p qlhs < sample_db.sql