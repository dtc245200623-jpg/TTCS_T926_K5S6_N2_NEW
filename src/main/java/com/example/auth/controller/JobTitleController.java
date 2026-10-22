package com.example.auth.controller;

import com.example.auth.dto.ApiResponse;
import com.example.auth.dto.JobTitleDto;
import com.example.auth.entity.JobTitle;
import com.example.auth.repository.JobTitleRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/job-titles")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class JobTitleController {

    private final JobTitleRepository jobTitleRepository;

    /**
     * Lấy danh sách chức danh.
     * Quy tắc bảo mật: Chỉ Trưởng phòng Nhân sự (HR_MANAGER) hoặc Admin mới được xem dải lương.
     */
    @GetMapping
    public ResponseEntity<?> getAllJobTitles(Authentication authentication) {
        boolean canViewSalary = checkCanViewSalary(authentication);

        List<JobTitleDto> list = jobTitleRepository.findAll().stream()
                .map(entity -> toDto(entity, canViewSalary))
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách chức danh thành công", list));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getJobTitleById(@PathVariable Long id, Authentication authentication) {
        Optional<JobTitle> optional = jobTitleRepository.findById(id);
        if (optional.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Không tìm thấy chức danh"));
        }
        boolean canViewSalary = checkCanViewSalary(authentication);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin chức danh thành công", toDto(optional.get(), canViewSalary)));
    }

    /**
     * Tạo chức danh mới kèm dải lương tối thiểu / tối đa.
     * Kiểm tra tính hợp lệ của khoảng lương.
     */
    @PostMapping
    public ResponseEntity<?> createJobTitle(@RequestBody JobTitleRequest request) {
        if (request.getCode() == null || request.getCode().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Mã chức danh không được để trống"));
        }
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Tên chức danh không được để trống"));
        }

        String normalizedCode = request.getCode().trim().toUpperCase();
        if (jobTitleRepository.findByCode(normalizedCode).isPresent()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Mã chức danh '" + normalizedCode + "' đã tồn tại!"));
        }

        // Kiểm tra tính hợp lệ của dải lương
        String salaryValidationError = validateSalaryRange(request.getMinSalary(), request.getMaxSalary());
        if (salaryValidationError != null) {
            return ResponseEntity.badRequest().body(ApiResponse.error(salaryValidationError));
        }

        JobTitle jobTitle = JobTitle.builder()
                .code(normalizedCode)
                .name(request.getName().trim())
                .level(request.getLevel() != null ? request.getLevel().trim() : "Junior")
                .minSalary(request.getMinSalary())
                .maxSalary(request.getMaxSalary())
                .description(request.getDescription())
                .status("ACTIVE")
                .build();

        JobTitle saved = jobTitleRepository.save(jobTitle);
        return ResponseEntity.ok(ApiResponse.success("Tạo chức danh thành công", toDto(saved, true)));
    }

    /**
     * Cập nhật thông tin chức danh và dải lương.
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateJobTitle(@PathVariable Long id, @RequestBody JobTitleRequest request) {
        Optional<JobTitle> optional = jobTitleRepository.findById(id);
        if (optional.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Không tìm thấy chức danh"));
        }

        JobTitle jobTitle = optional.get();

        if (request.getCode() != null && !request.getCode().trim().isEmpty()) {
            String newCode = request.getCode().trim().toUpperCase();
            Optional<JobTitle> existing = jobTitleRepository.findByCode(newCode);
            if (existing.isPresent() && !existing.get().getId().equals(id)) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Mã chức danh '" + newCode + "' đã được sử dụng!"));
            }
            jobTitle.setCode(newCode);
        }

        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            jobTitle.setName(request.getName().trim());
        }

        if (request.getLevel() != null) {
            jobTitle.setLevel(request.getLevel().trim());
        }

        // Kiểm tra tính hợp lệ của dải lương khi cập nhật
        Double min = request.getMinSalary() != null ? request.getMinSalary() : jobTitle.getMinSalary();
        Double max = request.getMaxSalary() != null ? request.getMaxSalary() : jobTitle.getMaxSalary();
        String salaryValidationError = validateSalaryRange(min, max);
        if (salaryValidationError != null) {
            return ResponseEntity.badRequest().body(ApiResponse.error(salaryValidationError));
        }

        jobTitle.setMinSalary(min);
        jobTitle.setMaxSalary(max);

        if (request.getDescription() != null) {
            jobTitle.setDescription(request.getDescription());
        }

        if (request.getStatus() != null) {
            jobTitle.setStatus(request.getStatus());
        }

        JobTitle saved = jobTitleRepository.save(jobTitle);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật chức danh thành công", toDto(saved, true)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteJobTitle(@PathVariable Long id) {
        Optional<JobTitle> optional = jobTitleRepository.findById(id);
        if (optional.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Không tìm thấy chức danh"));
        }

        JobTitle jobTitle = optional.get();
        // Cập nhật trạng thái sang INACTIVE thay vì xóa cứng
        jobTitle.setStatus("INACTIVE");
        jobTitleRepository.save(jobTitle);

        return ResponseEntity.ok(ApiResponse.success("Đã vô hiệu hóa chức danh", null));
    }

    private boolean checkCanViewSalary(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        return authentication.getAuthorities().stream().anyMatch(a ->
                a.getAuthority().equals("ROLE_HR_MANAGER") || a.getAuthority().equals("ROLE_ADMIN")
        );
    }

    private String validateSalaryRange(Double minSalary, Double maxSalary) {
        if (minSalary != null && minSalary <= 0) {
            return "Dải lương không hợp lệ: Lương tối thiểu phải lớn hơn 0 VNĐ.";
        }
        if (maxSalary != null && maxSalary <= 0) {
            return "Dải lương không hợp lệ: Lương tối đa phải lớn hơn 0 VNĐ.";
        }
        if (minSalary != null && maxSalary != null && maxSalary < minSalary) {
            return "Dải lương không hợp lệ: Lương tối đa (" + String.format("%,.0f", maxSalary) + "đ) phải lớn hơn hoặc bằng lương tối thiểu (" + String.format("%,.0f", minSalary) + "đ).";
        }
        return null;
    }

    private JobTitleDto toDto(JobTitle entity, boolean canViewSalary) {
        return JobTitleDto.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .name(entity.getName())
                .level(entity.getLevel())
                .minSalary(canViewSalary ? entity.getMinSalary() : null)
                .maxSalary(canViewSalary ? entity.getMaxSalary() : null)
                .description(entity.getDescription())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .salaryVisible(canViewSalary)
                .build();
    }

    @Data
    public static class JobTitleRequest {
        private String code;
        private String name;
        private String level;
        private Double minSalary;
        private Double maxSalary;
        private String description;
        private String status;
    }
}
