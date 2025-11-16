package com.zjsu.ljy.course.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "enrollment",
        uniqueConstraints = @UniqueConstraint(columnNames = {"courseId", "studentId"})
)
public class Enrollment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36, nullable = false, unique = true)
    private String id;

    @NotNull(message = "课程不能为空")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "courseId")
    private Course course;

    @NotNull(message = "学生不能为空")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "studentId")
    private Student student;

    @Column(nullable = false, name = "enrolledAt", updatable = false)
    @CreationTimestamp
    private LocalDateTime enrolledAt;

    public Enrollment(Course course, Student student) {
        this.course = course;
        this.student = student;
    }


}
