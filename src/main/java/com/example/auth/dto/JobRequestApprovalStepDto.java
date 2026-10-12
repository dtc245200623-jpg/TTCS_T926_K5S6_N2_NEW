package com.example.auth.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobRequestApprovalStepDto {
    private Long id;
    private Integer stepOrder;
    private String stepName;
    private String approverRole;
    private Long approverUserId;
    private String approverUsername;
    private String approverFullName;
    private String status;
    private String comments;
    private Boolean isOverThreshold;
    private LocalDateTime actionAt;
}
