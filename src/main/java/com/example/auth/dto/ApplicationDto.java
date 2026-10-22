package com.example.auth.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationDto {
    private Long id;
    private String trackingCode;
    private Long candidateId;
    private String candidateName;
    private Long jobRequestId;
    private String jobTitle;
    private String status;
    private LocalDateTime appliedAt;
    private String interviewScore;
    private String cvUrl;
}