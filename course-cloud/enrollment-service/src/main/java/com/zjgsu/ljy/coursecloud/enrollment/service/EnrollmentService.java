package com.zjgsu.ljy.coursecloud.enrollment.service;

import com.zjgsu.ljy.coursecloud.enrollment.client.CatalogServiceClient;
import com.zjgsu.ljy.coursecloud.enrollment.client.UserServiceClient;
import com.zjgsu.ljy.coursecloud.enrollment.model.EnrollmentRecord;
import com.zjgsu.ljy.coursecloud.enrollment.repository.EnrollmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class EnrollmentService {

    private static final Logger log = LoggerFactory.getLogger(EnrollmentService.class);

    private final UserServiceClient userServiceClient;
    private final CatalogServiceClient catalogServiceClient;
    private final EnrollmentRepository repository;

    public EnrollmentService(UserServiceClient userServiceClient, CatalogServiceClient catalogServiceClient, EnrollmentRepository repository) {
        this.userServiceClient = userServiceClient;
        this.catalogServiceClient = catalogServiceClient;
        this.repository = repository;
    }

    public EnrollmentRecord enroll(String courseId, String studentId) {
        log.info("开始选课: studentId={}, courseId={}", studentId, courseId);

        // Check if already enrolled
        if (repository.existsByCourseIdAndStudentId(courseId, studentId)) {
            log.warn("学生已选该课程: studentId={}, courseId={}", studentId, courseId);
            throw new IllegalStateException("Student is already enrolled in this course");
        }

        // 1. ⭐ 通过OpenFeign调用 user-service
        try {
            log.info("调用 user-service 验证学生: studentId={}", studentId);
            Map<String, Object> studentResponse = userServiceClient.getStudentById(studentId);
            log.info("学生验证成功，响应来自端口: {}", studentResponse.get("port"));  // ✅ 日志显示负载均衡
        } catch (HttpClientErrorException e) {
            log.error("验证学生时出错: {}", e.getMessage(), e);
            // 检查是否为404错误
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new IllegalArgumentException("Student not found: " + studentId);
            }
            throw new RuntimeException("Error verifying student with user-service: " + e.getMessage());
        } catch (RuntimeException e) {
            log.error("验证学生时服务降级: {}", e.getMessage(), e);
            // 处理fallback抛出的异常
            if (e.getMessage().contains("temporarily unavailable")) {
                throw new RuntimeException("User service is temporarily unavailable, please try again later");
            }
            throw e;
        } catch (Exception e) {
            log.error("验证学生时发生未知错误: {}", e.getMessage(), e);
            throw new RuntimeException("Unexpected error verifying student with user-service: " + e.getMessage());
        }

        // 2. ⭐ 通过OpenFeign调用 catalog-service
        try {
            log.info("调用 catalog-service 验证课程: courseId={}", courseId);
            Map<String, Object> courseResponse = catalogServiceClient.getCourseById(courseId);

            if (courseResponse == null) {
                log.error("课程不存在: {}", courseId);
                throw new IllegalArgumentException("Course not found: " + courseId);
            }

            log.info("课程验证成功，响应来自端口: {}", courseResponse.get("port"));  // ✅ 日志显示负载均衡

            // 处理嵌套的 data 结构
            Object dataObj = courseResponse.get("data");
            Map<String, Object> courseData = dataObj instanceof Map ? (Map<String, Object>) dataObj : courseResponse;

            Integer capacity = (Integer) courseData.get("capacity");
            Integer enrolled = (Integer) courseData.get("enrolled");

            log.debug("课程容量检查: capacity={}, enrolled={}", capacity, enrolled);

            if (enrolled != null && capacity != null && enrolled >= capacity) {
                log.warn("课程已满: courseId={}, capacity={}, enrolled={}", courseId, capacity, enrolled);
                throw new IllegalStateException("Course capacity reached");
            }

        } catch (HttpClientErrorException e) {
            log.error("验证课程时出错: {}", e.getMessage(), e);
            // 检查是否为404错误
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new IllegalArgumentException("Course not found: " + courseId);
            }
            throw new RuntimeException("Error verifying course with catalog-service: " + e.getMessage());
        } catch (RuntimeException e) {
            log.error("验证课程时服务降级: {}", e.getMessage(), e);
            // 处理fallback抛出的异常
            if (e.getMessage().contains("temporarily unavailable")) {
                throw new RuntimeException("Catalog service is temporarily unavailable, please try again later");
            }
            throw e;
        } catch (Exception e) {
            log.error("验证课程时发生未知错误: {}", e.getMessage(), e);
            throw new RuntimeException("Unexpected error verifying course with catalog-service: " + e.getMessage());
        }

        // 3. Create enrollment record
        EnrollmentRecord record = new EnrollmentRecord(courseId, studentId);
        EnrollmentRecord saved = repository.save(record);

        log.info("选课成功: studentId={}, courseId={}, enrollmentId={}", studentId, courseId, saved.getId());

        return saved;
    }

    @Transactional(readOnly = true)
    public List<EnrollmentRecord> listByCourse(String courseId) {
        log.debug("查询课程的选课记录: courseId={}", courseId);
        return repository.findByCourseId(courseId);
    }

    @Transactional(readOnly = true)
    public List<EnrollmentRecord> listByStudent(String studentId) {
        log.debug("查询学生的选课记录: studentId={}", studentId);
        return repository.findByStudentId(studentId);
    }

    @Transactional(readOnly = true)
    public List<EnrollmentRecord> listAll() {
        log.debug("查询所有选课记录");
        return repository.findAll();
    }
}
