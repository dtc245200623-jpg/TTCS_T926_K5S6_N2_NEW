package com.example.auth.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "candidate_evaluations")
@Getter
@Setter
@NoArgsConstructor
public class CandidateEvaluation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "application_id", nullable = false, unique = true)
    private Application application;

    private Integer skillScore;
    private Integer problemSolvingScore;

    @Column(length = 5000)
    private String comments;

    @Column(length = 100)
    private String salaryOffer;

    @Column(length = 20)
    private String decision;

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();
}
