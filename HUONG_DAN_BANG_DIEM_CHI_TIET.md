# HƯỚNG DẪN SỬ DỤNG BẢNG ĐIỂM CHI TIẾT HỌC SINH

## Tổng quan
Chức năng **Bảng điểm chi tiết** cho phép xem và in bảng điểm của từng học sinh theo định dạng chính thức, giống như bảng điểm giấy truyền thống.

## Cách sử dụng

### 1. Mở Bảng điểm chi tiết
- Từ màn hình Dashboard chính
- Chọn mục **"Bảng điểm chi tiết"** trong phần **Điểm**

### 2. Chọn thông tin
Dialog sẽ hiển thị với các trường chọn:
- **Học sinh**: Chọn học sinh cần xem bảng điểm
- **Học kỳ**: Chọn Học kỳ 1 hoặc Học kỳ 2
- **Năm học**: Chọn năm học (mặc định: 2024-2025)

### 3. Xem bảng điểm
- Nhấn nút **"Xem bảng điểm"**
- Bảng điểm sẽ hiển thị theo định dạng chính thức với:
  - **Header**: 
    - Sở GD&ĐT
    - Tên trường
    - Quốc hiệu: "CỘNG HÒA XÃ HỘI CHỦ NGHĨA VIỆT NAM"
    - Devises: "Độc lập - Tự do - Hạnh phúc"
    - Ngày tháng năm hiện tại
  - **Tiêu đề**: 
    - "BẢNG ĐIỂM HỌC SINH"
    - Tên học sinh
    - Học kỳ và năm học
  - **Bảng điểm** với các cột:
    - STT
    - Tên môn học
    - Điểm Miệng
    - Điểm 15 Phút (có thể nhiều điểm)
    - Điểm 1 Tiết (có thể nhiều điểm)
    - Điểm Học kỳ (điểm thi cuối kỳ)
    - TBHK (Trung bình học kỳ)
    - Ghi chú

### 4. In bảng điểm
- Nhấn nút **"In"**
- Chọn máy in trong hộp thoại in
- Xác nhận để in bảng điểm

### 5. Đóng
- Nhấn nút **"Đóng"** để thoát

## Các cột trong bảng điểm

| Cột | Mô tả |
|-----|-------|
| **STT** | Số thứ tự môn học |
| **Tên môn học** | Tên đầy đủ của môn học (Toán, Văn, Anh, Lý, Hóa, Sinh, v.v.) |
| **Miệng** | Điểm kiểm tra miệng (có thể có nhiều điểm) |
| **15 Phút** | Điểm kiểm tra 15 phút (có thể có nhiều điểm, cách nhau bởi dấu cách) |
| **1 Tiết** | Điểm kiểm tra 1 tiết (có thể có nhiều điểm, cách nhau bởi dấu cách) |
| **Học kỳ** | Điểm thi học kỳ (điểm thi chính thức cuối kỳ) |
| **TBHK** | Điểm trung bình học kỳ (tính theo công thức) |
| **Ghi chú** | Các ghi chú đặc biệt (nếu có) |

## Công thức tính điểm

### Điểm trung bình môn học (TBHK)
```
TBHK = (Tổng điểm Miệng + Tổng điểm 15p + Tổng điểm 1 Tiết×2 + Điểm Học kỳ×3) / (số lượng điểm + 5)
```

Ví dụ:
- Môn Toán: Miệng=7, 15p=5+5+5, 1Tiết=8+4+7+9, HK=5
- TBHK = (7 + 15 + (28)×2 + 5×3) / (1 + 3 + 4 + 1 + 3) ≈ 6.2

## Các môn học đặc biệt

### GDQP (Giáo dục Quốc phòng)
- Chỉ có điểm Đạt/Không đạt
- Hiển thị số 1 hoặc 0

### Thể dục
- Điểm đánh giá: Đ (Đạt), C (Chưa đạt)
- Không tính điểm số

## Lưu ý
- Bảng điểm có thể được in trực tiếp hoặc xuất ra PDF (sử dụng máy in PDF)
- Dữ liệu hiện tại là dữ liệu mẫu, cần kết nối với database thực tế
- Định dạng bảng điểm tuân theo quy định của Bộ Giáo dục

## Tương lai (TODO)
- [ ] Kết nối với database thực tế để lấy điểm từ bảng Diem
- [ ] Tự động tính điểm trung bình học kỳ theo công thức
- [ ] Thêm chức năng xuất ra file PDF
- [ ] Thêm chức năng gửi email bảng điểm
- [ ] Thêm logo trường vào header
- [ ] Thêm chữ ký giáo viên, hiệu trưởng

## Liên hệ
Nếu có thắc mắc hoặc đề xuất, vui lòng liên hệ với đội phát triển.
