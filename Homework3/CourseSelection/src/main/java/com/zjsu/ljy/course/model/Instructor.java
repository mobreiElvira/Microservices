// Instructor.java（教师实体）
package com.zjsu.ljy.course.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Instructor {
    private String id;       // 教师ID（如“T001”）
    private String name;     // 教师姓名（如“张教授”）
    private String email;    // 教师邮箱（如“zhang@example.edu.cn”）
}
