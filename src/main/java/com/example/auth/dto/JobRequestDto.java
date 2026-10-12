package com.example.auth.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobRequestDto {
    private Long id;
    private String title;
    private Integer headcount;
    private Double minSalary;
    private Double maxSalary;
    private String description;
    private String requirements;
    private String status;
    private Long departmentId;
    private String departmentName;
    private Long hiringManagerId;
    private String hiringManagerName;
    private Long recruiterId;
    private String recruiterName;
    private Boolean isOverBudget;
    private Integer currentApprovalStep;
    private java.util.List<JobRequestApprovalStepDto> approvalSteps;
    private LocalDateTime createdAt;
}
