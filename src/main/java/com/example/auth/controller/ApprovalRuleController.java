package com.example.auth.controller;

import com.example.auth.dto.ApiResponse;
import com.example.auth.dto.ApprovalRuleDto;
import com.example.auth.service.ApprovalRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/approval-rules")
@RequiredArgsConstructor
public class ApprovalRuleController {

    private final ApprovalRuleService approvalRuleService;

    @GetMapping
    @PreAuthorize("hasAnyRole('HR_MANAGER', 'ADMIN', 'HIRING_MANAGER', 'APPROVER')")
    public ResponseEntity<ApiResponse<List<ApprovalRuleDto>>> getAllRules() {
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách chuỗi cấp duyệt thành công", approvalRuleService.getAllRules()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('HR_MANAGER', 'ADMIN')")
    public ResponseEntity<ApiResponse<ApprovalRuleDto>> getRuleById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin chuỗi cấp duyệt thành công", approvalRuleService.getRuleById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('HR_MANAGER', 'ADMIN')")
    public ResponseEntity<ApiResponse<ApprovalRuleDto>> createRule(@RequestBody ApprovalRuleDto dto) {
        ApprovalRuleDto created = approvalRuleService.createRule(dto);
        return ResponseEntity.ok(ApiResponse.success("Khai báo chuỗi cấp duyệt thành công", created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('HR_MANAGER', 'ADMIN')")
    public ResponseEntity<ApiResponse<ApprovalRuleDto>> updateRule(@PathVariable Long id, @RequestBody ApprovalRuleDto dto) {
        ApprovalRuleDto updated = approvalRuleService.updateRule(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật chuỗi cấp duyệt thành công", updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('HR_MANAGER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteRule(@PathVariable Long id) {
        approvalRuleService.deleteRule(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa chuỗi cấp duyệt thành công", null));
    }

    @GetMapping("/preview")
    @PreAuthorize("hasAnyRole('HIRING_MANAGER', 'HR_MANAGER', 'ADMIN', 'APPROVER')")
    public ResponseEntity<ApiResponse<ApprovalRuleDto>> previewMatchingRule(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Double salary) {
        ApprovalRuleDto preview = approvalRuleService.previewMatchingRule(departmentId, salary);
        return ResponseEntity.ok(ApiResponse.success("Xem trước chuỗi cấp duyệt thành công", preview));
    }
}
