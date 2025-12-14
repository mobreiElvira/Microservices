package com.zjgsu.ljy.coursecloud.enrollment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

// StudentDto.java（通用核心DTO）
@Data
public class StudentDto {
    // 1. 服务间标识字段（Enrollment调用User时验证唯一性）
    private String id; // 对应Student实体的主键（User Service控制层入参是String类型id，需匹配）
    // 2. 业务核心字段（选课必需+前端展示）
    @NotBlank(message = "学号不能为空")
    private String studentId; // 学号
    @NotBlank(message = "姓名不能为空")
    private String name; // 姓名
    @NotBlank(message = "专业不能为空")
    private String major; // 专业
    @NotNull(message = "年级不能为空")
    private Integer grade; // 年级
    // 3. 控制层返回的实例标识（可选，Enrollment调用时可获取实例端口/主机名，便于排查负载均衡）
    private String port; // 对应User Service控制层的currentPort
    private String hostname; // 对应User Service控制层的hostname

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public @NotBlank(message = "学号不能为空") String getStudentId() {
        return studentId;
    }

    public void setStudentId(@NotBlank(message = "学号不能为空") String studentId) {
        this.studentId = studentId;
    }

    public @NotBlank(message = "姓名不能为空") String getName() {
        return name;
    }

    public void setName(@NotBlank(message = "姓名不能为空") String name) {
        this.name = name;
    }

    public @NotBlank(message = "专业不能为空") String getMajor() {
        return major;
    }

    public void setMajor(@NotBlank(message = "专业不能为空") String major) {
        this.major = major;
    }

    public @NotNull(message = "年级不能为空") Integer getGrade() {
        return grade;
    }

    public void setGrade(@NotNull(message = "年级不能为空") Integer grade) {
        this.grade = grade;
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
}
