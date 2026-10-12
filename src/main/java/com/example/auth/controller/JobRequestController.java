package com.example.auth.controller;

import com.example.auth.dto.ApiResponse;
import com.example.auth.dto.JobRequestDto;
import com.example.auth.service.JobRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/job-requests")
@RequiredArgsConstructor
public class JobRequestController {

    private final JobRequestService jobRequestService;

    @GetMapping
    @PreAuthorize("hasAnyRole('HIRING_MANAGER', 'APPROVER', 'HR_MANAGER', 'ADMIN', 'CANDIDATE')")
    public ResponseEntity<ApiResponse<List<JobRequestDto>>> getAllJobRequests() {
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách thành công", jobRequestService.getAllJobRequests()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('HIRING_MANAGER', 'APPROVER', 'HR_MANAGER', 'ADMIN', 'RECRUITER', 'INTERVIEWER')")
    public ResponseEntity<ApiResponse<JobRequestDto>> getJobRequestById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin yêu cầu tuyển dụng thành công", jobRequestService.getJobRequestById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('HIRING_MANAGER', 'HR_MANAGER', 'ADMIN')")
    public ResponseEntity<ApiResponse<JobRequestDto>> createJobRequest(
            @RequestBody JobRequestDto dto,
            Authentication authentication) {
        JobRequestDto created = jobRequestService.createJobRequest(dto, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Tạo yêu cầu tuyển dụng thành công", created));
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('APPROVER', 'HR_MANAGER', 'HIRING_MANAGER', 'ADMIN')")
    public ResponseEntity<ApiResponse<JobRequestDto>> approveJobRequest(
            @PathVariable Long id,
            @RequestParam boolean isApproved,
            @RequestParam(required = false) String comments,
            Authentication authentication) {
        JobRequestDto updated = jobRequestService.approveOrRejectStep(id, isApproved, comments, authentication.getName());
        String action = isApproved ? "Phê duyệt" : "Từ chối";
        return ResponseEntity.ok(ApiResponse.success(action + " yêu cầu thành công", updated));
    }

    @PutMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('HR_MANAGER', 'ADMIN')")
    public ResponseEntity<ApiResponse<JobRequestDto>> assignRecruiter(
            @PathVariable Long id,
            @RequestParam String recruiterUsername) {
        JobRequestDto updated = jobRequestService.assignRecruiter(id, recruiterUsername);
        return ResponseEntity.ok(ApiResponse.success("Phân công recruiter thành công", updated));
    }
}
