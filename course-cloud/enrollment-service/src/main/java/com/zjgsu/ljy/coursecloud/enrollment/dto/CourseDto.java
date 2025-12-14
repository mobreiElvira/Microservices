package com.zjgsu.ljy.coursecloud.enrollment.dto;

import lombok.Data;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * 课程数据传输DTO：
 * 1. 包含 Course 实体核心业务字段（code/title/capacity等）
 * 2. 新增传输必需的额外字段（port/hostname/status等，实体中无）
 */
@Data // Lombok 注解，自动生成 getter/setter/toString，简化代码（参考摘要5）
public class CourseDto {
    // 一、实体核心字段（与 Course 实体对应，用于承载课程业务数据）
    @NotBlank(message = "课程ID不能为空")
    private String id; // 对应实体的 UUID 主键（原控制层入参为 String 类型，需匹配）

    @NotBlank(message = "课程编号不能为空")
    private String code; // 课程编号（实体字段）

    @NotBlank(message = "课程名称不能为空")
    private String title; // 课程名称（实体字段）

    @NotNull(message = "课程容量不能为空")
    @Min(value = 1, message = "容量至少为1")
    private Integer capacity; // 总容量（实体字段，选课校验必需）

    @NotNull(message = "已选人数不能为空")
    private Integer enrolled; // 已选人数（实体字段，计算剩余容量）

    private LocalDateTime createdAt; // 创建时间（实体字段，可选）

    // 二、额外新增字段（实体中无，传输/调试必需）
    private String port; // 实例端口（原控制层的 currentPort，用于验证负载均衡）

    private String hostname; // 实例主机名（原控制层的 getHostname()，用于定位实例）

    private String status; // 响应状态（SUCCESS/ERROR，原控制层的 status 字段）

    private String message; // 错误信息（原控制层的 message 字段，降级/未找到课程时使用）

    // 三、可选：前端友好字段（按需新增，实体中无）
    private Integer remainingCapacity; // 剩余容量（= capacity - enrolled，避免前端二次计算）


    public @NotBlank(message = "课程ID不能为空") String getId() {
        return id;
    }

    public void setId(@NotBlank(message = "课程ID不能为空") String id) {
        this.id = id;
    }

    public @NotBlank(message = "课程编号不能为空") String getCode() {
        return code;
    }

    public void setCode(@NotBlank(message = "课程编号不能为空") String code) {
        this.code = code;
    }

    public @NotBlank(message = "课程名称不能为空") String getTitle() {
        return title;
    }

    public void setTitle(@NotBlank(message = "课程名称不能为空") String title) {
        this.title = title;
    }

    public @NotNull(message = "课程容量不能为空") @Min(value = 1, message = "容量至少为1") Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(@NotNull(message = "课程容量不能为空") @Min(value = 1, message = "容量至少为1") Integer capacity) {
        this.capacity = capacity;
    }

    public @NotNull(message = "已选人数不能为空") Integer getEnrolled() {
        return enrolled;
    }

    public void setEnrolled(@NotNull(message = "已选人数不能为空") Integer enrolled) {
        this.enrolled = enrolled;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getRemainingCapacity() {
        return remainingCapacity;
    }

    public void setRemainingCapacity(Integer remainingCapacity) {
        this.remainingCapacity = remainingCapacity;
    }
}
