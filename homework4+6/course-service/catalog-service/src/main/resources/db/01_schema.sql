CREATE DATABASE IF NOT EXISTS catalog_db;
USE catalog_db;
-- 1. 教师表（instructor）
CREATE TABLE instructor (
    id VARCHAR(36) PRIMARY KEY COMMENT '教师ID（如“T001”）',
    name VARCHAR(50) NOT NULL COMMENT '教师姓名（如“张教授”）',
    email VARCHAR(100) NOT NULL UNIQUE COMMENT '教师邮箱（如“zhang@example.edu.cn”）',
    CONSTRAINT uk_instructor_email UNIQUE (email) COMMENT '确保教师邮箱唯一'
) COMMENT '教师信息表';
-- 2. 课程安排表（schedule_slot）：移除 CHECK 约束（MySQL 兼容性问题）
CREATE TABLE schedule_slot (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '课程安排唯一ID（自增）',
    dayOfWeek VARCHAR(20) NOT NULL COMMENT '周几（如“MONDAY”）',
    startTime VARCHAR(10) NOT NULL COMMENT '开始时间（如“08:00”）',
    endTime VARCHAR(10) NOT NULL COMMENT '结束时间（如“10:00”）',
    expectedAttendance INT NOT NULL COMMENT '预期出勤人数（如50）'
) COMMENT '课程安排表'; -- 移除原 CHECK 约束，避免语法错误
-- 3. 课程表（course）：移除 CHECK 约束，业务逻辑改由代码控制
CREATE TABLE course (
    id VARCHAR(36) PRIMARY KEY COMMENT '课程ID（系统自动生成UUID）',
    code VARCHAR(20) NOT NULL UNIQUE COMMENT '课程编码（如“CS101”）',
    title VARCHAR(100) NOT NULL COMMENT '课程名称（如“计算机科学导论”）',
    instructor_id VARCHAR(36) NOT NULL COMMENT '授课教师ID（关联instructor表）',
    schedule_id BIGINT NOT NULL COMMENT '课程安排ID（关联schedule_slot表）',
    capacity INT NOT NULL COMMENT '课程容量（最大选课人数）',
    enrolled INT NOT NULL DEFAULT 0 COMMENT '当前选课人数（自动更新）',
    -- 外键关联（保留，确保数据完整性）
    CONSTRAINT fk_course_instructor FOREIGN KEY (instructor_id) REFERENCES instructor(id),
    CONSTRAINT fk_course_schedule FOREIGN KEY (schedule_id) REFERENCES schedule_slot(id)
) COMMENT '课程信息表';

USE catalog_db;
INSERT INTO `instructor` (`id`, `name`, `email`) VALUES ('T001', '张教授', 'zhang_prof@zjsu.edu.cn');
INSERT INTO `instructor` (`id`, `name`, `email`) VALUES ('T002', '李老师', 'li_teacher@zjsu.edu.cn');
INSERT INTO `instructor` (`id`, `name`, `email`) VALUES ('T003', '王讲师', 'wang_lecturer@zjsu.edu.cn');
INSERT INTO `instructor` (`id`, `name`, `email`) VALUES ('T004', '赵工程师', 'zhao@example.edu.cn');

USE catalog_db;
INSERT INTO `schedule_slot` (`id`, `dayOfWeek`, `startTime`, `endTime`, `expectedAttendance`) VALUES (1, 'MONDAY', '08:30', '10:00', 50);
INSERT INTO `schedule_slot` (`id`, `dayOfWeek`, `startTime`, `endTime`, `expectedAttendance`) VALUES (2, 'TUESDAY', '14:00', '15:30', 40);
INSERT INTO `schedule_slot` (`id`, `dayOfWeek`, `startTime`, `endTime`, `expectedAttendance`) VALUES (3, 'WEDNESDAY', '10:20', '11:50', 45);
INSERT INTO `schedule_slot` (`id`, `dayOfWeek`, `startTime`, `endTime`, `expectedAttendance`) VALUES (4, 'THURSDAY', '16:00', '17:30', 35);
INSERT INTO `schedule_slot` (`id`, `dayOfWeek`, `startTime`, `endTime`, `expectedAttendance`) VALUES (5, 'FRIDAY', '09:00', '10:30', 50);
INSERT INTO `schedule_slot` (`id`, `dayOfWeek`, `startTime`, `endTime`, `expectedAttendance`) VALUES (7, 'FRIDAY', '09:00', '11:30', 30);
INSERT INTO `schedule_slot` (`id`, `dayOfWeek`, `startTime`, `endTime`, `expectedAttendance`) VALUES (8, 'WEDNESDAY', '14:00', '16:00', 80);

USE catalog_db;
INSERT INTO `course` (`id`, `code`, `title`, `instructor_id`, `schedule_id`, `capacity`, `enrolled`) VALUES ('821fc70e-b3d3-11f0-beb5-00ff5c9af41d', 'CS255', '数据结构与算法分析', 'T002', 8, 2, 1);
INSERT INTO `course` (`id`, `code`, `title`, `instructor_id`, `schedule_id`, `capacity`, `enrolled`) VALUES ('821fd389-b3d3-11f0-beb5-00ff5c9af41d', 'CS202', '数据库原理', 'T002', 3, 45, 1);
INSERT INTO `course` (`id`, `code`, `title`, `instructor_id`, `schedule_id`, `capacity`, `enrolled`) VALUES ('821fd701-b3d3-11f0-beb5-00ff5c9af41d', 'CS301', 'Java编程基础', 'T003', 2, 40, 2);
INSERT INTO `course` (`id`, `code`, `title`, `instructor_id`, `schedule_id`, `capacity`, `enrolled`) VALUES ('821fd960-b3d3-11f0-beb5-00ff5c9af41d', 'AI101', '人工智能导论', 'T001', 5, 50, 1);
INSERT INTO `course` (`id`, `code`, `title`, `instructor_id`, `schedule_id`, `capacity`, `enrolled`) VALUES ('ab7b6c4a-9d17-44d6-853f-73e0b7e9cc88', 'CN301', '计算机网络实验', 'T004', 7, 40, 0);
