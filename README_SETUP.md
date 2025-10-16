# 🚀 Hướng dẫn chạy project QLHS

## ✅ Đã kiểm tra:
- ✓ MySQL đang chạy (MySQL80 Running)
- ✓ Database `qlhs` đã tồn tại
- ✓ Có đầy đủ 14 bảng trong database
- ✓ Đã tạo thư mục `lib/`
- ✓ Đã sửa tên database từ `QLHS` → `qlhs`

## ⚠️ Còn thiếu:
- ❌ **MySQL Connector/J JAR** - Thư viện JDBC để kết nối MySQL

## 📋 Các bước cần làm:

### Bước 1: Tải MySQL Connector/J
**Cách 1 - Tải từ Maven Central (Khuyến nghị):**
```powershell
# Download MySQL Connector 8.0.33
$url = "https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.0.33/mysql-connector-j-8.0.33.jar"
$output = "d:\huyngyuen\nam_5\Java\JavaCode\QLHS\lib\mysql-connector-j-8.0.33.jar"
Invoke-WebRequest -Uri $url -OutFile $output
```

**Cách 2 - Copy từ nơi khác:**
- Nếu đã có file JAR, copy vào thư mục `lib/`

### Bước 2: Cấu hình NetBeans
1. Mở project trong NetBeans
2. Right-click project → Properties
3. Libraries → Add JAR/Folder
4. Chọn file `mysql-connector-j-8.0.33.jar` trong thư mục `lib/`

### Bước 3: Chạy ứng dụng
- Nhấn F6 hoặc Run → Run Project
- Hoặc chạy main class: `com.sgu.qlhs.ui.MainDashboard`

## 🔧 Compile thủ công (nếu không dùng NetBeans):

```powershell
# Set classpath
$CP = "lib\mysql-connector-j-8.0.33.jar"

# Compile
javac -cp $CP -d build\classes src\com\sgu\qlhs\ui\**\*.java

# Run
java -cp "build\classes;$CP" com.sgu.qlhs.ui.MainDashboard
```

## 🗄️ Cấu trúc Database:
```
Database: qlhs
Tables:
  - chunhiem
  - danhgia
  - diem
  - diemhocky
  - giaovien
  - hocsinh
  - hocsinh_phuhuynh
  - lop
  - monhoc
  - nienkhoa
  - phancongday
  - phonghoc
  - phuhuynh
  - thoikhoabieu
```

## 📝 Thông tin kết nối:
- Host: localhost:3306
- Database: qlhs
- User: root
- Password: 9851343a
