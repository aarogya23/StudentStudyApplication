package com.deltagamma.Acedemy.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.deltagamma.Acedemy.model.Course;

public interface CourseRepository extends JpaRepository<Course, Long> {
}
