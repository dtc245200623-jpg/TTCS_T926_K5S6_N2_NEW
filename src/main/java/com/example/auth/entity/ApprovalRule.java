package com.example.auth.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "approval_rules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApprovalRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @Column(name = "min_salary", nullable = false)
    private Double minSalary;

    @Column(name = "max_salary", nullable = false)
    private Double maxSalary;

    @Column(name = "approval_level", nullable = false)
    private Integer approvalLevel;

    @Column(name = "approver_role", nullable = false)
    private String approverRole;
}