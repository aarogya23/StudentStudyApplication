package com.deltagamma.Acedemy.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.deltagamma.Acedemy.model.Assessment;

public interface AssessmentRepository extends JpaRepository<Assessment, Long> {
    List<Assessment> findByCourseId(Long courseId);
}
