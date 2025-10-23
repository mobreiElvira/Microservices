package com.zjsu.ljy.course.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Student {
    // 系统自动生成UUID，唯一标识符
    private String id;
    // 学号（全局唯一，如“S2024001”）
    private String studentId;
    // 学生姓名（必填）
    private String name;
    // 专业（必填，如“计算机科学与技术”）
    private String major;
    // 入学年份（必填，如2024）
    private Integer grade;
    // 邮箱（必填，需符合标准格式）
    private String email;
    // 系统自动生成创建时间戳
    private LocalDateTime createdAt;

    // 创建学生时自动生成id和createdAt
    public Student(String studentId, String name, String major, Integer grade, String email) {
        this.id = UUID.randomUUID().toString();
        this.studentId = studentId;
        this.name = name;
        this.major = major;
        this.grade = grade;
        this.email = email;
        this.createdAt = LocalDateTime.now();
    }
}
