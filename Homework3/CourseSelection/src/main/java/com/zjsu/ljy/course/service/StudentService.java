package com.zjsu.ljy.course.service;

import com.zjsu.ljy.course.exception.BusinessException;
import com.zjsu.ljy.course.exception.InvalidParamException;
import com.zjsu.ljy.course.exception.ResourceNotFoundException;
import com.zjsu.ljy.course.model.Student;
import com.zjsu.ljy.course.repository.EnrollmentRepository;
import com.zjsu.ljy.course.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class StudentService {
    private final StudentRepository studentRepository;
    private final EnrollmentRepository enrollmentRepository;

    // 邮箱格式正则（验证标准邮箱）
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
    );

    /**
     * 创建学生（验证学号唯一、邮箱格式）
     */
    public Student createStudent(String studentId, String name, String major, Integer grade, String email) {
        // 1. 验证必填字段
        if (studentId == null || studentId.trim().isEmpty()) {
            throw new InvalidParamException("学号不能为空");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidParamException("学生姓名不能为空");
        }
        if (major == null || major.trim().isEmpty()) {
            throw new InvalidParamException("专业不能为空");
        }
        if (grade == null) {
            throw new InvalidParamException("入学年份不能为空");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new InvalidParamException("邮箱不能为空");
        }

        // 2. 验证学号唯一性
        if (studentRepository.existsByStudentId(studentId)) {
            throw new BusinessException("学号已存在：" + studentId);
        }

        // 3. 验证邮箱格式
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new InvalidParamException("邮箱格式无效：" + email);
        }

        // 4. 创建并保存学生
        Student student = new Student(studentId, name, major, grade, email);
        return studentRepository.save(student);
    }

    /**
     * 查询所有学生
     */
    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    /**
     * 根据id查询学生
     */
    public Student getStudentById(String id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("学生不存在，id：" + id));
    }

    /**
     * 更新学生信息（id和createdAt不可修改）
     */
    public Student updateStudent(String id, String newStudentId, String newName, String newMajor, Integer newGrade, String newEmail) {
        // 1. 验证学生是否存在
        Student existingStudent = getStudentById(id);

        // 2. 验证新学号（若修改）
        if (newStudentId != null && !newStudentId.equals(existingStudent.getStudentId())) {
            if (studentRepository.existsByStudentId(newStudentId)) {
                throw new BusinessException("新学号已存在：" + newStudentId);
            }
            existingStudent.setStudentId(newStudentId);
        }

        // 3. 验证新邮箱（若修改）
        if (newEmail != null && !newEmail.equals(existingStudent.getEmail())) {
            if (!EMAIL_PATTERN.matcher(newEmail).matches()) {
                throw new InvalidParamException("新邮箱格式无效：" + newEmail);
            }
            existingStudent.setEmail(newEmail);
        }

        // 4. 更新其他字段
        if (newName != null) existingStudent.setName(newName);
        if (newMajor != null) existingStudent.setMajor(newMajor);
        if (newGrade != null) existingStudent.setGrade(newGrade);

        // 5. 保存更新
        return studentRepository.save(existingStudent);
    }

    /**
     * 删除学生（需检查是否有选课记录）
     */
    public void deleteStudent(String id) {
        // 1. 验证学生是否存在
        Student student = getStudentById(id);

        // 2. 检查是否有选课记录
        if (!enrollmentRepository.findByStudentId(id).isEmpty()) {
            throw new BusinessException("无法删除：该学生存在选课记录");
        }

        // 3. 删除学生
        studentRepository.deleteById(id);
    }

    /**
     * 根据学号查询学生（用于选课验证）
     */
    public Student getStudentByStudentId(String studentId) {
        return studentRepository.findByStudentId(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("学生不存在，学号：" + studentId));
    }
}
