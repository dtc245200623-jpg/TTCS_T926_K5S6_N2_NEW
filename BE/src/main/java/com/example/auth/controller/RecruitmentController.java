package com.example.auth.controller;

import com.example.auth.dto.ApiResponse;
import com.example.auth.entity.Application;
import com.example.auth.entity.CandidateEvaluation;
import com.example.auth.entity.Candidate;
import com.example.auth.entity.JobOpening;
import com.example.auth.exception.AppException;
import com.example.auth.repository.ApplicationRepository;
import com.example.auth.repository.CandidateEvaluationRepository;
import com.example.auth.repository.CandidateRepository;
import com.example.auth.repository.JobOpeningRepository;
import com.example.auth.repository.UserRepository;
import com.example.auth.service.RecruitmentNotificationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/recruitment")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@org.springframework.security.access.prepost.PreAuthorize("@accessPolicy.isInternal(authentication)")
public class RecruitmentController {
    private static final Set<String> STAGES = Set.of("NEW", "SCREENING", "INTERVIEW", "OFFER", "HIRED", "REJECTED", "WITHDRAWN");

    private final JobOpeningRepository jobs;
    private final CandidateRepository candidates;
    private final ApplicationRepository applications;
    private final CandidateEvaluationRepository evaluations;
    private final UserRepository users;
    private final RecruitmentNotificationService notificationService;

    @GetMapping("/jobs")
    @org.springframework.security.access.prepost.PreAuthorize("@accessPolicy.has(authentication, 'VIEW_JOBS')")
    public ResponseEntity<ApiResponse<List<JobOpening>>> listJobs() {
        return ResponseEntity.ok(ApiResponse.success("Danh sách vị trí tuyển dụng", jobs.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))));
    }

    @GetMapping("/my-applications")
    @org.springframework.security.access.prepost.PreAuthorize("@accessPolicy.has(authentication, 'APPLY_FOR_JOB')")
    public ResponseEntity<ApiResponse<List<ApplicationView>>> myApplications(Authentication authentication) {
        var user = users.findByUsername(authentication.getName())
                .orElseThrow(() -> new AppException("Không tìm thấy tài khoản nhân viên"));
        if (user.getEmail() == null || user.getEmail().isBlank())
            throw new AppException("Tài khoản chưa có email. Hãy liên hệ quản trị viên để cập nhật hồ sơ.");
        var result = applications.findByCandidate_EmailIgnoreCaseOrderByAppliedAtDesc(user.getEmail()).stream()
                .map(ApplicationView::from).toList();
        return ResponseEntity.ok(ApiResponse.success("Danh sách hồ sơ ứng tuyển của bạn", result));
    }

    @PostMapping("/jobs/{jobId}/apply")
    @org.springframework.security.access.prepost.PreAuthorize("@accessPolicy.has(authentication, 'APPLY_FOR_JOB')")
    @Transactional
    public ResponseEntity<ApiResponse<ApplicationView>> applyToJob(@PathVariable Long jobId, Authentication authentication) {
        var user = users.findByUsername(authentication.getName())
                .orElseThrow(() -> new AppException("Không tìm thấy tài khoản nhân viên"));
        if (user.getEmail() == null || user.getEmail().isBlank())
            throw new AppException("Tài khoản chưa có email. Hãy liên hệ quản trị viên để cập nhật hồ sơ.");
        JobOpening job = jobs.findById(jobId).orElseThrow(() -> new AppException("Không tìm thấy vị trí tuyển dụng"));
        if (!"OPEN".equalsIgnoreCase(job.getStatus())) throw new AppException("Vị trí tuyển dụng này đã đóng");

        String email = user.getEmail().trim().toLowerCase();
        Candidate candidate = candidates.findByEmailIgnoreCase(email).orElseGet(() -> {
            Candidate created = new Candidate();
            created.setFullName(user.getUsername());
            created.setEmail(email);
            created.setSource("Ứng tuyển nội bộ");
            return candidates.save(created);
        });
        if (applications.existsByCandidate_IdAndJob_Id(candidate.getId(), job.getId()))
            throw new AppException("Bạn đã ứng tuyển vị trí này rồi");

        Application application = new Application();
        application.setCandidate(candidate);
        application.setJob(job);
        return ResponseEntity.ok(ApiResponse.success("Ứng tuyển thành công", ApplicationView.from(applications.save(application))));
    }

    @PostMapping("/jobs")
    @org.springframework.security.access.prepost.PreAuthorize("@accessPolicy.has(authentication, 'MANAGE_JOBS')")
    public ResponseEntity<ApiResponse<JobOpening>> createJob(@Valid @RequestBody JobRequest request) {
        JobOpening job = new JobOpening();
        job.setTitle(request.title().trim());
        job.setDepartment(request.department().trim());
        job.setHeadcount(request.headcount());
        job.setDescription(request.description() == null ? "" : request.description().trim());
        return ResponseEntity.ok(ApiResponse.success("Đã tạo vị trí tuyển dụng", jobs.save(job)));
    }

    @PutMapping("/jobs/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("@accessPolicy.has(authentication, 'MANAGE_JOBS')")
    public ResponseEntity<ApiResponse<JobOpening>> updateJob(@PathVariable Long id, @Valid @RequestBody JobRequest request) {
        JobOpening job = jobs.findById(id).orElseThrow(() -> new AppException("Không tìm thấy vị trí tuyển dụng"));
        job.setTitle(request.title().trim());
        job.setDepartment(request.department().trim());
        job.setHeadcount(request.headcount());
        job.setDescription(request.description() == null ? "" : request.description().trim());
        return ResponseEntity.ok(ApiResponse.success("Đã cập nhật vị trí tuyển dụng", jobs.save(job)));
    }

    @GetMapping("/candidates")
    @org.springframework.security.access.prepost.PreAuthorize("@accessPolicy.has(authentication, 'VIEW_CANDIDATES')")
    public ResponseEntity<ApiResponse<List<Candidate>>> listCandidates() {
        return ResponseEntity.ok(ApiResponse.success("Danh sách ứng viên", candidates.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))));
    }

    @PostMapping("/candidates")
    @org.springframework.security.access.prepost.PreAuthorize("@accessPolicy.has(authentication, 'MANAGE_CANDIDATES')")
    public ResponseEntity<ApiResponse<Candidate>> createCandidate(@Valid @RequestBody CandidateRequest request) {
        String email = request.email().trim().toLowerCase();
        if (candidates.findByEmailIgnoreCase(email).isPresent()) throw new AppException("Email ứng viên đã tồn tại");
        Candidate candidate = new Candidate();
        candidate.setFullName(request.fullName().trim());
        candidate.setEmail(email);
        candidate.setPhone(request.phone() == null ? "" : request.phone().trim());
        candidate.setSource(request.source() == null || request.source().isBlank() ? "Trực tiếp" : request.source().trim());
        return ResponseEntity.ok(ApiResponse.success("Đã tạo hồ sơ ứng viên", candidates.save(candidate)));
    }

    @PutMapping("/candidates/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("@accessPolicy.has(authentication, 'MANAGE_CANDIDATES')")
    public ResponseEntity<ApiResponse<Candidate>> updateCandidate(@PathVariable Long id, @Valid @RequestBody CandidateRequest request) {
        Candidate candidate = candidates.findById(id).orElseThrow(() -> new AppException("Không tìm thấy ứng viên"));
        String email = request.email().trim().toLowerCase();
        candidates.findByEmailIgnoreCase(email).filter(other -> !other.getId().equals(id))
                .ifPresent(other -> { throw new AppException("Email ứng viên đã tồn tại"); });
        candidate.setFullName(request.fullName().trim());
        candidate.setEmail(email);
        candidate.setPhone(request.phone() == null ? "" : request.phone().trim());
        candidate.setSource(request.source() == null || request.source().isBlank() ? "Trực tiếp" : request.source().trim());
        return ResponseEntity.ok(ApiResponse.success("Đã cập nhật hồ sơ ứng viên", candidates.save(candidate)));
    }

    @GetMapping("/applications")
    @org.springframework.security.access.prepost.PreAuthorize("@accessPolicy.has(authentication, 'VIEW_APPLICATIONS')")
    public ResponseEntity<ApiResponse<List<ApplicationView>>> listApplications() {
        var result = applications.findAll(Sort.by(Sort.Direction.DESC, "appliedAt")).stream()
                .map(ApplicationView::from).toList();
        return ResponseEntity.ok(ApiResponse.success("Danh sách hồ sơ ứng tuyển", result));
    }

    @GetMapping("/onboarding")
    @org.springframework.security.access.prepost.PreAuthorize("@accessPolicy.has(authentication, 'MANAGE_ONBOARDING')")
    public ResponseEntity<ApiResponse<List<OnboardingView>>> listOnboarding() {
        var result = applications.findAll(Sort.by(Sort.Direction.DESC, "appliedAt")).stream()
                .filter(application -> "OFFER".equals(application.getStage()) || "HIRED".equals(application.getStage()))
                .map(application -> new OnboardingView(ApplicationView.from(application),
                        evaluations.findByApplication_Id(application.getId()).map(CandidateEvaluation::getSalaryOffer).orElse("")))
                .toList();
        return ResponseEntity.ok(ApiResponse.success("Danh sách tiếp nhận nhân sự", result));
    }

    @PatchMapping("/applications/{id}/onboarding")
    @org.springframework.security.access.prepost.PreAuthorize("@accessPolicy.has(authentication, 'MANAGE_ONBOARDING')")
    @Transactional
    public ResponseEntity<ApiResponse<ApplicationView>> updateOnboarding(@PathVariable Long id,
                                                                          @Valid @RequestBody OnboardingRequest request) {
        Application application = applications.findById(id)
                .orElseThrow(() -> new AppException("Không tìm thấy hồ sơ ứng tuyển"));
        if (Boolean.TRUE.equals(request.salaryAccepted())
                && !"OFFER".equals(application.getStage()) && !"HIRED".equals(application.getStage()))
            throw new AppException("Chỉ ghi nhận chấp nhận lương khi hồ sơ đã đến bước offer.");
        if (Boolean.TRUE.equals(request.salaryAccepted())
                && (request.agreedSalary() == null || request.agreedSalary().isBlank())
                && (application.getAgreedSalary() == null || application.getAgreedSalary().isBlank()))
            throw new AppException("Vui lòng nhập mức lương đã được hai bên thống nhất.");
        if (Boolean.TRUE.equals(request.offerAccepted())) {
            if (!"OFFER".equals(application.getStage()) && !"HIRED".equals(application.getStage()))
                throw new AppException("Chỉ xác nhận nhận việc cho hồ sơ đã đến bước offer.");
            if (request.startDate() == null && application.getStartDate() == null)
                throw new AppException("Vui lòng chọn ngày bắt đầu làm việc.");
            if (!Boolean.TRUE.equals(request.salaryAccepted()) && !Boolean.TRUE.equals(application.getSalaryAccepted()))
                throw new AppException("Cần xác nhận ứng viên đã chấp nhận mức lương trước khi ghi nhận nhận việc.");
            if ((request.agreedSalary() == null || request.agreedSalary().isBlank())
                    && (application.getAgreedSalary() == null || application.getAgreedSalary().isBlank()))
                throw new AppException("Vui lòng nhập mức lương đã được hai bên thống nhất.");
            application.setStage("HIRED");
            if (application.getOfferAcceptedAt() == null) application.setOfferAcceptedAt(java.time.LocalDateTime.now());
        }
        if (request.startDate() != null) application.setStartDate(request.startDate());
        if (request.salaryAccepted() != null) application.setSalaryAccepted(request.salaryAccepted());
        if (request.agreedSalary() != null) application.setAgreedSalary(request.agreedSalary().trim());
        if (request.documentsReceived() != null) application.setDocumentsReceived(request.documentsReceived());
        if (request.accountReady() != null) application.setAccountReady(request.accountReady());
        if (request.equipmentReady() != null) application.setEquipmentReady(request.equipmentReady());
        if (request.orientationComplete() != null) application.setOrientationComplete(request.orientationComplete());
        return ResponseEntity.ok(ApiResponse.success("Đã cập nhật kế hoạch nhận việc", ApplicationView.from(applications.save(application))));
    }

    @PostMapping("/applications")
    @org.springframework.security.access.prepost.PreAuthorize("@accessPolicy.has(authentication, 'CREATE_APPLICATION')")
    public ResponseEntity<ApiResponse<ApplicationView>> createApplication(@Valid @RequestBody ApplicationRequest request) {
        Candidate candidate = candidates.findById(request.candidateId())
                .orElseThrow(() -> new AppException("Không tìm thấy ứng viên"));
        JobOpening job = jobs.findById(request.jobId())
                .orElseThrow(() -> new AppException("Không tìm thấy vị trí tuyển dụng"));
        if (applications.existsByCandidate_IdAndJob_Id(candidate.getId(), job.getId()))
            throw new AppException("Ứng viên đã được thêm vào quy trình của vị trí này");
        Application application = new Application();
        application.setCandidate(candidate);
        application.setJob(job);
        return ResponseEntity.ok(ApiResponse.success("Đã thêm ứng viên vào quy trình tuyển dụng", ApplicationView.from(applications.save(application))));
    }

    @PatchMapping("/applications/{id}/stage")
    @org.springframework.security.access.prepost.PreAuthorize("@accessPolicy.has(authentication, 'MANAGE_PIPELINE')")
    public ResponseEntity<ApiResponse<ApplicationView>> changeStage(@PathVariable Long id, @Valid @RequestBody StageRequest request) {
        String stage = request.stage().trim().toUpperCase();
        if (!STAGES.contains(stage)) throw new AppException("Trạng thái quy trình không hợp lệ");
        Application application = applications.findById(id)
                .orElseThrow(() -> new AppException("Không tìm thấy hồ sơ ứng tuyển"));
        application.setStage(stage);
        Application saved = applications.save(application);
        notificationService.notifyCandidate(saved, "STAGE_CHANGED");
        return ResponseEntity.ok(ApiResponse.success("Đã cập nhật giai đoạn tuyển dụng", ApplicationView.from(saved)));
    }

    @PatchMapping("/applications/{id}/interview")
    @org.springframework.security.access.prepost.PreAuthorize("@accessPolicy.has(authentication, 'SCHEDULE_INTERVIEWS')")
    public ResponseEntity<ApiResponse<ApplicationView>> scheduleInterview(@PathVariable Long id, @Valid @RequestBody InterviewRequest request) {
        Application application = applications.findById(id)
                .orElseThrow(() -> new AppException("Không tìm thấy hồ sơ ứng tuyển"));
        application.setInterviewAt(request.interviewAt());
        if (request.interviewAt() != null && "NEW".equals(application.getStage())) application.setStage("SCREENING");
        Application saved = applications.save(application);
        notificationService.notifyCandidate(saved, "INTERVIEW_SCHEDULED");
        return ResponseEntity.ok(ApiResponse.success("Đã cập nhật lịch phỏng vấn", ApplicationView.from(saved)));
    }

    @GetMapping("/applications/{id}/evaluation")
    @org.springframework.security.access.prepost.PreAuthorize("@accessPolicy.has(authentication, 'VIEW_EVALUATIONS')")
    public ResponseEntity<ApiResponse<EvaluationView>> getEvaluation(@PathVariable Long id) {
        if (!applications.existsById(id)) throw new AppException("Không tìm thấy hồ sơ ứng tuyển");
        var evaluation = evaluations.findByApplication_Id(id).orElse(null);
        return ResponseEntity.ok(ApiResponse.success("Phiếu đánh giá ứng viên", evaluation == null ? null : EvaluationView.from(evaluation)));
    }

    @PutMapping("/applications/{id}/evaluation")
    @org.springframework.security.access.prepost.PreAuthorize("@accessPolicy.has(authentication, 'SUBMIT_EVALUATIONS')")
    @Transactional
    public ResponseEntity<ApiResponse<EvaluationView>> saveEvaluation(@PathVariable Long id, @Valid @RequestBody EvaluationRequest request) {
        Application application = applications.findById(id)
                .orElseThrow(() -> new AppException("Không tìm thấy hồ sơ ứng tuyển"));
        CandidateEvaluation evaluation = evaluations.findByApplication_Id(id).orElseGet(CandidateEvaluation::new);
        evaluation.setApplication(application);
        evaluation.setSkillScore(request.skillScore());
        evaluation.setProblemSolvingScore(request.problemSolvingScore());
        evaluation.setComments(request.comments() == null ? "" : request.comments().trim());
        evaluation.setSalaryOffer(request.salaryOffer() == null ? "" : request.salaryOffer().trim());
        evaluation.setDecision(request.decision() == null || request.decision().isBlank() ? null : request.decision().trim().toUpperCase());
        evaluation.setUpdatedAt(java.time.LocalDateTime.now());
        return ResponseEntity.ok(ApiResponse.success("Đã lưu phiếu đánh giá", EvaluationView.from(evaluations.save(evaluation))));
    }

    public record JobRequest(@NotBlank @Size(max=160) String title, @NotBlank @Size(max=120) String department,
                             @Min(1) int headcount, @Size(max=4000) String description) {}
    public record CandidateRequest(@NotBlank @Size(max=160) String fullName, @NotBlank @Email @Size(max=254) String email,
                                   @Size(max=40) String phone, @Size(max=100) String source) {}
    public record ApplicationRequest(@NotNull Long candidateId, @NotNull Long jobId) {}
    public record StageRequest(@NotBlank String stage) {}
    public record InterviewRequest(@NotNull java.time.LocalDateTime interviewAt) {}
    public record OnboardingRequest(Boolean offerAccepted, Boolean salaryAccepted, String agreedSalary,
                                    java.time.LocalDate startDate, Boolean documentsReceived,
                                    Boolean accountReady, Boolean equipmentReady, Boolean orientationComplete) {}
    public record EvaluationRequest(@Min(1) @jakarta.validation.constraints.Max(5) Integer skillScore,
                                    @Min(1) @jakarta.validation.constraints.Max(5) Integer problemSolvingScore,
                                    @Size(max=5000) String comments, @Size(max=100) String salaryOffer,
                                    @jakarta.validation.constraints.Pattern(regexp="^(PASS|CONSIDER|FAIL)?$", message="Quyết định không hợp lệ") String decision) {}
    public record EvaluationView(Long id, Long applicationId, Integer skillScore, Integer problemSolvingScore,
                                 String comments, String salaryOffer, String decision, java.time.LocalDateTime updatedAt) {
        static EvaluationView from(CandidateEvaluation e) {
            return new EvaluationView(e.getId(), e.getApplication().getId(), e.getSkillScore(), e.getProblemSolvingScore(),
                    e.getComments(), e.getSalaryOffer(), e.getDecision(), e.getUpdatedAt());
        }
    }
    public record ApplicationView(Long id, Long candidateId, String candidateName, String candidateEmail,
                                  Long jobId, String jobTitle, String department, String stage, java.time.LocalDateTime appliedAt,
                                  java.time.LocalDateTime interviewAt, java.time.LocalDate startDate,
                                  java.time.LocalDateTime offerAcceptedAt, boolean documentsReceived,
                                  boolean accountReady, boolean equipmentReady, boolean orientationComplete,
                                  boolean salaryAccepted, String agreedSalary) {
        static ApplicationView from(Application a) {
            return new ApplicationView(a.getId(), a.getCandidate().getId(), a.getCandidate().getFullName(), a.getCandidate().getEmail(),
                    a.getJob().getId(), a.getJob().getTitle(), a.getJob().getDepartment(), a.getStage(), a.getAppliedAt(), a.getInterviewAt(),
                    a.getStartDate(), a.getOfferAcceptedAt(), Boolean.TRUE.equals(a.getDocumentsReceived()),
                    Boolean.TRUE.equals(a.getAccountReady()), Boolean.TRUE.equals(a.getEquipmentReady()),
                    Boolean.TRUE.equals(a.getOrientationComplete()), Boolean.TRUE.equals(a.getSalaryAccepted()), a.getAgreedSalary());
        }
    }
    public record OnboardingView(ApplicationView application, String proposedSalary) {}
}
