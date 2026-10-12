package com.example.auth.controller;

import com.example.auth.dto.ApiResponse;
import com.example.auth.entity.InAppNotification;
import com.example.auth.entity.EmailTemplate;
import com.example.auth.entity.JobOpening;
import com.example.auth.entity.OrganizationCatalogItem;
import com.example.auth.entity.RecruitmentRequisition;
import com.example.auth.entity.RequisitionApproval;
import com.example.auth.exception.AppException;
import com.example.auth.repository.InAppNotificationRepository;
import com.example.auth.repository.EmailTemplateRepository;
import com.example.auth.repository.JobOpeningRepository;
import com.example.auth.repository.OrganizationCatalogRepository;
import com.example.auth.repository.RecruitmentRequisitionRepository;
import com.example.auth.repository.RequisitionApprovalRepository;
import com.example.auth.repository.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/operations")
@RequiredArgsConstructor
public class OperationsController {
    private static final Set<String> CATALOG_TYPES = Set.of("DEPARTMENT", "JOB_TITLE", "COMPETENCY", "INTERVIEW_QUESTION");
    private final OrganizationCatalogRepository catalogs;
    private final RecruitmentRequisitionRepository requisitions;
    private final RequisitionApprovalRepository approvals;
    private final JobOpeningRepository jobs;
    private final UserRepository users;
    private final InAppNotificationRepository notifications;
    private final EmailTemplateRepository emailTemplates;

    @Value("${app.requisition-second-approval-threshold:50000000}")
    private BigDecimal secondApprovalThreshold;

    @GetMapping("/catalog/{type}")
    @PreAuthorize("@accessPolicy.has(authentication, 'MANAGE_CATALOGS')")
    public ResponseEntity<ApiResponse<List<OrganizationCatalogItem>>> catalog(@PathVariable String type) {
        return ResponseEntity.ok(ApiResponse.success("Danh mục tổ chức", catalogs.findByCatalogTypeOrderByNameAsc(normalizeType(type)).stream()
                .filter(OrganizationCatalogItem::isActive).toList()));
    }

    @PostMapping("/catalog/{type}")
    @PreAuthorize("@accessPolicy.has(authentication, 'MANAGE_CATALOGS')")
    public ResponseEntity<ApiResponse<OrganizationCatalogItem>> createCatalogItem(@PathVariable String type,
            @Valid @RequestBody CatalogRequest request) {
        String catalogType = normalizeType(type);
        String name = request.name().trim();
        if (catalogs.findByCatalogTypeAndNameIgnoreCase(catalogType, name).isPresent())
            throw new AppException("Mục này đã có trong danh mục.");
        OrganizationCatalogItem item = new OrganizationCatalogItem();
        item.setCatalogType(catalogType);
        item.setName(name);
        item.setDescription(request.description() == null ? "" : request.description().trim());
        item.setSalaryMin(request.salaryMin());
        item.setSalaryMax(request.salaryMax());
        validateSalaryRange(request.salaryMin(), request.salaryMax());
        return ResponseEntity.ok(ApiResponse.success("Đã thêm mục danh mục", catalogs.save(item)));
    }

    @PutMapping("/catalog/{type}/{id}")
    @PreAuthorize("@accessPolicy.has(authentication, 'MANAGE_CATALOGS')")
    public ResponseEntity<ApiResponse<OrganizationCatalogItem>> updateCatalogItem(@PathVariable String type,
            @PathVariable Long id, @Valid @RequestBody CatalogRequest request) {
        String catalogType = normalizeType(type);
        OrganizationCatalogItem item = catalogs.findById(id).filter(value -> value.getCatalogType().equals(catalogType))
                .orElseThrow(() -> new AppException("Không tìm thấy mục danh mục."));
        catalogs.findByCatalogTypeAndNameIgnoreCase(catalogType, request.name().trim())
                .filter(other -> !other.getId().equals(id))
                .ifPresent(other -> { throw new AppException("Tên này đã được dùng trong danh mục."); });
        validateSalaryRange(request.salaryMin(), request.salaryMax());
        item.setName(request.name().trim());
        item.setDescription(request.description() == null ? "" : request.description().trim());
        item.setSalaryMin(request.salaryMin());
        item.setSalaryMax(request.salaryMax());
        return ResponseEntity.ok(ApiResponse.success("Đã cập nhật mục danh mục", catalogs.save(item)));
    }

    @DeleteMapping("/catalog/{type}/{id}")
    @PreAuthorize("@accessPolicy.has(authentication, 'MANAGE_CATALOGS')")
    public ResponseEntity<ApiResponse<Void>> deactivateCatalogItem(@PathVariable String type, @PathVariable Long id) {
        OrganizationCatalogItem item = catalogs.findById(id).filter(value -> value.getCatalogType().equals(normalizeType(type)))
                .orElseThrow(() -> new AppException("Không tìm thấy mục danh mục."));
        item.setActive(false);
        catalogs.save(item);
        return ResponseEntity.ok(ApiResponse.success("Đã ngừng sử dụng mục danh mục."));
    }

    @GetMapping("/requisitions")
    @PreAuthorize("@accessPolicy.has(authentication, 'VIEW_REQUISITIONS')")
    public ResponseEntity<ApiResponse<List<RequisitionView>>> listRequisitions() {
        return ResponseEntity.ok(ApiResponse.success("Danh sách yêu cầu tuyển dụng",
                requisitions.findAllByOrderByCreatedAtDesc().stream().map(this::view).toList()));
    }

    @PostMapping("/requisitions")
    @PreAuthorize("@accessPolicy.has(authentication, 'MANAGE_REQUISITIONS')")
    public ResponseEntity<ApiResponse<RequisitionView>> createRequisition(@Valid @RequestBody RequisitionRequest request,
            Authentication authentication) {
        validateSalaryRange(request.salaryMin(), request.salaryMax());
        RecruitmentRequisition item = new RecruitmentRequisition();
        item.setTitle(request.title().trim());
        item.setDepartment(request.department().trim());
        item.setHeadcount(request.headcount());
        item.setJustification(request.justification().trim());
        item.setSalaryMin(request.salaryMin());
        item.setSalaryMax(request.salaryMax());
        item.setRequestedBy(authentication.getName());
        item.setAssignedRecruiter(validateRecruiter(request.assignedRecruiter()));
        item.setApprovalsRequired(request.salaryMax() != null && request.salaryMax().compareTo(secondApprovalThreshold) > 0 ? 2 : 1);
        RecruitmentRequisition saved = requisitions.save(item);
        notifyUser(authentication.getName(), "Yêu cầu đã được tạo", "Yêu cầu tuyển dụng #" + saved.getId() + " đang chờ phê duyệt.");
        return ResponseEntity.ok(ApiResponse.success("Đã gửi yêu cầu tuyển dụng để phê duyệt", view(saved)));
    }

    @PostMapping("/requisitions/{id}/decision")
    @PreAuthorize("@accessPolicy.has(authentication, 'APPROVE_REQUISITIONS')")
    @Transactional
    public ResponseEntity<ApiResponse<RequisitionView>> decideRequisition(@PathVariable Long id,
            @Valid @RequestBody DecisionRequest request, Authentication authentication) {
        RecruitmentRequisition item = requisitions.findById(id).orElseThrow(() -> new AppException("Không tìm thấy yêu cầu tuyển dụng."));
        if (!"PENDING_APPROVAL".equals(item.getStatus())) throw new AppException("Yêu cầu này không còn chờ phê duyệt.");
        int step = item.getApprovalsReceived() + 1;
        String authority = authentication.getAuthorities().stream().map(Object::toString).findFirst().orElse("");
        boolean admin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!admin && step == 1 && !authority.equals("ROLE_HR_MANAGER"))
            throw new AppException("Bước phê duyệt đầu tiên cần Trưởng phòng Nhân sự.");
        if (!admin && step > 1 && !authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_APPROVER")))
            throw new AppException("Bước phê duyệt tiếp theo cần Người duyệt.");
        if (approvals.existsByRequisition_IdAndApprovalStepAndApproverUsername(id, step, authentication.getName()))
            throw new AppException("Bạn đã xử lý bước phê duyệt này.");
        RequisitionApproval decision = new RequisitionApproval();
        decision.setRequisition(item);
        decision.setApprovalStep(step);
        decision.setApproverUsername(authentication.getName());
        decision.setDecision(request.approved() ? "APPROVED" : "REJECTED");
        decision.setComment(request.comment() == null ? "" : request.comment().trim());
        approvals.save(decision);
        if (!request.approved()) {
            item.setStatus("REJECTED");
        } else {
            item.setApprovalsReceived(step);
            if (step >= item.getApprovalsRequired()) {
                item.setStatus("APPROVED");
                JobOpening job = new JobOpening();
                job.setTitle(item.getTitle());
                job.setDepartment(item.getDepartment());
                job.setHeadcount(item.getHeadcount());
                job.setDescription(item.getJustification());
                job.setStatus("DRAFT");
                item.setPublishedJobId(jobs.save(job).getId());
            }
        }
        item.setUpdatedAt(LocalDateTime.now());
        requisitions.save(item);
        notifyUser(item.getRequestedBy(), request.approved() ? "Yêu cầu được phê duyệt" : "Yêu cầu bị từ chối",
                "Yêu cầu tuyển dụng #" + id + " hiện có trạng thái " + item.getStatus() + ".");
        if (item.getAssignedRecruiter() != null && "APPROVED".equals(item.getStatus()))
            notifyUser(item.getAssignedRecruiter(), "Có vị trí tuyển dụng mới", item.getTitle() + " đã được phê duyệt.");
        return ResponseEntity.ok(ApiResponse.success("Đã ghi nhận quyết định", view(item)));
    }

    @PostMapping("/requisitions/{id}/publish")
    @PreAuthorize("@accessPolicy.has(authentication, 'MANAGE_JOBS')")
    public ResponseEntity<ApiResponse<RequisitionView>> publishRequisition(@PathVariable Long id) {
        RecruitmentRequisition item = requisitions.findById(id).orElseThrow(() -> new AppException("Không tìm thấy yêu cầu tuyển dụng."));
        if (!"APPROVED".equals(item.getStatus()) || item.getPublishedJobId() == null)
            throw new AppException("Chỉ được đăng vị trí sau khi yêu cầu đã được phê duyệt.");
        JobOpening job = jobs.findById(item.getPublishedJobId()).orElseThrow(() -> new AppException("Không tìm thấy vị trí được tạo từ yêu cầu."));
        job.setStatus("OPEN");
        jobs.save(job);
        item.setStatus("PUBLISHED");
        item.setUpdatedAt(LocalDateTime.now());
        return ResponseEntity.ok(ApiResponse.success("Vị trí tuyển dụng đã được đăng công khai", view(requisitions.save(item))));
    }

    @GetMapping("/notifications")
    public ResponseEntity<ApiResponse<List<InAppNotification>>> notifications(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success("Thông báo của bạn",
                notifications.findTop100ByRecipientUsernameOrderByCreatedAtDesc(authentication.getName())));
    }

    @PostMapping("/notifications/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markNotificationRead(@PathVariable Long id, Authentication authentication) {
        InAppNotification notification = notifications.findById(id)
                .filter(item -> item.getRecipientUsername().equals(authentication.getName()))
                .orElseThrow(() -> new AppException("Không tìm thấy thông báo."));
        notification.setRead(true);
        notifications.save(notification);
        return ResponseEntity.ok(ApiResponse.success("Đã đánh dấu thông báo đã đọc."));
    }

    @GetMapping("/email-templates")
    @PreAuthorize("@accessPolicy.has(authentication, 'MANAGE_NOTIFICATIONS')")
    public ResponseEntity<ApiResponse<List<EmailTemplate>>> emailTemplates() {
        return ResponseEntity.ok(ApiResponse.success("Mẫu email", emailTemplates.findAllByOrderByTemplateKeyAsc()));
    }

    @PutMapping("/email-templates/{key}")
    @PreAuthorize("@accessPolicy.has(authentication, 'MANAGE_NOTIFICATIONS')")
    public ResponseEntity<ApiResponse<EmailTemplate>> updateEmailTemplate(@PathVariable String key,
            @Valid @RequestBody EmailTemplateRequest request, Authentication authentication) {
        String normalizedKey = key.toUpperCase(Locale.ROOT);
        if (!Set.of("APPLICATION_RECEIVED", "STAGE_CHANGED", "INTERVIEW_SCHEDULED").contains(normalizedKey))
            throw new AppException("Mã mẫu email không hợp lệ.");
        EmailTemplate template = emailTemplates.findByTemplateKey(normalizedKey).orElseGet(EmailTemplate::new);
        template.setTemplateKey(normalizedKey);
        template.setSubject(request.subject().trim());
        template.setBody(request.body().trim());
        template.setEnabled(request.enabled());
        template.setUpdatedBy(authentication.getName());
        template.setUpdatedAt(LocalDateTime.now());
        return ResponseEntity.ok(ApiResponse.success("Đã lưu mẫu email", emailTemplates.save(template)));
    }

    private String validateRecruiter(String username) {
        if (username == null || username.isBlank()) return null;
        var user = users.findByUsername(username.trim()).orElseThrow(() -> new AppException("Không tìm thấy recruiter được phân công."));
        if (!user.isActive() || !"ROLE_RECRUITER".equals(user.getRole()))
            throw new AppException("Chỉ có thể phân công cho tài khoản recruiter đang hoạt động.");
        return user.getUsername();
    }

    private void notifyUser(String username, String title, String message) {
        if (username == null || username.isBlank()) return;
        InAppNotification notification = new InAppNotification();
        notification.setRecipientUsername(username);
        notification.setTitle(title);
        notification.setMessage(message);
        notifications.save(notification);
    }

    private RequisitionView view(RecruitmentRequisition item) {
        return new RequisitionView(item.getId(), item.getTitle(), item.getDepartment(), item.getHeadcount(),
                item.getJustification(), item.getSalaryMin(), item.getSalaryMax(), item.getStatus(),
                item.getApprovalsRequired(), item.getApprovalsReceived(), item.getRequestedBy(),
                item.getAssignedRecruiter(), item.getPublishedJobId(), item.getCreatedAt(),
                approvals.findByRequisition_IdOrderByApprovalStepAsc(item.getId()).stream()
                        .map(a -> new ApprovalView(a.getApprovalStep(), a.getApproverUsername(), a.getDecision(), a.getComment(), a.getDecidedAt())).toList());
    }

    private static void validateSalaryRange(BigDecimal minimum, BigDecimal maximum) {
        if (minimum != null && minimum.signum() < 0 || maximum != null && maximum.signum() < 0)
            throw new AppException("Mức lương không được âm.");
        if (minimum != null && maximum != null && minimum.compareTo(maximum) > 0)
            throw new AppException("Mức lương tối thiểu phải nhỏ hơn mức tối đa.");
    }

    private static String normalizeType(String type) {
        String normalized = type.toUpperCase(Locale.ROOT);
        if (!CATALOG_TYPES.contains(normalized)) throw new AppException("Loại danh mục không hợp lệ.");
        return normalized;
    }

    public record CatalogRequest(@NotBlank @Size(max = 160) String name, @Size(max = 1000) String description,
                                 @DecimalMin("0") BigDecimal salaryMin, @DecimalMin("0") BigDecimal salaryMax) {}
    public record RequisitionRequest(@NotBlank @Size(max = 160) String title, @NotBlank @Size(max = 120) String department,
                                     @Min(1) @Max(500) int headcount, @NotBlank @Size(max = 3000) String justification,
                                     @DecimalMin("0") BigDecimal salaryMin, @DecimalMin("0") BigDecimal salaryMax,
                                     @Size(max = 80) String assignedRecruiter) {}
    public record DecisionRequest(@NotNull Boolean approved, @Size(max = 1000) String comment) {}
    public record EmailTemplateRequest(@NotBlank @Size(max = 180) String subject,
            @NotBlank @Size(max = 5000) String body, @NotNull Boolean enabled) {}
    public record ApprovalView(int step, String approver, String decision, String comment, LocalDateTime decidedAt) {}
    public record RequisitionView(Long id, String title, String department, int headcount, String justification,
                                  BigDecimal salaryMin, BigDecimal salaryMax, String status, int approvalsRequired,
                                  int approvalsReceived, String requestedBy, String assignedRecruiter, Long publishedJobId,
                                  LocalDateTime createdAt, List<ApprovalView> approvals) {}
}
