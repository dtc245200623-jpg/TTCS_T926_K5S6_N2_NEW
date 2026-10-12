package com.example.auth.repository;

import com.example.auth.entity.RequisitionApproval;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RequisitionApprovalRepository extends JpaRepository<RequisitionApproval, Long> {
    List<RequisitionApproval> findByRequisition_IdOrderByApprovalStepAsc(Long requisitionId);
    boolean existsByRequisition_IdAndApprovalStepAndApproverUsername(Long requisitionId, int step, String username);
}
