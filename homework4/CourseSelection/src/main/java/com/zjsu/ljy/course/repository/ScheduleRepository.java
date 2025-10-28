package com.zjsu.ljy.course.repository;

import com.zjsu.ljy.course.model.ScheduleSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// 泛型说明：第一个是实体类 ScheduleSlot，第二个是主键类型 Long（对应数据库自增 id）
@Repository
public interface ScheduleRepository extends JpaRepository<ScheduleSlot, Long> {
}
