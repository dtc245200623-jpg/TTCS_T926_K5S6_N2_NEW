package com.example.auth.repository;
import com.example.auth.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface ApplicationRepository extends JpaRepository<Application, Long> {
    boolean existsByCandidate_IdAndJob_Id(Long candidateId, Long jobId);
    List<Application> findByCandidate_EmailIgnoreCaseOrderByAppliedAtDesc(String email);
    Optional<Application> findByTrackingCode(String trackingCode);
}
