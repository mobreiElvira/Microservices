package com.zjsu.ljy.course.controller;

import com.zjsu.ljy.course.dto.request.CourseCreateRequest;
import com.zjsu.ljy.course.dto.request.CourseUpdateRequest;
import com.zjsu.ljy.course.dto.response.ApiResponse;
import com.zjsu.ljy.course.model.Course;
import com.zjsu.ljy.course.model.Instructor;
import com.zjsu.ljy.course.model.ScheduleSlot;
import com.zjsu.ljy.course.service.CourseService;
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

    /**
     * 创建课程（POST /api/courses）
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Course>> createCourse(@RequestBody CourseCreateRequest request) {
        // 1. 将 CourseCreateRequest.InstructorRequest（DTO）转换为 Instructor（实体）
        Instructor instructor = new Instructor();
        BeanUtils.copyProperties(request.getInstructor(), instructor); // 拷贝同名属性（id/name/email）

        // 2. 将 CourseCreateRequest.ScheduleRequest（DTO）转换为 ScheduleSlot（实体）
        ScheduleSlot scheduleSlot = new ScheduleSlot();
        BeanUtils.copyProperties(request.getSchedule(), scheduleSlot); // 拷贝同名属性（dayOfWeek/startTime等）

        // 3. 调用 Service 方法（此时参数类型完全匹配）
        Course course = courseService.createCourse(
                request.getCode(),
                request.getTitle(),
                instructor, // 转换后的实体类
                scheduleSlot, // 转换后的实体类
                request.getCapacity()
        );
        return new ResponseEntity<>(ApiResponse.success(course), HttpStatus.CREATED);
    }

    /**
     * 查询所有课程（GET /api/courses）
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Course>>> getAllCourses() {
        List<Course> courses = courseService.getAllCourses();
        return ResponseEntity.ok(ApiResponse.success(courses));
    }

    /**
     * 根据id查询课程（GET /api/courses/{id}）
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Course>> getCourseById(@PathVariable String id) {
        Course course = courseService.getCourseById(id);
        return ResponseEntity.ok(ApiResponse.success(course));
    }

    /**
     * 更新课程（PUT /api/courses/{id}）
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Course>> updateCourse(
            @PathVariable String id,
            @RequestBody CourseUpdateRequest request
    ) {
        // 1. 转换教师信息（DTO -> 实体，若请求中未传则为 null）
        Instructor instructor = null;
        if (request.getInstructor() != null) {
            instructor = new Instructor();
            BeanUtils.copyProperties(request.getInstructor(), instructor);
        }

        // 2. 转换课程安排（DTO -> 实体，若请求中未传则为 null）
        ScheduleSlot scheduleSlot = null;
        if (request.getSchedule() != null) {
            scheduleSlot = new ScheduleSlot();
            BeanUtils.copyProperties(request.getSchedule(), scheduleSlot);
        }

        // 3. 调用 Service 方法（参数类型匹配）
        Course updatedCourse = courseService.updateCourse(
                id,
                request.getCode(),
                request.getTitle(),
                instructor, // 转换后的实体类（可能为 null，Service 需处理非空）
                scheduleSlot, // 转换后的实体类（可能为 null，Service 需处理非空）
                request.getCapacity()
        );
        return ResponseEntity.ok(ApiResponse.success(updatedCourse));
    }

    /**
     * 删除课程（DELETE /api/courses/{id}）
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCourse(@PathVariable String id) {
        courseService.deleteCourse(id);
        return new ResponseEntity<>(ApiResponse.success(), HttpStatus.NO_CONTENT);
    }
}
