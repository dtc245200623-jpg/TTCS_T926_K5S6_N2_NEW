package com.example.auth.controller;

import com.example.auth.dto.ApiResponse;
import com.example.auth.entity.JobTitle;
import com.example.auth.repository.JobTitleRepository;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/job-titles")
public class JobTitleController {

    @Autowired
    private JobTitleRepository jobTitleRepository;

    @Autowired
    private com.example.auth.repository.CompetencyRepository competencyRepository;

    @GetMapping
    public ResponseEntity<?> getAllJobTitles() {
        List<JobTitle> list = jobTitleRepository.findAll();
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách chức danh thành công", list));
    }

    @PostMapping
    public ResponseEntity<?> createJobTitle(@RequestBody JobTitleRequest request) {
        if (request.getName() == null || request.getName().isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Tên chức danh không được để trống"));
        }
        if (request.getCode() == null || request.getCode().isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Mã chức danh không được để trống"));
        }

        JobTitle jobTitle = new JobTitle();
        jobTitle.setCode(request.getCode());
        jobTitle.setName(request.getName());
        jobTitle.setLevel(request.getLevel());
        jobTitle.setDescription(request.getDescription());
        jobTitleRepository.save(jobTitle);

        return ResponseEntity.ok(ApiResponse.success("Tạo chức danh thành công", jobTitle));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateJobTitle(@PathVariable Long id, @RequestBody JobTitleRequest request) {
        Optional<JobTitle> optional = jobTitleRepository.findById(id);
        if (optional.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Không tìm thấy chức danh"));
        }
        
        JobTitle jobTitle = optional.get();
        if (request.getName() != null) jobTitle.setName(request.getName());
        if (request.getCode() != null) jobTitle.setCode(request.getCode());
        if (request.getLevel() != null) jobTitle.setLevel(request.getLevel());
        if (request.getDescription() != null) jobTitle.setDescription(request.getDescription());
        
        jobTitleRepository.save(jobTitle);
        
        return ResponseEntity.ok(ApiResponse.success("Cập nhật chức danh thành công", jobTitle));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteJobTitle(@PathVariable Long id) {
        Optional<JobTitle> optional = jobTitleRepository.findById(id);
        if (optional.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Không tìm thấy chức danh"));
        }
        
        JobTitle jobTitle = optional.get();
        // Cập nhật trạng thái thay vì xóa cứng
        jobTitle.setStatus("INACTIVE");
        jobTitleRepository.save(jobTitle);
        
        return ResponseEntity.ok(ApiResponse.success("Đã vô hiệu hóa chức danh", null));
    }

    @PostMapping("/{id}/competencies")
    public ResponseEntity<?> mapCompetencies(@PathVariable Long id, @RequestBody java.util.List<Long> competencyIds) {
        Optional<JobTitle> optional = jobTitleRepository.findById(id);
        if (optional.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Không tìm thấy chức danh"));
        }
        JobTitle jobTitle = optional.get();
        java.util.List<com.example.auth.entity.Competency> competencies = competencyRepository.findAllById(competencyIds);
        jobTitle.setCompetencies(new java.util.HashSet<>(competencies));
        jobTitleRepository.save(jobTitle);
        
        return ResponseEntity.ok(ApiResponse.success("Ánh xạ khung năng lực thành công", jobTitle));
    }

    @Data
    static class JobTitleRequest {
        private String code;
        private String name;
        private String level;
        private String description;
    }
}
