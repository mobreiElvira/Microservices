package com.zjsu.ljy.repository;

import com.zjsu.ljy.model.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
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

    // 按课程和学生组合查询（支持分页场景下的精确查询）
    List<Enrollment> findByCourseIdAndStudentIdIn(String courseId, List<String> studentIds);

    // 按学生和课程组合查询（支持学生查看多门课程的选课情况）
    List<Enrollment> findByStudentIdAndCourseIdIn(String studentId, List<String> courseIds);

    // 按选课时间范围查询（支持统计某时间段内的选课情况）
    List<Enrollment> findByEnrolledAtBetween(LocalDateTime start, LocalDateTime end);

    // 按课程和选课时间范围查询（统计课程在某时间段内的活跃人数）
    List<Enrollment> findByCourseIdAndEnrolledAtBetween(String courseId, LocalDateTime start, LocalDateTime end);

    // 统计课程活跃人数（基于选课时间，最近N天内的选课视为活跃）
    // Spring Data JPA支持使用@Query自定义查询
    // @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.course.id = :courseId AND e.enrolledAt >= :activeDate")
    // long countActiveByCourseId(String courseId, LocalDateTime activeDate);

    // 按课程和学生组合统计（用于批量检查学生是否已选多门课程）
    long countByCourseIdAndStudentIdIn(String courseId, List<String> studentIds);

    // 批量查询多个学生对多门课程的选课情况
    List<Enrollment> findByCourseIdInAndStudentIdIn(List<String> courseIds, List<String> studentIds);

    // 检查多个学生是否已选某课程
    List<Enrollment> findByCourseIdAndStudentIdInOrderByStudentId(String courseId, List<String> studentIds);
}
