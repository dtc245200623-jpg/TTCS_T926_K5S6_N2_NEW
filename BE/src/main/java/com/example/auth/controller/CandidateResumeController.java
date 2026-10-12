package com.example.auth.controller;

import com.example.auth.entity.Candidate;
import com.example.auth.exception.AppException;
import com.example.auth.repository.CandidateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/recruitment/candidates")
@RequiredArgsConstructor
public class CandidateResumeController {
    private final CandidateRepository candidates;

    @GetMapping("/{id}/resume")
    @PreAuthorize("@accessPolicy.has(authentication, 'VIEW_CANDIDATES')")
    public ResponseEntity<ByteArrayResource> download(@PathVariable Long id) {
        Candidate candidate = candidates.findById(id).orElseThrow(() -> new AppException("Không tìm thấy ứng viên."));
        if (candidate.getResumeData() == null || candidate.getResumeData().length == 0)
            throw new AppException("Ứng viên chưa tải CV lên.");
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(candidate.getResumeContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(candidate.getResumeFileName(), StandardCharsets.UTF_8).build().toString())
                .body(new ByteArrayResource(candidate.getResumeData()));
    }
}
