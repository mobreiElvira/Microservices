package com.zjsu.ljy.course.controller;

import com.zjsu.ljy.course.dto.request.CourseCreateRequest;
import com.zjsu.ljy.course.dto.request.CourseUpdateRequest;
import com.zjsu.ljy.course.dto.response.ApiResponse;
import com.zjsu.ljy.course.model.Course;
import com.zjsu.ljy.course.model.Instructor;
import com.zjsu.ljy.course.model.ScheduleSlot;
import com.zjsu.ljy.course.service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")  // 符合文档URL要求
@RequiredArgsConstructor
public class CourseController {
    private final CourseService courseService;

    @PostMapping
    public ResponseEntity<ApiResponse<Course>> createCourse(@RequestBody @Valid CourseCreateRequest request) {
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
            @RequestBody @Valid CourseUpdateRequest request // 加@Valid触发DTO校验
    ) {
        // 直接传递 2 个参数：路径id + DTO对象，删除所有手动转换实体的代码
        Course updatedCourse = courseService.updateCourse(id, request);
        return ResponseEntity.ok(ApiResponse.success(updatedCourse));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCourse(@PathVariable String id) {
        courseService.deleteCourse(id);
        return new ResponseEntity<>(ApiResponse.success(), HttpStatus.NO_CONTENT);
    }
}
