package com.example.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name = "job_openings")
@Getter @Setter @NoArgsConstructor
public class JobOpening {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false) private String title;
    @Column(nullable = false) private String department;
    @Column(nullable = false) private int headcount = 1;
    @Column(length = 4000) private String description;
    @Column(nullable = false) private String status = "OPEN";
    @Column(nullable = false) private LocalDateTime createdAt = LocalDateTime.now();
}
