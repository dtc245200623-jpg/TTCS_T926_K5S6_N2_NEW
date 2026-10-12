package com.example.auth.repository;

import com.example.auth.entity.ApprovalRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApprovalRuleRepository
        extends JpaRepository<ApprovalRule, Long> {

    List<ApprovalRule>
    findByDepartmentIdOrderByApprovalLevelAsc(Long departmentId);

    List<ApprovalRule>
    findByDepartmentIdAndMinSalaryLessThanEqualAndMaxSalaryGreaterThanEqualOrderByApprovalLevelAsc(
            Long departmentId,
            Double salary1,
            Double salary2
    );
}