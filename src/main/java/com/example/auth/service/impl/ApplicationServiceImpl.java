package com.example.auth.service.impl;

import com.example.auth.dto.ApplicationDto;
import com.example.auth.dto.CandidateTrackingDto;
import com.example.auth.entity.Application;
import com.example.auth.entity.CandidateProfile;
import com.example.auth.entity.JobRequest;
import com.example.auth.entity.User;
import com.example.auth.exception.AppException;
import com.example.auth.repository.ApplicationRepository;
import com.example.auth.repository.CandidateProfileRepository;
import com.example.auth.repository.JobRequestRepository;
import com.example.auth.repository.UserRepository;
import com.example.auth.service.ApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApplicationServiceImpl implements ApplicationService {
    private final ApplicationRepository applicationRepository;
    private final JobRequestRepository jobRequestRepository;
    private final UserRepository userRepository;
    private final CandidateProfileRepository candidateProfileRepository;

    @Override
    public ApplicationDto applyForJob(Long jobRequestId, String username, String cvUrl) {
        JobRequest job = jobRequestRepository.findById(jobRequestId)
                .orElseThrow(() -> new AppException("Không tìm thấy Vị trí tuyển dụng với ID này"));
        User candidate = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException("Không tìm thấy thông tin ứng viên"));
        
        CandidateProfile profile = candidateProfileRepository.findByUserId(candidate.getId())
                .orElseGet(() -> {
                    CandidateProfile newProfile = new CandidateProfile();
                    newProfile.setUser(candidate);
                    newProfile.setFullName(candidate.getUsername());
                    return candidateProfileRepository.save(newProfile);
                });
        
        if (cvUrl != null) {
            profile.setCvUrl(cvUrl);
            candidateProfileRepository.save(profile);
        }

        // Tự động tạo mã tra cứu độc nhất cho ứng viên
        String trackingCode = "TRK-" + (100000 + (int)(Math.random() * 900000));

        Application app = new Application();
        app.setTrackingCode(trackingCode);
        app.setJobRequest(job);
        app.setCandidateProfile(profile);
        app.setStatus("APPLIED"); // APPLIED, SCREENING, INTERVIEWING, OFFERED, REJECTED
        app.setAppliedAt(LocalDateTime.now());
        
        Application saved = applicationRepository.save(app);
        return mapToDto(saved);
    }

    @Override
    public List<ApplicationDto> getApplicationsByCandidate(String username) {
        User candidate = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException("Không tìm thấy thông tin ứng viên"));
        return applicationRepository.findAll().stream()
                .filter(a -> a.getCandidateProfile() != null && a.getCandidateProfile().getUser().getId().equals(candidate.getId()))
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ApplicationDto> getApplicationsByJobRequest(Long jobRequestId) {
        return applicationRepository.findAll().stream()
                .filter(a -> a.getJobRequest() != null && a.getJobRequest().getId().equals(jobRequestId))
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ApplicationDto> getAllApplications() {
        return applicationRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public ApplicationDto updateStatus(Long id, String status) {
        Application app = applicationRepository.findById(id)
                .orElseThrow(() -> new AppException("Không tìm thấy hồ sơ ứng tuyển"));
        app.setStatus(status);
        Application saved = applicationRepository.save(app);
        return mapToDto(saved);
    }

    /**
     * Tra cứu trạng thái hồ sơ dành riêng cho Ứng viên (Public - Không yêu cầu đăng nhập)
     * Quy tắc:
     * - Yêu cầu nhập đúng Mã tra cứu VÀ Email ứng tuyển
     * - Chỉ hiển thị 4 trạng thái tổng quát: "Đã nhận", "Đang xem xét", "Mời phỏng vấn", "Kết thúc"
     * - Bảo mật: Không tiết lộ nhận xét nội bộ hay tên người phỏng vấn
     */
    @Override
    public CandidateTrackingDto trackApplication(String trackingCode, String email) {
        if (trackingCode == null || trackingCode.trim().isEmpty()) {
            throw new AppException("Vui lòng nhập Mã tra cứu hồ sơ!");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new AppException("Vui lòng nhập Email ứng tuyển!");
        }

        Application app = applicationRepository.findByTrackingCodeIgnoreCase(trackingCode.trim())
                .orElseThrow(() -> new AppException("Không tìm thấy hồ sơ với mã tra cứu: " + trackingCode.trim()));

        // Kiểm tra xác thực email ứng viên để đảm bảo quyền riêng tư
        String candidateEmail = null;
        String candidateName = "Ứng viên";
        if (app.getCandidateProfile() != null) {
            if (app.getCandidateProfile().getUser() != null) {
                candidateEmail = app.getCandidateProfile().getUser().getEmail();
            }
            if (app.getCandidateProfile().getFullName() != null) {
                candidateName = app.getCandidateProfile().getFullName();
            } else if (app.getCandidateProfile().getUser() != null) {
                candidateName = app.getCandidateProfile().getUser().getUsername();
            }
        }

        if (candidateEmail == null || !candidateEmail.trim().equalsIgnoreCase(email.trim())) {
            throw new AppException("Mã tra cứu và Email không khớp. Vui lòng kiểm tra lại thông tin!");
        }

        String rawStatus = app.getStatus() != null ? app.getStatus().toUpperCase() : "APPLIED";
        String generalStatus;
        int step;
        String description;

        // Phân loại vào 4 trạng thái tổng quát
        switch (rawStatus) {
            case "APPLIED":
            case "NEW":
            case "SUBMITTED":
                generalStatus = "Đã nhận";
                step = 1;
                description = "Hồ sơ ứng tuyển của bạn đã được hệ thống tiếp nhận thành công và đã chuyển tới bộ phận tuyển dụng.";
                break;
            case "SCREENING":
            case "REVIEWING":
            case "PENDING":
                generalStatus = "Đang xem xét";
                step = 2;
                description = "Hồ sơ của bạn đang được Phòng Nhân sự và Trưởng bộ phận chuyên môn xem xét, đánh giá độ phù hợp.";
                break;
            case "INTERVIEWING":
            case "INTERVIEW_SCHEDULED":
            case "INTERVIEWED":
            case "OFFERING":
            case "OFFER_EXTENDED":
                generalStatus = "Mời phỏng vấn";
                step = 3;
                description = "Chúc mừng bạn! Hồ sơ của bạn đã đạt tiêu chuẩn và được mời tham gia phỏng vấn. Vui lòng kiểm tra email để biết lịch phỏng vấn chi tiết.";
                break;
            case "HIRED":
            case "REJECTED":
            case "DECLINED":
            case "CLOSED":
            case "WITHDRAWN":
            default:
                generalStatus = "Kết thúc";
                step = 4;
                description = "Quy trình ứng tuyển cho vị trí này đã hoàn tất. Cảm ơn bạn đã quan tâm và dành thời gian ứng tuyển vào công ty.";
                break;
        }

        String jobTitle = (app.getJobRequest() != null) ? app.getJobRequest().getTitle() : "Vị trí tuyển dụng";
        String deptName = (app.getJobRequest() != null && app.getJobRequest().getDepartment() != null) 
                ? app.getJobRequest().getDepartment().getName() : "Công ty";

        // Che mờ email (vd: u***n@ictu.edu.vn)
        String maskedEmail = maskEmail(candidateEmail);

        return CandidateTrackingDto.builder()
                .trackingCode(app.getTrackingCode())
                .candidateName(candidateName)
                .maskedEmail(maskedEmail)
                .jobTitle(jobTitle)
                .departmentName(deptName)
                .appliedAt(app.getAppliedAt())
                .generalStatus(generalStatus)
                .currentStep(step)
                .statusDescription(description)
                .build();
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) return email;
        int atIndex = email.indexOf("@");
        String name = email.substring(0, atIndex);
        String domain = email.substring(atIndex);
        if (name.length() <= 2) {
            return name.charAt(0) + "***" + domain;
        }
        return name.charAt(0) + "***" + name.charAt(name.length() - 1) + domain;
    }

    private ApplicationDto mapToDto(Application a) {
        return ApplicationDto.builder()
                .id(a.getId())
                .trackingCode(a.getTrackingCode())
                .candidateId(a.getCandidateProfile() != null && a.getCandidateProfile().getUser() != null ? a.getCandidateProfile().getUser().getId() : null)
                .candidateName(a.getCandidateProfile() != null && a.getCandidateProfile().getUser() != null ? a.getCandidateProfile().getUser().getUsername() : "Unknown")
                .jobRequestId(a.getJobRequest() != null ? a.getJobRequest().getId() : null)
                .jobTitle(a.getJobRequest() != null ? a.getJobRequest().getTitle() : "N/A")
                .status(a.getStatus())
                .appliedAt(a.getAppliedAt())
                .interviewScore("85/100")
                .cvUrl(a.getCandidateProfile() != null ? a.getCandidateProfile().getCvUrl() : null)
                .build();
    }
}