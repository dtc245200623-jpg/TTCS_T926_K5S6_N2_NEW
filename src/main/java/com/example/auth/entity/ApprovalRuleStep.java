package com.example.auth.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "approval_rule_steps")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApprovalRuleStep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rule_id", nullable = false)
    private ApprovalRule rule;

    @Column(name = "step_order", nullable = false)
    private Integer stepOrder; // 1, 2, 3...

    @Column(name = "step_name", columnDefinition = "NVARCHAR(255)")
    private String stepName; // Tên bước duyệt

    @Column(name = "approver_role", length = 50, nullable = false)
    private String approverRole; // ROLE_HIRING_MANAGER, ROLE_HR_MANAGER, ROLE_APPROVER

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "specific_approver_id")
    private User specificApprover; // Chỉ định người duyệt cụ thể nếu có
}
