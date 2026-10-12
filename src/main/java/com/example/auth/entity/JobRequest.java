package com.example.auth.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "job_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "NVARCHAR(255)")
    private String title;

    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToOne
    @JoinColumn(name = "hiring_manager_id")
    private User hiringManager;

    @ManyToOne
    @JoinColumn(name = "recruiter_id")
    private User recruiter;

    private Integer headcount;

    private Double minSalary;

    private Double maxSalary;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String description;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String requirements;

    @Column(length = 50)
    private String status; // DRAFT, PENDING_APPROVAL, APPROVED, OPEN, CLOSED

    @Column(name = "current_approval_level")
    private Integer currentApprovalLevel;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}