package com.example.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "applications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Application {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tracking_code", length = 50, unique = true)
    private String trackingCode; // Mã tra cứu hồ sơ (VD: TRK-2026-101)

    @ManyToOne
    @JoinColumn(name = "candidate_profile_id")
    private CandidateProfile candidateProfile;

    @ManyToOne
    @JoinColumn(name = "job_request_id")
    private JobRequest jobRequest;

    @Column(length = 50)
    private String status; // APPLIED, SCREENING, INTERVIEWING, OFFERING, HIRED, REJECTED

    @Column(name = "applied_at")
    @Builder.Default
    private LocalDateTime appliedAt = LocalDateTime.now();
}