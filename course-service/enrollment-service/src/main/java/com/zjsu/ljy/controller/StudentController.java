package com.zjsu.ljy.controller;

import com.zjsu.ljy.dto.request.StudentCreateRequest;
import com.zjsu.ljy.dto.request.StudentUpdateRequest;
import com.zjsu.ljy.dto.response.ApiResponse;
import com.zjsu.ljy.model.Student;
import com.zjsu.ljy.service.StudentService;
import jakarta.validation.Valid;
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

    @PostMapping
    public ResponseEntity<ApiResponse<Student>> createStudent(@RequestBody @Valid StudentCreateRequest request) {
        Student student = studentService.createStudent(request);
        return new ResponseEntity<>(ApiResponse.success(student), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Student>>> getAllStudents() {
        List<Student> students = studentService.getAllStudents();
        return ResponseEntity.ok(ApiResponse.success(students));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Student>> getStudentById(@PathVariable String id) {
        Student student = studentService.getStudentById(id);
        return ResponseEntity.ok(ApiResponse.success(student));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Student>> updateStudent(
            @PathVariable String id,
            @RequestBody @Valid StudentUpdateRequest request
    ) {
        Student updatedStudent = studentService.updateStudent(id, request);
        return ResponseEntity.ok(ApiResponse.success(updatedStudent));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteStudent(@PathVariable String id) {
        studentService.deleteStudent(id);

        return new ResponseEntity<>(ApiResponse.success(), HttpStatus.NO_CONTENT);
    }
    // 添加按邮箱查询的API
    @GetMapping("/email/{email}")
    public ResponseEntity<ApiResponse<Student>> getStudentByEmail(@PathVariable String email) {
        Student student = studentService.getStudentByEmail(email);
        return ResponseEntity.ok(ApiResponse.success(student));
    }

    // 添加按专业筛选的API
    @GetMapping("/major/{major}")
    public ResponseEntity<ApiResponse<List<Student>>> getStudentsByMajor(@PathVariable String major) {
        List<Student> students = studentService.getStudentsByMajor(major);
        return ResponseEntity.ok(ApiResponse.success(students));
    }

    // 添加按年级筛选的API
    @GetMapping("/grade/{grade}")
    public ResponseEntity<ApiResponse<List<Student>>> getStudentsByGrade(@PathVariable Integer grade) {
        List<Student> students = studentService.getStudentsByGrade(grade);
        return ResponseEntity.ok(ApiResponse.success(students));
    }
}
