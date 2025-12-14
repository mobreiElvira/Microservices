package com.zjgsu.ljy.coursecloud.enrollment.controller;

import com.zjgsu.ljy.coursecloud.enrollment.model.EnrollmentRecord;
import com.zjgsu.ljy.coursecloud.enrollment.service.EnrollmentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/enrollments")
public class EnrollmentController {
    // 1. 添加日志记录器
    private static final Logger logger = LoggerFactory.getLogger(EnrollmentController.class);

    private final EnrollmentService enrollmentService;

    @Value("${server.port}")
    private String currentPort;

    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    // ==================== 辅助方法：获取主机名/容器名 ====================
    private String getHostname() {
        // 优先使用环境变量 HOSTNAME (Docker 容器中最可靠)
        String hostname = System.getenv("HOSTNAME");
        if (hostname != null && !hostname.isEmpty()) {
            return hostname;
        }

        // 备用方案：使用 InetAddress
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            // 忽略异常
        }

        // 最后的 fallback
        return "unknown-" + currentPort;
    }

    // ==================== 辅助方法：生成实例标识 ====================
    private String getInstanceInfo() {
        String hostname = getHostname();
        return String.format("[容器名: %s | 端口: %s]", hostname, currentPort);
    }

    // ==================== Enrollment Endpoints ====================
    @PostMapping
    public ResponseEntity<EnrollmentResponse> enroll(@Valid @RequestBody EnrollmentRequest request) {
        // 2. 记录请求日志（包含实例标识）
        String instanceInfo = getInstanceInfo();
        logger.info("{} 处理选课请求 - courseId: {}, studentId: {}",
                instanceInfo, request.courseId(), request.studentId());

        EnrollmentRecord record = enrollmentService.enroll(request.courseId(), request.studentId());

        // 3. 记录响应日志
        logger.info("{} 完成选课请求 - 生成选课记录ID: {}",
                instanceInfo, record.getId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new EnrollmentResponse(
                        record.getId(),
                        record.getCourseId(),
                        record.getStudentId(),
                        record.getEnrolledAt().toString()
                ));
    }

    @GetMapping("/course/{courseId}")
    public List<EnrollmentResponse> listByCourse(@PathVariable String courseId) {
        String instanceInfo = getInstanceInfo();
        logger.info("{} 处理按课程查询选课记录请求 - courseId: {}",
                instanceInfo, courseId);

        List<EnrollmentResponse> responses = enrollmentService.listByCourse(courseId)
                .stream()
                .map(record -> new EnrollmentResponse(
                        record.getId(),
                        record.getCourseId(),
                        record.getStudentId(),
                        record.getEnrolledAt().toString()
                ))
                .toList();

        logger.info("{} 完成按课程查询 - courseId: {}, 共返回 {} 条记录",
                instanceInfo, courseId, responses.size());
        return responses;
    }

    @GetMapping("/student/{studentId}")
    public List<EnrollmentResponse> listByStudent(@PathVariable String studentId) {
        String instanceInfo = getInstanceInfo();
        logger.info("{} 处理按学生查询选课记录请求 - studentId: {}",
                instanceInfo, studentId);

        List<EnrollmentResponse> responses = enrollmentService.listByStudent(studentId)
                .stream()
                .map(record -> new EnrollmentResponse(
                        record.getId(),
                        record.getCourseId(),
                        record.getStudentId(),
                        record.getEnrolledAt().toString()
                ))
                .toList();

        logger.info("{} 完成按学生查询 - studentId: {}, 共返回 {} 条记录",
                instanceInfo, studentId, responses.size());
        return responses;
    }

    @GetMapping
    public List<EnrollmentResponse> listAll() {
        String instanceInfo = getInstanceInfo();
        logger.info("{} 处理查询所有选课记录请求", instanceInfo);

        List<EnrollmentResponse> responses = enrollmentService.listAll()
                .stream()
                .map(record -> new EnrollmentResponse(
                        record.getId(),
                        record.getCourseId(),
                        record.getStudentId(),
                        record.getEnrolledAt().toString()
                ))
                .toList();

        logger.info("{} 完成查询所有选课记录 - 共返回 {} 条记录",
                instanceInfo, responses.size());
        return responses;
    }

    // ==================== 健康检查接口 ====================
    @GetMapping("/health")
    public Map<String, Object> health() {
        String instanceInfo = getInstanceInfo();
        logger.info("{} 处理健康检查请求", instanceInfo);

        Map<String, Object> healthResponse = new HashMap<>();
        healthResponse.put("status", "UP");
        healthResponse.put("service", "enrollment-service");
        healthResponse.put("port", currentPort);
        healthResponse.put("hostname", getHostname());
        healthResponse.put("instanceInfo", instanceInfo); // 新增：返回实例标识
        healthResponse.put("timestamp", System.currentTimeMillis());

        logger.info("{} 健康检查通过 - 响应: {}", instanceInfo, healthResponse);
        return healthResponse;
    }

    // ==================== Record 定义 ====================
    public record EnrollmentRequest(
            @NotBlank String courseId,
            @NotBlank String studentId
    ) {}

    public record EnrollmentResponse(
            String id,
            String courseId,
            String studentId,
            String enrolledAt
    ) {}
}
