# Backend Spring Boot

Mã nguồn backend của ứng dụng được đặt trong `BE/src/main`. Maven được cấu hình tại thư mục dự án để biên dịch mã nguồn này và phục vụ FE trên cổng 8080.

Người dùng xem/cập nhật hồ sơ của chính mình qua `GET/PUT /api/auth/me`: họ tên, email, số điện thoại và ảnh đại diện PNG/JPEG (tối đa 1 MB, 2048 × 2048 pixel). Backend kiểm tra nội dung ảnh trước khi lưu.

## Quản trị tài khoản nội bộ

- `GET /api/auth/admin/accounts?q=`: danh sách và tìm kiếm theo tên đăng nhập, họ tên hoặc email.
- `POST /api/auth/admin/accounts`: tạo tài khoản, mật khẩu được mã hóa BCrypt.
- `PUT /api/auth/admin/accounts/{id}`: sửa họ tên, email, số điện thoại, vai trò và trạng thái hoạt động.
- Chỉ `ROLE_ADMIN` được phép gọi API quản trị. Đổi vai trò hoặc khóa/mở khóa sẽ thu hồi các phiên cũ của tài khoản.

Chạy backend từ thư mục gốc bằng `.\mvnw.cmd spring-boot:run`.

## Cơ sở dữ liệu

Mặc định ứng dụng dùng H2 dạng file tại `data/atsdb`, nên dữ liệu vẫn còn sau khi tắt backend. Có thể cấu hình nguồn SQL qua biến môi trường `DB_URL`, `DB_DRIVER`, `DB_USERNAME` và `DB_PASSWORD`; giá trị mặc định lần lượt là JDBC URL H2, `org.h2.Driver`, `sa` và mật khẩu trống. Driver tương ứng với JDBC URL cần có trong `pom.xml`/classpath trước khi đổi sang một hệ quản trị khác.

Mở `http://localhost:8080/h2-console` để kiểm tra DB; JDBC URL là `jdbc:h2:file:./data/atsdb`, user `sa`, mật khẩu để trống.
