package com.example.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "job_titles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobTitle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code; // Mã chức danh (VD: DEV_01, PM_01, HR_01,...)

    @Column(nullable = false)
    private String name; // Tên chức danh

    @Column(length = 50)
    private String level; // Cấp bậc: Intern, Fresher, Junior, Middle, Senior, Lead/Manager

    @Column(name = "min_salary")
    private Double minSalary; // Dải lương tối thiểu

    @Column(name = "max_salary")
    private Double maxSalary; // Dải lương tối đa

    @Column(length = 1000)
    private String description;

    @Builder.Default
    @Column(nullable = false)
    private String status = "ACTIVE"; // ACTIVE, INACTIVE

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

