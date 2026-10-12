package com.example.auth.service.impl;

import com.example.auth.dto.ApprovalRuleDto;
import com.example.auth.dto.ApprovalRuleStepDto;
import com.example.auth.entity.ApprovalRule;
import com.example.auth.entity.ApprovalRuleStep;
import com.example.auth.entity.Department;
import com.example.auth.entity.User;
import com.example.auth.exception.AppException;
import com.example.auth.repository.ApprovalRuleRepository;
import com.example.auth.repository.DepartmentRepository;
import com.example.auth.repository.UserRepository;
import com.example.auth.service.ApprovalRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApprovalRuleServiceImpl implements ApprovalRuleService {

    private final ApprovalRuleRepository approvalRuleRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;

    @Override
    public List<ApprovalRuleDto> getAllRules() {
        return approvalRuleRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public ApprovalRuleDto getRuleById(Long id) {
        ApprovalRule rule = approvalRuleRepository.findById(id)
                .orElseThrow(() -> new AppException("Không tìm thấy chuỗi cấp duyệt với ID: " + id));
        return mapToDto(rule);
    }

    @Override
    @Transactional
    public ApprovalRuleDto createRule(ApprovalRuleDto dto) {
        validateRuleConfiguration(dto);

        Department dept = null;
        if (dto.getDepartmentId() != null) {
            dept = departmentRepository.findById(dto.getDepartmentId())
                    .orElseThrow(() -> new AppException("Không tìm thấy phòng ban với ID: " + dto.getDepartmentId()));
        }

        ApprovalRule rule = ApprovalRule.builder()
                .name(dto.getName().trim())
                .department(dept)
                .minSalary(dto.getMinSalary() != null ? dto.getMinSalary() : 0.0)
                .maxSalary(dto.getMaxSalary())
                .overThresholdRole(dto.getOverThresholdRole() != null && !dto.getOverThresholdRole().isBlank()
                        ? dto.getOverThresholdRole() : "ROLE_APPROVER")
                .isActive(dto.getIsActive() != null ? dto.getIsActive() : true)
                .createdAt(LocalDateTime.now())
                .steps(new ArrayList<>())
                .build();

        mapStepsFromDto(rule, dto.getSteps());

        ApprovalRule saved = approvalRuleRepository.save(rule);
        return mapToDto(saved);
    }

    @Override
    @Transactional
    public ApprovalRuleDto updateRule(Long id, ApprovalRuleDto dto) {
        validateRuleConfiguration(dto);

        ApprovalRule rule = approvalRuleRepository.findById(id)
                .orElseThrow(() -> new AppException("Không tìm thấy chuỗi cấp duyệt với ID: " + id));

        Department dept = null;
        if (dto.getDepartmentId() != null) {
            dept = departmentRepository.findById(dto.getDepartmentId())
                    .orElseThrow(() -> new AppException("Không tìm thấy phòng ban với ID: " + dto.getDepartmentId()));
        }

        rule.setName(dto.getName().trim());
        rule.setDepartment(dept);
        rule.setMinSalary(dto.getMinSalary() != null ? dto.getMinSalary() : 0.0);
        rule.setMaxSalary(dto.getMaxSalary());
        if (dto.getOverThresholdRole() != null && !dto.getOverThresholdRole().isBlank()) {
            rule.setOverThresholdRole(dto.getOverThresholdRole());
        }
        if (dto.getIsActive() != null) {
            rule.setIsActive(dto.getIsActive());
        }

        rule.getSteps().clear();
        mapStepsFromDto(rule, dto.getSteps());

        ApprovalRule saved = approvalRuleRepository.save(rule);
        return mapToDto(saved);
    }

    @Override
    @Transactional
    public void deleteRule(Long id) {
        ApprovalRule rule = approvalRuleRepository.findById(id)
                .orElseThrow(() -> new AppException("Không tìm thấy chuỗi cấp duyệt với ID: " + id));
        approvalRuleRepository.delete(rule);
    }

    @Override
    public ApprovalRuleDto previewMatchingRule(Long departmentId, Double salary) {
        ApprovalRule rule = findMatchingRule(departmentId, salary);
        ApprovalRuleDto dto = mapToDto(rule);

        // KN-221: Nếu mức lương vượt hạn mức maxSalary của rule -> Tự động mô phỏng thêm cấp duyệt cao hơn
        if (salary != null && rule.getMaxSalary() != null && salary > rule.getMaxSalary()) {
            int nextOrder = dto.getSteps().size() + 1;
            String overRole = rule.getOverThresholdRole() != null ? rule.getOverThresholdRole() : "ROLE_APPROVER";
            String roleName = "Ban Giám đốc (Người duyệt)";
            if ("ROLE_APPROVER".equals(overRole)) roleName = "Ban Giám đốc (Người duyệt)";
            else if ("ROLE_HR_MANAGER".equals(overRole)) roleName = "Trưởng phòng Nhân sự";

            dto.getSteps().add(ApprovalRuleStepDto.builder()
                    .stepOrder(nextOrder)
                    .stepName("Cấp duyệt bổ sung: Phê duyệt vượt hạn mức lương (" + roleName + ")")
                    .approverRole(overRole)
                    .build());
        }

        return dto;
    }

    public ApprovalRule findMatchingRule(Long departmentId, Double salary) {
        List<ApprovalRule> rules = approvalRuleRepository.findMatchingRulesForDepartment(departmentId);
        if (rules.isEmpty()) {
            // Không có cấu hình nào khớp -> Chặn lưu (KN-222)
            throw new AppException("Cấu hình sai: Không có cấp duyệt/chuỗi duyệt nào khớp với phòng ban này. Vui lòng liên hệ Trưởng phòng Nhân sự để cấu hình.");
        }

        // Chọn rule phù hợp nhất (ưu tiên rule theo phòng ban cụ thể trước)
        ApprovalRule bestMatch = null;
        for (ApprovalRule r : rules) {
            if (r.getSteps() == null || r.getSteps().isEmpty()) {
                continue;
            }
            if (departmentId != null && r.getDepartment() != null && r.getDepartment().getId().equals(departmentId)) {
                bestMatch = r;
                break;
            }
        }

        if (bestMatch == null) {
            // Fallback lấy rule chung (department == null)
            for (ApprovalRule r : rules) {
                if (r.getDepartment() == null && r.getSteps() != null && !r.getSteps().isEmpty()) {
                    bestMatch = r;
                    break;
                }
            }
        }

        if (bestMatch == null) {
            bestMatch = rules.get(0);
        }

        if (bestMatch.getSteps() == null || bestMatch.getSteps().isEmpty()) {
            throw new AppException("Cấu hình sai: Chuỗi duyệt khớp không có cấp duyệt nào. Vui lòng kiểm tra lại cấu hình chuỗi cấp duyệt!");
        }

        return bestMatch;
    }

    private void validateRuleConfiguration(ApprovalRuleDto dto) {
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new AppException("Cấu hình sai: Tên chuỗi cấp duyệt không được để trống!");
        }

        if (dto.getMinSalary() != null && dto.getMaxSalary() != null && dto.getMinSalary() > dto.getMaxSalary()) {
            throw new AppException("Cấu hình sai: Mức lương tối thiểu (" + dto.getMinSalary() + ") không được lớn hơn hạn mức lương (" + dto.getMaxSalary() + ")!");
        }

        // KN-222: Cấu hình sai (không có cấp duyệt nào khớp / rỗng) bị chặn lưu
        if (dto.getSteps() == null || dto.getSteps().isEmpty()) {
            throw new AppException("Cấu hình sai (bị chặn lưu): Chuỗi phê duyệt phải có ít nhất 1 cấp duyệt!");
        }

        for (int i = 0; i < dto.getSteps().size(); i++) {
            ApprovalRuleStepDto step = dto.getSteps().get(i);
            if (step.getApproverRole() == null || step.getApproverRole().trim().isEmpty()) {
                throw new AppException("Cấu hình sai (bị chặn lưu): Cấp duyệt thứ " + (i + 1) + " chưa được chọn vai trò người duyệt!");
            }
        }
    }

    private void mapStepsFromDto(ApprovalRule rule, List<ApprovalRuleStepDto> stepDtos) {
        int order = 1;
        for (ApprovalRuleStepDto sDto : stepDtos) {
            User specificUser = null;
            if (sDto.getSpecificApproverId() != null) {
                specificUser = userRepository.findById(sDto.getSpecificApproverId()).orElse(null);
            }

            String defaultStepName = switch (sDto.getApproverRole()) {
                case "ROLE_HIRING_MANAGER" -> "Cấp " + order + ": Trưởng bộ phận";
                case "ROLE_HR_MANAGER" -> "Cấp " + order + ": Trưởng phòng Nhân sự";
                case "ROLE_APPROVER" -> "Cấp " + order + ": Ban Giám đốc (Người duyệt)";
                default -> "Cấp " + order + ": Phê duyệt";
            };

            ApprovalRuleStep step = ApprovalRuleStep.builder()
                    .rule(rule)
                    .stepOrder(order++)
                    .stepName(sDto.getStepName() != null && !sDto.getStepName().isBlank() ? sDto.getStepName().trim() : defaultStepName)
                    .approverRole(sDto.getApproverRole().trim())
                    .specificApprover(specificUser)
                    .build();

            rule.getSteps().add(step);
        }
    }

    private ApprovalRuleDto mapToDto(ApprovalRule entity) {
        List<ApprovalRuleStepDto> stepDtos = entity.getSteps() != null
                ? entity.getSteps().stream().map(s -> ApprovalRuleStepDto.builder()
                .id(s.getId())
                .stepOrder(s.getStepOrder())
                .stepName(s.getStepName())
                .approverRole(s.getApproverRole())
                .specificApproverId(s.getSpecificApprover() != null ? s.getSpecificApprover().getId() : null)
                .specificApproverName(s.getSpecificApprover() != null
                        ? (s.getSpecificApprover().getFullName() != null ? s.getSpecificApprover().getFullName() : s.getSpecificApprover().getUsername())
                        : null)
                .build()).collect(Collectors.toList())
                : new ArrayList<>();

        return ApprovalRuleDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .departmentId(entity.getDepartment() != null ? entity.getDepartment().getId() : null)
                .departmentName(entity.getDepartment() != null ? entity.getDepartment().getName() : "Tất cả phòng ban")
                .minSalary(entity.getMinSalary())
                .maxSalary(entity.getMaxSalary())
                .overThresholdRole(entity.getOverThresholdRole())
                .isActive(entity.getIsActive())
                .steps(stepDtos)
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
