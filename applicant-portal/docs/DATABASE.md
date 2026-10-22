# Thiết kế cơ sở dữ liệu

## Bảng `applications`
| Cột | Ý nghĩa |
|---|---|
| id | Khóa chính tự tăng |
| tracking_code | Mã tra cứu duy nhất |
| full_name | Họ tên ứng viên |
| email | Email ứng viên |
| phone | Số điện thoại |
| source | Nguồn biết tin |
| introduction | Thư giới thiệu |
| position | Vị trí ứng tuyển |
| cv_original_name | Tên tệp gốc |
| cv_stored_name | Tên tệp lưu nội bộ đã ngẫu nhiên hóa |
| cv_mime_type | MIME type của CV |
| cv_size_bytes | Kích thước tệp |
| ip_hash | Hash IP phục vụ kiểm toán giới hạn lạm dụng |
| created_at | Thời gian tiếp nhận |

Ràng buộc unique trên `tracking_code` và `(email, position)`. Khi mở rộng production, nên chuẩn hóa vị trí thành bảng riêng và áp dụng quy tắc so sánh email/vị trí thống nhất ở ứng dụng lẫn database.
