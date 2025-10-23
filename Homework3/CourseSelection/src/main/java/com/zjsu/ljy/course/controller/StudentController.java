package com.zjsu.ljy.course.controller;

import com.zjsu.ljy.course.dto.request.StudentCreateRequest;
import com.zjsu.ljy.course.dto.request.StudentUpdateRequest;
import com.zjsu.ljy.course.dto.response.ApiResponse;
import com.zjsu.ljy.course.model.Student;
import com.zjsu.ljy.course.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")  // 符合文档URL要求
@RequiredArgsConstructor
public class StudentController {
    private final StudentService studentService;

    /**
     * 创建学生（POST /api/students）
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Student>> createStudent(@RequestBody StudentCreateRequest request) {
        Student student = studentService.createStudent(
                request.getStudentId(),
                request.getName(),
                request.getMajor(),
                request.getGrade(),
                request.getEmail()
        );
        // 创建成功返回201 Created
        return new ResponseEntity<>(ApiResponse.success(student), HttpStatus.CREATED);
    }

    /**
     * 查询所有学生（GET /api/students）
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Student>>> getAllStudents() {
        List<Student> students = studentService.getAllStudents();
        return ResponseEntity.ok(ApiResponse.success(students));
    }

    /**
     * 根据id查询学生（GET /api/students/{id}）
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Student>> getStudentById(@PathVariable String id) {
        Student student = studentService.getStudentById(id);
        return ResponseEntity.ok(ApiResponse.success(student));
    }

    /**
     * 更新学生信息（PUT /api/students/{id}）
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Student>> updateStudent(
            @PathVariable String id,
            @RequestBody StudentUpdateRequest request
    ) {
        Student updatedStudent = studentService.updateStudent(
                id,
                request.getStudentId(),
                request.getName(),
                request.getMajor(),
                request.getGrade(),
                request.getEmail()
        );
        return ResponseEntity.ok(ApiResponse.success(updatedStudent));
    }

    /**
     * 删除学生（DELETE /api/students/{id}）
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteStudent(@PathVariable String id) {
        studentService.deleteStudent(id);
        // 删除成功返回204 No Content
        return new ResponseEntity<>(ApiResponse.success(), HttpStatus.NO_CONTENT);
    }
}
