package com.zjsu.ljy.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "student")
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID) // JPA 自动生成 UUID 主键
    @Column(length = 36, nullable = false, unique = true)
    private String id;

    @NotBlank(message = "学号不能为空")
    @Column(length = 20, nullable = false, unique = true, name = "studentId")
    private String studentId;

    @NotBlank(message = "学生姓名不能为空")
    @Column(length = 50, nullable = false)
    private String name;

    @NotBlank(message = "专业不能为空")
    @Column(length = 50, nullable = false)
    private String major;

    @Column(nullable = false)
    private Integer grade;

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Column(length = 100, nullable = false, unique = true)
    private String email;
    @Column(nullable = false, name = "createdAt", updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

}
