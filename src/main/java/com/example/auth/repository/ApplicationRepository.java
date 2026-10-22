package com.example.auth.repository;

import com.example.auth.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {
    List<Application> findByCandidateProfileId(Long candidateProfileId);
    List<Application> findByJobRequestId(Long jobRequestId);
    List<Application> findByStatus(String status);
    Optional<Application> findByTrackingCodeIgnoreCase(String trackingCode);
}