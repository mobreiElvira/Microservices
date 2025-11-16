package com.zjsu.ljy.course.service;

import com.zjsu.ljy.course.dto.request.EnrollmentCreateRequest;
import com.zjsu.ljy.course.exception.BusinessException;
import com.zjsu.ljy.course.exception.ResourceNotFoundException;
import com.zjsu.ljy.course.model.Course;
import com.zjsu.ljy.course.model.Enrollment;
import com.zjsu.ljy.course.model.Student;
import com.zjsu.ljy.course.repository.EnrollmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EnrollmentService {
    private final EnrollmentRepository enrollmentRepository;
    private final CourseService courseService;
    private final StudentService studentService;

    /**
     * 学生选课：直接接收 EnrollmentCreateRequest DTO，获取 courseId 和 studentId
     */
    @Transactional
    public Enrollment enrollCourse(EnrollmentCreateRequest request) {
        // 1. 从 DTO 中获取核心参数，避免零散传参
        String courseId = request.getCourseId();
        String studentId = request.getStudentId();

        // 2. 校验课程存在（复用 CourseService 方法）
        Course course = courseService.getCourseById(courseId);

        // 3. 校验学生存在（按学号查询，复用 StudentService 方法）
        Student student = studentService.getStudentByStudentId(studentId);

        // 4. 校验课程容量（统计当前选课人数）
        long currentEnrolled = enrollmentRepository.countByCourseId(courseId);
        if (currentEnrolled >= course.getCapacity()) {
            throw new BusinessException("课程容量已满，当前：" + currentEnrolled + "，容量：" + course.getCapacity());
        }

        // 5. 校验重复选课（按课程ID+学生ID判断）
        if (enrollmentRepository.existsByCourseIdAndStudentId(courseId, student.getId())) {
            throw new BusinessException("学生已选该课程，学号：" + studentId + "，课程id：" + courseId);
        }

        // 6. 构建选课记录并保存
        Enrollment enrollment = new Enrollment();
        enrollment.setCourse(course);
        enrollment.setStudent(student);
        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);

        // 7. 更新课程选课人数
        courseService.incrementEnrolledCount(courseId);

        return savedEnrollment;
    }

    // 以下方法逻辑不变，仅保留
    @Transactional
    public void dropCourse(String enrollmentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("选课记录不存在，id：" + enrollmentId));

        String courseId = enrollment.getCourse().getId();
        enrollmentRepository.delete(enrollment);
        courseService.decrementEnrolledCount(courseId);
    }

    public List<Enrollment> getAllEnrollments() {
        return enrollmentRepository.findAll();
    }

    public List<Enrollment> getEnrollmentsByCourseId(String courseId) {
        courseService.getCourseById(courseId);
        return enrollmentRepository.findByCourseId(courseId);
    }

    public List<Enrollment> getEnrollmentsByStudentId(String studentId) {
        Student student = studentService.getStudentByStudentId(studentId);
        return enrollmentRepository.findByStudentId(student.getId());
    }
}
