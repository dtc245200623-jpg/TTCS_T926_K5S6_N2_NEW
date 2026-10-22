package com.example.auth.controller;

import com.example.auth.dto.ApiResponse;
import com.example.auth.service.OfferService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/offers")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class OfferController {
    private final OfferService offerService;

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('APPROVER', 'ADMIN', 'HR_MANAGER')")
    public ResponseEntity<?> getPendingOffers() {
        return ResponseEntity.ok(ApiResponse.success("Thành công", offerService.getPendingApprovalOffers()));
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('APPROVER', 'ADMIN')")
    public ResponseEntity<?> approveOffer(@PathVariable Long id, @RequestParam boolean isApproved) {
        try {
            if (isApproved) {
                offerService.approveOffer(id);
                return ResponseEntity.ok(ApiResponse.success("Đã phê duyệt Offer thành công. Mức lương phù hợp với dải lương quy định."));
            } else {
                offerService.rejectOffer(id);
                return ResponseEntity.ok(ApiResponse.success("Đã từ chối Offer."));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/validate-salary")
    @PreAuthorize("hasAnyRole('RECRUITER', 'HIRING_MANAGER', 'APPROVER', 'ADMIN', 'HR_MANAGER')")
    public ResponseEntity<?> validateSalary(@RequestBody ValidateSalaryRequest request) {
        Map<String, Object> result = offerService.validateOfferSalary(request.getJobTitle(), request.getOfferedSalary());
        return ResponseEntity.ok(ApiResponse.success("Kiểm tra dải lương thành công", result));
    }

    @Data
    public static class ValidateSalaryRequest {
        private String jobTitle;
        private Double offeredSalary;
    }
}

