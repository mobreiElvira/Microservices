
// ScheduleSlot.java（课程安排实体）
package com.zjsu.ljy.course.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleSlot {
    private String dayOfWeek;    // 周几（如“MONDAY”）
    private String startTime;    // 开始时间（如“08:00”）
    private String endTime;      // 结束时间（如“10:00”）
    private Integer expectedAttendance;  // 预期出勤人数（如50）
}
