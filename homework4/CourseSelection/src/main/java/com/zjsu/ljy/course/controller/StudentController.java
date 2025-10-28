package com.zjsu.ljy.course.controller;

import com.zjsu.ljy.course.dto.request.StudentCreateRequest;
import com.zjsu.ljy.course.dto.request.StudentUpdateRequest;
import com.zjsu.ljy.course.dto.response.ApiResponse;
import com.zjsu.ljy.course.model.Student;
import com.zjsu.ljy.course.service.StudentService;
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
}
