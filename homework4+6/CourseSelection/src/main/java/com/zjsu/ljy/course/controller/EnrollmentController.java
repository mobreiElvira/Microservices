package com.zjsu.ljy.course.controller;

import com.zjsu.ljy.course.dto.request.EnrollmentCreateRequest;
import com.zjsu.ljy.course.dto.response.ApiResponse;
import com.zjsu.ljy.course.model.Enrollment;
import com.zjsu.ljy.course.service.EnrollmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
}
