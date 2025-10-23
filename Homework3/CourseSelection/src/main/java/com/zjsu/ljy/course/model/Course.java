package com.zjsu.ljy.course.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Course {
    // 系统自动生成UUID
    private String id;
    // 课程编码（如“CS101”）
    private String code;
    // 课程名称（如“计算机科学导论”）
    private String title;
    // 授课教师信息
    private Instructor instructor;
    // 课程安排（周几、起止时间等）
    private ScheduleSlot schedule;
    // 课程容量（最大选课人数）
    private Integer capacity;
    // 当前选课人数（自动更新）
    private Integer enrolled = 0;

    // 创建课程时自动生成id
    public Course(String code, String title, Instructor instructor, ScheduleSlot schedule, Integer capacity) {
        this.id = UUID.randomUUID().toString();
        this.code = code;
        this.title = title;
        this.instructor = instructor;
        this.schedule = schedule;
        this.capacity = capacity;
    }
}
