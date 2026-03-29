package com.deltagamma.Acedemy.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.deltagamma.Acedemy.model.CourseMessage;

public interface CourseMessageRepository extends JpaRepository<CourseMessage, Long> {
    List<CourseMessage> findTop20ByCourseIdOrderBySentAtDesc(Long courseId);
}
