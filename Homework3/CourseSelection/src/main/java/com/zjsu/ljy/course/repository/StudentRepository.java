package com.zjsu.ljy.course.repository;

import com.zjsu.ljy.course.model.Student;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class StudentRepository {
    // 内存存储：key=学生id，value=Student对象
    private final Map<String, Student> students = new ConcurrentHashMap<>();

    // 保存学生（新增/更新）
    public Student save(Student student) {
        students.put(student.getId(), student);
        return student;
    }

    // 根据id查询学生
    public Optional<Student> findById(String id) {
        return Optional.ofNullable(students.get(id));
    }

    // 根据学号查询学生（验证学号唯一性）
    public Optional<Student> findByStudentId(String studentId) {
        return students.values().stream()
                .filter(student -> student.getStudentId().equals(studentId))
                .findFirst();
    }

    // 查询所有学生
    public List<Student> findAll() {
        return new ArrayList<>(students.values());
    }

    // 根据id删除学生
    public void deleteById(String id) {
        students.remove(id);
    }

    // 检查学生是否存在
    public boolean existsById(String id) {
        return students.containsKey(id);
    }

    // 检查学号是否已存在
    public boolean existsByStudentId(String studentId) {
        return students.values().stream()
                .anyMatch(student -> student.getStudentId().equals(studentId));
    }
}
