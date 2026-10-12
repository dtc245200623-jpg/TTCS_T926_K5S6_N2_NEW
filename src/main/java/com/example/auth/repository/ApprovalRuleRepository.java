package com.example.auth.repository;

import com.example.auth.entity.ApprovalRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApprovalRuleRepository extends JpaRepository<ApprovalRule, Long> {

    List<ApprovalRule> findByIsActiveTrue();

    List<ApprovalRule> findByDepartmentIdAndIsActiveTrue(Long departmentId);

    @Query("SELECT r FROM ApprovalRule r WHERE r.isActive = true AND (r.department.id = :deptId OR r.department IS NULL) ORDER BY r.department.id DESC NULLS LAST")
    List<ApprovalRule> findMatchingRulesForDepartment(@Param("deptId") Long deptId);
}
