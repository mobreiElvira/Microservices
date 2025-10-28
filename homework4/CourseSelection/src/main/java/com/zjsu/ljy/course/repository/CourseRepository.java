package com.zjsu.ljy.course.repository;

import com.zjsu.ljy.course.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, String> {

    Optional<Course> findByCode(String code);

    List<Course> findByInstructorId(String instructorId);

    List<Course> findByScheduleId(Long scheduleId);

    boolean existsByCode(String code);
}
