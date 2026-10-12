package com.example.auth.service;

import com.example.auth.dto.ApprovalRuleDto;
import java.util.List;

public interface ApprovalRuleService {
    List<ApprovalRuleDto> getAllRules();
    ApprovalRuleDto getRuleById(Long id);
    ApprovalRuleDto createRule(ApprovalRuleDto dto);
    ApprovalRuleDto updateRule(Long id, ApprovalRuleDto dto);
    void deleteRule(Long id);
    ApprovalRuleDto previewMatchingRule(Long departmentId, Double salary);
}
