USE course_select;


CREATE TABLE instructor (
                            id VARCHAR(36) PRIMARY KEY COMMENT '教师ID（如“T001”）',
                            name VARCHAR(50) NOT NULL COMMENT '教师姓名（如“张教授”）',
                            email VARCHAR(100) NOT NULL UNIQUE COMMENT '教师邮箱（如“zhang@example.edu.cn”）',
                            CONSTRAINT uk_instructor_email UNIQUE (email) COMMENT '确保教师邮箱唯一'
) COMMENT '教师信息表';


CREATE TABLE schedule_slot (
                               id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '课程安排唯一ID（自增）',
                               dayOfWeek VARCHAR(20) NOT NULL COMMENT '周几（如“MONDAY”）',
                               startTime VARCHAR(10) NOT NULL COMMENT '开始时间（如“08:00”）',
                               endTime VARCHAR(10) NOT NULL COMMENT '结束时间（如“10:00”）',
                               expectedAttendance INT NOT NULL COMMENT '预期出勤人数（如50）'
) COMMENT '课程安排表';


CREATE TABLE course (
                        id VARCHAR(36) PRIMARY KEY COMMENT '课程ID（系统自动生成UUID）',
                        code VARCHAR(20) NOT NULL UNIQUE COMMENT '课程编码（如“CS101”）',
                        title VARCHAR(100) NOT NULL COMMENT '课程名称（如“计算机科学导论”）',
                        instructor_id VARCHAR(36) NOT NULL COMMENT '授课教师ID（关联instructor表）',
                        schedule_id BIGINT NOT NULL COMMENT '课程安排ID（关联schedule_slot表）',
                        capacity INT NOT NULL COMMENT '课程容量（最大选课人数）',
                        enrolled INT NOT NULL DEFAULT 0 COMMENT '当前选课人数（自动更新）',
                        CONSTRAINT fk_course_instructor FOREIGN KEY (instructor_id) REFERENCES instructor(id),
                        CONSTRAINT fk_course_schedule FOREIGN KEY (schedule_id) REFERENCES schedule_slot(id)
) COMMENT '课程信息表';


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
                            CONSTRAINT fk_enrollment_course FOREIGN KEY (courseId) REFERENCES course(id) ON DELETE CASCADE,
                            CONSTRAINT fk_enrollment_student FOREIGN KEY (studentId) REFERENCES student(id) ON DELETE CASCADE,
                            CONSTRAINT uk_enrollment_course_student UNIQUE (courseId, studentId)
) COMMENT '选课记录表';
