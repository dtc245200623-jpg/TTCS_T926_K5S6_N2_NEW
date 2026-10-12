package com.example.auth.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "job_titles")
@Data
public class JobTitle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(name = "job_level")
    private String level;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private String status = "ACTIVE"; // ACTIVE, INACTIVE

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "job_title_competencies",
        joinColumns = @JoinColumn(name = "job_title_id"),
        inverseJoinColumns = @JoinColumn(name = "competency_id")
    )
    private java.util.Set<Competency> competencies = new java.util.HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
