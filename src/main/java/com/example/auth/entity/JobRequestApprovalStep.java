package com.example.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "job_request_approval_steps")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobRequestApprovalStep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_request_id", nullable = false)
    private JobRequest jobRequest;

    @Column(name = "step_order", nullable = false)
    private Integer stepOrder; // 1, 2, 3...

    @Column(name = "step_name", columnDefinition = "NVARCHAR(255)")
    private String stepName;

    @Column(name = "approver_role", length = 50, nullable = false)
    private String approverRole; // ROLE_HIRING_MANAGER, ROLE_HR_MANAGER, ROLE_APPROVER

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approver_user_id")
    private User approverUser; // Người thực tế đã duyệt/từ chối

    @Column(name = "status", length = 50)
    @Builder.Default
    private String status = "WAITING"; // WAITING, PENDING, APPROVED, REJECTED

    @Column(name = "comments", columnDefinition = "NVARCHAR(MAX)")
    private String comments;

    @Column(name = "is_over_threshold")
    @Builder.Default
    private Boolean isOverThreshold = false; // Bước tự động bổ sung khi vượt hạn mức (KN-221)

    @Column(name = "action_at")
    private LocalDateTime actionAt;
}
