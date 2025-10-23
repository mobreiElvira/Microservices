package com.zjsu.ljy.course.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 学生创建请求DTO
 * 对应接口：POST /api/students
 * 文档要求：studentId全局唯一、email符合格式、所有字段（除id/createdAt）必填
 */
@Data
public class StudentCreateRequest {

    /**
     * 学号（必填，全局唯一）
     * 文档要求：studentId必须全局唯一，系统需验证是否已存在
     */
    @NotBlank(message = "学号不能为空")
    private String studentId;

    /**
     * 学生姓名（必填）
     * 文档要求：所有字段（除id/createdAt）均为必填
     */
    @NotBlank(message = "学生姓名不能为空")
    private String name;

    /**
     * 专业（必填）
     * 示例："计算机科学与技术"
     */
    @NotBlank(message = "专业不能为空")
    private String major;

    /**
     * 入学年份（必填，整数类型）
     * 示例：2024
     */
    @NotNull(message = "入学年份不能为空")
    private Integer grade;

    /**
     * 邮箱（必填，符合标准格式）
     * 文档要求：email必须包含@和域名，需验证格式
     */
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式无效（需包含@和域名）", regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")
    private String email;
}
