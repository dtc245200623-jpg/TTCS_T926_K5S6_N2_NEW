package com.example.auth.service;

import com.example.auth.dto.ApprovalRuleDto;

import java.util.List;

public interface ApprovalRuleService {

    ApprovalRuleDto createRule(ApprovalRuleDto dto);

    List<ApprovalRuleDto> getRulesByDepartment(Long departmentId);

    ApprovalRuleDto updateRule(Long id, ApprovalRuleDto dto);
}