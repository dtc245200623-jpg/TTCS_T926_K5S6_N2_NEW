# Đăng tin & Cổng ứng tuyển - Ứng viên

Dự án **Đăng tin & Cổng ứng tuyển - Ứng viên**, xây dựng theo đầy đủ các đầu việc trong ảnh: biểu mẫu ứng tuyển, tải CV PDF/DOCX tối đa 10MB, giới hạn gửi theo IP, chống nộp trùng theo email + vị trí, mã tra cứu hồ sơ, email xác nhận (SMTP tùy chọn), MySQL, phpMyAdmin, Docker Compose và Nginx.

## Công nghệ
- Frontend: React + Vite
- Backend: Node.js + Express
- Database: MySQL 8
- Quản trị DB: phpMyAdmin
- Reverse proxy: Nginx
- Triển khai local: Docker Compose

## Chạy nhanh bằng Docker
1. Cài Docker Desktop và mở Docker Desktop.
2. Sao chép `.env.example` thành `.env`.
3. Thay tất cả mật khẩu mẫu trong `.env`, đặc biệt `MYSQL_ROOT_PASSWORD`, `MYSQL_PASSWORD` và `JWT_SECRET`.
4. Mở terminal tại thư mục dự án:
   ```bash
   docker compose up --build -d
   docker compose ps
   docker compose logs -f backend
   ```
5. Truy cập:
   - Website: http://localhost
   - API health: http://localhost/api/health
   - phpMyAdmin: http://localhost:8081
6. Đăng nhập phpMyAdmin bằng `PMA_USER` / `PMA_PASSWORD` trong `.env` (hoặc tài khoản DB `MYSQL_USER` / `MYSQL_PASSWORD`).

Lần đầu chạy, MySQL tự tạo bảng từ `database/init.sql`. Nếu bạn đã chạy trước đó và muốn khởi tạo lại dữ liệu từ đầu, chỉ làm khi không cần dữ liệu hiện có:
```bash
docker compose down -v
docker compose up --build -d
```
Lệnh `down -v` xóa volume database.

## Chạy không dùng Docker
Yêu cầu Node.js 20+ và MySQL 8.
1. Tạo database bằng `database/init.sql`.
2. Backend:
   ```bash
   cd backend
   cp ../.env.example .env
   npm install
   npm run dev
   ```
   Chỉnh `DB_HOST=127.0.0.1` trong `backend/.env` và điền thông tin DB/SMTP.
3. Frontend ở terminal khác:
   ```bash
   cd frontend
   npm install
   npm run dev
   ```
   Mở URL Vite hiển thị trong terminal. Có thể cấu hình `VITE_API_BASE=http://localhost:5000/api` trong `frontend/.env`.

## Cấu hình email
Email xác nhận chỉ gửi khi cấu hình SMTP hợp lệ trong `.env`:
- `SMTP_HOST`, `SMTP_PORT`, `SMTP_SECURE`
- `SMTP_USER`, `SMTP_PASS`, `MAIL_FROM`

Nếu chưa cấu hình SMTP, hồ sơ vẫn được lưu và API trả về trạng thái email chưa được gửi. Không dùng mật khẩu email cá nhân; nên dùng SMTP credentials/app password do nhà cung cấp cấp.

## Đưa lên GitHub
Trong thư mục dự án:
```bash
git init
git add .
git commit -m "Initial applicant portal"
git branch -M main
git remote add origin https://github.com/<TEN_TAI_KHOAN>/<TEN_REPOSITORY>.git
git push -u origin main
```
Tạo repository rỗng trên GitHub trước khi chạy lệnh. Không commit file `.env`, mật khẩu, CV thật hoặc dữ liệu ứng viên. File `.gitignore` đã loại trừ `.env`.

## API chính
- `GET /api/health` — trạng thái dịch vụ
- `POST /api/applications` — gửi hồ sơ multipart/form-data
- `GET /api/applications/lookup/:code` — tra cứu bằng mã hồ sơ

## Các quy tắc đã triển khai
- Trường: họ tên, email, số điện thoại, nguồn biết tin, thư giới thiệu, vị trí ứng tuyển, CV.
- Chấp nhận PDF/DOCX và giới hạn 10MB.
- Kiểm tra chữ ký nội dung của tệp PDF/DOCX; không dựa riêng vào MIME do trình duyệt gửi.
- Giới hạn số lần gửi theo IP: mặc định 5 lần/giờ, cấu hình bằng `RATE_LIMIT_MAX`.
- Biểu mẫu có trường honeypot chống bot; IP được lưu dưới dạng HMAC để không lưu IP gốc.
- Chống nộp trùng theo email + vị trí (không phân biệt chữ hoa/thường).
- Sinh mã tra cứu hồ sơ duy nhất.
- Email xác nhận có mã tra cứu khi SMTP được cấu hình.
- Đặt `IP_HASH_SECRET` thành chuỗi ngẫu nhiên dài trong `.env` trước khi triển khai.
- Lưu metadata CV và dữ liệu hồ sơ trong MySQL; file CV được lưu trong thư mục uploads.
- Nginx làm reverse proxy; frontend gọi API qua `/api`.

## Lưu ý bảo mật trước khi triển khai thật
Đây là bộ khung chạy được để phát triển/học tập, chưa thay thế quy trình kiểm thử bảo mật và vận hành production. Cần bật HTTPS với chứng chỉ hợp lệ, chính sách lưu/xóa CV, quyền truy cập hồ sơ dành cho quản trị viên, sao lưu DB, giám sát và chống lạm dụng ở tầng proxy. Không đưa CV hoặc dữ liệu cá nhân lên repository công khai.
