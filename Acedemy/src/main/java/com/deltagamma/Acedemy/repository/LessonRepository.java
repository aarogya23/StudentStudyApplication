package com.deltagamma.Acedemy.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.deltagamma.Acedemy.model.Lesson;

public interface LessonRepository extends JpaRepository<Lesson, Long> {
}
