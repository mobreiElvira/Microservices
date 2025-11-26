package com.zjsu.ljy.controller;

import com.zjsu.ljy.dto.request.EnrollmentCreateRequest;
import com.zjsu.ljy.dto.response.ApiResponse;
import com.zjsu.ljy.exception.ResourceNotFoundException;
import com.zjsu.ljy.service.EnrollmentService;
import com.zjsu.ljy.model.Enrollment;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/enrollments")  // 符合文档URL要求
@RequiredArgsConstructor
public class EnrollmentController {
    private final EnrollmentService enrollmentService;

    /**
     * 学生选课（POST /api/enrollments）
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Enrollment>> enrollCourse(@RequestBody @Valid EnrollmentCreateRequest request) {
        // 直接将 request 传给 Service，无需拆分为两个参数
        Enrollment enrollment = enrollmentService.enrollCourse(request);
        return new ResponseEntity<>(ApiResponse.success(enrollment), HttpStatus.CREATED);
    }

    /**
     * 学生退课（DELETE /api/enrollments/{id}）
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> dropCourse(@PathVariable String id) {
        enrollmentService.dropCourse(id);
        return new ResponseEntity<>(ApiResponse.success(), HttpStatus.NO_CONTENT);
    }

    /**
     * 查询所有选课记录（GET /api/enrollments）
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Enrollment>>> getAllEnrollments() {
        List<Enrollment> enrollments = enrollmentService.getAllEnrollments();
        return ResponseEntity.ok(ApiResponse.success(enrollments));
    }

    /**
     * 按课程查询选课记录（GET /api/enrollments/course/{courseId}）
     */
    @GetMapping("/course/{courseId}")
    public ResponseEntity<ApiResponse<List<Enrollment>>> getEnrollmentsByCourseId(@PathVariable String courseId) {
        List<Enrollment> enrollments = enrollmentService.getEnrollmentsByCourseId(courseId);
        return ResponseEntity.ok(ApiResponse.success(enrollments));
    }

    /**
     * 按学生查询选课记录（GET /api/enrollments/student/{studentId}）
     */
    @GetMapping("/student/{studentId}")
    public ResponseEntity<ApiResponse<List<Enrollment>>> getEnrollmentsByStudentId(@PathVariable String studentId) {
        List<Enrollment> enrollments = enrollmentService.getEnrollmentsByStudentId(studentId);
        return ResponseEntity.ok(ApiResponse.success(enrollments));
    }

    /**
     * 查询单个选课记录详情（GET /api/enrollments/{id}）
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Enrollment>> getEnrollmentById(@PathVariable String id) {
        // 修改：使用getAllEnrollments()过滤出指定ID的记录
        Enrollment enrollment = enrollmentService.getAllEnrollments()
                .stream()
                .filter(e -> e.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("选课记录不存在，id：" + id));
        return ResponseEntity.ok(ApiResponse.success(enrollment));
    }

    /**
     * 统计特定课程的选课人数（GET /api/enrollments/count/course/{courseId}）
     */
    @GetMapping("/count/course/{courseId}")
    public ResponseEntity<ApiResponse<Long>> countEnrollmentsByCourseId(@PathVariable String courseId) {
        // 修改：使用getEnrollmentsByCourseId()获取数量
        long count = enrollmentService.getEnrollmentsByCourseId(courseId).size();
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    /**
     * 判断学生是否已选特定课程（GET /api/enrollments/check/course/{courseId}/student/{studentId}）
     */
    @GetMapping("/check/course/{courseId}/student/{studentId}")
    public ResponseEntity<ApiResponse<Boolean>> checkStudentEnrollment(
            @PathVariable String courseId,
            @PathVariable String studentId) {
        // 修改：使用现有的批量检查方法
        Map<String, Boolean> result = enrollmentService.checkStudentsEnrollmentStatus(courseId, List.of(studentId));
        boolean isEnrolled = result.getOrDefault(studentId, false);
        return ResponseEntity.ok(ApiResponse.success(isEnrolled));
    }


    /**
     * 批量查询多个学生的选课状态（POST /api/enrollments/check/batch/course/{courseId}）
     */
    @PostMapping("/check/batch/course/{courseId}")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> checkBatchStudentEnrollments(
            @PathVariable String courseId,
            @RequestBody List<String> studentIds) {
        Map<String, Boolean> result = enrollmentService.checkStudentsEnrollmentStatus(courseId, studentIds);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 查询特定课程的活跃人数（基于选课时间）（GET /api/enrollments/active/count/course/{courseId}）
     */
    @GetMapping("/active/count/course/{courseId}")
    public ResponseEntity<ApiResponse<Long>> countActiveEnrollmentsByCourseId(
            @PathVariable String courseId,
            @RequestParam(required = false, defaultValue = "30") int days) {
        long count = enrollmentService.countActiveStudentsByCourseId(courseId, days);
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    /**
     * 查询特定课程的活跃选课记录（基于选课时间）（GET /api/enrollments/active/course/{courseId}）
     */
    @GetMapping("/active/course/{courseId}")
    public ResponseEntity<ApiResponse<List<Enrollment>>> getActiveEnrollmentsByCourseId(
            @PathVariable String courseId,
            @RequestParam(required = false, defaultValue = "30") int days) {
        // 使用现有方法实现，通过计算日期范围
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        List<Enrollment> enrollments = enrollmentService.getEnrollmentsByCourseId(courseId)
                .stream()
                .filter(e -> e.getEnrolledAt().isAfter(since))
                .toList();
        return ResponseEntity.ok(ApiResponse.success(enrollments));
    }

    /**
     * 按课程和学生ID列表查询选课记录（POST /api/enrollments/course/{courseId}/students）
     */
    @PostMapping("/course/{courseId}/students")
    public ResponseEntity<ApiResponse<List<Enrollment>>> getEnrollmentsByCourseIdAndStudentIds(
            @PathVariable String courseId,
            @RequestBody List<String> studentIds) {
        List<Enrollment> enrollments = enrollmentService.getEnrollmentsByCourseId(courseId)
                .stream()
                .filter(e -> studentIds.contains(e.getStudent().getId()))
                .toList();
        return ResponseEntity.ok(ApiResponse.success(enrollments));
    }
}
