package com.example.auth.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "recruitment_requisitions")
@Getter @Setter @NoArgsConstructor
public class RecruitmentRequisition {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 160)
    private String title;
    @Column(nullable = false, length = 120)
    private String department;
    @Column(nullable = false)
    private int headcount;
    @Column(nullable = false, length = 3000)
    private String justification;
    @Column(precision = 14, scale = 2)
    private BigDecimal salaryMin;
    @Column(precision = 14, scale = 2)
    private BigDecimal salaryMax;
    @Column(nullable = false, length = 32)
    private String status = "PENDING_APPROVAL";
    @Column(nullable = false)
    private int approvalsRequired = 1;
    @Column(nullable = false)
    private int approvalsReceived = 0;
    @Column(nullable = false, length = 80)
    private String requestedBy;
    @Column(length = 80)
    private String assignedRecruiter;
    @Column
    private Long publishedJobId;
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();
}
