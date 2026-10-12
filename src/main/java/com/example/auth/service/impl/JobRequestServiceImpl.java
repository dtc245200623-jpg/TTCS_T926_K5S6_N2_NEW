package com.example.auth.service.impl;

import com.example.auth.dto.JobRequestDto;
import com.example.auth.entity.ApprovalRule;
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

        if (dto.getDepartmentId() == null) {
            throw new AppException("Phòng ban không được để trống");
        }

        Department department = departmentRepository.findById(dto.getDepartmentId())
                .orElseThrow(() -> new AppException("Phòng ban không tồn tại"));

        if (dto.getMaxSalary() == null) {
            throw new AppException("Mức lương đề xuất không được để trống");
        }

        /*
         * Lấy toàn bộ cấu hình duyệt của phòng ban.
         * Sau đó xác định chuỗi cấp duyệt theo mức lương đề xuất.
         */
        List<ApprovalRule> allRules =
                approvalRuleRepository
                        .findByDepartmentIdOrderByApprovalLevelAsc(department.getId());

        if (allRules.isEmpty()) {
            throw new AppException(
                    "Không có cấu hình cấp duyệt cho phòng ban này"
            );
        }

        Double proposedSalary = dto.getMaxSalary();

        /*
         * Mức lương không được vượt ngoài mức tối đa đã cấu hình.
         * Đây là một phần của KN-222.
         */
        Double highestConfiguredSalary = allRules.stream()
                .map(ApprovalRule::getMaxSalary)
                .filter(value -> value != null)
                .max(Double::compareTo)
                .orElse(null);

        if (highestConfiguredSalary == null
                || proposedSalary > highestConfiguredSalary) {

            throw new AppException(
                    "Không có cấp duyệt phù hợp với mức lương đề xuất"
            );
        }

        /*
         * Ví dụ:
         *
         * Level 1: từ 0
         * Level 2: từ 15 triệu
         * Level 3: từ 30 triệu
         *
         * Lương 25 triệu sẽ cần Level 1 + Level 2.
         */
        List<ApprovalRule> approvalRules = allRules.stream()
                .filter(rule ->
                        rule.getMinSalary() != null
                                && proposedSalary >= rule.getMinSalary()
                )
                .collect(Collectors.toList());

        if (approvalRules.isEmpty()) {
            throw new AppException(
                    "Không có cấp duyệt phù hợp với phòng ban và mức lương đề xuất"
            );
        }

        User recruiter = null;

        if (dto.getRecruiterId() != null) {
            recruiter = userRepository.findById(dto.getRecruiterId())
                    .orElseThrow(() ->
                            new AppException("Recruiter không tồn tại"));
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
                .currentApprovalLevel(
                        approvalRules.get(0).getApprovalLevel()
                )
                .build();

        JobRequest saved = jobRequestRepository.save(jobRequest);

        /*
         * KN-235:
         * Snapshot chuỗi duyệt tại thời điểm tạo Job Request.
         *
         * Sau này ApprovalRule có thay đổi thì Job Request đang chạy
         * vẫn tiếp tục dùng các bước đã lưu ở đây.
         */
        for (int i = 0; i < approvalRules.size(); i++) {

            ApprovalRule rule = approvalRules.get(i);

            JobRequestApprovalStep step =
                    JobRequestApprovalStep.builder()
                            .jobRequest(saved)
                            .approvalLevel(rule.getApprovalLevel())
                            .approverRole(rule.getApproverRole())
                            .status("PENDING")
                            .isCurrent(i == 0)
                            .build();

            approvalStepRepository.save(step);
        }

        return mapToDto(saved);
    }

    @Override
    public List<JobRequestDto> getAllJobRequests() {

        return jobRequestRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<JobRequestDto> getJobRequestsByDepartment(Long departmentId) {

        return jobRequestRepository.findByDepartmentId(departmentId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public JobRequestDto approveOrReject(
            Long id,
            boolean isApproved,
            String approverUsername) {

        JobRequest jobRequest = jobRequestRepository.findById(id)
                .orElseThrow(() ->
                        new AppException("Yêu cầu không tồn tại"));

        if (!"PENDING_APPROVAL".equals(jobRequest.getStatus())) {
            throw new AppException(
                    "Yêu cầu này hiện không ở trạng thái chờ phê duyệt"
            );
        }

        JobRequestApprovalStep currentStep =
                approvalStepRepository
                        .findByJobRequestIdAndIsCurrentTrue(id)
                        .orElseThrow(() ->
                                new AppException(
                                        "Không tìm thấy cấp duyệt hiện tại"
                                ));

        User approver = userRepository.findByUsername(approverUsername)
                .orElseThrow(() ->
                        new AppException("Người duyệt không tồn tại"));

        /*
         * Trong DB role đang lưu dạng ROLE_ADMIN, ROLE_USER...
         *
         * ApprovalRule có thể nhập:
         * ADMIN
         * hoặc ROLE_ADMIN
         *
         * Đều xử lý được.
         */
        String requiredRole = currentStep.getApproverRole();

        if (!requiredRole.startsWith("ROLE_")) {
            requiredRole = "ROLE_" + requiredRole;
        }

        if (!approver.getRoles().contains(requiredRole)) {
            throw new AppException(
                    "Bạn không có quyền duyệt ở cấp hiện tại"
            );
        }

        /*
         * Nếu từ chối thì kết thúc toàn bộ luồng.
         */
        if (!isApproved) {

            currentStep.setStatus("REJECTED");
            currentStep.setIsCurrent(false);

            approvalStepRepository.save(currentStep);

            jobRequest.setStatus("REJECTED");
            jobRequest.setCurrentApprovalLevel(null);

            JobRequest updated =
                    jobRequestRepository.save(jobRequest);

            return mapToDto(updated);
        }

        /*
         * Duyệt cấp hiện tại.
         */
        currentStep.setStatus("APPROVED");
        currentStep.setIsCurrent(false);

        approvalStepRepository.save(currentStep);

        List<JobRequestApprovalStep> steps =
                approvalStepRepository
                        .findByJobRequestIdOrderByApprovalLevelAsc(id);

        /*
         * KN-221:
         * Tìm cấp duyệt tiếp theo.
         */
        JobRequestApprovalStep nextStep = steps.stream()
                .filter(step ->
                        step.getApprovalLevel()
                                > currentStep.getApprovalLevel()
                                && "PENDING".equals(step.getStatus())
                )
                .findFirst()
                .orElse(null);

        if (nextStep != null) {

            nextStep.setIsCurrent(true);

            approvalStepRepository.save(nextStep);

            jobRequest.setCurrentApprovalLevel(
                    nextStep.getApprovalLevel()
            );

            jobRequest.setStatus("PENDING_APPROVAL");

        } else {

            /*
             * Không còn cấp nào nữa thì duyệt hoàn tất.
             */
            jobRequest.setStatus("APPROVED");
            jobRequest.setCurrentApprovalLevel(null);
        }

        JobRequest updated =
                jobRequestRepository.save(jobRequest);

        return mapToDto(updated);
    }

    private JobRequestDto mapToDto(JobRequest entity) {

        return JobRequestDto.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .headcount(entity.getHeadcount())
                .minSalary(entity.getMinSalary())
                .maxSalary(entity.getMaxSalary())
                .description(entity.getDescription())
                .requirements(entity.getRequirements())
                .status(entity.getStatus())

                .departmentId(
                        entity.getDepartment() != null
                                ? entity.getDepartment().getId()
                                : null
                )

                .hiringManagerId(
                        entity.getHiringManager() != null
                                ? entity.getHiringManager().getId()
                                : null
                )

                .hiringManagerName(
                        entity.getHiringManager() != null
                                ? entity.getHiringManager().getUsername()
                                : null
                )

                .recruiterId(
                        entity.getRecruiter() != null
                                ? entity.getRecruiter().getId()
                                : null
                )

                .recruiterName(
                        entity.getRecruiter() != null
                                ? entity.getRecruiter().getUsername()
                                : null
                )

                .currentApprovalLevel(
                        entity.getCurrentApprovalLevel()
                )

                .createdAt(entity.getCreatedAt())
                .build();
    }

    @Override
    public JobRequestDto assignRecruiter(
            Long id,
            String recruiterUsername) {

        JobRequest job = jobRequestRepository.findById(id)
                .orElseThrow(() ->
                        new AppException(
                                "Không tìm thấy Yêu cầu tuyển dụng này"
                        ));

        User recruiter = userRepository
                .findByUsername(recruiterUsername)
                .orElseThrow(() ->
                        new AppException(
                                "Không tìm thấy Recruiter này"
                        ));

        job.setRecruiter(recruiter);
        job.setStatus("OPEN");

        JobRequest saved =
                jobRequestRepository.save(job);

        return mapToDto(saved);
    }
}