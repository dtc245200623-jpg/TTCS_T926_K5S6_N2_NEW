package com.example.auth.controller;

import com.example.auth.dto.ApiResponse;
import com.example.auth.dto.ApprovalRuleDto;
import com.example.auth.service.ApprovalRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/approval-rules")
@RequiredArgsConstructor
public class ApprovalRuleController {

    private final ApprovalRuleService approvalRuleService;

    @PostMapping
    public ResponseEntity<ApiResponse<ApprovalRuleDto>> createRule(
            @RequestBody ApprovalRuleDto dto) {

        ApprovalRuleDto created = approvalRuleService.createRule(dto);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Tạo cấu hình cấp duyệt thành công",
                        created
                )
        );
    }

    @GetMapping("/department/{departmentId}")
    public ResponseEntity<ApiResponse<List<ApprovalRuleDto>>> getRulesByDepartment(
            @PathVariable Long departmentId) {

        List<ApprovalRuleDto> rules =
                approvalRuleService.getRulesByDepartment(departmentId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Lấy chuỗi cấp duyệt thành công",
                        rules
                )
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ApprovalRuleDto>> updateRule(
            @PathVariable Long id,
            @RequestBody ApprovalRuleDto dto) {

        ApprovalRuleDto updated = approvalRuleService.updateRule(id, dto);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Cập nhật cấu hình cấp duyệt thành công",
                        updated
                )
        );
    }
}