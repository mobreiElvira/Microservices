package com.zjsu.ljy.course.repository;

import com.zjsu.ljy.course.model.Instructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// 泛型：实体类Instructor，主键类型String（对应教师ID如"T004"）
@Repository
public interface InstructorRepository extends JpaRepository<Instructor, String> {
}
