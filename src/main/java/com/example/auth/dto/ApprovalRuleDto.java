package com.example.auth.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalRuleDto {
    private Long id;
    private String name;
    private Long departmentId;
    private String departmentName;
    private Double minSalary;
    private Double maxSalary;
    private String overThresholdRole;
    private Boolean isActive;
    private List<ApprovalRuleStepDto> steps;
    private LocalDateTime createdAt;
}
