package com.example.auth.service.impl;

import com.example.auth.dto.ApprovalRuleDto;
import com.example.auth.entity.ApprovalRule;
import com.example.auth.entity.Department;
import com.example.auth.repository.ApprovalRuleRepository;
import com.example.auth.repository.DepartmentRepository;
import com.example.auth.service.ApprovalRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApprovalRuleServiceImpl implements ApprovalRuleService {

    private final ApprovalRuleRepository approvalRuleRepository;
    private final DepartmentRepository departmentRepository;

    @Override
    public ApprovalRuleDto createRule(ApprovalRuleDto dto) {

        Department department = departmentRepository
                .findById(dto.getDepartmentId())
                .orElseThrow(() -> new RuntimeException("Phòng ban không tồn tại"));

        ApprovalRule rule = ApprovalRule.builder()
                .department(department)
                .minSalary(dto.getMinSalary())
                .maxSalary(dto.getMaxSalary())
                .approvalLevel(dto.getApprovalLevel())
                .approverRole(dto.getApproverRole())
                .build();

        ApprovalRule saved = approvalRuleRepository.save(rule);

        return mapToDto(saved);
    }

    @Override
    public List<ApprovalRuleDto> getRulesByDepartment(Long departmentId) {

        return approvalRuleRepository
                .findByDepartmentIdOrderByApprovalLevelAsc(departmentId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public ApprovalRuleDto updateRule(Long id, ApprovalRuleDto dto) {

        ApprovalRule rule = approvalRuleRepository
                .findById(id)
                .orElseThrow(() -> new RuntimeException("Cấu hình cấp duyệt không tồn tại"));

        if (dto.getDepartmentId() != null) {
            Department department = departmentRepository
                    .findById(dto.getDepartmentId())
                    .orElseThrow(() -> new RuntimeException("Phòng ban không tồn tại"));

            rule.setDepartment(department);
        }

        rule.setMinSalary(dto.getMinSalary());
        rule.setMaxSalary(dto.getMaxSalary());
        rule.setApprovalLevel(dto.getApprovalLevel());
        rule.setApproverRole(dto.getApproverRole());

        ApprovalRule updated = approvalRuleRepository.save(rule);

        return mapToDto(updated);
    }

    private ApprovalRuleDto mapToDto(ApprovalRule rule) {

        return ApprovalRuleDto.builder()
                .id(rule.getId())
                .departmentId(rule.getDepartment().getId())
                .minSalary(rule.getMinSalary())
                .maxSalary(rule.getMaxSalary())
                .approvalLevel(rule.getApprovalLevel())
                .approverRole(rule.getApproverRole())
                .build();
    }
}