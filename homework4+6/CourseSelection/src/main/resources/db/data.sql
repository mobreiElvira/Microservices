-- 切换到目标数据库
USE course_select;

-- 1. 插入教师数据（基础数据，无外键依赖）
INSERT INTO instructor (id, name, email)
VALUES
    ('T001', '张教授', 'zhang_prof@zjsu.edu.cn'),
    ('T002', '李老师', 'li_teacher@zjsu.edu.cn'),
    ('T003', '王讲师', 'wang_lecturer@zjsu.edu.cn');

-- 2. 插入课程安排数据（自增ID，后续课程表会关联）
INSERT INTO schedule_slot (dayOfWeek, startTime, endTime, expectedAttendance)
VALUES
    ('MONDAY', '08:30', '10:00', 50),   -- ID=1（自增）
    ('TUESDAY', '14:00', '15:30', 40),  -- ID=2
    ('WEDNESDAY', '10:20', '11:50', 45),-- ID=3
    ('THURSDAY', '16:00', '17:30', 35), -- ID=4
    ('FRIDAY', '09:00', '10:30', 50);   -- ID=5

-- 3. 插入课程数据（关联教师ID和课程安排ID）
INSERT INTO course (id, code, title, instructor_id, schedule_id, capacity, enrolled)
VALUES
    (UUID(), 'CS101', '计算机科学导论', 'T001', 1, 50, 0),  -- 关联周一第1节课、张教授
    (UUID(), 'CS202', '数据库原理', 'T002', 3, 45, 0),      -- 关联周三第3节课、李老师
    (UUID(), 'CS301', 'Java编程基础', 'T003', 2, 40, 0),    -- 关联周二第2节课、王讲师
    (UUID(), 'AI101', '人工智能导论', 'T001', 5, 50, 0);    -- 关联周五第5节课、张教授

-- 4. 插入学生数据（包含学号、专业等信息）
INSERT INTO student (id, studentId, name, major, grade, email, createdAt)
VALUES
    (UUID(), 'S2024001', '张三', '计算机科学与技术', 2024, 'zhangsan@zjsu.edu.cn', NOW()),
    (UUID(), 'S2024002', '李四', '软件工程', 2024, 'lisi@zjsu.edu.cn', NOW()),
    (UUID(), 'S2023001', '王五', '数据科学与大数据技术', 2023, 'wangwu@zjsu.edu.cn', NOW()),
    (UUID(), 'S2023002', '赵六', '人工智能', 2023, 'zhaoliu@zjsu.edu.cn', NOW()),
    (UUID(), 'S2024003', '孙七', '计算机科学与技术', 2024, 'sunqi@zjsu.edu.cn', NOW());

-- 5. 插入选课记录（关键：用子查询动态获取course.id和student.id，避免外键错误）
INSERT INTO enrollment (id, courseId, studentId, enrolledAt)
VALUES
-- 张三（S2024001）选 计算机科学导论（CS101）
(UUID(),
 (SELECT id FROM course WHERE code = 'CS101'),  -- 动态获取CS101的真实ID
 (SELECT id FROM student WHERE studentId = 'S2024001'),  -- 动态获取S2024001的真实ID
 NOW()),

-- 李四（S2024002）选 Java编程基础（CS301）
(UUID(),
 (SELECT id FROM course WHERE code = 'CS301'),
 (SELECT id FROM student WHERE studentId = 'S2024002'),
 NOW()),

-- 王五（S2023001）选 人工智能导论（AI101）
(UUID(),
 (SELECT id FROM course WHERE code = 'AI101'),
 (SELECT id FROM student WHERE studentId = 'S2023001'),
 NOW()),

-- 赵六（S2023002）选 数据库原理（CS202）
(UUID(),
 (SELECT id FROM course WHERE code = 'CS202'),
 (SELECT id FROM student WHERE studentId = 'S2023002'),
 NOW());

-- 6. 同步更新课程的选课人数（enrolled字段）
UPDATE course
SET enrolled = enrolled + 1
WHERE code IN ('CS101', 'CS301', 'AI101', 'CS202');
