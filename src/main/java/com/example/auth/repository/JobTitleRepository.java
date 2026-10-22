package com.example.auth.repository;

import com.example.auth.entity.JobTitle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobTitleRepository extends JpaRepository<JobTitle, Long> {
    Optional<JobTitle> findByCode(String code);
    Optional<JobTitle> findByName(String name);
    List<JobTitle> findByStatus(String status);
}
