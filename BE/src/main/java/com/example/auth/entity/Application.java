package com.example.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.UUID;

@Entity @Table(name = "job_applications", uniqueConstraints = @UniqueConstraint(columnNames = {"candidate_id", "job_id"}))
@Getter @Setter @NoArgsConstructor
public class Application {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional = false, fetch = FetchType.EAGER) @JoinColumn(name = "candidate_id") private Candidate candidate;
    @ManyToOne(optional = false, fetch = FetchType.EAGER) @JoinColumn(name = "job_id") private JobOpening job;
    @Column(nullable = false) private String stage = "NEW";
    @Column(nullable = false) private LocalDateTime appliedAt = LocalDateTime.now();
    private LocalDateTime interviewAt;
    private LocalDate startDate;
    private LocalDateTime offerAcceptedAt;
    private Boolean salaryAccepted = false;
    @Column(length = 100) private String agreedSalary;
    @Column(unique = true, length = 32) private String trackingCode = UUID.randomUUID().toString().replace("-", "");
    @Column(length = 3000) private String coverLetter;
    private Boolean documentsReceived = false;
    private Boolean accountReady = false;
    private Boolean equipmentReady = false;
    private Boolean orientationComplete = false;
}
