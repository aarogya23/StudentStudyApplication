package com.deltagamma.Acedemy.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.deltagamma.Acedemy.model.AssessmentAttempt;

public interface AssessmentAttemptRepository extends JpaRepository<AssessmentAttempt, Long> {
    List<AssessmentAttempt> findByUserIdOrderBySubmittedAtDesc(Long userId);
}
