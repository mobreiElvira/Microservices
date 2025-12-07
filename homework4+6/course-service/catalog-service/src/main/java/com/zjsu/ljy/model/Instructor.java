// Instructor.java（教师实体）
package com.zjsu.ljy.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "instructor")
public class Instructor {
    @Id
    @NotBlank(message = "教师ID不能为空")
    @Column(length = 36, nullable = false, unique = true, updatable = false)
    private String id;

    @NotBlank(message = "教师姓名不能为空")
    @Column(length = 50, nullable = false)
    private String name;

    @NotBlank(message = "教师邮箱不能为空")
    @Email(message = "邮箱格式不正确") // 邮箱格式校验
    @Column(length = 100, nullable = false, unique = true)
    private String email;

}
