package com.zjsu.ljy.course.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * 课程更新请求DTO
 * 对应接口：PUT /api/courses/{id}
 * 文档要求：支持更新课程所有字段（非必填，仅更新传入字段）
 */
@Data
public class CourseUpdateRequest {

    /**
     * 新课程编码（可选）
     */
    private String code;

    /**
     * 新课程名称（可选）
     */
    private String title;

    /**
     * 新教师信息（可选，嵌套对象）
     * 若传入则校验子字段格式
     */
    @Valid
    private InstructorUpdateRequest instructor;

    /**
     * 新课程安排（可选，嵌套对象）
     * 若传入则校验子字段格式
     */
    @Valid
    private ScheduleUpdateRequest schedule;

    /**
     * 新课程容量（可选，正整数）
     */
    @Positive(message = "课程容量必须大于0", groups = UpdateCapacityGroup.class)
    private Integer capacity;

    /**
     * 分组校验标记：仅当更新容量时触发正整数校验
     */
    public interface UpdateCapacityGroup {}

    /**
     * 嵌套DTO：教师信息更新（非必填字段）
     */
    @Data
    public static class InstructorUpdateRequest {
        private String id;  // 可选更新

        private String name;  // 可选更新

        @Email(message = "教师邮箱格式无效", regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$", groups = UpdateInstructorEmailGroup.class)
        private String email;  // 可选更新，传值时校验格式

        public interface UpdateInstructorEmailGroup {}
    }

    /**
     * 嵌套DTO：课程安排更新（非必填字段）
     */
    @Data
    public static class ScheduleUpdateRequest {
        private String dayOfWeek;  // 可选更新（示例：MONDAY）

        private String startTime;  // 可选更新（示例：08:00）

        private String endTime;  // 可选更新（示例：10:00）

        @Positive(message = "预期出勤人数必须大于0", groups = UpdateScheduleAttendanceGroup.class)
        private Integer expectedAttendance;  // 可选更新，传值时校验正整数

        public interface UpdateScheduleAttendanceGroup {}
    }
}
