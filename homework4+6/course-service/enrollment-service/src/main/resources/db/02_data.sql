USE enrollment_db;

INSERT INTO `student` (`id`, `studentId`, `name`, `major`, `grade`, `email`, `createdAt`) VALUES ('0a75b3f0-efb2-4952-8614-a20fb48fcc00', 'S2025003', '陈思远', '人工智能', 2025, 'chensiyuan@example.edu.cn', '2025-10-28 18:27:34');
INSERT INTO `student` (`id`, `studentId`, `name`, `major`, `grade`, `email`, `createdAt`) VALUES ('8220ae32-b3d3-11f0-beb5-00ff5c9af41d', 'S2024001', '张三', '计算机科学与技术', 2024, 'zhangsan@zjsu.edu.cn', '2025-10-28 15:55:45');
INSERT INTO `student` (`id`, `studentId`, `name`, `major`, `grade`, `email`, `createdAt`) VALUES ('8220bc3e-b3d3-11f0-beb5-00ff5c9af41d', 'S202655', '林小明', '软件工程', 2024, 'lxmg@example.edu.cn', '2025-10-28 15:55:45');
INSERT INTO `student` (`id`, `studentId`, `name`, `major`, `grade`, `email`, `createdAt`) VALUES ('8220bf6b-b3d3-11f0-beb5-00ff5c9af41d', 'S2023001', '王五', '数据科学与大数据技术', 2023, 'wangwu@zjsu.edu.cn', '2025-10-28 15:55:45');
INSERT INTO `student` (`id`, `studentId`, `name`, `major`, `grade`, `email`, `createdAt`) VALUES ('8220c131-b3d3-11f0-beb5-00ff5c9af41d', 'S2023002', '赵六', '人工智能', 2023, 'zhaoliu@zjsu.edu.cn', '2025-10-28 15:55:45');

INSERT INTO `enrollment` (`id`, `courseId`, `studentId`, `enrolledAt`) VALUES ('8221ad4e-b3d3-11f0-beb5-00ff5c9af41d', '821fc70e-b3d3-11f0-beb5-00ff5c9af41d', '8220ae32-b3d3-11f0-beb5-00ff5c9af41d', '2025-10-28 15:55:45');
INSERT INTO `enrollment` (`id`, `courseId`, `studentId`, `enrolledAt`) VALUES ('8221c2d0-b3d3-11f0-beb5-00ff5c9af41d', '821fd701-b3d3-11f0-beb5-00ff5c9af41d', '8220bc3e-b3d3-11f0-beb5-00ff5c9af41d', '2025-10-28 15:55:45');
INSERT INTO `enrollment` (`id`, `courseId`, `studentId`, `enrolledAt`) VALUES ('8221ca8c-b3d3-11f0-beb5-00ff5c9af41d', '821fd960-b3d3-11f0-beb5-00ff5c9af41d', '8220bf6b-b3d3-11f0-beb5-00ff5c9af41d', '2025-10-28 15:55:45');
INSERT INTO `enrollment` (`id`, `courseId`, `studentId`, `enrolledAt`) VALUES ('8221ceba-b3d3-11f0-beb5-00ff5c9af41d', '821fd389-b3d3-11f0-beb5-00ff5c9af41d', '8220c131-b3d3-11f0-beb5-00ff5c9af41d', '2025-10-28 15:55:45');
INSERT INTO `enrollment` (`id`, `courseId`, `studentId`, `enrolledAt`) VALUES ('a2503294-0a02-4e2c-8d1e-015fcb0cb864', '821fd701-b3d3-11f0-beb5-00ff5c9af41d', '8220ae32-b3d3-11f0-beb5-00ff5c9af41d', '2025-10-28 18:34:12');

