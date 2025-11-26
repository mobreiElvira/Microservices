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
