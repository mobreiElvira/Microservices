package com.zjsu.ljy.course.service;

import com.zjsu.ljy.course.dto.request.StudentCreateRequest;
import com.zjsu.ljy.course.dto.request.StudentUpdateRequest;
import com.zjsu.ljy.course.exception.BusinessException;
import com.zjsu.ljy.course.exception.InvalidParamException;
import com.zjsu.ljy.course.exception.ResourceNotFoundException;
import com.zjsu.ljy.course.model.Student;
import com.zjsu.ljy.course.repository.EnrollmentRepository;
import com.zjsu.ljy.course.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class StudentService {
    private final StudentRepository studentRepository;
    private final EnrollmentRepository enrollmentRepository;

    // 邮箱格式正则（复用原有逻辑）
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );

    /**
     * 创建学生：直接接收 StudentCreateRequest DTO，内部完成参数校验与实体转换
     */
    @Transactional
    public Student createStudent(StudentCreateRequest request) {
        // 1. 基础参数校验（复用 DTO 注解校验结果，补充范围校验）
        if (request.getGrade() < 2000 || request.getGrade() > 2100) {
            throw new InvalidParamException("入学年份必须在2000-2100之间");
        }

        // 2. 学号唯一性校验
        if (studentRepository.existsByStudentId(request.getStudentId())) {
            throw new BusinessException("学号已存在：" + request.getStudentId());
        }

        // 3. 邮箱格式与唯一性校验
        if (!EMAIL_PATTERN.matcher(request.getEmail()).matches()) {
            throw new InvalidParamException("邮箱格式无效：" + request.getEmail());
        }
        if (studentRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("邮箱已被使用：" + request.getEmail());
        }

        // 4. DTO 转换为实体（直接映射所有字段）
        Student student = new Student();
        student.setStudentId(request.getStudentId());
        student.setName(request.getName());
        student.setMajor(request.getMajor());
        student.setGrade(request.getGrade());
        student.setEmail(request.getEmail());

        return studentRepository.save(student);
    }

    /**
     * 更新学生：直接接收 StudentUpdateRequest DTO，按需更新字段
     */
    @Transactional
    public Student updateStudent(String studentId, StudentUpdateRequest request) {
        // 1. 校验学生存在（按学生ID查询）
        Student existingStudent = getStudentById(studentId);

        // 2. 学号更新（非空且变化时校验唯一性）
        if (request.getStudentId() != null && !request.getStudentId().equals(existingStudent.getStudentId())) {
            if (studentRepository.existsByStudentId(request.getStudentId())) {
                throw new BusinessException("新学号已存在：" + request.getStudentId());
            }
            existingStudent.setStudentId(request.getStudentId());
        }

        // 3. 姓名更新（非空则改）
        if (request.getName() != null) {
            existingStudent.setName(request.getName());
        }

        // 4. 专业更新（非空则改）
        if (request.getMajor() != null) {
            existingStudent.setMajor(request.getMajor());
        }

        // 5. 入学年份更新（非空且合法则改）
        if (request.getGrade() != null) {
            if (request.getGrade() < 2000 || request.getGrade() > 2100) {
                throw new InvalidParamException("入学年份必须在2000-2100之间");
            }
            existingStudent.setGrade(request.getGrade());
        }

        // 6. 邮箱更新（非空则校验格式与唯一性，再改）
        if (request.getEmail() != null && !request.getEmail().equals(existingStudent.getEmail())) {
            if (!EMAIL_PATTERN.matcher(request.getEmail()).matches()) {
                throw new InvalidParamException("新邮箱格式无效：" + request.getEmail());
            }
            if (studentRepository.existsByEmail(request.getEmail())) {
                throw new BusinessException("新邮箱已被使用：" + request.getEmail());
            }
            existingStudent.setEmail(request.getEmail());
        }

        return studentRepository.save(existingStudent);
    }

    // 以下方法逻辑不变，仅保留
    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    public Student getStudentById(String id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("学生不存在，id：" + id));
    }

    @Transactional
    public void deleteStudent(String id) {
        Student student = getStudentById(id);
        if (!enrollmentRepository.findByStudentId(id).isEmpty()) {
            throw new BusinessException("无法删除：该学生存在选课记录，请先退课");
        }
        studentRepository.deleteById(id);
    }

    public Student getStudentByStudentId(String studentId) {
        return studentRepository.findByStudentId(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("学生不存在，学号：" + studentId));
    }
}
