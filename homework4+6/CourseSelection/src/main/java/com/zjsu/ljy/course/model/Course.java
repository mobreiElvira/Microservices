package com.zjsu.ljy.course.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "course")
public class Course {
    @Id
    private String id;
    private String code;
    private String title;

    // 立即加载教师信息（关键修改）
    @ManyToOne(fetch = FetchType.EAGER) // 替换 LAZY 为 EAGER
    @JoinColumn(name = "instructor_id")
    private Instructor instructor;

    // 立即加载课程安排（关键修改）
    @ManyToOne(fetch = FetchType.EAGER) // 替换 LAZY 为 EAGER
    @JoinColumn(name = "schedule_id")
    private ScheduleSlot schedule;

    private Integer capacity;
    private Integer enrolled;

    // getter/setter 略
}
