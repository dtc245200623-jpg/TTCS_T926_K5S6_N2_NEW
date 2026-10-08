package com.example.auth.repository;

import com.example.auth.entity.InterviewQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InterviewQuestionRepository extends JpaRepository<InterviewQuestion, Long> {}
