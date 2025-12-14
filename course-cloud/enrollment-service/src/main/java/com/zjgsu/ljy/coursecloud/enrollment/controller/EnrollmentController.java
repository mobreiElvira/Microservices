package com.zjgsu.ljy.coursecloud.enrollment.controller;
 // 09 本地测试前
import com.zjgsu.ljy.coursecloud.enrollment.model.EnrollmentRecord;
import com.zjgsu.ljy.coursecloud.enrollment.service.EnrollmentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/enrollments")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${server.port}")
    private String currentPort;

    // 配置 user-service 服务名（网关/微服务环境）
    @Value("${service.user.url:http://user-service}")
    private String userServiceUrl;

    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    // ==================== 辅助方法：获取主机名 ====================
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

    // ==================== Enrollment Endpoints ====================
//    @PostMapping
//    public ResponseEntity<EnrollmentResponse> enroll(@Valid @RequestBody EnrollmentRequest request) {
//        EnrollmentRecord record = enrollmentService.enroll(request.courseId(), request.studentId());
//        return ResponseEntity.status(HttpStatus.CREATED)
//                .body(new EnrollmentResponse(
//                        record.getId(),
//                        record.getCourseId(),
//                        record.getStudentId(),
//                        record.getEnrolledAt().toString()
//                ));
//    }

//    @PostMapping
//    public ResponseEntity<EnrollmentResponse> enroll(
//            // 1. 从请求头获取网关传递的用户信息
//            @RequestHeader("X-User-Id") String userId,
//            @RequestHeader("X-Username") String username,
//            @RequestHeader("X-User-Role") String userRole,
//            // 2. 原有请求参数
//            @Valid @RequestBody EnrollmentRequest request) {
//
////        // 3. 身份校验：仅学生可选课（根据业务定义Role值，如"STUDENT"）
////        if (!"STUDENT".equals(userRole)) {
////            log.warn("非学生用户尝试选课：userId={}, username={}, role={}", userId, username, userRole);
////            return ResponseEntity.status(HttpStatus.FORBIDDEN)
////                    .body(new EnrollmentResponse(null, null, null, "仅学生可进行选课操作"));
////        }
////
////        // 4. 业务校验：确保选课的学生ID与当前登录用户ID一致（防止越权）
////        if (!userId.equals(request.studentId())) {
////            log.warn("选课学生ID与登录用户不一致：登录userId={}, 选课studentId={}", userId, request.studentId());
////            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
////                    .body(new EnrollmentResponse(null, null, null, "不能替其他学生选课"));
////        }
////
//      //   5. 执行选课业务逻辑
//        log.info("学生选课请求：userId={}, username={}, courseId={}", userId, username, request.courseId());
//        EnrollmentRecord record = enrollmentService.enroll(request.courseId(), request.studentId());
//
//        // 6. 返回结果
//        return ResponseEntity.status(HttpStatus.CREATED)
//                .body(new EnrollmentResponse(
//                        record.getId(),
//                        record.getCourseId(),
//                        record.getStudentId(),
//                        record.getEnrolledAt().toString()
//                ));
//
//
//    }


    // ==================== 核心选课接口（网关适配版） ====================
    @PostMapping
    public ResponseEntity<EnrollmentResponse> enroll(
            // 1. 网关传递的认证信息（Token 已由网关校验，仅做空值兜底）
            @RequestHeader(value = "Authorization", required = false) String token,
            // 2. 网关解析 Token 后传递的用户信息
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Username") String username,
            @RequestHeader("X-User-Role") String userRole,
            // 3. 选课请求参数
            @Valid @RequestBody EnrollmentRequest request) {

        // ===== 兜底校验：Token 不能为空（网关层已拦截，仅防御性校验）=====
        if (token == null || !token.startsWith("Bearer ")) {
            log.warn("未携带有效Token的选课请求: userId={}, username={}", userId, username);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new EnrollmentResponse(null, null, null, "请先登录后再选课"));
        }

        // ===== 权限校验：仅学生可选课 =====
        if (!"STUDENT".equals(userRole)) {
            log.warn("非学生用户尝试选课: userId={}, username={}, role={}", userId, username, userRole);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new EnrollmentResponse(null, null, null, "仅学生可进行选课操作"));
        }

        // ===== 越权校验：验证选课学生ID与登录用户匹配 =====
        String requestStudentId = request.studentId();
        try {
            // 调用 user-service 根据学号查询学生信息（验证学号存在+匹配 userId）
            String studentApiUrl = userServiceUrl + "/api/users/students/studentId/" + requestStudentId;
            Map<String, Object> studentResponse = restTemplate.getForObject(studentApiUrl, Map.class);

            // 校验学生是否存在
            if (studentResponse == null || "ERROR".equals(studentResponse.get("status"))) {
                log.error("选课学生不存在: studentId={}, userId={}", requestStudentId, userId);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new EnrollmentResponse(null, null, null, "学生不存在：" + requestStudentId));
            }

            // 校验登录用户ID与学生主键ID一致（防止替他人选课）
            Map<String, Object> studentData = (Map<String, Object>) studentResponse.get("data");
            String realUserId = (String) studentData.get("id");
            if (!userId.equals(realUserId)) {
                log.warn("越权选课检测: 登录userId={}, 选课学生真实userId={}, studentId={}",
                        userId, realUserId, requestStudentId);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new EnrollmentResponse(null, null, null, "不能替其他学生选课"));
            }
        } catch (HttpClientErrorException.NotFound e) {
            log.error("学生不存在: studentId={}", requestStudentId, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new EnrollmentResponse(null, null, null, "学生不存在：" + requestStudentId));
        } catch (Exception e) {
            log.error("验证学生信息失败: studentId={}, userId={}", requestStudentId, userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new EnrollmentResponse(null, null, null, "选课失败：验证学生信息异常"));
        }

        // ===== 执行选课业务逻辑 =====
        log.info("学生选课请求成功（网关版）: userId={}, username={}, courseId={}, studentId={}",
                userId, username, request.courseId(), requestStudentId);
        EnrollmentRecord record = enrollmentService.enroll(request.courseId(), requestStudentId);

        // ===== 返回选课结果 =====
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
        return enrollmentService.listByCourse(courseId)
                .stream()
                .map(record -> new EnrollmentResponse(
                        record.getId(),
                        record.getCourseId(),
                        record.getStudentId(),
                        record.getEnrolledAt().toString()
                ))
                .toList();
    }

    @GetMapping("/student/{studentId}")
    public List<EnrollmentResponse> listByStudent(@PathVariable String studentId) {
        return enrollmentService.listByStudent(studentId)
                .stream()
                .map(record -> new EnrollmentResponse(
                        record.getId(),
                        record.getCourseId(),
                        record.getStudentId(),
                        record.getEnrolledAt().toString()
                ))
                .toList();
    }

    @GetMapping
    public List<EnrollmentResponse> listAll() {
        return enrollmentService.listAll()
                .stream()
                .map(record -> new EnrollmentResponse(
                        record.getId(),
                        record.getCourseId(),
                        record.getStudentId(),
                        record.getEnrolledAt().toString()
                ))
                .toList();
    }


    // ==================== 测试接口(负载均衡验证) ====================
    @GetMapping("/test")
    public Map<String, Object> test() {
        Map<String, Object> response = new HashMap<>();
        response.put("service", "enrollment-service");
        response.put("port", currentPort);
        response.put("hostname", getHostname());

        // ★ 添加容器IP
        try {
            response.put("ip", InetAddress.getLocalHost().getHostAddress());
        } catch (Exception e) {
            response.put("ip", "unknown");
        }

        response.put("timestamp", LocalDateTime.now());

        // 测试调用 user-service
        try {
            Map<String, Object> userTest = restTemplate.getForObject(
                    "http://user-service/api/students/test",
                    Map.class
            );
            response.put("user-service", userTest);
        } catch (Exception e) {
            response.put("user-service-error", e.getMessage());
        }

        // 测试调用 catalog-service
        try {
            Map<String, Object> catalogTest = restTemplate.getForObject(
                    "http://catalog-service/api/courses/test",
                    Map.class
            );
            response.put("catalog-service", catalogTest);
        } catch (Exception e) {
            response.put("catalog-service-error", e.getMessage());
        }

        return response;
    }

    // ==================== 健康检查接口 ====================
    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> healthResponse = new HashMap<>();
        healthResponse.put("status", "UP");
        healthResponse.put("service", "enrollment-service");
        healthResponse.put("port", currentPort);
        healthResponse.put("hostname", getHostname());
        healthResponse.put("timestamp", System.currentTimeMillis());
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

// 09本地测试

//package com.zjgsu.cyd.coursecloud.enrollment.controller;
//
//import com.zjgsu.cyd.coursecloud.enrollment.model.EnrollmentRecord;
//import com.zjgsu.cyd.coursecloud.enrollment.service.EnrollmentService;
//import jakarta.validation.Valid;
//import jakarta.validation.constraints.NotBlank;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.client.HttpClientErrorException;
//import org.springframework.web.client.RestTemplate;
//
//import java.net.InetAddress;
//import java.time.LocalDateTime;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//@Slf4j
//@RestController
//@RequestMapping("/api/enrollments")
//public class EnrollmentController {
//
//    private final EnrollmentService enrollmentService;
//
//    @Autowired
//    private RestTemplate restTemplate;
//
//    @Value("${server.port}")
//    private String currentPort;
//
//    // 本地测试：配置user/catalog服务的地址（可通过配置文件注入，更灵活）
//    @Value("${user.service.url:http://localhost:8081}")
//    private String userServiceUrl;
//    @Value("${catalog.service.url:http://localhost:8082}")
//    private String catalogServiceUrl;
//
//    public EnrollmentController(EnrollmentService enrollmentService) {
//        this.enrollmentService = enrollmentService;
//    }
//
//    // ==================== 辅助方法：获取主机名 ====================
//    private String getHostname() {
//        // 优先使用环境变量 HOSTNAME (Docker 容器中最可靠)
//        String hostname = System.getenv("HOSTNAME");
//        if (hostname != null && !hostname.isEmpty()) {
//            return hostname;
//        }
//
//        // 备用方案：使用 InetAddress
//        try {
//            return InetAddress.getLocalHost().getHostName();
//        } catch (Exception e) {
//            // 忽略异常
//        }
//
//        // 最后的 fallback
//        return "unknown-" + currentPort;
//    }
//
//    // ==================== Enrollment Endpoints ====================
////    @PostMapping
////    public ResponseEntity<EnrollmentResponse> enroll(@Valid @RequestBody EnrollmentRequest request) {
////        EnrollmentRecord record = enrollmentService.enroll(request.courseId(), request.studentId());
////        return ResponseEntity.status(HttpStatus.CREATED)
////                .body(new EnrollmentResponse(
////                        record.getId(),
////                        record.getCourseId(),
////                        record.getStudentId(),
////                        record.getEnrolledAt().toString()
////                ));
////    }
//    @PostMapping
//    public ResponseEntity<EnrollmentResponse> enroll(
//            // 1. 从请求头获取网关传递的用户信息
//            @RequestHeader(value = "Authorization", required = false) String token, // 新增Token请求头
//            @RequestHeader("X-User-Id") String userId,
//            @RequestHeader("X-Username") String username,
//            @RequestHeader("X-User-Role") String userRole,
//            // 2. 原有请求参数
//            @Valid @RequestBody EnrollmentRequest request) {
//
//        // 新增：1. 校验Token是否存在
//        if (token == null || !token.startsWith("Bearer ")) {
//            log.warn("未提供Token，拒绝选课请求");
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                    .body(new EnrollmentResponse(null, null, null, "请提供有效的Token"));
//        }
//
//        // 新增：2. 校验Token是否有效（调用user-service的Token验证接口）
//        String jwtToken = token.substring(7); // 去掉"Bearer "前缀
//        try {
//            String validateUrl = userServiceUrl + "/api/auth/verifyToken?token=" + jwtToken;
//            Boolean isValid = restTemplate.getForObject(validateUrl, Boolean.class);
//            if (isValid == null || !isValid) {
//                log.warn("Token无效，拒绝选课请求");
//                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                        .body(new EnrollmentResponse(null, null, null, "Token无效或已过期"));
//            }
//        } catch (Exception e) {
//            log.error("Token验证失败", e);
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(new EnrollmentResponse(null, null, null, "Token验证失败"));
//        }
//
//        // 3. 身份校验：仅学生可选课（根据业务定义Role值，如"STUDENT"）
//        if (!"STUDENT".equals(userRole)) {
//            log.warn("非学生用户尝试选课：userId={}, username={}, role={}", userId, username, userRole);
//            return ResponseEntity.status(HttpStatus.FORBIDDEN)
//                    .body(new EnrollmentResponse(null, null, null, "仅学生可进行选课操作"));
//        }
//
//        // 4. 修正：调用user-service的现有接口，根据studentId查询学生（验证该学号是否存在）
//        String requestStudentId = request.studentId();
//        try {
//            // 调用user-service的现有接口：/api/users/students/studentId/{studentId}
//            String userApiUrl = userServiceUrl + "/api/users/students/studentId/" + requestStudentId;
//            log.info("调用user-service验证学生: {}", userApiUrl);
//
//            Map<String, Object> studentResponse = restTemplate.getForObject(userApiUrl, Map.class);
//            // 验证学生是否存在
//            if (studentResponse == null || "ERROR".equals(studentResponse.get("status"))) {
//                log.error("学生不存在: {}", requestStudentId);
//                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//                        .body(new EnrollmentResponse(null, null, null, "学生不存在：" + requestStudentId));
//            }
//
//            // （可选）若想严格校验：从返回的学生信息中获取userId，对比请求头的X-User-Id
//            Map<String, Object> studentData = (Map<String, Object>) studentResponse.get("data");
//            String realUserId = (String) studentData.get("id"); // 学生的主键ID（对应X-User-Id）
//            if (!userId.equals(realUserId)) {
//                log.warn("选课学生ID与登录用户不一致：登录userId={}, 学生真实userId={}", userId, realUserId);
//                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//                        .body(new EnrollmentResponse(null, null, null, "不能替其他学生选课"));
//            }
//        } catch (HttpClientErrorException.NotFound e) {
//            log.error("学生不存在: {}", requestStudentId);
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//                    .body(new EnrollmentResponse(null, null, null, "学生不存在：" + requestStudentId));
//        } catch (Exception e) {
//            log.error("验证学生时出错: {}", e.getMessage(), e);
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(new EnrollmentResponse(null, null, null, "验证学生信息失败：" + e.getMessage()));
//        }
//
//        //   5. 执行选课业务逻辑
//        log.info("学生选课请求：userId={}, username={}, courseId={}", userId, username, request.courseId());
//        EnrollmentRecord record = enrollmentService.enroll(request.courseId(), request.studentId());
//
//        // 6. 返回结果
//        return ResponseEntity.status(HttpStatus.CREATED)
//                .body(new EnrollmentResponse(
//                        record.getId(),
//                        record.getCourseId(),
//                        record.getStudentId(),
//                        record.getEnrolledAt().toString()
//                ));
//    }
////
//
//
//    @GetMapping("/course/{courseId}")
//    public List<EnrollmentResponse> listByCourse(@PathVariable String courseId) {
//        return enrollmentService.listByCourse(courseId)
//                .stream()
//                .map(record -> new EnrollmentResponse(
//                        record.getId(),
//                        record.getCourseId(),
//                        record.getStudentId(),
//                        record.getEnrolledAt().toString()
//                ))
//                .toList();
//    }
//
//    @GetMapping("/student/{studentId}")
//    public List<EnrollmentResponse> listByStudent(@PathVariable String studentId) {
//        return enrollmentService.listByStudent(studentId)
//                .stream()
//                .map(record -> new EnrollmentResponse(
//                        record.getId(),
//                        record.getCourseId(),
//                        record.getStudentId(),
//                        record.getEnrolledAt().toString()
//                ))
//                .toList();
//    }
//
//    @GetMapping
//    public List<EnrollmentResponse> listAll() {
//        return enrollmentService.listAll()
//                .stream()
//                .map(record -> new EnrollmentResponse(
//                        record.getId(),
//                        record.getCourseId(),
//                        record.getStudentId(),
//                        record.getEnrolledAt().toString()
//                ))
//                .toList();
//    }
//
//    // ==================== 测试接口(负载均衡验证) ====================
//    @GetMapping("/test")
//    public Map<String, Object> test() {
//        Map<String, Object> response = new HashMap<>();
//        response.put("service", "enrollment-service");
//        response.put("port", currentPort);
//        response.put("hostname", getHostname());
//
//        // ★ 添加容器IP
//        try {
//            response.put("ip", InetAddress.getLocalHost().getHostAddress());
//        } catch (Exception e) {
//            response.put("ip", "unknown");
//        }
//
//        response.put("timestamp", LocalDateTime.now());
//
//        // 测试调用 user-service（修改：使用本地IP+端口，而非服务名）
//        try {
//            Map<String, Object> userTest = restTemplate.getForObject(
//                    userServiceUrl + "/api/students/test",  // 本地地址：http://localhost:8081/api/students/test
//                    Map.class
//            );
//            response.put("user-service", userTest);
//        } catch (Exception e) {
//            response.put("user-service-error", e.getMessage());
//        }
//
//        // 测试调用 catalog-service（修改：使用本地IP+端口，而非服务名）
//        try {
//            Map<String, Object> catalogTest = restTemplate.getForObject(
//                    catalogServiceUrl + "/api/courses/test",  // 本地地址：http://localhost:8082/api/courses/test
//                    Map.class
//            );
//            response.put("catalog-service", catalogTest);
//        } catch (Exception e) {
//            response.put("catalog-service-error", e.getMessage());
//        }
//
//        return response;
//    }
//
//    // ==================== 健康检查接口 ====================
//    @GetMapping("/health")
//    public Map<String, Object> health() {
//        Map<String, Object> healthResponse = new HashMap<>();
//        healthResponse.put("status", "UP");
//        healthResponse.put("service", "enrollment-service");
//        healthResponse.put("port", currentPort);
//        healthResponse.put("hostname", getHostname());
//        healthResponse.put("timestamp", System.currentTimeMillis());
//        return healthResponse;
//    }
//
//    // ==================== Record 定义 ====================
//    public record EnrollmentRequest(
//            @NotBlank String courseId,
//            @NotBlank String studentId
//    ) {}
//
//    public record EnrollmentResponse(
//            String id,
//            String courseId,
//            String studentId,
//            String enrolledAt
//    ) {}
//}
//
