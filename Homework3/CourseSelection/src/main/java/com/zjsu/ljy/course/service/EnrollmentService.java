package com.zjsu.ljy.course.service;

import com.zjsu.ljy.course.exception.BusinessException;
import com.zjsu.ljy.course.exception.ResourceNotFoundException;
import com.zjsu.ljy.course.model.Course;
import com.zjsu.ljy.course.model.Enrollment;
import com.zjsu.ljy.course.model.Student;
import com.zjsu.ljy.course.repository.EnrollmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EnrollmentService {
    private final EnrollmentRepository enrollmentRepository;
    private final CourseService courseService;
    private final StudentService studentService;

    /**
     * 学生选课（验证课程存在、学生存在、容量、重复选课）
     */
    public Enrollment enrollCourse(String courseId, String studentId) {
        // 1. 验证课程是否存在
        Course course = courseService.getCourseById(courseId);

        // 2. 验证学生是否存在
        Student student = studentService.getStudentByStudentId(studentId);

        // 3. 验证课程容量是否充足
        int currentEnrolled = enrollmentRepository.countByCourseId(courseId);
        if (currentEnrolled >= course.getCapacity()) {
            throw new BusinessException("课程容量已满，当前：" + currentEnrolled + "，容量：" + course.getCapacity());
        }

        // 4. 验证是否重复选课
        if (enrollmentRepository.existsByCourseIdAndStudentId(courseId, student.getId())) {
            throw new BusinessException("学生已选该课程，学号：" + studentId + "，课程id：" + courseId);
        }

        // 5. 创建选课记录
        Enrollment enrollment = new Enrollment(courseId, student.getId());
        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);

        // 6. 级联更新：课程选课人数+1
        courseService.incrementEnrolledCount(courseId);

        return savedEnrollment;
    }

    /**
     * 学生退课
     */
    public void dropCourse(String enrollmentId) {
        // 1. 验证选课记录是否存在
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("选课记录不存在，id：" + enrollmentId));

        // 2. 删除选课记录
        enrollmentRepository.deleteById(enrollmentId);

        // （可选）退课后更新课程选课人数-1，文档未强制要求，可根据需求添加
    }

    /**
     * 查询所有选课记录
     */
    public List<Enrollment> getAllEnrollments() {
        return enrollmentRepository.findAll();
    }

    /**
     * 按课程查询选课记录
     */
    public List<Enrollment> getEnrollmentsByCourseId(String courseId) {
        // 验证课程是否存在
        courseService.getCourseById(courseId);
        return enrollmentRepository.findByCourseId(courseId);
    }

    /**
     * 按学生查询选课记录
     */
    public List<Enrollment> getEnrollmentsByStudentId(String studentId) {
        // 验证学生是否存在
        Student student = studentService.getStudentByStudentId(studentId);
        return enrollmentRepository.findByStudentId(student.getId());
    }
}
