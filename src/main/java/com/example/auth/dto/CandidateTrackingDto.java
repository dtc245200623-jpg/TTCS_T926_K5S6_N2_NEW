package com.example.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidateTrackingDto {
    private String trackingCode;        // Mã tra cứu
    private String candidateName;       // Tên ứng viên
    private String maskedEmail;         // Email ẩn bớt ký tự để bảo mật
    private String jobTitle;            // Vị trí ứng tuyển
    private String departmentName;      // Phòng ban
    private LocalDateTime appliedAt;    // Ngày nộp hồ sơ
    private String generalStatus;       // "Đã nhận", "Đang xem xét", "Mời phỏng vấn", "Kết thúc"
    private Integer currentStep;        // 1 (Đã nhận), 2 (Đang xem xét), 3 (Mời phỏng vấn), 4 (Kết thúc)
    private String statusDescription;   // Lời nhắn tiến độ minh bạch cho ứng viên
    // TUYỆT ĐỐI KHÔNG chứa thông tin: nhận xét nội bộ, điểm đánh giá, tên người phỏng vấn
}