package com.example.auth.repository;

import com.example.auth.entity.Competency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompetencyRepository extends JpaRepository<Competency, Long> {}
