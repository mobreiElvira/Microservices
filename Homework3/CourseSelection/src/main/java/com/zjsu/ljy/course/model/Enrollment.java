package com.zjsu.ljy.course.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Enrollment {
    // 选课记录ID（系统自动生成）
    private String id;
    // 课程ID（关联Course）
    private String courseId;
    // 学生ID（关联Student）
    private String studentId;

    // 创建选课记录时自动生成id
    public Enrollment(String courseId, String studentId) {
        this.id = UUID.randomUUID().toString();
        this.courseId = courseId;
        this.studentId = studentId;
    }
}
