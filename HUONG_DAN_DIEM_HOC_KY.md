# 📊 Hướng dẫn sử dụng chức năng ĐIỂM HỌC KỲ & CẢ NĂM

## 🎯 Tổng quan

Hệ thống đã được bổ sung các chức năng mới để tính và xem điểm theo học kỳ và cả năm học:

### ✨ Các chức năng mới:

1. **Tính điểm trung bình học kỳ** (HK1 hoặc HK2)
2. **Tính điểm trung bình cả năm** (Kết hợp HK1 và HK2)
3. **Xem điểm và xuất báo cáo** theo học kỳ/cả năm
4. **Xuất file CSV** để lưu trữ và in ấn

---

## 📐 Công thức tính điểm

### 1. Điểm trung bình môn học (1 học kỳ):
```
ĐTB Môn = (Điểm Miệng + Điểm 15p + Điểm Giữa kỳ × 2 + Điểm Cuối kỳ × 3) / 7
```

### 2. Điểm trung bình học kỳ:
```
ĐTB HK = Trung bình các môn trong học kỳ đó
```

### 3. Điểm trung bình cả năm:
```
ĐTB Cả năm = (ĐTB HK1 + ĐTB HK2 × 2) / 3
```

### 4. Xếp loại học lực:
- **Xuất sắc**: ≥ 9.0
- **Giỏi**: ≥ 8.0
- **Khá**: ≥ 6.5
- **Trung bình**: ≥ 5.0
- **Yếu**: ≥ 3.5
- **Kém**: < 3.5

---

## 🚀 Cách sử dụng

### Bước 1: Mở chức năng
1. Vào **Dashboard** (màn hình chính)
2. Tìm mục **"Điểm"**
3. Click vào **"Xem điểm theo HK/Cả năm"**

### Bước 2: Chọn loại xem
- **Học kỳ 1**: Xem điểm trung bình học kỳ 1
- **Học kỳ 2**: Xem điểm trung bình học kỳ 2
- **Cả năm**: Xem điểm trung bình cả năm (hiển thị cả HK1, HK2 và TB cả năm)

### Bước 3: Chọn lọc dữ liệu
- **Lớp**: Chọn lớp cụ thể hoặc "Tất cả"
- **Năm học**: Chọn năm học cần xem

### Bước 4: Tải dữ liệu
- Click nút **"Tải dữ liệu"** để hiển thị điểm

### Bước 5: Xuất báo cáo (Tùy chọn)
- Click nút **"Xuất CSV"** để lưu file
- Chọn vị trí lưu file
- File có thể mở bằng Excel để in hoặc chỉnh sửa

---

## 📊 Giao diện

### Khi xem theo HK1 hoặc HK2:
| Mã HS | Họ tên        | Lớp  | TB Học kỳ | Xếp loại |
|-------|---------------|------|-----------|----------|
| 1     | Nguyễn Văn A  | 10A1 | 8.5       | Giỏi     |
| 2     | Trần Thị B    | 10A1 | 7.8       | Khá      |

### Khi xem Cả năm:
| Mã HS | Họ tên        | Lớp  | TB HK1 | TB HK2 | TB Cả năm | Xếp loại    |
|-------|---------------|------|--------|--------|-----------|-------------|
| 1     | Nguyễn Văn A  | 10A1 | 8.5    | 8.8    | 8.7       | Giỏi        |
| 2     | Trần Thị B    | 10A1 | 7.8    | 8.0    | 7.9       | Khá         |

---

## 🗂️ Cấu trúc file mới

### 1. DiemHocKyDAO.java
**Đường dẫn**: `src/com/sgu/qlhs/ui/database/DiemHocKyDAO.java`

**Chức năng**:
- Tính điểm trung bình học kỳ từ database
- Tính điểm trung bình cả năm
- Lấy chi tiết điểm từng môn
- Xếp loại học lực

### 2. DiemXemTheoHocKyDialog.java
**Đường dẫn**: `src/com/sgu/qlhs/ui/dialogs/DiemXemTheoHocKyDialog.java`

**Chức năng**:
- Giao diện xem điểm theo học kỳ/cả năm
- Lọc theo lớp và năm học
- Xuất file CSV
- Hiển thị thống kê

---

## 🔍 Ví dụ thực tế

### Ví dụ 1: Tính điểm HK1 của học sinh
**Giả sử học sinh có điểm các môn trong HK1:**
- Toán: Miệng=8, 15p=7, GK=8, CK=9 → ĐTB = (8+7+8×2+9×3)/7 = **8.3**
- Văn: Miệng=7, 15p=8, GK=7, CK=8 → ĐTB = (7+8+7×2+8×3)/7 = **7.6**
- Anh: Miệng=9, 15p=8, GK=8, CK=9 → ĐTB = (9+8+8×2+9×3)/7 = **8.6**

**Điểm TB HK1** = (8.3 + 7.6 + 8.6) / 3 = **8.2** → **Xếp loại: Giỏi**

### Ví dụ 2: Tính điểm cả năm
- HK1: 8.2
- HK2: 8.5

**Điểm TB Cả năm** = (8.2 + 8.5×2) / 3 = **8.4** → **Xếp loại: Giỏi**

---

## ⚙️ Lưu ý kỹ thuật

### Yêu cầu dữ liệu:
1. Phải có điểm đầy đủ trong bảng `Diem` (DiemMieng, Diem15p, DiemGiuaKy, DiemCuoiKy)
2. Phải có `HocKy` (1 hoặc 2) và `MaNK` (mã niên khóa)
3. Học sinh phải có ít nhất 1 môn có điểm để hiển thị

### Xử lý dữ liệu thiếu:
- Nếu học sinh chưa có điểm HK2, điểm cả năm sẽ tính dựa trên HK1
- Điểm được làm tròn 1 chữ số thập phân

### Hiệu năng:
- Sử dụng SQL JOIN và GROUP BY để tính nhanh
- Có thể xử lý hàng ngàn học sinh
- Kết quả được cache trong bảng

---

## 📞 Hỗ trợ

Nếu gặp lỗi, kiểm tra:
1. ✅ Database đã có dữ liệu điểm
2. ✅ Kết nối MySQL hoạt động
3. ✅ Đã compile lại sau khi cập nhật code

**Các lỗi thường gặp:**
- "Không có dữ liệu": Kiểm tra bảng Diem có dữ liệu chưa
- "Lỗi kết nối": Kiểm tra MySQL đang chạy
- "Không xuất được CSV": Kiểm tra quyền ghi file

---

## 🎓 Tóm tắt

✅ **Đã hoàn thành:**
- [x] Tính điểm TB học kỳ
- [x] Tính điểm TB cả năm  
- [x] Xem điểm theo HK/Cả năm
- [x] Xuất file CSV
- [x] Xếp loại học lực
- [x] Thống kê số lượng học sinh

✅ **Công thức áp dụng:**
- Điểm TB môn = (M + 15p + GK×2 + CK×3) / 7
- Điểm TB HK = Trung bình các môn
- Điểm TB Cả năm = (HK1 + HK2×2) / 3

✅ **Chạy ứng dụng:**
```powershell
cd d:\huyngyuen\nam_5\Java\JavaCode\QLHS
$CP = "build\classes;lib\mysql-connector-j-8.0.33.jar"
java -cp $CP com.sgu.qlhs.ui.MainDashboard
```

Chúc anh sử dụng hiệu quả! 🚀
