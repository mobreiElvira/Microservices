package com.zjsu.ljy.course.dto.request;

import jakarta.validation.constraints.Email;
import lombok.Data;

/**
 * 学生更新请求DTO
 * 对应接口：PUT /api/students/{id}
 * 文档要求：可更新studentId/name/major/grade/email，id和createdAt不可修改
 */
@Data
public class StudentUpdateRequest {

    /**
     * 新学号（可选，若修改需验证唯一性）
     * 文档要求：更新的studentId与其他学生重复时返回错误
     */
    private String studentId;

    /**
     * 新姓名（可选）
     */
    private String name;

    /**
     * 新专业（可选）
     */
    private String major;

    /**
     * 新入学年份（可选）
     */
    private Integer grade;

    /**
     * 新邮箱（可选，若修改需验证格式）
     * 文档要求：email格式不正确时返回错误
     */
    @Email(message = "新邮箱格式无效（需包含@和域名）", regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$", groups = UpdateEmailGroup.class)
    private String email;

    /**
     * 分组校验标记：仅当更新邮箱时触发格式校验
     * 避免未传邮箱时仍校验格式（解决“非必填字段校验”问题）
     */
    public interface UpdateEmailGroup {}
}
