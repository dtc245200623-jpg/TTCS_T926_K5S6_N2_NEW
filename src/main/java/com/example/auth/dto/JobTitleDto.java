package com.example.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobTitleDto {
    private Long id;
    private String code;
    private String name;
    private String level;
    private Double minSalary;
    private Double maxSalary;
    private String description;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean salaryVisible; // True nếu người xem có quyền Trưởng phòng Nhân sự hoặc Admin
}
