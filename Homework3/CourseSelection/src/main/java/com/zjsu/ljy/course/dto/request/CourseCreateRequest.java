package com.zjsu.ljy.course.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * 课程创建请求DTO
 * 对应接口：POST /api/courses
 * 文档示例：包含code、title、instructor（教师信息）、schedule（课程安排）、capacity（容量）
 */
@Data
public class CourseCreateRequest {

    /**
     * 课程编码（必填）
     * 示例："CS101"
     */
    @NotBlank(message = "课程编码不能为空")
    private String code;

    /**
     * 课程名称（必填）
     * 示例："计算机科学导论"
     */
    @NotBlank(message = "课程名称不能为空")
    private String title;

    /**
     * 授课教师信息（必填，嵌套对象）
     * 包含教师id、name、email，需校验子字段非空
     */
    @NotNull(message = "教师信息不能为空")
    @Valid  // 触发嵌套对象的字段校验
    private InstructorRequest instructor;

    /**
     * 课程安排（必填，嵌套对象）
     * 包含周几、起止时间、预期出勤人数，需校验子字段非空
     */
    @NotNull(message = "课程安排不能为空")
    @Valid  // 触发嵌套对象的字段校验
    private ScheduleRequest schedule;

    /**
     * 课程容量（必填，正整数）
     * 文档要求：课程选课人数不能超过容量（capacity）
     */
    @NotNull(message = "课程容量不能为空")
    @Positive(message = "课程容量必须大于0")
    private Integer capacity;

    /**
     * 嵌套DTO：教师信息
     * 与文档中instructor结构一致（id、name、email）
     */
    @Data
    public static class InstructorRequest {
        @NotBlank(message = "教师ID不能为空")
        private String id;  // 示例："T001"

        @NotBlank(message = "教师姓名不能为空")
        private String name;  // 示例："张教授"

        @NotBlank(message = "教师邮箱不能为空")
        @Email(message = "教师邮箱格式无效", regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")
        private String email;  // 示例："zhang@example.edu.cn"
    }

    /**
     * 嵌套DTO：课程安排
     * 与文档中schedule结构一致（dayOfWeek、startTime、endTime、expectedAttendance）
     */
    @Data
    public static class ScheduleRequest {
        @NotBlank(message = "周几不能为空（示例：MONDAY）")
        private String dayOfWeek;  // 示例："MONDAY"（周一）

        @NotBlank(message = "开始时间不能为空（示例：08:00）")
        private String startTime;  // 示例："08:00"

        @NotBlank(message = "结束时间不能为空（示例：10:00）")
        private String endTime;  // 示例："10:00"

        @NotNull(message = "预期出勤人数不能为空")
        @Positive(message = "预期出勤人数必须大于0")
        private Integer expectedAttendance;  // 示例：50
    }
}
