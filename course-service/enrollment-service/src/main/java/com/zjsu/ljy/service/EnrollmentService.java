package com.zjsu.ljy.service;

import com.zjsu.ljy.dto.request.EnrollmentCreateRequest;
import com.zjsu.ljy.exception.BusinessException;
import com.zjsu.ljy.exception.ResourceNotFoundException;
import com.zjsu.ljy.model.Enrollment;
import com.zjsu.ljy.model.Student;
import com.zjsu.ljy.repository.EnrollmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EnrollmentService {
    private final EnrollmentRepository enrollmentRepository;
    private final StudentService studentService;
    private final RestTemplate restTemplate;

    @Value("${catalog-service.url}")
    private String catalogServiceUrl;

    /**
     * 学生选课：直接接收 EnrollmentCreateRequest DTO，获取 courseId 和 studentId
     */
    @Transactional
    public Enrollment enrollCourse(EnrollmentCreateRequest request) {
        // 1. 从 DTO 中获取核心参数
        String courseId = request.getCourseId();
        String studentId = request.getStudentId();

        // 2. 校验课程存在（使用RestTemplate调用catalog-service）
        Map<String, Object> courseResponse = null;
        try {
            // 确保URL拼接正确，避免斜杠问题
            String url = buildUrl("api/courses/" + courseId);
            courseResponse = restTemplate.getForObject(url, Map.class);
            if (courseResponse == null || courseResponse.get("data") == null) {
                throw new ResourceNotFoundException("课程不存在，id：" + courseId);
            }
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResourceNotFoundException("课程不存在，id：" + courseId);
        } catch (Exception e) {
            throw new BusinessException("调用课程服务失败：" + e.getMessage());
        }

        // 获取嵌套的data对象
        Map<String, Object> courseData = (Map<String, Object>) courseResponse.get("data");

        // 3. 校验学生存在（按学号查询，复用 StudentService 方法）
        Student student = studentService.getStudentByStudentId(studentId);

        // 4. 校验课程容量（从返回的课程信息中获取容量和已选人数）
        Object capacityObj = courseData.get("capacity");
        Object enrolledObj = courseData.get("enrolled");

        if (capacityObj == null) {
            throw new BusinessException("获取课程容量信息失败，课程ID：" + courseId);
        }

        Integer capacity;
        Integer enrolled = 0;
        try {
            capacity = Integer.valueOf(capacityObj.toString());
            if (enrolledObj != null) {
                enrolled = Integer.valueOf(enrolledObj.toString());
            }
        } catch (NumberFormatException e) {
            throw new BusinessException("课程容量或已选人数格式错误");
        }

        // 本地数据库中也统计一下当前选课人数，确保数据一致性
        long currentEnrolled = enrollmentRepository.countByCourseId(courseId);
        if (currentEnrolled >= capacity) {
            throw new BusinessException("课程容量已满，当前：" + currentEnrolled + "，容量：" + capacity);
        }

        // 5. 校验重复选课（按课程ID+学生ID判断）
        if (enrollmentRepository.existsByCourseIdAndStudentId(courseId, student.getId())) {
            throw new BusinessException("学生已选该课程，学号：" + studentId + "，课程id：" + courseId);
        }

        // 6. 构建选课记录并保存
        Enrollment enrollment = new Enrollment(courseId, student);
        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);

        // 7. 更新课程选课人数（使用RestTemplate调用catalog-service）
        try {
            String updateUrl = buildUrl("api/courses/" + courseId + "/enroll");
            // 使用POST方法，而不是之前的PUT方法
            restTemplate.postForObject(updateUrl, null, Map.class);
        } catch (Exception e) {
            throw new BusinessException("更新课程选课人数失败：" + e.getMessage());
        }

        return savedEnrollment;
    }

    @Transactional
    public void dropCourse(String enrollmentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("选课记录不存在，id：" + enrollmentId));

        String courseId = enrollment.getCourseId();
        enrollmentRepository.delete(enrollment);

        // 使用RestTemplate调用catalog-service更新选课人数
        try {
            String updateUrl = buildUrl("api/courses/" + courseId + "/drop");
            // 使用POST方法，而不是之前的PUT方法
            restTemplate.postForObject(updateUrl, null, Map.class);
        } catch (Exception e) {
            // 记录日志，但不影响删除操作
            System.err.println("更新课程选课人数失败：" + e.getMessage());
        }
    }

    public List<Enrollment> getAllEnrollments() {
        return enrollmentRepository.findAll();
    }

    public List<Enrollment> getEnrollmentsByCourseId(String courseId) {
        // 使用RestTemplate调用catalog-service验证课程存在
        try {
            String url = buildUrl("api/courses/" + courseId);
            Map<String, Object> courseResponse = restTemplate.getForObject(url, Map.class);
            if (courseResponse == null || courseResponse.get("data") == null) {
                throw new ResourceNotFoundException("课程不存在，id：" + courseId);
            }
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResourceNotFoundException("课程不存在，id：" + courseId);
        }
        return enrollmentRepository.findByCourseId(courseId);
    }

    public List<Enrollment> getEnrollmentsByStudentId(String studentId) {
        Student student = studentService.getStudentByStudentId(studentId);
        return enrollmentRepository.findByStudentId(student.getId());
    }

    // 统计课程活跃人数（例如最近30天内的选课人数）
    public long countActiveStudentsByCourseId(String courseId, int days) {
        // 先验证课程存在
        try {
            String url = buildUrl("api/courses/" + courseId);
            Map<String, Object> courseResponse = restTemplate.getForObject(url, Map.class);
            if (courseResponse == null || courseResponse.get("data") == null) {
                throw new ResourceNotFoundException("课程不存在，id：" + courseId);
            }
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResourceNotFoundException("课程不存在，id：" + courseId);
        }

        LocalDateTime activeDate = LocalDateTime.now().minusDays(days);
        return enrollmentRepository.findByCourseIdAndEnrolledAtBetween(courseId, activeDate, LocalDateTime.now()).size();
    }

    // 批量检查学生是否已选某课程
    public Map<String, Boolean> checkStudentsEnrollmentStatus(String courseId, List<String> studentIds) {
        // 先验证课程存在
        try {
            String url = buildUrl("api/courses/" + courseId);
            Map<String, Object> courseResponse = restTemplate.getForObject(url, Map.class);
            if (courseResponse == null || courseResponse.get("data") == null) {
                throw new ResourceNotFoundException("课程不存在，id：" + courseId);
            }
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResourceNotFoundException("课程不存在，id：" + courseId);
        }

        List<Enrollment> enrollments = enrollmentRepository.findByCourseIdAndStudentIdInOrderByStudentId(courseId, studentIds);
        Map<String, Boolean> result = new HashMap<>();

        // 初始化所有学生为未选课
        studentIds.forEach(studentId -> result.put(studentId, false));

        // 标记已选课的学生
        enrollments.forEach(e -> result.put(e.getStudent().getId(), true));

        return result;
    }

    // 辅助方法：构建正确的URL，避免斜杠问题
    private String buildUrl(String endpoint) {
        if (catalogServiceUrl.endsWith("/")) {
            return catalogServiceUrl + endpoint;
        } else {
            return catalogServiceUrl + "/" + endpoint;
        }
    }
}
