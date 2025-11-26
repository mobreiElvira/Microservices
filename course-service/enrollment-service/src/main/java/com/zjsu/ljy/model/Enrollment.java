package com.zjsu.ljy.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

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
    @Column(name = "courseId")
    private String  courseId;

    @NotNull(message = "学生不能为空")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "studentId")
    private Student student;

    @Column(nullable = false, name = "enrolledAt", updatable = false)
    @CreationTimestamp
    private LocalDateTime enrolledAt;

    public Enrollment(String courseId, Student student) {
        this.courseId = courseId;
        this.student = student;
    }


}
