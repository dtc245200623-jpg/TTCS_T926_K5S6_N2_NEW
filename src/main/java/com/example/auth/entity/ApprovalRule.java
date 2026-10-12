package com.example.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "approval_rules")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApprovalRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "NVARCHAR(255)", nullable = false)
    private String name; // Tên chuỗi duyệt

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department; // null nếu áp dụng chung

    @Column(name = "min_salary")
    @Builder.Default
    private Double minSalary = 0.0;

    @Column(name = "max_salary")
    private Double maxSalary; // Hạn mức lương tối đa áp dụng

    @Column(name = "over_threshold_role", length = 50)
    @Builder.Default
    private String overThresholdRole = "ROLE_APPROVER"; // Cấp duyệt bổ sung khi vượt hạn mức

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @OneToMany(mappedBy = "rule", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("stepOrder ASC")
    @Builder.Default
    private List<ApprovalRuleStep> steps = new ArrayList<>();

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
