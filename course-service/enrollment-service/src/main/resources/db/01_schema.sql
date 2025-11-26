CREATE DATABASE IF NOT EXISTS enrollment_db;

USE enrollment_db;

CREATE TABLE student (
                         id VARCHAR(36) PRIMARY KEY COMMENT '学生ID（系统自动生成UUID）',
                         studentId VARCHAR(20) NOT NULL UNIQUE COMMENT '学号（全局唯一，如“S2024001”）',
                         name VARCHAR(50) NOT NULL COMMENT '学生姓名（必填）',
                         major VARCHAR(50) NOT NULL COMMENT '专业（必填，如“计算机科学与技术”）',
                         grade INT NOT NULL COMMENT '入学年份（必填，如2024）',
                         email VARCHAR(100) NOT NULL UNIQUE COMMENT '邮箱（必填，需符合标准格式）',
                         createdAt DATETIME NOT NULL COMMENT '系统自动生成的创建时间戳'
) COMMENT '学生信息表';


CREATE TABLE enrollment (
                            id VARCHAR(36) PRIMARY KEY COMMENT '选课记录ID（系统自动生成UUID）',
                            courseId VARCHAR(36) NOT NULL COMMENT '课程ID（关联course表）',
                            studentId VARCHAR(36) NOT NULL COMMENT '学生ID（关联student表）',
                            enrolledAt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '选课时间（自动记录）',
                            CONSTRAINT fk_enrollment_student FOREIGN KEY (studentId) REFERENCES student(id) ON DELETE CASCADE,
                            CONSTRAINT uk_enrollment_course_student UNIQUE (courseId, studentId)
) COMMENT '选课记录表';
