package com.example.auth.controller;

import com.example.auth.dto.ApiResponse;
import com.example.auth.entity.Application;
import com.example.auth.entity.Candidate;
import com.example.auth.entity.JobOpening;
import com.example.auth.exception.AppException;
import com.example.auth.repository.ApplicationRepository;
import com.example.auth.repository.CandidateRepository;
import com.example.auth.repository.JobOpeningRepository;
import com.example.auth.service.RecruitmentNotificationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicRecruitmentController {
    private static final long MAX_RESUME_BYTES = 5L * 1024 * 1024;
    private final JobOpeningRepository jobs;
    private final CandidateRepository candidates;
    private final ApplicationRepository applications;
    private final RecruitmentNotificationService notificationService;

    @GetMapping("/jobs")
    public ResponseEntity<ApiResponse<List<JobOpening>>> openJobs() {
        return ResponseEntity.ok(ApiResponse.success("Vị trí đang tuyển", jobs.findAll().stream()
                .filter(job -> "OPEN".equalsIgnoreCase(job.getStatus())).toList()));
    }

    @PostMapping(value = "/jobs/{jobId}/applications", consumes = "multipart/form-data")
    @Transactional
    public ResponseEntity<ApiResponse<PublicApplicationReceipt>> apply(@PathVariable Long jobId,
            @Valid @RequestPart("application") PublicApplicationRequest request,
            @RequestPart("resume") MultipartFile resume) throws IOException {
        JobOpening job = jobs.findById(jobId).filter(item -> "OPEN".equalsIgnoreCase(item.getStatus()))
                .orElseThrow(() -> new AppException("Vị trí này không còn nhận hồ sơ."));
        if (resume == null || resume.isEmpty() || resume.getSize() > MAX_RESUME_BYTES)
            throw new AppException("Vui lòng tải CV có dung lượng tối đa 5 MB.");
        String contentType = validateResume(resume);
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        Candidate candidate = candidates.findByEmailIgnoreCase(email).orElseGet(Candidate::new);
        candidate.setFullName(request.fullName().trim());
        candidate.setEmail(email);
        candidate.setPhone(request.phone() == null ? "" : request.phone().trim());
        String source = request.referralSource() == null || request.referralSource().isBlank()
                ? "Cổng tuyển dụng" : request.referralSource().trim();
        candidate.setSource(source);
        candidate.setReferralSource(source);
        candidate.setResumeFileName(safeFileName(resume.getOriginalFilename()));
        candidate.setResumeContentType(contentType);
        candidate.setResumeData(resume.getBytes());
        candidate = candidates.save(candidate);
        if (applications.existsByCandidate_IdAndJob_Id(candidate.getId(), job.getId()))
            throw new AppException("Email này đã ứng tuyển vị trí này.");
        Application application = new Application();
        application.setCandidate(candidate);
        application.setJob(job);
        application.setCoverLetter(request.coverLetter() == null ? "" : request.coverLetter().trim());
        application.setTrackingCode(UUID.randomUUID().toString().replace("-", ""));
        application = applications.save(application);
        notificationService.notifyStaff("Hồ sơ ứng tuyển mới", request.fullName().trim() + " đã ứng tuyển vị trí " + job.getTitle() + ".");
        notificationService.notifyCandidate(application, "APPLICATION_RECEIVED");
        return ResponseEntity.ok(ApiResponse.success("Đã nhận hồ sơ ứng tuyển",
                new PublicApplicationReceipt(application.getTrackingCode(), application.getStage(), job.getTitle(), application.getAppliedAt())));
    }

    @GetMapping("/applications/{trackingCode}")
    public ResponseEntity<ApiResponse<PublicApplicationStatus>> status(@PathVariable String trackingCode) {
        Application application = findByCode(trackingCode);
        return ResponseEntity.ok(ApiResponse.success("Trạng thái hồ sơ",
                new PublicApplicationStatus(application.getTrackingCode(), application.getCandidate().getFullName(),
                        application.getJob().getTitle(), application.getStage(), application.getInterviewAt(), application.getAppliedAt())));
    }

    @PostMapping("/applications/{trackingCode}/withdraw")
    @Transactional
    public ResponseEntity<ApiResponse<PublicApplicationStatus>> withdraw(@PathVariable String trackingCode) {
        Application application = findByCode(trackingCode);
        if (List.of("HIRED", "REJECTED", "WITHDRAWN").contains(application.getStage()))
            throw new AppException("Hồ sơ ở trạng thái hiện tại không thể rút.");
        application.setStage("WITHDRAWN");
        applications.save(application);
        return ResponseEntity.ok(ApiResponse.success("Đã rút hồ sơ ứng tuyển",
                new PublicApplicationStatus(application.getTrackingCode(), application.getCandidate().getFullName(),
                        application.getJob().getTitle(), application.getStage(), application.getInterviewAt(), application.getAppliedAt())));
    }

    private Application findByCode(String code) {
        if (code == null || !code.matches("[a-fA-F0-9]{32}")) throw new AppException("Mã tra cứu không hợp lệ.");
        return applications.findByTrackingCode(code.toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new AppException("Không tìm thấy hồ sơ ứng tuyển."));
    }

    private static String validateResume(MultipartFile resume) throws IOException {
        String name = safeFileName(resume.getOriginalFilename()).toLowerCase(Locale.ROOT);
        byte[] bytes = resume.getBytes();
        boolean pdf = name.endsWith(".pdf") && new String(bytes, 0, Math.min(bytes.length, 5), StandardCharsets.US_ASCII).startsWith("%PDF-");
        boolean docx = name.endsWith(".docx") && bytes.length >= 4 && bytes[0] == 0x50 && bytes[1] == 0x4b;
        if (!pdf && !docx) throw new AppException("CV phải là tệp PDF hoặc DOCX hợp lệ.");
        return pdf ? "application/pdf" : "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
    }

    private static String safeFileName(String filename) {
        if (filename == null || filename.isBlank()) return "resume";
        String safe = filename.replace('\\', '/');
        safe = safe.substring(safe.lastIndexOf('/') + 1).replaceAll("[^A-Za-z0-9._-]", "_");
        return safe.length() > 240 ? safe.substring(safe.length() - 240) : safe;
    }

    public record PublicApplicationRequest(@NotBlank @Size(max = 160) String fullName,
            @NotBlank @Email @Size(max = 254) String email, @Size(max = 40) String phone,
            @Size(max = 100) String referralSource, @Size(max = 3000) String coverLetter) {}
    public record PublicApplicationReceipt(String trackingCode, String stage, String jobTitle, LocalDateTime appliedAt) {}
    public record PublicApplicationStatus(String trackingCode, String candidateName, String jobTitle, String stage,
            LocalDateTime interviewAt, LocalDateTime appliedAt) {}
}
