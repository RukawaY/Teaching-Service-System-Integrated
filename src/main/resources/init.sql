CREATE DATABASE IF NOT EXISTS project_management;
USE project_management;

-- 创建用户表
CREATE TABLE IF NOT EXISTS user (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    account VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(10) NOT NULL,               -- 角色: s-学生, t-教师, a-管理员
    department VARCHAR(100),                 -- 部门/院系
    contact VARCHAR(100),                    -- 联系方式
    avatar_path VARCHAR(255),                -- 头像路径
    major_id INT
);

-- 专业表
CREATE TABLE IF NOT EXISTS major (
    major_id INT PRIMARY KEY,
    major_name VARCHAR(100) NOT NULL
);

-- 创建课程表
CREATE TABLE IF NOT EXISTS Course (
    course_id INT PRIMARY KEY AUTO_INCREMENT,
    course_name VARCHAR(100) NOT NULL,
    course_description TEXT,
    teacher_id INT NOT NULL,
    credit FLOAT NOT NULL,
    category VARCHAR(50),
    hours_per_week INT,
    FOREIGN KEY (teacher_id) REFERENCES user(user_id)
);

-- 创建教室表
CREATE TABLE IF NOT EXISTS Classroom (
    classroom_id INT PRIMARY KEY AUTO_INCREMENT,
    location VARCHAR(100) NOT NULL,
    capacity INT NOT NULL,
    category VARCHAR(10) NOT NULL
);

-- 创建开课信息表
CREATE TABLE IF NOT EXISTS Section (
    section_id INT PRIMARY KEY AUTO_INCREMENT,
    course_id INT NOT NULL,
    classroom_id INT NOT NULL,
    capacity INT NOT NULL,
    available_capacity INT NOT NULL,
    semester VARCHAR(20) NOT NULL,
    sec_year INT NOT NULL,
    sec_time VARCHAR(100) NOT NULL,
    FOREIGN KEY (course_id) REFERENCES Course(course_id),
    FOREIGN KEY (classroom_id) REFERENCES Classroom(classroom_id)
);

-- 创建基础成绩表
CREATE TABLE IF NOT EXISTS GradeBase (
    grade_id INT PRIMARY KEY AUTO_INCREMENT,
    student_id INT NOT NULL,
    course_id INT NOT NULL,
    section_id INT NOT NULL,
    score INT,
    gpa FLOAT,
    submit_status ENUM('0', '1') DEFAULT '0',
    FOREIGN KEY (student_id) REFERENCES user(user_id),
    FOREIGN KEY (course_id) REFERENCES Course(course_id),
    FOREIGN KEY (section_id) REFERENCES Section(section_id)
);

-- 创建成绩组成表
CREATE TABLE IF NOT EXISTS GradeComponent (
    component_id INT PRIMARY KEY AUTO_INCREMENT,
    component_name VARCHAR(255) NOT NULL,
    grade_id INT NOT NULL,
    component_type ENUM('0', '1', '2') NOT NULL,
    ratio INT NOT NULL,
    score INT,
    FOREIGN KEY (grade_id) REFERENCES GradeBase(grade_id)
);

-- 创建成绩修改申请表
CREATE TABLE IF NOT EXISTS Apply (
    apply_id INT PRIMARY KEY AUTO_INCREMENT,
    teacher_id INT NOT NULL,
    admin_id INT,
    grade_id INT NOT NULL,
    old_score INT NOT NULL,
    new_score INT NOT NULL,
    reason VARCHAR(255) NOT NULL,
    audit_reason VARCHAR(255) NOT NULL,
    audit_status ENUM('0', '1', '2') DEFAULT '0',  -- 0-待审核, 1-已通过, 2-已拒绝
    apply_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    review_time DATETIME,
    FOREIGN KEY (teacher_id) REFERENCES user(user_id),
    FOREIGN KEY (admin_id) REFERENCES user(user_id),
    FOREIGN KEY (grade_id) REFERENCES GradeBase(grade_id)
);

-- 选课记录表
CREATE TABLE IF NOT EXISTS course_selection (
    id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    section_id INT NOT NULL,
    select_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_stu_section (student_id, section_id),
    FOREIGN KEY (student_id) REFERENCES user(user_id),
    FOREIGN KEY (section_id) REFERENCES section(section_id)
);

-- 补选申请表
CREATE TABLE IF NOT EXISTS course_supplement (
    supplement_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    section_id INT NOT NULL,
    apply_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status TINYINT DEFAULT 0 COMMENT '0-待处理, 1-已同意, 2-已拒绝',
    FOREIGN KEY (student_id) REFERENCES user(user_id),
    FOREIGN KEY (section_id) REFERENCES section(section_id),
    UNIQUE KEY uk_stu_section_suppl (student_id, section_id)
);

-- 专业培养方案表（存储为JSON格式）
CREATE TABLE IF NOT EXISTS curriculum (
    id INT AUTO_INCREMENT PRIMARY KEY,
    major_id INT NOT NULL,
    curriculum_json JSON NOT NULL,
    UNIQUE KEY uk_major (major_id),
    FOREIGN KEY (major_id) REFERENCES major(major_id)
);

-- 个人培养方案表（存储为JSON格式）
CREATE TABLE IF NOT EXISTS personal_curriculum (
    id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    curriculum_json JSON NOT NULL,
    UNIQUE KEY uk_student (student_id),
    FOREIGN KEY (student_id) REFERENCES user(user_id)
);

-- 选课系统时间配置表（存储为JSON格式）
CREATE TABLE IF NOT EXISTS selection_time (
    id INT PRIMARY KEY DEFAULT 1,
    max_number INT NOT NULL,
    first_time_list JSON NOT NULL,
    second_time_list JSON NOT NULL,
    drop_time_list JSON NOT NULL
);

-- 创建章节表 (新添加)
CREATE TABLE IF NOT EXISTS Chapter (
  chapter_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '章节ID，主键',
  course_id INT NOT NULL COMMENT '所属课程ID',
  chapter_name VARCHAR(255) NOT NULL COMMENT '章节名称',
  sequence INT NOT NULL COMMENT '章节顺序号',
  FOREIGN KEY (course_id) REFERENCES Course(course_id),
  UNIQUE KEY uk_course_chapter_sequence (course_id, sequence) COMMENT '同一课程下章节顺序唯一',
  UNIQUE KEY uk_course_chapter_name (course_id, chapter_name) COMMENT '同一课程下章节名唯一 (可选)'
) COMMENT '课程章节表';

-- 创建题库表 (新添加)
CREATE TABLE IF NOT EXISTS QuestionBank (
  question_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '题目ID，主键',
  course_id INT NOT NULL COMMENT '所属课程ID',
  chapter_id INT NOT NULL COMMENT '所属章节ID',
  question_type VARCHAR(10) NOT NULL COMMENT '题目类型 (例如: MC-选择题, TF-判断题)',
  content TEXT NOT NULL COMMENT '题目内容',
  options JSON COMMENT '题目选项 (对于选择题，以JSON数组形式存储，例如：["选项A", "选项B"])',
  answer VARCHAR(255) NOT NULL COMMENT '题目答案 (对于选择题可以是选项标识如A/B/C/D，或正确选项的文本；对于判断题是True/False)',
  score INT NOT NULL DEFAULT 1 COMMENT '题目分数',
  difficulty INT NOT NULL DEFAULT 1 COMMENT '题目难度 (例如1-5)',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  FOREIGN KEY (course_id) REFERENCES Course(course_id),
  FOREIGN KEY (chapter_id) REFERENCES Chapter(chapter_id),
  INDEX idx_qb_course (course_id),
  INDEX idx_qb_chapter (chapter_id)
) COMMENT '题库表';

-- 创建试卷发布表 (新添加)
CREATE TABLE IF NOT EXISTS TestPublish (
  test_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '测试/考试ID，主键',
  teacher_id INT NOT NULL COMMENT '发布教师ID',
  course_id INT NOT NULL COMMENT '所属课程ID',
  test_name VARCHAR(255) NOT NULL COMMENT '测试/考试名称',
  publish_time TIMESTAMP NULL COMMENT '发布时间',
  deadline TIMESTAMP NULL COMMENT '截止时间',
  question_count INT COMMENT '题目数量',
  is_random BOOLEAN DEFAULT FALSE COMMENT '是否随机组卷',
  question_ids JSON COMMENT '题目ID列表 (如果非随机组卷，以JSON数组形式存储题目ID，例如：[101, 102, 105])',
  ratio INT NOT NULL,
  FOREIGN KEY (teacher_id) REFERENCES User(user_id),
  FOREIGN KEY (course_id) REFERENCES Course(course_id),
  INDEX idx_tp_teacher (teacher_id),
  INDEX idx_tp_course (course_id)
) COMMENT '试卷发布表';

-- 创建学生作答结果表 (新添加)
CREATE TABLE IF NOT EXISTS StudentAnswerResult (
  result_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '作答结果ID，主键',
  test_id INT NOT NULL COMMENT '所属测试/考试ID',
  student_id INT NOT NULL COMMENT '学生ID',
  question_id INT NOT NULL COMMENT '题目ID',
  student_answer VARCHAR(255) COMMENT '学生答案',
  is_correct BOOLEAN COMMENT '是否正确',
  score_obtained INT COMMENT '得分',
  answer_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '作答时间',
  FOREIGN KEY (test_id) REFERENCES TestPublish(test_id),
  FOREIGN KEY (student_id) REFERENCES User(user_id),
  FOREIGN KEY (question_id) REFERENCES QuestionBank(question_id),
  UNIQUE KEY uk_student_test_question (student_id, test_id, question_id) COMMENT '确保学生对同一测试的同一题目只作答一次',
  INDEX idx_sar_test_student (test_id, student_id),
  INDEX idx_sar_student (student_id)
) COMMENT '学生作答结果表';

-- 作业表
CREATE TABLE IF NOT EXISTS Homework (
  homework_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '作业ID',
  course_id INT NOT NULL COMMENT '所属课程ID',
  title VARCHAR(255) NOT NULL COMMENT '作业标题',
  description TEXT COMMENT '作业描述',
  deadline TIMESTAMP NOT NULL COMMENT '截止日期',
  weight DOUBLE COMMENT '在总成绩中的权重',
  requirements TEXT COMMENT '作业要求',
  FOREIGN KEY (course_id) REFERENCES Course(course_id) ON DELETE CASCADE,
  INDEX idx_homework_course (course_id)
) COMMENT '课程作业表';

-- 作业提交表
CREATE TABLE IF NOT EXISTS homework_submission (
  submission_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '提交ID',
  homework_id INT NOT NULL COMMENT '作业ID',
  student_id INT NOT NULL COMMENT '学生ID',
  submit_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
  file_name VARCHAR(255) COMMENT '文件名称',
  file_url VARCHAR(512) COMMENT '文件路径/URL',
  score DOUBLE COMMENT '得分',
  comment TEXT COMMENT '教师评语',
  FOREIGN KEY (homework_id) REFERENCES Homework(homework_id) ON DELETE CASCADE,
  FOREIGN KEY (student_id) REFERENCES user(user_id),
  UNIQUE KEY uk_homework_student (homework_id, student_id) COMMENT '一个学生对一个作业只能有一个提交',
  INDEX idx_submission_homework (homework_id),
  INDEX idx_submission_student (student_id)
) COMMENT '作业提交表';

-- 专业数据
INSERT INTO major (major_id, major_name) VALUES 
(1, '软件工程'),
(2, '计算机科学与技术');

-- 用户数据：管理员、教师、学生
INSERT INTO user (account, password, name, role, department, contact, major_id) VALUES
('admin001', 'adminpass', '系统管理员', 'a', '信息中心', '13900000001', NULL),
('t_zhang', 'teachpass1', '张老师', 't', '软件学院', '13800000002', 1),
('t_li', 'teachpass2', '李老师', 't', '计算机学院', '13800000003', 2),
('s_wang', 'studpass1', '王同学', 's', '软件学院', '13700000004', 1),
('s_chen', 'studpass2', '陈同学', 's', '计算机学院', '13700000005', 2),
('s_liu', 'studpass3', '刘同学', 's', '计算机学院', '13700000006', 2);

-- 课程数据
INSERT INTO Course (course_name, course_description, teacher_id, credit, category, hours_per_week) VALUES
('程序设计基础', '学习C语言程序设计基础', 2, 3.0, '普通', 4),
('数据结构', '介绍各种数据结构及其实现', 2, 4.0, '普通', 4),
('计算机组成原理', '讲解计算机基本组成部件及其工作原理', 3, 3.5, '普通', 3),
('操作系统', '操作系统的概念与实现机制', 3, 4.0, '实验', 4),
('计算机科学导论', '本课程介绍计算机科学的基本概念和原理', 2, 3.0, '普通', 5);

-- 章节数据
INSERT INTO Chapter (course_id, chapter_name, sequence) VALUES
(1, '第一章：C语言概述', 1),
(1, '第二章：变量与表达式', 2),
(2, '第一章：线性结构', 1),
(2, '第二章：栈与队列', 2),
(3, '第一章：计算机硬件基础', 1),
(4, '第一章：进程管理', 1);

-- 教室数据
INSERT INTO Classroom (location, capacity, category) VALUES
('A101', 60, '普通'),
('B201', 30, '实验'),
('C301', 100, '普通');

-- 开课信息
INSERT INTO Section (course_id, classroom_id, capacity, available_capacity, semester, sec_year, sec_time) VALUES
(1, 1, 60, 60, '秋冬', 2024, 'Monday 1; Monday 2'),
(2, 3, 100, 98, '秋冬', 2024, 'Wednesday 3; Wednesday 4'),
(3, 3, 90, 90, '春夏', 2025, 'Friday 1; Friday 2'),
(4, 2, 30, 30, '春夏', 2025, 'Thursday 5; Thursday 6');

-- 选课记录
INSERT INTO course_selection (student_id, section_id) VALUES
(4, 1),
(4, 2),
(5, 2),
(6, 3);

-- 基础成绩
INSERT INTO GradeBase (student_id, course_id, section_id, score, gpa, submit_status) VALUES
(4, 1, 1, 90, 4.0, '1'),
(4, 2, 2, 85, 3.7, '1'),
(5, 2, 2, 78, 3.0, '1'),
(6, 3, 3, 88, 3.8, '0');

-- 成绩组成
INSERT INTO GradeComponent (component_name, grade_id, component_type, ratio, score) VALUES
('期中考试', 1, '0', 50, 45),
('期末考试', 1, '1', 50, 45),
('作业', 2, '2', 20, 17),
('项目', 2, '0', 30, 25),
('期末考试', 2, '1', 50, 43);

-- 成绩修改申请
INSERT INTO Apply (teacher_id, admin_id, grade_id, old_score, new_score, reason, audit_reason, audit_status, apply_time, review_time) VALUES
(2, 1, 3, 78, 82, '评分计算有误', '确认修改无误', '1', NOW(), NOW()),
(3, NULL, 4, 88, 90, '补交作业影响成绩', '', '0', NOW(), NULL);

-- 补选申请
INSERT INTO course_supplement (student_id, section_id) VALUES
(5, 1),
(6, 2);

-- 专业培养方案
INSERT INTO curriculum (major_id, curriculum_json) VALUES
    (2, '[
            {
                "section_credit": 11,
                "section_name": "专业必修课程",
                "course_list": [
                    {
                        "course_name": "程序设计基础",
                        "credit": 3.0
                    },
                    {
                        "course_name": "数据结构",
                        "credit": 4.0
                    },
                    {
                        "course_name": "操作系统",
                        "credit": 4.0
                    }
                ]
            }
        ]');
        
INSERT INTO curriculum (major_id, curriculum_json) VALUES
    (1, '[
            {
                "section_credit": 11.5,
                "section_name": "专业必修课程",
                "course_list": [
                    {
                        "course_name": "数据结构",
                        "credit": 4.0
                    },
                    {
                        "course_name": "计算机组成原理",
                        "credit": 3.5
                    },
                    {
                        "course_name": "操作系统",
                        "credit": 4.0
                    }
                ]
            }
        ]');

-- -- 个人培养方案
INSERT INTO personal_curriculum (student_id, curriculum_json) VALUES
    (4, '[
            {
                "section_credit": 11.5,
                "section_name": "专业必修课程",
                "course_list": [
                    {
                        "course_name": "数据结构",
                        "credit": 4.0
                    },
                    {
                        "course_name": "计算机组成原理",
                        "credit": 3.5
                    },
                    {
                        "course_name": "操作系统",
                        "credit": 4.0
                    }
                ]
            }
        ]');
        
INSERT INTO personal_curriculum (student_id, curriculum_json) VALUES
    (5, '[
            {
                "section_credit": 11,
                "section_name": "专业必修课程",
                "course_list": [
                    {
                        "course_name": "程序设计基础",
                        "credit": 3.0
                    },
                    {
                        "course_name": "数据结构",
                        "credit": 4.0
                    },
                    {
                        "course_name": "操作系统",
                        "credit": 4.0
                    }
                ]
            }
        ]');

-- 选课系统时间配置
INSERT INTO selection_time (id, max_number, first_time_list, second_time_list, drop_time_list)
VALUES (1, 23, 
        '["2026-03-29 13:58:50", "2026-05-29 13:58:50"]', 
        '["2025-04-23 12:33:04", "2025-05-27 12:00:00"]', 
        '["2025-04-23 12:33:04", "2025-05-27 12:00:00"]');

-- 题库数据
INSERT INTO QuestionBank (course_id, chapter_id, question_type, content, options, answer, score, difficulty) VALUES
(1, 1, 'MC', 'C语言的发明者是？', '["Dennis Ritchie", "Bjarne Stroustrup", "James Gosling", "Guido van Rossum"]', 'A', 5, 1),
(1, 2, 'TF', 'C语言支持面向对象编程。', NULL, 'F', 3, 1),
(2, 4, 'MC', '栈的特点是？', '["先进先出", "后进先出", "随机访问", "双向访问"]', 'B', 5, 2);

-- 试卷发布
INSERT INTO TestPublish (teacher_id, course_id, test_name, publish_time, deadline, question_count, is_random, question_ids, ratio) VALUES
(2, 1, '程序设计期中测试', NOW(), DATE_ADD(NOW(), INTERVAL 7 DAY), 2, FALSE, '[1, 2]', 30),
(2, 2, '数据结构小测验', NOW(), DATE_ADD(NOW(), INTERVAL 3 DAY), 1, TRUE, NULL, 10);

-- 学生作答记录
INSERT INTO StudentAnswerResult (test_id, student_id, question_id, student_answer, is_correct, score_obtained) VALUES
(1, 4, 1, 'A', TRUE, 5),
(1, 4, 2, 'T', FALSE, 0),
(2, 5, 3, 'B', TRUE, 5);

-- 作业数据
INSERT INTO Homework (course_id, title, description, deadline, weight, requirements) VALUES
(1, '第一次作业：变量与表达式', '掌握变量定义和基本运算', DATE_ADD(NOW(), INTERVAL 5 DAY), 0.1, '提交PDF文件'),
(2, '第二次作业：链表实现', '实现链表的基本功能', DATE_ADD(NOW(), INTERVAL 10 DAY), 0.2, '附加代码说明');

-- 作业提交记录
INSERT INTO homework_submission (homework_id, student_id, file_name, file_url) VALUES
(1, 4, '变量表达式作业.pdf', '/uploads/hw1_student4.pdf'),
(2, 5, '链表实现作业.zip', '/uploads/hw2_student5.zip');
