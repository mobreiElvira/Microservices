package com.zjsu.ljy.repository;

import com.zjsu.ljy.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, String> {

    // 按课程代码查询
    Optional<Course> findByCode(String code);

    // 按讲师ID查询
    List<Course> findByInstructorId(String instructorId);

    // 按排课ID查询
    List<Course> findByScheduleId(Long scheduleId);

    // 检查课程代码是否存在
    boolean existsByCode(String code);

    // 标题关键字模糊查询（忽略大小写）
    List<Course> findByTitleContainingIgnoreCase(String keyword);

    // 筛选有剩余容量的课程（使用@Query明确比较逻辑）
    @Query("SELECT c FROM Course c WHERE c.capacity > c.enrolled")
    List<Course> findByCapacityGreaterThanEnrolled();

    // 组合查询：按讲师ID + 有剩余容量（使用@Query明确比较逻辑）
    @Query("SELECT c FROM Course c WHERE c.instructor.id = :instructorId AND c.capacity > c.enrolled")
    List<Course> findByInstructorIdAndCapacityGreaterThanEnrolled(String instructorId);

    // 组合查询：标题关键字 + 有剩余容量（使用@Query明确比较逻辑）
    @Query("SELECT c FROM Course c WHERE LOWER(c.title) LIKE LOWER(concat('%', :keyword, '%')) AND c.capacity > c.enrolled")
    List<Course> findByTitleContainingIgnoreCaseAndCapacityGreaterThanEnrolled(String keyword);

    // 新增：统计有剩余容量的课程数量
    @Query("SELECT COUNT(c) FROM Course c WHERE c.capacity > c.enrolled")
    long countByCapacityGreaterThanEnrolled();

    // 新增：统计指定讲师有剩余容量的课程数量
    @Query("SELECT COUNT(c) FROM Course c WHERE c.instructor.id = :instructorId AND c.capacity > c.enrolled")
    long countByInstructorIdAndCapacityGreaterThanEnrolled(String instructorId);
}
