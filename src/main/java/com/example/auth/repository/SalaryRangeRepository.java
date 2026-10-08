package com.example.auth.repository;

import com.example.auth.entity.SalaryRange;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SalaryRangeRepository extends JpaRepository<SalaryRange, Long> {}
