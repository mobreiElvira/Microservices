package com.zjgsu.ljy.coursecloud.user.controller;


import com.zjgsu.ljy.coursecloud.user.Util.JwtUtil;
import com.zjgsu.ljy.coursecloud.user.dto.LoginRequest;
import com.zjgsu.ljy.coursecloud.user.dto.LoginResponse;
import com.zjgsu.ljy.coursecloud.user.model.Student;
import com.zjgsu.ljy.coursecloud.user.model.Teacher;
import com.zjgsu.ljy.coursecloud.user.model.User;
import com.zjgsu.ljy.coursecloud.user.model.UserType;

import com.zjgsu.ljy.coursecloud.user.service.UserService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * 认证控制器
 * 处理登录、分角色注册等认证相关请求
 * 网关会剥离 /api 前缀，实际访问路径为 /auth/xxx
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    // 改为注入容器中的PasswordEncoder Bean（推荐）
    @Autowired
    private PasswordEncoder passwordEncoder;
    /**
     * 统一登录接口（支持学生/教师）
     * @param request 登录请求（用户名+密码）
     * @return 包含JWT和用户信息的响应
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("用户登录请求: username={}", request.getUsername());

        // 1. 基础参数校验（兜底，配合@Valid双重校验）
        if (request.getUsername() == null || request.getUsername().isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "用户名不能为空"));
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "密码不能为空"));
        }

        // 2. 查询用户（学生/教师统一从User表查询）
        User user = userService.findByUsername(request.getUsername());
        if (user == null) {
            log.warn("登录失败：用户不存在，username={}", request.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(401, "用户名或密码错误"));
        }

        // 3. 密码验证（加密对比）
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("登录失败：密码错误，username={}", request.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(401, "用户名或密码错误"));
        }

        // 4. 生成JWT Token（包含用户ID、用户名、角色）
        String token = jwtUtil.generateToken(
                user.getId(),
                user.getUsername(),
                user.getUserType().name()
        );
        log.info("登录成功：username={}, userType={}", user.getUsername(), user.getUserType());

        // 5. 封装响应（LoginResponse已适配userType推导role）
        LoginResponse loginResponse = new LoginResponse(token, user);
        return ResponseEntity.ok(ApiResponse.success(200, "登录成功", loginResponse));
    }

    /**
     * 学生注册接口
     * @param request 学生注册信息（含密码）
     * @return 注册后的学生信息
     */
    @PostMapping("/register/student")
    public ResponseEntity<ApiResponse<StudentResponse>> registerStudent(
            @Valid @RequestBody StudentRequest request) { // 密码单独传参（也可合并到StudentRequest）
        log.info("学生注册请求: studentId={}, username={}", request.studentId(), request.username());

        // 1. 校验用户名是否已存在
        if (userService.findByUsername(request.username()) != null) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "用户名已存在"));
        }

        // 2. 构建Student对象（密码加密）
        Student student = new Student(
                request.username(),
                request.email(),
//                request.password(), // 密码加密存储
                passwordEncoder.encode(request.password()), // 加密密码
                UserType.STUDENT,
                request.studentId(),
                request.name(),
                request.major(),
                request.grade()
        );

        // 3. 保存学生
        Student createdStudent = userService.createStudent(student);
        log.info("学生注册成功：id={}, username={}", createdStudent.getId(), createdStudent.getUsername());

        // 4. 封装响应
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(StudentResponse.from(createdStudent)));
    }

    /**
     * 教师注册接口
     * @param request 教师注册信息（含密码）
     * @return 注册后的教师信息
     */
    @PostMapping("/register/teacher")
    public ResponseEntity<ApiResponse<TeacherResponse>> registerTeacher(
            @Valid @RequestBody TeacherRequest request) {
        log.info("教师注册请求: teacherId={}, username={}", request.teacherId(), request.username());

        // 1. 校验用户名是否已存在
        if (userService.findByUsername(request.username()) != null) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "用户名已存在"));
        }


        // 2. 构建Teacher对象（密码加密）
        Teacher teacher = new Teacher(
                request.username(),
                request.email(),
//                request.password(), // 密码加密存储
                passwordEncoder.encode(request.password()), // 加密密码
                UserType.TEACHER,
                request.teacherId(),
                request.name(),
                request.department(),
                request.title()
        );



        // 3. 保存教师
        Teacher createdTeacher = userService.createTeacher(teacher);
        log.info("教师注册成功：id={}, username={}", createdTeacher.getId(), createdTeacher.getUsername());

        // 4. 封装响应
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(TeacherResponse.from(createdTeacher)));
    }

    /**
     * Token验证接口（网关鉴权后调用）
     * @param userId 用户ID
     * @param username 用户名
     * @param role 角色（UserType）
     * @return 验证结果
     */
    @GetMapping("/verify")
    public ResponseEntity<ApiResponse<String>> verifyToken(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Username") String username,
            @RequestHeader("X-User-Role") String role) {

        log.info("Token验证请求：userId={}, username={}, role={}", userId, username, role);
        String message = String.format("Token有效！用户ID: %s, 用户名: %s, 角色: %s", userId, username, role);
        return ResponseEntity.ok(ApiResponse.success(200, "验证成功", message));
    }

    /**
     * 通用响应封装类（建议抽成公共DTO）
     * 这里临时内置，实际项目建议放到dto包下
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApiResponse<T> {
        private int code;
        private String message;
        private T data;

        // 成功响应
        public static <T> ApiResponse<T> success(int code, String message, T data) {
            return new ApiResponse<>(code, message, data);
        }

        // 错误响应
        public static <T> ApiResponse<T> error(int code, String message) {
            return new ApiResponse<>(code, message, null);
        }

        // 创建成功响应
        public static <T> ApiResponse<T> created(T data) {
            return new ApiResponse<>(201, "创建成功", data);
        }
    }
}
