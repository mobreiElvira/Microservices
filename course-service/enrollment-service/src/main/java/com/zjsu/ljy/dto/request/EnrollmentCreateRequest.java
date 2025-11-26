package com.zjsu.ljy.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 选课创建请求DTO
 * 对应接口：POST /api/enrollments
 * 文档要求：包含courseId（课程ID）和studentId（学生学号）
 */
@Data
public class EnrollmentCreateRequest {

    /**
     * 课程ID（必填，需验证课程是否存在）
     */
    @NotBlank(message = "课程ID不能为空")
    private String courseId;

    /**
     * 学生学号（必填，需验证学生是否存在）
     */
    @NotBlank(message = "学生学号不能为空")
    private String studentId;
}
