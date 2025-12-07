
// ScheduleSlot.java（课程安排实体）
package com.zjsu.ljy.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "schedule_slot")
public class ScheduleSlot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 自增主键（对应 SQL 的 AUTO_INCREMENT）
    private Long id;

    @NotBlank(message = "星期不能为空")
    @Column(length = 20, nullable = false)
    private String dayOfWeek;

    @NotBlank(message = "开始时间不能为空")
    @Column(length = 10, nullable = false)
    private String startTime;

    @NotBlank(message = "结束时间不能为空")
    @Column(length = 10, nullable = false)
    private String endTime;

    @Column(nullable = false)
    private Integer expectedAttendance;
}
