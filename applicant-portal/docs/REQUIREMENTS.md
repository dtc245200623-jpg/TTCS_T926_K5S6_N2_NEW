# Đăng tin & Cổng ứng tuyển - Ứng viên
## Danh sách yêu cầu theo các ảnh

### 1. Đăng tin & Cổng ứng tuyển - Ứng viên
- Biểu mẫu gồm họ tên, email, số điện thoại, nguồn biết tin, thư giới thiệu và tệp CV.
- CV chấp nhận PDF hoặc DOCX, tối đa 10MB.
- Có chống spam và giới hạn số lần nộp từ một địa chỉ trong một giờ.
- Nộp trùng cùng một vị trí bằng cùng email bị từ chối kèm thông báo rõ ràng.
- Nộp thành công sinh mã tra cứu và gửi email xác nhận.

### 2. Xây dựng biểu mẫu ứng tuyển cho ứng viên
- Tạo giao diện biểu mẫu rõ ràng, dễ sử dụng, responsive trên máy tính và điện thoại.
- Thu thập họ tên, email, số điện thoại, nguồn biết tin, thư giới thiệu, vị trí ứng tuyển và CV.
- Kiểm tra trường bắt buộc ở giao diện và backend.

### 3. Xử lý tải lên CV với giới hạn định dạng và dung lượng
- Chỉ chấp nhận PDF/DOCX.
- Giới hạn dung lượng tối đa 10MB.
- Hiển thị lỗi phù hợp nếu định dạng hoặc dung lượng không hợp lệ.
- Lưu CV bằng tên nội bộ ngẫu nhiên thay vì tin cậy tên tệp do người dùng gửi.

### 4. Thiết lập chống spam và giới hạn số lần nộp theo IP
- Áp dụng rate limit tại API nộp hồ sơ.
- Mặc định giới hạn 5 yêu cầu/giờ/IP; có thể thay đổi bằng biến môi trường.
- Khi vượt giới hạn, trả thông báo dễ hiểu.

### 5. Ngăn chặn nộp trùng theo vị trí và email
- Kiểm tra email + vị trí ứng tuyển trước khi lưu.
- Tạo unique constraint trong database để tăng độ an toàn khi có nhiều yêu cầu đồng thời.
- Trả mã HTTP 409 và thông báo khi hồ sơ trùng.

### 6. Sinh mã tra cứu và gửi email xác nhận sau khi nộp thành công
- Sinh mã tra cứu duy nhất cho hồ sơ đã lưu thành công.
- Gửi email xác nhận khi SMTP đã cấu hình hợp lệ.
- Nếu chưa cấu hình SMTP hoặc gửi email lỗi, thông báo rõ hồ sơ đã lưu nhưng email chưa gửi.
- Cung cấp API tra cứu bằng mã hồ sơ.

### 7. Lưu trữ và xử lý dữ liệu hồ sơ ứng tuyển ở backend
- Backend Express cung cấp API tiếp nhận và tra cứu hồ sơ.
- MySQL lưu thông tin ứng viên, vị trí, mã tra cứu, metadata CV, thời gian nộp và hash IP.
- phpMyAdmin hỗ trợ quản trị database.
- Docker Compose khởi động các dịch vụ và chờ MySQL healthy trước khi chạy backend.

## Đối chiếu các phần trong mã nguồn
- Giao diện biểu mẫu: `frontend/src/main.jsx`, `frontend/src/style.css`
- API/backend: `backend/src/server.js`
- Database schema: `database/init.sql`
- Docker: `docker-compose.yml`, `backend/Dockerfile`, `frontend/Dockerfile`
- Reverse proxy và security headers: `nginx/default.conf`
- Hướng dẫn cài đặt/chạy: `README.md`
- Checklist kiểm thử: `docs/CHECKLIST.md`
- Thiết kế database: `docs/DATABASE.md`

## Các điểm cần cấu hình trước khi demo
1. Sao chép `.env.example` thành `.env` và đổi mật khẩu mẫu.
2. Cấu hình SMTP nếu cần gửi email xác nhận thật.
3. Chạy `docker compose up --build -d`.
4. Kiểm tra từng mục trong `docs/CHECKLIST.md`.
5. Không đưa `.env`, CV thật hoặc dữ liệu cá nhân lên GitHub.

## Phạm vi chưa có trong phiên bản này
Ảnh yêu cầu các chức năng cổng ứng tuyển và xử lý hồ sơ. Phiên bản này chưa có bảng điều khiển quản trị viên để duyệt/sửa/xóa hồ sơ hoặc quản lý tin tuyển dụng; nếu giảng viên yêu cầu các chức năng quản trị đó, cần bổ sung riêng.
