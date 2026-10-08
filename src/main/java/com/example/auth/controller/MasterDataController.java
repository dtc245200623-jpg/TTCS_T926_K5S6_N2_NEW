package com.example.auth.controller;

import com.example.auth.dto.ApiResponse;
import com.example.auth.entity.Competency;
import com.example.auth.entity.InterviewQuestion;
import com.example.auth.entity.SalaryRange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

import com.example.auth.repository.SalaryRangeRepository;
import com.example.auth.repository.CompetencyRepository;
import com.example.auth.repository.InterviewQuestionRepository;

@RestController
@RequestMapping("/api/master-data")
public class MasterDataController {

    @Autowired
    private SalaryRangeRepository salaryRangeRepo;
    @Autowired
    private CompetencyRepository competencyRepo;
    @Autowired
    private InterviewQuestionRepository questionRepo;

    // --- SALARY RANGES ---
    @GetMapping("/salary-ranges")
    public ResponseEntity<?> getSalaryRanges() {
        return ResponseEntity.ok(ApiResponse.success("Success", salaryRangeRepo.findAll()));
    }

    @PostMapping("/salary-ranges")
    public ResponseEntity<?> createSalaryRange(@RequestBody SalaryRange req) {
        if(req.getJobTitleName() == null) return ResponseEntity.badRequest().body(ApiResponse.error("Thiếu chức danh"));
        return ResponseEntity.ok(ApiResponse.success("Created", salaryRangeRepo.save(req)));
    }

    @DeleteMapping("/salary-ranges/{id}")
    public ResponseEntity<?> deleteSalaryRange(@PathVariable Long id) {
        Optional<SalaryRange> opt = salaryRangeRepo.findById(id);
        if (opt.isEmpty()) return ResponseEntity.badRequest().body(ApiResponse.error("Không tìm thấy"));
        SalaryRange item = opt.get();
        item.setStatus("INACTIVE");
        salaryRangeRepo.save(item);
        return ResponseEntity.ok(ApiResponse.success("Deleted", null));
    }

    // --- COMPETENCIES ---
    @GetMapping("/competencies")
    public ResponseEntity<?> getCompetencies() {
        return ResponseEntity.ok(ApiResponse.success("Success", competencyRepo.findAll()));
    }

    @PostMapping("/competencies")
    public ResponseEntity<?> createCompetency(@RequestBody Competency req) {
        if(req.getName() == null) return ResponseEntity.badRequest().body(ApiResponse.error("Thiếu tên năng lực"));
        return ResponseEntity.ok(ApiResponse.success("Created", competencyRepo.save(req)));
    }

    @DeleteMapping("/competencies/{id}")
    public ResponseEntity<?> deleteCompetency(@PathVariable Long id) {
        Optional<Competency> opt = competencyRepo.findById(id);
        if (opt.isEmpty()) return ResponseEntity.badRequest().body(ApiResponse.error("Không tìm thấy"));
        Competency item = opt.get();
        item.setStatus("INACTIVE");
        competencyRepo.save(item);
        return ResponseEntity.ok(ApiResponse.success("Deleted", null));
    }

    // --- INTERVIEW QUESTIONS ---
    @GetMapping("/questions")
    public ResponseEntity<?> getQuestions() {
        return ResponseEntity.ok(ApiResponse.success("Success", questionRepo.findAll()));
    }

    @PostMapping("/questions")
    public ResponseEntity<?> createQuestion(@RequestBody InterviewQuestion req) {
        if(req.getQuestionContent() == null) return ResponseEntity.badRequest().body(ApiResponse.error("Thiếu nội dung"));
        return ResponseEntity.ok(ApiResponse.success("Created", questionRepo.save(req)));
    }

    @DeleteMapping("/questions/{id}")
    public ResponseEntity<?> deleteQuestion(@PathVariable Long id) {
        Optional<InterviewQuestion> opt = questionRepo.findById(id);
        if (opt.isEmpty()) return ResponseEntity.badRequest().body(ApiResponse.error("Không tìm thấy"));
        InterviewQuestion item = opt.get();
        item.setStatus("INACTIVE");
        questionRepo.save(item);
        return ResponseEntity.ok(ApiResponse.success("Deleted", null));
    }
}
