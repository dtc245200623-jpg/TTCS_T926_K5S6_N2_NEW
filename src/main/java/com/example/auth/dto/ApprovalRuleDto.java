package com.example.auth.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalRuleDto {

    private Long id;

    private Long departmentId;

    private Double minSalary;

    private Double maxSalary;

    private Integer approvalLevel;

    private String approverRole;
}
