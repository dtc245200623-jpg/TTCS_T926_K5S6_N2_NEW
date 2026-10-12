package com.example.auth.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "requisition_approvals")
@Getter @Setter @NoArgsConstructor
public class RequisitionApproval {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "requisition_id", nullable = false)
    private RecruitmentRequisition requisition;
    @Column(nullable = false)
    private int approvalStep;
    @Column(nullable = false, length = 80)
    private String approverUsername;
    @Column(nullable = false, length = 16)
    private String decision;
    @Column(length = 1000)
    private String comment;
    @Column(nullable = false)
    private LocalDateTime decidedAt = LocalDateTime.now();
}
