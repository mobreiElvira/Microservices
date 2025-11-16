package com.zjsu.ljy.course.repository;

import com.zjsu.ljy.course.model.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, String> {

    long countByCourseId(String courseId);

    long countByStudentId(String studentId);

    boolean existsByCourseIdAndStudentId(String courseId, String studentId);

    List<Enrollment> findByCourseId(String courseId);

    List<Enrollment> findByStudentId(String studentId);

    Optional<Enrollment> findByCourseIdAndStudentId(String courseId, String studentId);
}
