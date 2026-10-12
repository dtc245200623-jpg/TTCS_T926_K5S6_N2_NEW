package com.example.auth.repository;

import com.example.auth.entity.CandidateEvaluation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CandidateEvaluationRepository extends JpaRepository<CandidateEvaluation, Long> {
    Optional<CandidateEvaluation> findByApplication_Id(Long applicationId);
}
