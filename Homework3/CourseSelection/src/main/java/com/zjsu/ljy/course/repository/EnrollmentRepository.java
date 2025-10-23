package com.zjsu.ljy.course.repository;

import com.zjsu.ljy.course.model.Enrollment;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class EnrollmentRepository {
    private final Map<String, Enrollment> enrollments = new ConcurrentHashMap<>();

    public Enrollment save(Enrollment enrollment) {
        enrollments.put(enrollment.getId(), enrollment);
        return enrollment;
    }

    public Optional<Enrollment> findById(String id) {
        return Optional.ofNullable(enrollments.get(id));
    }

    // 根据课程ID查询所有选课记录
    public List<Enrollment> findByCourseId(String courseId) {
        return enrollments.values().stream()
                .filter(enrollment -> enrollment.getCourseId().equals(courseId))
                .collect(Collectors.toList());
    }

    // 根据学生ID查询所有选课记录
    public List<Enrollment> findByStudentId(String studentId) {
        return enrollments.values().stream()
                .filter(enrollment -> enrollment.getStudentId().equals(studentId))
                .collect(Collectors.toList());
    }

    // 检查学生是否已选某门课（避免重复选课）
    public boolean existsByCourseIdAndStudentId(String courseId, String studentId) {
        return enrollments.values().stream()
                .anyMatch(enrollment ->
                        enrollment.getCourseId().equals(courseId) &&
                                enrollment.getStudentId().equals(studentId)
                );
    }

    public List<Enrollment> findAll() {
        return new ArrayList<>(enrollments.values());
    }

    public void deleteById(String id) {
        enrollments.remove(id);
    }

    // 统计某课程的选课人数
    public int countByCourseId(String courseId) {
        return (int) enrollments.values().stream()
                .filter(enrollment -> enrollment.getCourseId().equals(courseId))
                .count();
    }
}
