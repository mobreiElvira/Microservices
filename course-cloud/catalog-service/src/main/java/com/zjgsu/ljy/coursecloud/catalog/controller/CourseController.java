package com.zjgsu.ljy.coursecloud.catalog.controller;

import com.zjgsu.ljy.coursecloud.catalog.model.Course;
import com.zjgsu.ljy.coursecloud.catalog.model.Instructor;
import com.zjgsu.ljy.coursecloud.catalog.model.ScheduleSlot;
import com.zjgsu.ljy.coursecloud.catalog.repository.CourseRepository;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.InetAddress;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    // 统一日志记录器（规范命名）
    private static final Logger logger = LoggerFactory.getLogger(CourseController.class);

    private final CourseRepository repository;

    @Value("${server.port}")
    private String currentPort;

    public CourseController(CourseRepository repository) {
        this.repository = repository;
    }

    // ==================== 核心辅助方法：生成统一的实例标识 ====================
    private String getInstanceIdentifier() {
        return String.format("[容器名: %s | 端口: %s]", getHostname(), currentPort);
    }

    // ==================== 辅助方法：获取主机名/容器名 ====================
    private String getHostname() {
        // 优先使用 Docker 容器的 HOSTNAME 环境变量
        String hostname = System.getenv("HOSTNAME");
        if (hostname != null && !hostname.isEmpty()) {
            return hostname;
        }

        // 备用方案：获取本机主机名
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            logger.warn("{} 获取主机名失败: {}", getInstanceIdentifier(), e.getMessage());
        }

        // 最终兜底
        return "unknown-" + currentPort;
    }

    // ==================== 辅助方法：获取容器IP（增强负载均衡验证） ====================
    private String getHostIp() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            logger.warn("{} 获取IP地址失败: {}", getInstanceIdentifier(), e.getMessage());
            return "unknown";
        }
    }

    // ==================== Course Endpoints ====================
    @GetMapping
    public Map<String, Object> listCourses() {
        // 1. 请求日志：明确操作+实例标识
        String instanceId = getInstanceIdentifier();
        logger.info("{} 开始处理【查询所有课程】请求", instanceId);

        // 2. 业务逻辑
        List<CourseResponse> courses = repository.findAll()
                .stream()
                .map(CourseResponse::from)
                .collect(Collectors.toList());

        // 3. 响应日志：包含处理结果
        logger.info("{} 完成【查询所有课程】请求 - 共返回 {} 条课程记录", instanceId, courses.size());

        // 4. 响应体增强：包含完整实例信息
        Map<String, Object> response = new HashMap<>();
        response.put("instance", instanceId);  // 统一实例标识
        response.put("port", currentPort);
        response.put("hostname", getHostname());
        response.put("ip", getHostIp());       // 新增IP信息
        response.put("data", courses);
        response.put("count", courses.size());
        response.put("status", "SUCCESS");
        response.put("timestamp", LocalDateTime.now());  // 新增时间戳
        return response;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getCourse(@PathVariable String id) {
        String instanceId = getInstanceIdentifier();
        logger.info("{} 开始处理【查询单个课程】请求 - 课程ID: {}", instanceId, id);

        return repository.findById(id)
                .map(course -> {
                    // 3. 成功响应日志
                    logger.info("{} 完成【查询单个课程】请求 - 课程ID: {}，查询成功", instanceId, id);

                    // 4. 成功响应体：增强实例信息
                    Map<String, Object> response = new HashMap<>();
                    response.put("instance", instanceId);
                    response.put("port", currentPort);
                    response.put("hostname", getHostname());
                    response.put("ip", getHostIp());
                    response.put("data", CourseResponse.from(course));
                    response.put("status", "SUCCESS");
                    response.put("timestamp", LocalDateTime.now());
                    return ResponseEntity.ok(response);
                })
                .orElseGet(() -> {
                    // 3. 失败响应日志
                    logger.warn("{} 完成【查询单个课程】请求 - 课程ID: {}，不存在该课程", instanceId, id);

                    // 4. 失败响应体：统一格式
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("instance", instanceId);
                    errorResponse.put("port", currentPort);
                    errorResponse.put("hostname", getHostname());
                    errorResponse.put("ip", getHostIp());
                    errorResponse.put("status", "ERROR");
                    errorResponse.put("message", "Course with id " + id + " not found");
                    errorResponse.put("timestamp", LocalDateTime.now());
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
                });
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createCourse(@Valid @RequestBody CourseRequest request) {
        String instanceId = getInstanceIdentifier();
        logger.info("{} 开始处理【创建课程】请求 - 课程编码: {}", instanceId, request.code());

        try {
            // 2. 业务逻辑
            Course course = new Course(
                    request.code(),
                    request.title(),
                    new Instructor(request.instructorId(), request.instructorName(), request.instructorEmail()),
                    new ScheduleSlot(
                            request.dayOfWeek().toDayOfWeek(),
                            LocalTime.parse(request.start()),
                            LocalTime.parse(request.end()),
                            request.expectedAttendance()
                    ),
                    request.capacity()
            );
            Course saved = repository.save(course);

            // 3. 成功日志
            logger.info("{} 完成【创建课程】请求 - 课程编码: {}，生成课程ID: {}", instanceId, request.code(), saved.getId());

            // 4. 成功响应体
            Map<String, Object> response = new HashMap<>();
            response.put("instance", instanceId);
            response.put("port", currentPort);
            response.put("hostname", getHostname());
            response.put("ip", getHostIp());
            response.put("data", CourseResponse.from(saved));
            response.put("status", "SUCCESS");
            response.put("timestamp", LocalDateTime.now());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            // 3. 异常日志（新增：捕获创建失败异常）
            logger.error("{} 处理【创建课程】请求失败 - 课程编码: {}，异常信息: {}",
                    instanceId, request.code(), e.getMessage(), e);

            // 4. 异常响应体
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("instance", instanceId);
            errorResponse.put("port", currentPort);
            errorResponse.put("hostname", getHostname());
            errorResponse.put("ip", getHostIp());
            errorResponse.put("status", "ERROR");
            errorResponse.put("message", "Failed to create course: " + e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==================== 测试接口（负载均衡验证，强化版）====================
    @GetMapping("/test")
    public Map<String, Object> test() {
        String instanceId = getInstanceIdentifier();
        logger.info("{} 处理【负载均衡测试】请求", instanceId);

        Map<String, Object> response = new HashMap<>();
        response.put("service", "catalog-service");
        response.put("instance", instanceId);  // 统一实例标识
        response.put("port", currentPort);
        response.put("hostname", getHostname());
        response.put("ip", getHostIp());       // 容器IP
        response.put("timestamp", LocalDateTime.now());
        response.put("status", "UP");
        return response;
    }

    // ==================== 健康检查接口（标准化） ====================
    @GetMapping("/actuator/health")
    public Map<String, Object> health() {
        String instanceId = getInstanceIdentifier();
        logger.info("{} 处理【健康检查】请求", instanceId);

        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("instance", instanceId);    // 统一实例标识
        health.put("port", currentPort);
        health.put("hostname", getHostname());
        health.put("ip", getHostIp());         // 新增IP
        health.put("service", "catalog-service");
        health.put("timestamp", System.currentTimeMillis());
        return health;
    }
}
