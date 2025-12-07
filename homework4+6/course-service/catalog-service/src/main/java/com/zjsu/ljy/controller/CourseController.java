package com.zjsu.ljy.controller;

import com.zjsu.ljy.dto.request.CourseCreateRequest;
import com.zjsu.ljy.dto.request.CourseUpdateRequest;
import com.zjsu.ljy.dto.response.ApiResponse;
import com.zjsu.ljy.service.CourseService;
import com.zjsu.ljy.model.Course;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {
    private final CourseService courseService;

    @PostMapping
    public ResponseEntity<ApiResponse<Course>> createCourse(@Valid @RequestBody CourseCreateRequest request) {
        Course course = courseService.createCourse(request);
        return new ResponseEntity<>(ApiResponse.success(course), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Course>>> getAllCourses() {
        List<Course> courses = courseService.getAllCourses();
        return ResponseEntity.ok(ApiResponse.success(courses));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Course>> getCourseById(@PathVariable String id) {
        Course course = courseService.getCourseById(id);
        return ResponseEntity.ok(ApiResponse.success(course));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Course>> updateCourse(
            @PathVariable String id,
            @Valid @RequestBody CourseUpdateRequest request) {
        Course course = courseService.updateCourse(id, request);
        return ResponseEntity.ok(ApiResponse.success(course));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCourse(@PathVariable String id) {
        courseService.deleteCourse(id);
        return ResponseEntity.ok(ApiResponse.success());
    }

    // 按课程代码查询
    @GetMapping("/code/{code}")
    public ResponseEntity<ApiResponse<Course>> getCourseByCode(@PathVariable String code) {
        Course course = courseService.getCourseByCode(code);
        return ResponseEntity.ok(ApiResponse.success(course));
    }

    // 按讲师ID查询
    @GetMapping("/instructor/{instructorId}")
    public ResponseEntity<ApiResponse<List<Course>>> getCoursesByInstructor(
            @PathVariable String instructorId,
            @RequestParam(required = false, defaultValue = "false") boolean hasRemainingCapacity
    ) {
        List<Course> courses = hasRemainingCapacity
                ? courseService.getCoursesByInstructorWithRemainingCapacity(instructorId)
                : courseService.getCoursesByInstructor(instructorId);
        return ResponseEntity.ok(ApiResponse.success(courses));
    }

    // 筛选有剩余容量的课程
    @GetMapping("/remaining-capacity")
    public ResponseEntity<ApiResponse<List<Course>>> getCoursesWithRemainingCapacity() {
        List<Course> courses = courseService.getCoursesWithRemainingCapacity();
        return ResponseEntity.ok(ApiResponse.success(courses));
    }

    // 标题关键字模糊查询
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<Course>>> searchCoursesByTitle(
            @RequestParam String keyword,
            @RequestParam(required = false, defaultValue = "false") boolean hasRemainingCapacity
    ) {
        List<Course> courses = hasRemainingCapacity
                ? courseService.searchCoursesByTitleWithRemainingCapacity(keyword)
                : courseService.searchCoursesByTitle(keyword);
        return ResponseEntity.ok(ApiResponse.success(courses));
    }

    @GetMapping("/stats/remaining-capacity")
    public ResponseEntity<ApiResponse<Long>> countCoursesWithRemainingCapacity() {
        long count = courseService.countCoursesWithRemainingCapacity();
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    @GetMapping("/stats/instructor/{instructorId}/remaining-capacity")
    public ResponseEntity<ApiResponse<Long>> countCoursesByInstructorWithRemainingCapacity(@PathVariable String instructorId) {
        long count = courseService.countCoursesByInstructorWithRemainingCapacity(instructorId);
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    @PostMapping("/{courseId}/enroll")
    public ResponseEntity<ApiResponse<Void>> enrollCourse(@PathVariable String courseId) {
        courseService.incrementEnrolledCount(courseId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @PostMapping("/{courseId}/drop")
    public ResponseEntity<ApiResponse<Void>> dropCourse(@PathVariable String courseId) {
        courseService.decrementEnrolledCount(courseId);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
