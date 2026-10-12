package com.example.auth.repository;

import com.example.auth.entity.RecruitmentRequisition;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RecruitmentRequisitionRepository extends JpaRepository<RecruitmentRequisition, Long> {
    List<RecruitmentRequisition> findAllByOrderByCreatedAtDesc();
}
