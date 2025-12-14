package com.zjgsu.ljy.coursecloud.user.controller;

import com.zjgsu.ljy.coursecloud.user.model.Student;
import com.zjgsu.ljy.coursecloud.user.model.Teacher;
import com.zjgsu.ljy.coursecloud.user.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class UserController {

    // 统一日志记录器（规范命名）
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    @Value("${server.port}")
    private String currentPort;

    public UserController(UserService userService) {
        this.userService = userService;
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

    // ==================== Student Endpoints ====================
    @PostMapping("/students")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> createStudent(@Valid @RequestBody StudentRequest request) {
        String instanceId = getInstanceIdentifier();
        logger.info("{} 开始处理【创建学生】请求 - 学生ID: {}", instanceId, request.studentId());

        try {
            Student student = new Student(
                    request.username(),
                    request.email(),
                    request.studentId(),
                    request.name(),
                    request.major(),
                    request.grade()
            );
            Student created = userService.createStudent(student);

            logger.info("{} 完成【创建学生】请求 - 学生ID: {}，生成用户ID: {}", instanceId, request.studentId(), created.getId());

            // 标准化响应体
            Map<String, Object> response = new HashMap<>();
            response.put("instance", instanceId);      // 统一实例标识
            response.put("port", currentPort);
            response.put("hostname", getHostname());
            response.put("ip", getHostIp());           // 容器IP
            response.put("data", StudentResponse.from(created));
            response.put("status", "SUCCESS");
            response.put("timestamp", LocalDateTime.now());
            return response;
        } catch (Exception e) {
            logger.error("{} 处理【创建学生】请求失败 - 学生ID: {}，异常信息: {}",
                    instanceId, request.studentId(), e.getMessage(), e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("instance", instanceId);
            errorResponse.put("port", currentPort);
            errorResponse.put("hostname", getHostname());
            errorResponse.put("ip", getHostIp());
            errorResponse.put("status", "ERROR");
            errorResponse.put("message", "创建学生失败: " + e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());
            return errorResponse;
        }
    }

    @GetMapping("/students")
    public Map<String, Object> getAllStudents() {
        String instanceId = getInstanceIdentifier();
        logger.info("{} 开始处理【查询所有学生】请求", instanceId);

        List<StudentResponse> students = userService.getAllStudents().stream()
                .map(StudentResponse::from)
                .collect(Collectors.toList());

        logger.info("{} 完成【查询所有学生】请求 - 共返回 {} 条学生记录", instanceId, students.size());

        Map<String, Object> response = new HashMap<>();
        response.put("instance", instanceId);
        response.put("port", currentPort);
        response.put("hostname", getHostname());
        response.put("ip", getHostIp());
        response.put("data", students);
        response.put("count", students.size());
        response.put("status", "SUCCESS");
        response.put("timestamp", LocalDateTime.now());
        return response;
    }

    @GetMapping("/students/{id}")
    public ResponseEntity<Map<String, Object>> getStudentById(@PathVariable String id) {
        String instanceId = getInstanceIdentifier();
        logger.info("{} 开始处理【查询单个学生】请求 - 学生ID: {}", instanceId, id);

        return userService.getStudentById(id)
                .map(student -> {
                    logger.info("{} 完成【查询单个学生】请求 - 学生ID: {}，查询成功", instanceId, id);

                    Map<String, Object> response = new HashMap<>();
                    response.put("instance", instanceId);
                    response.put("port", currentPort);
                    response.put("hostname", getHostname());
                    response.put("ip", getHostIp());
                    response.put("data", StudentResponse.from(student));
                    response.put("status", "SUCCESS");
                    response.put("timestamp", LocalDateTime.now());
                    return ResponseEntity.ok(response);
                })
                .orElseGet(() -> {
                    logger.warn("{} 完成【查询单个学生】请求 - 学生ID: {}，不存在该学生", instanceId, id);

                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("instance", instanceId);
                    errorResponse.put("port", currentPort);
                    errorResponse.put("hostname", getHostname());
                    errorResponse.put("ip", getHostIp());
                    errorResponse.put("status", "ERROR");
                    errorResponse.put("message", "Student with id " + id + " not found");
                    errorResponse.put("timestamp", LocalDateTime.now());
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
                });
    }

    @GetMapping("/students/studentId/{studentId}")
    public ResponseEntity<Map<String, Object>> getStudentByStudentId(@PathVariable String studentId) {
        String instanceId = getInstanceIdentifier();
        logger.info("{} 开始处理【按学号查询学生】请求 - 学号: {}", instanceId, studentId);

        return userService.getStudentByStudentId(studentId)
                .map(student -> {
                    logger.info("{} 完成【按学号查询学生】请求 - 学号: {}，查询成功", instanceId, studentId);

                    Map<String, Object> response = new HashMap<>();
                    response.put("instance", instanceId);
                    response.put("port", currentPort);
                    response.put("hostname", getHostname());
                    response.put("ip", getHostIp());
                    response.put("data", StudentResponse.from(student));
                    response.put("status", "SUCCESS");
                    response.put("timestamp", LocalDateTime.now());
                    return ResponseEntity.ok(response);
                })
                .orElseGet(() -> {
                    logger.warn("{} 完成【按学号查询学生】请求 - 学号: {}，不存在该学生", instanceId, studentId);

                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("instance", instanceId);
                    errorResponse.put("port", currentPort);
                    errorResponse.put("hostname", getHostname());
                    errorResponse.put("ip", getHostIp());
                    errorResponse.put("status", "ERROR");
                    errorResponse.put("message", "Student with studentId " + studentId + " not found");
                    errorResponse.put("timestamp", LocalDateTime.now());
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
                });
    }

    @PutMapping("/students/{id}")
    public ResponseEntity<Map<String, Object>> updateStudent(
            @PathVariable String id,
            @Valid @RequestBody StudentRequest request) {
        String instanceId = getInstanceIdentifier();
        logger.info("{} 开始处理【更新学生】请求 - 学生ID: {}", instanceId, id);

        return userService.getStudentById(id)
                .map(existing -> {
                    try {
                        existing.setUsername(request.username());
                        existing.setEmail(request.email());
                        existing.setStudentId(request.studentId());
                        existing.setName(request.name());
                        existing.setMajor(request.major());
                        existing.setGrade(request.grade());
                        Student updated = userService.updateStudent(existing);

                        logger.info("{} 完成【更新学生】请求 - 学生ID: {}，更新成功", instanceId, id);

                        Map<String, Object> response = new HashMap<>();
                        response.put("instance", instanceId);
                        response.put("port", currentPort);
                        response.put("hostname", getHostname());
                        response.put("ip", getHostIp());
                        response.put("data", StudentResponse.from(updated));
                        response.put("status", "SUCCESS");
                        response.put("timestamp", LocalDateTime.now());
                        return ResponseEntity.ok(response);
                    } catch (Exception e) {
                        logger.error("{} 处理【更新学生】请求失败 - 学生ID: {}，异常信息: {}",
                                instanceId, id, e.getMessage(), e);

                        Map<String, Object> errorResponse = new HashMap<>();
                        errorResponse.put("instance", instanceId);
                        errorResponse.put("port", currentPort);
                        errorResponse.put("hostname", getHostname());
                        errorResponse.put("ip", getHostIp());
                        errorResponse.put("status", "ERROR");
                        errorResponse.put("message", "更新学生失败: " + e.getMessage());
                        errorResponse.put("timestamp", LocalDateTime.now());
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
                    }
                })
                .orElseGet(() -> {
                    logger.warn("{} 完成【更新学生】请求 - 学生ID: {}，不存在该学生", instanceId, id);

                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("instance", instanceId);
                    errorResponse.put("port", currentPort);
                    errorResponse.put("hostname", getHostname());
                    errorResponse.put("ip", getHostIp());
                    errorResponse.put("status", "ERROR");
                    errorResponse.put("message", "Student with id " + id + " not found");
                    errorResponse.put("timestamp", LocalDateTime.now());
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
                });
    }

    @DeleteMapping("/students/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteStudent(@PathVariable String id) {
        String instanceId = getInstanceIdentifier();
        logger.info("{} 开始处理【删除学生】请求 - 学生ID: {}", instanceId, id);

        try {
            userService.deleteStudent(id);
            logger.info("{} 完成【删除学生】请求 - 学生ID: {}，删除成功", instanceId, id);
        } catch (Exception e) {
            logger.error("{} 处理【删除学生】请求失败 - 学生ID: {}，异常信息: {}",
                    instanceId, id, e.getMessage(), e);
            throw e; // 抛出异常以保持原有状态码逻辑
        }
    }

    // ==================== Teacher Endpoints ====================
    @PostMapping("/teachers")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> createTeacher(@Valid @RequestBody TeacherRequest request) {
        String instanceId = getInstanceIdentifier();
        logger.info("{} 开始处理【创建教师】请求 - 教师ID: {}", instanceId, request.teacherId());

        try {
            Teacher teacher = new Teacher(
                    request.username(),
                    request.email(),
                    request.teacherId(),
                    request.name(),
                    request.department(),
                    request.title()
            );
            Teacher created = userService.createTeacher(teacher);

            logger.info("{} 完成【创建教师】请求 - 教师ID: {}，生成用户ID: {}", instanceId, request.teacherId(), created.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("instance", instanceId);
            response.put("port", currentPort);
            response.put("hostname", getHostname());
            response.put("ip", getHostIp());
            response.put("data", TeacherResponse.from(created));
            response.put("status", "SUCCESS");
            response.put("timestamp", LocalDateTime.now());
            return response;
        } catch (Exception e) {
            logger.error("{} 处理【创建教师】请求失败 - 教师ID: {}，异常信息: {}",
                    instanceId, request.teacherId(), e.getMessage(), e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("instance", instanceId);
            errorResponse.put("port", currentPort);
            errorResponse.put("hostname", getHostname());
            errorResponse.put("ip", getHostIp());
            errorResponse.put("status", "ERROR");
            errorResponse.put("message", "创建教师失败: " + e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());
            return errorResponse;
        }
    }

    @GetMapping("/teachers")
    public Map<String, Object> getAllTeachers() {
        String instanceId = getInstanceIdentifier();
        logger.info("{} 开始处理【查询所有教师】请求", instanceId);

        List<TeacherResponse> teachers = userService.getAllTeachers().stream()
                .map(TeacherResponse::from)
                .collect(Collectors.toList());

        logger.info("{} 完成【查询所有教师】请求 - 共返回 {} 条教师记录", instanceId, teachers.size());

        Map<String, Object> response = new HashMap<>();
        response.put("instance", instanceId);
        response.put("port", currentPort);
        response.put("hostname", getHostname());
        response.put("ip", getHostIp());
        response.put("data", teachers);
        response.put("count", teachers.size());
        response.put("status", "SUCCESS");
        response.put("timestamp", LocalDateTime.now());
        return response;
    }

    @GetMapping("/teachers/{id}")
    public ResponseEntity<Map<String, Object>> getTeacherById(@PathVariable String id) {
        String instanceId = getInstanceIdentifier();
        logger.info("{} 开始处理【查询单个教师】请求 - 教师ID: {}", instanceId, id);

        return userService.getTeacherById(id)
                .map(teacher -> {
                    logger.info("{} 完成【查询单个教师】请求 - 教师ID: {}，查询成功", instanceId, id);

                    Map<String, Object> response = new HashMap<>();
                    response.put("instance", instanceId);
                    response.put("port", currentPort);
                    response.put("hostname", getHostname());
                    response.put("ip", getHostIp());
                    response.put("data", TeacherResponse.from(teacher));
                    response.put("status", "SUCCESS");
                    response.put("timestamp", LocalDateTime.now());
                    return ResponseEntity.ok(response);
                })
                .orElseGet(() -> {
                    logger.warn("{} 完成【查询单个教师】请求 - 教师ID: {}，不存在该教师", instanceId, id);

                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("instance", instanceId);
                    errorResponse.put("port", currentPort);
                    errorResponse.put("hostname", getHostname());
                    errorResponse.put("ip", getHostIp());
                    errorResponse.put("status", "ERROR");
                    errorResponse.put("message", "Teacher with id " + id + " not found");
                    errorResponse.put("timestamp", LocalDateTime.now());
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
                });
    }

    @GetMapping("/teachers/teacherId/{teacherId}")
    public ResponseEntity<Map<String, Object>> getTeacherByTeacherId(@PathVariable String teacherId) {
        String instanceId = getInstanceIdentifier();
        logger.info("{} 开始处理【按教师号查询教师】请求 - 教师号: {}", instanceId, teacherId);

        return userService.getTeacherByTeacherId(teacherId)
                .map(teacher -> {
                    logger.info("{} 完成【按教师号查询教师】请求 - 教师号: {}，查询成功", instanceId, teacherId);

                    Map<String, Object> response = new HashMap<>();
                    response.put("instance", instanceId);
                    response.put("port", currentPort);
                    response.put("hostname", getHostname());
                    response.put("ip", getHostIp());
                    response.put("data", TeacherResponse.from(teacher));
                    response.put("status", "SUCCESS");
                    response.put("timestamp", LocalDateTime.now());
                    return ResponseEntity.ok(response);
                })
                .orElseGet(() -> {
                    logger.warn("{} 完成【按教师号查询教师】请求 - 教师号: {}，不存在该教师", instanceId, teacherId);

                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("instance", instanceId);
                    errorResponse.put("port", currentPort);
                    errorResponse.put("hostname", getHostname());
                    errorResponse.put("ip", getHostIp());
                    errorResponse.put("status", "ERROR");
                    errorResponse.put("message", "Teacher with teacherId " + teacherId + " not found");
                    errorResponse.put("timestamp", LocalDateTime.now());
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
                });
    }

    @DeleteMapping("/teachers/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTeacher(@PathVariable String id) {
        String instanceId = getInstanceIdentifier();
        logger.info("{} 开始处理【删除教师】请求 - 教师ID: {}", instanceId, id);

        try {
            userService.deleteTeacher(id);
            logger.info("{} 完成【删除教师】请求 - 教师ID: {}，删除成功", instanceId, id);
        } catch (Exception e) {
            logger.error("{} 处理【删除教师】请求失败 - 教师ID: {}，异常信息: {}",
                    instanceId, id, e.getMessage(), e);
            throw e;
        }
    }

    // ==================== Health Check（标准化） ====================
    @GetMapping("/actuator/health")
    public Map<String, Object> healthCheck() {
        String instanceId = getInstanceIdentifier();
        logger.info("{} 处理【健康检查】请求", instanceId);

        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("instance", instanceId);      // 统一实例标识
        health.put("port", currentPort);
        health.put("hostname", getHostname());
        health.put("ip", getHostIp());           // 新增IP信息
        health.put("service", "user-service");
        health.put("timestamp", System.currentTimeMillis());
        return health;
    }

    // ==================== 测试接口（负载均衡验证，强化版）====================
    @GetMapping("/students/test")
    public Map<String, Object> testStudent() {
        String instanceId = getInstanceIdentifier();
        logger.info("{} 处理【负载均衡测试】请求 - 端点: /api/students/test", instanceId);

        Map<String, Object> response = new HashMap<>();
        response.put("service", "user-service");
        response.put("endpoint", "/api/students/test");
        response.put("instance", instanceId);    // 统一实例标识
        response.put("port", currentPort);
        response.put("hostname", getHostname());
        response.put("ip", getHostIp());         // 容器IP
        response.put("timestamp", LocalDateTime.now());
        response.put("status", "UP");
        return response;
    }

    // ==================== Record 定义（保持原有结构，修复时间格式）====================
    public record StudentRequest(
            String username,
            String email,
            String studentId,
            String name,
            String major,
            Integer grade
    ) {}

    public record StudentResponse(
            String id,
            String username,
            String email,
            String studentId,
            String name,
            String major,
            Integer grade,
            String createdAt
    ) {
        public static StudentResponse from(Student student) {
            return new StudentResponse(
                    student.getId(),
                    student.getUsername(),
                    student.getEmail(),
                    student.getStudentId(),
                    student.getName(),
                    student.getMajor(),
                    student.getGrade(),
                    student.getCreatedAt().toString()
            );
        }
    }

    public record TeacherRequest(
            String username,
            String email,
            String teacherId,
            String name,
            String department,
            String title
    ) {}

    public record TeacherResponse(
            String id,
            String username,
            String email,
            String teacherId,
            String name,
            String department,
            String title
    ) {
        public static TeacherResponse from(Teacher teacher) {
            return new TeacherResponse(
                    teacher.getId(),
                    teacher.getUsername(),
                    teacher.getEmail(),
                    teacher.getTeacherId(),
                    teacher.getName(),
                    teacher.getDepartment(),
                    teacher.getTitle()
            );
        }
    }
}
