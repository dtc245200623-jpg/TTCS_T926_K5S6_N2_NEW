package com.example.auth.service.impl;

import com.example.auth.dto.JobRequestApprovalStepDto;
import com.example.auth.dto.JobRequestDto;
import com.example.auth.entity.ApprovalRule;
import com.example.auth.entity.ApprovalRuleStep;
import com.example.auth.entity.Department;
import com.example.auth.entity.JobRequest;
import com.example.auth.entity.JobRequestApprovalStep;
import com.example.auth.entity.User;
import com.example.auth.exception.AppException;
import com.example.auth.repository.ApprovalRuleRepository;
import com.example.auth.repository.DepartmentRepository;
import com.example.auth.repository.JobRequestApprovalStepRepository;
import com.example.auth.repository.JobRequestRepository;
import com.example.auth.repository.UserRepository;
import com.example.auth.service.JobRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobRequestServiceImpl implements JobRequestService {

    private final JobRequestRepository jobRequestRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final ApprovalRuleRepository approvalRuleRepository;
    private final JobRequestApprovalStepRepository approvalStepRepository;

    @Override
    @Transactional
    public JobRequestDto createJobRequest(JobRequestDto dto, String username) {
        User hiringManager = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException("Người dùng không tồn tại"));

        Department department = null;
        if (dto.getDepartmentId() != null) {
            department = departmentRepository.findById(dto.getDepartmentId())
                    .orElse(null);
        }

        // KN-222: Cấu hình sai (không có cấp duyệt nào khớp) bị chặn lưu
        ApprovalRule matchingRule = findMatchingRule(dto.getDepartmentId(), dto.getMaxSalary());
        if (matchingRule == null || matchingRule.getSteps() == null || matchingRule.getSteps().isEmpty()) {
            throw new AppException("Cấu hình sai (không có cấp duyệt nào khớp) bị chặn lưu: Không tìm thấy chuỗi cấp duyệt hợp lệ nào cho phòng ban và mức lương này. Vui lòng liên hệ Trưởng phòng Nhân sự để cấu hình chuỗi duyệt!");
        }

        User recruiter = null;
        if (dto.getRecruiterId() != null) {
            recruiter = userRepository.findById(dto.getRecruiterId()).orElse(null);
        }

        JobRequest jobRequest = JobRequest.builder()
                .title(dto.getTitle())
                .headcount(dto.getHeadcount())
                .minSalary(dto.getMinSalary())
                .maxSalary(dto.getMaxSalary())
                .description(dto.getDescription())
                .requirements(dto.getRequirements())
                .department(department)
                .hiringManager(hiringManager)
                .recruiter(recruiter)
                .createdAt(LocalDateTime.now())
                .status("PENDING_APPROVAL")
                .currentApprovalStep(1)
                .approvalSteps(new ArrayList<>())
                .build();

        // KN-235: Luồng đang chạy không bị ảnh hưởng khi cấu hình thay đổi
        // Tạo snapshot các bước phê duyệt độc lập từ matchingRule
        List<JobRequestApprovalStep> stepsToSave = new ArrayList<>();
        int order = 1;
        for (ApprovalRuleStep ruleStep : matchingRule.getSteps()) {
            JobRequestApprovalStep approvalStep = JobRequestApprovalStep.builder()
                    .jobRequest(jobRequest)
                    .stepOrder(order)
                    .stepName(ruleStep.getStepName())
                    .approverRole(ruleStep.getApproverRole())
                    .approverUser(ruleStep.getSpecificApprover())
                    .status(order == 1 ? "PENDING" : "WAITING")
                    .isOverThreshold(false)
                    .build();
            stepsToSave.add(approvalStep);
            order++;
        }

        // KN-221: Yêu cầu vượt hạn mức tự động thêm cấp duyệt cao hơn
        boolean isOver = false;
        if (dto.getMaxSalary() != null && matchingRule.getMaxSalary() != null && dto.getMaxSalary() > matchingRule.getMaxSalary()) {
            isOver = true;
            String overRole = matchingRule.getOverThresholdRole() != null && !matchingRule.getOverThresholdRole().isBlank()
                    ? matchingRule.getOverThresholdRole()
                    : "ROLE_APPROVER";

            String roleName = "Ban Giám đốc (Người duyệt)";
            if ("ROLE_APPROVER".equals(overRole)) roleName = "Ban Giám đốc (Người duyệt)";
            else if ("ROLE_HR_MANAGER".equals(overRole)) roleName = "Trưởng phòng Nhân sự";

            JobRequestApprovalStep extraStep = JobRequestApprovalStep.builder()
                    .jobRequest(jobRequest)
                    .stepOrder(order)
                    .stepName("Ban Giám đốc (Duyệt vượt hạn mức lương)")
                    .approverRole(overRole)
                    .status("WAITING")
                    .isOverThreshold(true)
                    .build();
            stepsToSave.add(extraStep);
        }

        jobRequest.setIsOverBudget(isOver);
        jobRequest.getApprovalSteps().addAll(stepsToSave);

        JobRequest saved = jobRequestRepository.save(jobRequest);
        return mapToDto(saved);
    }

    private ApprovalRule findMatchingRule(Long departmentId, Double maxSalary) {
        List<ApprovalRule> rules = approvalRuleRepository.findMatchingRulesForDepartment(departmentId);
        if (rules.isEmpty()) {
            return null;
        }

        // Ưu tiên theo department trước
        for (ApprovalRule r : rules) {
            if (r.getSteps() != null && !r.getSteps().isEmpty()) {
                if (departmentId != null && r.getDepartment() != null && r.getDepartment().getId().equals(departmentId)) {
                    return r;
                }
            }
        }

        // Nếu không có rule riêng cho phòng ban, lấy rule chung (department == null)
        for (ApprovalRule r : rules) {
            if (r.getDepartment() == null && r.getSteps() != null && !r.getSteps().isEmpty()) {
                return r;
            }
        }

        return rules.stream().filter(r -> r.getSteps() != null && !r.getSteps().isEmpty()).findFirst().orElse(null);
    }

    @Override
    public List<JobRequestDto> getAllJobRequests() {
        return jobRequestRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<JobRequestDto> getJobRequestsByDepartment(Long departmentId) {
        return jobRequestRepository.findByDepartmentId(departmentId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public JobRequestDto getJobRequestById(Long id) {
        JobRequest job = jobRequestRepository.findById(id)
                .orElseThrow(() -> new AppException("Yêu cầu không tồn tại"));
        return mapToDto(job);
    }

    @Override
    @Transactional
    public JobRequestDto approveOrReject(Long id, boolean isApproved, String approverUsername) {
        return approveOrRejectStep(id, isApproved, null, approverUsername);
    }

    @Override
    @Transactional
    public JobRequestDto approveOrRejectStep(Long id, boolean isApproved, String comments, String approverUsername) {
        JobRequest jobRequest = jobRequestRepository.findById(id)
                .orElseThrow(() -> new AppException("Yêu cầu không tồn tại với ID: " + id));

        User currentUser = userRepository.findByUsername(approverUsername)
                .orElseThrow(() -> new AppException("Người duyệt không tồn tại: " + approverUsername));

        List<JobRequestApprovalStep> steps = jobRequest.getApprovalSteps();
        if (steps == null || steps.isEmpty()) {
            // Trường hợp dữ liệu cũ không có steps: cập nhật trực tiếp status
            jobRequest.setStatus(isApproved ? "APPROVED" : "REJECTED");
            return mapToDto(jobRequestRepository.save(jobRequest));
        }

        // Tìm bước đang chờ duyệt (status = "PENDING")
        JobRequestApprovalStep currentPendingStep = steps.stream()
                .filter(s -> "PENDING".equalsIgnoreCase(s.getStatus()))
                .findFirst()
                .orElse(null);

        if (currentPendingStep == null) {
            throw new AppException("Yêu cầu tuyển dụng này hiện không có cấp nào đang chờ duyệt (Trạng thái hiện tại: " + jobRequest.getStatus() + ")");
        }

        // Kiểm tra quyền duyệt: người dùng phải có vai trò tương ứng hoặc là ADMIN
        boolean hasRole = currentUser.getRoles().contains("ROLE_ADMIN")
                || currentUser.getRoles().contains(currentPendingStep.getApproverRole())
                || (currentPendingStep.getApproverUser() != null && currentPendingStep.getApproverUser().getId().equals(currentUser.getId()));

        if (!hasRole) {
            throw new AppException("Bạn không có quyền duyệt cấp này (Yêu cầu vai trò: " + currentPendingStep.getApproverRole() + ")");
        }

        if (!isApproved) {
            // Từ chối ở cấp này
            currentPendingStep.setStatus("REJECTED");
            currentPendingStep.setApproverUser(currentUser);
            currentPendingStep.setComments(comments != null ? comments : "Từ chối yêu cầu");
            currentPendingStep.setActionAt(LocalDateTime.now());

            jobRequest.setStatus("REJECTED");
        } else {
            // Phê duyệt cấp này
            currentPendingStep.setStatus("APPROVED");
            currentPendingStep.setApproverUser(currentUser);
            currentPendingStep.setComments(comments != null ? comments : "Đồng ý phê duyệt");
            currentPendingStep.setActionAt(LocalDateTime.now());

            // Tìm bước kế tiếp
            int nextOrder = currentPendingStep.getStepOrder() + 1;
            JobRequestApprovalStep nextStep = steps.stream()
                    .filter(s -> s.getStepOrder() == nextOrder)
                    .findFirst()
                    .orElse(null);

            if (nextStep != null) {
                // Chuyển bước tiếp theo sang PENDING
                nextStep.setStatus("PENDING");
                jobRequest.setCurrentApprovalStep(nextOrder);
                jobRequest.setStatus("PENDING_APPROVAL");
            } else {
                // Đã hoàn thành tất cả các cấp duyệt!
                jobRequest.setStatus("APPROVED");
            }
        }

        JobRequest updated = jobRequestRepository.save(jobRequest);
        return mapToDto(updated);
    }

    private JobRequestDto mapToDto(JobRequest entity) {
        List<JobRequestApprovalStepDto> stepDtos = entity.getApprovalSteps() != null
                ? entity.getApprovalSteps().stream().map(s -> JobRequestApprovalStepDto.builder()
                .id(s.getId())
                .stepOrder(s.getStepOrder())
                .stepName(s.getStepName())
                .approverRole(s.getApproverRole())
                .approverUserId(s.getApproverUser() != null ? s.getApproverUser().getId() : null)
                .approverUsername(s.getApproverUser() != null ? s.getApproverUser().getUsername() : null)
                .approverFullName(s.getApproverUser() != null
                        ? (s.getApproverUser().getFullName() != null ? s.getApproverUser().getFullName() : s.getApproverUser().getUsername())
                        : null)
                .status(s.getStatus())
                .comments(s.getComments())
                .isOverThreshold(s.getIsOverThreshold())
                .actionAt(s.getActionAt())
                .build()).collect(Collectors.toList())
                : new ArrayList<>();

        return JobRequestDto.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .headcount(entity.getHeadcount())
                .minSalary(entity.getMinSalary())
                .maxSalary(entity.getMaxSalary())
                .description(entity.getDescription())
                .requirements(entity.getRequirements())
                .status(entity.getStatus())
                .departmentId(entity.getDepartment() != null ? entity.getDepartment().getId() : null)
                .departmentName(entity.getDepartment() != null ? entity.getDepartment().getName() : null)
                .hiringManagerId(entity.getHiringManager() != null ? entity.getHiringManager().getId() : null)
                .hiringManagerName(entity.getHiringManager() != null ? entity.getHiringManager().getUsername() : null)
                .recruiterId(entity.getRecruiter() != null ? entity.getRecruiter().getId() : null)
                .recruiterName(entity.getRecruiter() != null ? entity.getRecruiter().getUsername() : null)
                .isOverBudget(entity.getIsOverBudget())
                .currentApprovalStep(entity.getCurrentApprovalStep())
                .approvalSteps(stepDtos)
                .createdAt(entity.getCreatedAt())
                .build();
    }

    @Override
    public JobRequestDto assignRecruiter(Long id, String recruiterUsername) {
        JobRequest job = jobRequestRepository.findById(id)
                .orElseThrow(() -> new AppException("Không tìm thấy Yêu cầu tuyển dụng này"));

        User recruiter = userRepository.findByUsername(recruiterUsername)
                .orElseThrow(() -> new AppException("Không tìm thấy Recruiter này"));

        job.setRecruiter(recruiter);
        job.setStatus("OPEN");
        JobRequest saved = jobRequestRepository.save(job);
        return mapToDto(saved);
    }
}
