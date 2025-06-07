-- 创建用户表
CREATE TABLE IF NOT EXISTS user (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    account VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(10) NOT NULL,              -- 角色: s-学生, t-教师, a-管理员
    department VARCHAR(100),                -- 部门/院系
    contact VARCHAR(100),                   -- 联系方式
    avatar_path VARCHAR(255),               -- 头像路径
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
    audit_reason VARCHAR(255),
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

-- 资源表
CREATE TABLE IF NOT EXISTS resource (
    resource_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '资源ID',
    uploader_id BIGINT NOT NULL COMMENT '上传者ID',
    course_id BIGINT COMMENT '关联课程ID',
    resource_name VARCHAR(255) NOT NULL COMMENT '资源名称',
    resource_type VARCHAR(50) NOT NULL COMMENT '资源类型（如文档、视频）',
    upload_time DATETIME NOT NULL COMMENT '上传时间',
    file_path VARCHAR(255) NOT NULL COMMENT '文件存储路径',
    description TEXT COMMENT '资源描述',
    keywords VARCHAR(255) COMMENT '关键词',
    directory_id BIGINT COMMENT '所属目录ID',
    FOREIGN KEY (course_id) REFERENCES course(course_id) ON DELETE SET NULL
) COMMENT '资源表';

-- 专业数据
INSERT INTO major (major_id, major_name) VALUES 
(1, '软件工程'),
(2, '计算机科学与技术'),
(3, '信息安全'),
(4, '人工智能');

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
('计算机科学导论', '本课程介绍计算机科学的基本概念和原理', 2, 3.0, '普通', 5),
('数据库系统', '数据库的设计、实现与应用', 3, 3.5, '实验', 3),
('Web开发技术', '学习HTML, CSS, JavaScript及后端框架', 2, 3.0, '实验', 4),
('编译原理', '编译器设计的基本原理和技术', 3, 3.0, '普通', 3),
('软件测试', '介绍软件测试的基本理论与实践方法', 2, 2.5, '实验', 3),
('网络安全', '探讨网络攻击与防御技术', 3, 3.0, '普通', 3);

-- 章节数据
INSERT INTO Chapter (course_id, chapter_name, sequence) VALUES
(1, '第一章：C语言概述', 1),
(1, '第二章：变量与表达式', 2),
(2, '第一章：线性结构', 1),
(2, '第二章：栈与队列', 2),
(3, '第一章：计算机硬件基础', 1),
(4, '第一章：进程管理', 1),
(1, '第三章：流程控制', 3),
(2, '第三章：树与二叉树', 3),
(6, '第一章：关系模型', 1),
(6, '第二章：SQL语言', 2),
(7, '第一章：HTML基础', 1),
(9, '第一章：软件测试概论', 1),
(9, '第二章：黑盒测试', 2),
(10, '第一章：网络安全基础', 1);

-- 教室数据
INSERT INTO Classroom (location, capacity, category) VALUES
('A101', 60, '普通'),
('B201', 30, '实验'),
('C301', 100, '普通'),
('D404', 120, '普通'),
('E102', 40, '实验');

-- 开课信息
INSERT INTO Section (course_id, classroom_id, capacity, available_capacity, semester, sec_year, sec_time) VALUES
(1, 1, 60, 60, '秋冬', 2024, 'Monday 1; Monday 2'),
(2, 3, 100, 98, '秋冬', 2024, 'Wednesday 3; Wednesday 4'),
(3, 3, 90, 90, '春夏', 2025, 'Friday 1; Friday 2'),
(4, 2, 30, 30, '春夏', 2025, 'Thursday 5; Thursday 6'),
(5, 4, 120, 119, '秋冬', 2024, 'Tuesday 3; Tuesday 4'),
(6, 5, 40, 40, '春夏', 2025, 'Monday 7; Monday 8'),
(7, 2, 30, 30, '秋冬', 2024, 'Friday 7; Friday 8'),
(9, 2, 30, 30, '春夏', 2025, 'Tuesday 7; Tuesday 8'),
(10, 1, 60, 60, '春夏', 2025, 'Wednesday 5; Wednesday 6');

-- 选课记录
INSERT INTO course_selection (student_id, section_id) VALUES
(4, 1),
(4, 2),
(5, 2),
(6, 3),
(5, 5),
(6, 1),
(4, 5),
(5, 4),
(6, 7),
(6, 5),
(4, 8),
(5, 9),
(6, 8),
(4, 6),
(5, 3),
(6, 2);

-- 基础成绩
INSERT INTO GradeBase (student_id, course_id, section_id, score, gpa, submit_status) VALUES
(4, 1, 1, 90, 4.0, '1'),
(4, 2, 2, 85, 3.7, '1'),
(5, 2, 2, 78, 3.0, '1'),
(6, 3, 3, 88, 3.8, '0'),
(5, 5, 5, 92, 4.0, '1'),
(6, 1, 1, 80, 3.3, '1'),
(4, 5, 5, 88, 3.8, '1'),
(5, 4, 4, 91, 4.0, '1'),
(6, 7, 7, 76, 2.9, '1'),
(6, 5, 5, 82, 3.5, '0'),
(4, 9, 8, 85, 3.7, '1'),
(5, 10, 9, 90, 4.0, '1'),
(6, 9, 8, 78, 3.0, '1'),
(4, 6, 6, 92, 4.0, '1'),
(5, 3, 3, 86, 3.7, '1'),
(6, 2, 2, 89, 3.9, '1');

-- 成绩组成
INSERT INTO GradeComponent (component_name, grade_id, component_type, ratio, score) VALUES
('期中考试', 1, '0', 50, 45),
('期末考试', 1, '1', 50, 45),
('作业', 2, '2', 20, 17),
('项目', 2, '0', 30, 25),
('期末考试', 2, '1', 50, 43),
('平时表现', 5, '2', 30, 28),
('期末大作业', 5, '1', 70, 64),
('课堂测验', 6, '0', 40, 35),
('实验报告', 6, '2', 60, 45),
('平时分', 7, '2', 40, 35),
('期末报告', 7, '1', 60, 53),
('实验一', 8, '2', 25, 22),
('实验二', 8, '2', 25, 24),
('期末机考', 8, '1', 50, 45),
('课堂参与', 9, '2', 20, 16),
('项目演示', 9, '0', 40, 30),
('最终笔试', 9, '1', 40, 30),
('期中测验', 10, '0', 50, 40),
('期末测验', 10, '1', 50, 42),
('单元测试报告', 11, '2', 50, 42),
('期末项目', 11, '1', 50, 43),
('渗透测试实验', 12, '2', 60, 55),
('理论考试', 12, '1', 40, 35),
('单元测试报告', 13, '2', 50, 38),
('期末项目', 13, '1', 50, 40),
('SQL作业', 14, '2', 40, 38),
('数据库设计大作业', 14, '1', 60, 54),
('逻辑电路实验', 15, '2', 50, 43),
('期末笔试', 15, '1', 50, 43),
('算法实现', 16, '2', 40, 35),
('期中考试', 16, '0', 30, 25),
('期末考试', 16, '1', 30, 29);

-- 成绩修改申请
INSERT INTO Apply (teacher_id, admin_id, grade_id, old_score, new_score, reason, audit_reason, audit_status, apply_time, review_time) VALUES
(2, 1, 3, 78, 82, '评分计算有误', '确认修改无误', '1', NOW(), NOW()),
(3, NULL, 4, 88, 90, '补交作业影响成绩', '', '0', NOW(), NULL),
(2, NULL, 6, 80, 85, '实验报告分数录入错误', '', '0', NOW(), NULL);

-- 补选申请
INSERT INTO course_supplement (student_id, section_id) VALUES
(5, 1),
(6, 2),
(4, 3);

-- 专业培养方案
INSERT INTO curriculum (major_id, curriculum_json) VALUES
(2, '[{"section_credit": 11, "section_name": "专业必修课程", "course_list": [{"course_name": "程序设计基础", "credit": 3.0}, {"course_name": "数据结构", "credit": 4.0}, {"course_name": "操作系统", "credit": 4.0}]}]'),
(1, '[{"section_credit": 11.5, "section_name": "专业必修课程", "course_list": [{"course_name": "数据结构", "credit": 4.0}, {"course_name": "计算机组成原理", "credit": 3.5}, {"course_name": "操作系统", "credit": 4.0}]}]'),
(3, '[{"section_credit": 10.5, "section_name": "专业核心课程", "course_list": [{"course_name": "数据结构", "credit": 4.0}, {"course_name": "数据库系统", "credit": 3.5}, {"course_name": "编译原理", "credit": 3.0}]}]'),
(4, '[{"section_credit": 11.0, "section_name": "专业必修课程", "course_list": [{"course_name": "程序设计基础", "credit": 3.0}, {"course_name": "数据结构", "credit": 4.0}, {"course_name": "操作系统", "credit": 4.0}]}]');   

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
(2, 4, 'MC', '栈的特点是？', '["先进先出", "后进先出", "随机访问", "双向访问"]', 'B', 5, 2),
(6, 8, 'MC', 'SQL中用于查询数据的关键字是？', '["SELECT", "INSERT", "UPDATE", "DELETE"]', 'A', 4, 1),
(6, 8, 'TF', '主键约束可以为空。', NULL, 'F', 2, 1),
(7, 9, 'MC', '哪个HTML标签用于创建超链接？', '["<a>", "<p>", "<div>", "<h1>"]', 'A', 5, 1),
-- 为数据结构(course_id=2)添加更多题目
(2, 3, 'MC', '以下哪种不是线性结构？', '["数组", "链表", "栈", "树"]', 'D', 5, 2),
(2, 3, 'TF', '数组是一种随机存取结构。', NULL, 'T', 5, 1),
(2, 4, 'MC', '链式队列的队头在链表的哪个位置？', '["链表头部", "链表尾部", "任意位置", "不确定"]', 'A', 5, 3),
(2, 4, 'TF', '循环队列解决了“假溢出”问题。', NULL, 'T', 5, 2),
(2, 8, 'MC', '二叉树的第i层最多有多少个节点？', '["2^(i-1)", "2^i", "2i", "i^2"]', 'A', 5, 2),
(2, 8, 'TF', '满二叉树一定是完全二叉树。', NULL, 'T', 5, 2),
(2, 8, 'MC', '对一棵二叉排序树进行什么遍历可以得到有序序列？', '["前序遍历", "中序遍历", "后序遍历", "层序遍历"]', 'B', 5, 3),
(2, 3, 'MC', '在单链表中，增加头结点的目的是什么？', '["方便运算", "美观", "没有用", "增加长度"]', 'A', 5, 2),
(2, 4, 'TF', '队列（Queue）是一种先进后出（FILO）的数据结构。', NULL, 'F', 5, 1),
(2, 8, 'MC', '下列关于图的说法正确的是？', '["有向图的邻接矩阵一定是对称的", "图的遍历只能从一个顶点开始", "无向图的全部顶点的度的和等于边数的2倍", "图不能有环"]', 'C', 5, 3),
(2, 3, 'MC', '一个顺序存储的线性表,其优点是？', '["插入运算方便", "删除运算方便", "可以随机存取", "可以表示稀疏矩阵"]', 'C', 5, 1),
(2, 4, 'TF', '共享栈是为了更有效地利用存储空间。', NULL, 'T', 5, 2),
(2, 8, 'MC', '哈夫曼树是一种？', '["二叉排序树", "满二叉树", "完全二叉树", "最优二叉树"]', 'D', 5, 3),
(2, 8, 'TF', '二叉树的任何节点的度都不能大于2。', NULL, 'T', 5, 1),
(2, 3, 'MC', '在长度为n的顺序表中删除一个元素，平均需要移动多少个元素？', '["(n-1)/2", "n/2", "n", "n-1"]', 'A', 5, 2),
(2, 4, 'MC', '判断一个循环队列Q（最多元素为m）为空的条件是？', '["Q.front == Q.rear", "Q.front != Q.rear", "(Q.rear+1)%m == Q.front", "Q.front == (Q.rear+1)%m"]', 'A', 5, 3),
(2, 8, 'TF', '平衡二叉树的左右子树高度差的绝对值不超过1。', NULL, 'T', 5, 2),
(2, 8, 'MC', '邻接表是图的一种？', '["顺序存储结构", "链式存储结构", "索引存储结构", "散列存储结构"]', 'B', 5, 2),
(2, 3, 'TF', '线性表的逻辑顺序与物理顺序总是一致的。', NULL, 'F', 5, 2),
(2, 4, 'MC', '哪种数据结构可用于实现递归函数的非递归转换？', '["栈", "队列", "树", "图"]', 'A', 5, 3),
-- 为操作系统(course_id=4)添加更多题目
(4, 6, 'MC', '操作系统中，进程和程序的根本区别是？', '["静态和动态", "存储在内存和外存", "大小不同", "是否可并发"]', 'A', 10, 2),
(4, 6, 'TF', '进程是资源分配的基本单位，线程是调度的基本单位。', NULL, 'T', 10, 2),
(4, 6, 'MC', '下列哪种调度算法可能导致长作业饥饿？', '["先来先服务", "短作业优先", "时间片轮转", "多级反馈队列"]', 'B', 10, 3),
(4, 6, 'TF', '死锁的四个必要条件是互斥、请求与保持、不可剥夺和循环等待。', NULL, 'T', 10, 2),
(4, 6, 'MC', '在分页存储管理中，页面大小与内存利用率的关系是？', '["页面越大利用率越高", "页面越小利用率越高", "没有关系", "页面大小适中时利用率较高"]', 'D', 10, 3),
-- 为软件测试(course_id=9)添加题目
(9, 12, 'MC', '软件测试的目的是什么？', '["证明软件没有错误", "尽可能多地发现软件中的错误", "改进软件质量", "评估软件开发过程"]', 'B', 5, 1),
(9, 12, 'TF', '测试用例越多，测试质量越高。', NULL, 'F', 5, 2),
(9, 13, 'MC', '等价类划分法属于哪种测试方法？', '["白盒测试", "黑盒测试", "灰盒测试", "回归测试"]', 'B', 5, 2),
-- 为网络安全(course_id=10)添加题目
(10, 14, 'MC', '以下哪项不属于网络安全的三要素（CIA三元组）？', '["机密性", "完整性", "可用性", "不可否认性"]', 'D', 10, 2),
(10, 14, 'TF', '对称加密算法中，加密和解密使用相同的密钥。', NULL, 'T', 10, 1);

-- 试卷发布
INSERT INTO TestPublish (teacher_id, course_id, test_name, publish_time, deadline, question_count, is_random, question_ids, ratio) VALUES
(2, 1, '程序设计期中测试', NOW(), DATE_ADD(NOW(), INTERVAL 7 DAY), 2, FALSE, '[1, 2]', 30),
(2, 2, '数据结构小测验', NOW(), DATE_ADD(NOW(), INTERVAL 3 DAY), 1, FALSE, '[3]', 10),
(3, 6, '数据库期末考试', DATE_ADD(NOW(), INTERVAL 14 DAY), DATE_ADD(NOW(), INTERVAL 21 DAY), 2, FALSE, '[4, 5]', 50),
(2, 2, '数据结构期末综合测试', NOW(), DATE_ADD(NOW(), INTERVAL 10 DAY), 20, FALSE, '[7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26]', 40),
(3, 4, '操作系统第三章测验', NOW(), DATE_ADD(NOW(), INTERVAL 5 DAY), 5, FALSE, '[27, 28, 29, 30, 31]', 20),
(2, 9, '软件测试第一单元测验', NOW(), DATE_ADD(NOW(), INTERVAL 7 DAY), 3, FALSE, '[32, 33, 34]', 25),
(3, 10, '网络安全入门测试', NOW(), DATE_ADD(NOW(), INTERVAL 7 DAY), 2, FALSE, '[35, 36]', 30);

-- 学生作答记录
INSERT INTO StudentAnswerResult (test_id, student_id, question_id, student_answer, is_correct, score_obtained) VALUES
(1, 4, 1, 'A', TRUE, 5),
(1, 4, 2, 'T', FALSE, 0),
(2, 5, 3, 'B', TRUE, 5),
(1, 6, 1, 'A', TRUE, 5),
(1, 6, 2, 'F', TRUE, 3),
-- 为 test_id=4 (数据结构期末综合测试) 添加作答记录
-- 学生4 (s_wang) 的作答记录
(4, 4, 7, 'D', TRUE, 5),
(4, 4, 8, 'T', TRUE, 5),
(4, 4, 9, 'A', TRUE, 5),
(4, 4, 10, 'T', TRUE, 5),
(4, 4, 11, 'B', FALSE, 0), -- 答错
(4, 4, 12, 'T', TRUE, 5),
(4, 4, 13, 'B', TRUE, 5),
(4, 4, 14, 'A', TRUE, 5),
(4, 4, 15, 'T', FALSE, 0), -- 答错
(4, 4, 16, 'C', TRUE, 5),
(4, 4, 17, 'C', TRUE, 5),
(4, 4, 18, 'T', TRUE, 5),
(4, 4, 19, 'D', TRUE, 5),
(4, 4, 20, 'T', TRUE, 5),
(4, 4, 21, 'A', TRUE, 5),
(4, 4, 22, 'B', FALSE, 0), -- 答错
(4, 4, 23, 'T', TRUE, 5),
(4, 4, 24, 'B', TRUE, 5),
(4, 4, 25, 'F', TRUE, 5),
(4, 4, 26, 'A', TRUE, 5),
-- 学生5 (s_chen) 的作答记录
(4, 5, 7, 'D', TRUE, 5),
(4, 5, 8, 'T', TRUE, 5),
(4, 5, 9, 'A', TRUE, 5),
(4, 5, 10, 'T', TRUE, 5),
(4, 5, 11, 'A', TRUE, 5),
(4, 5, 12, 'T', TRUE, 5),
(4, 5, 13, 'B', TRUE, 5),
(4, 5, 14, 'A', TRUE, 5),
(4, 5, 15, 'F', TRUE, 5),
(4, 5, 16, 'C', TRUE, 5),
(4, 5, 17, 'C', TRUE, 5),
(4, 5, 18, 'T', TRUE, 5),
(4, 5, 19, 'D', TRUE, 5),
(4, 5, 20, 'F', FALSE, 0), -- 答错
(4, 5, 21, 'A', TRUE, 5),
(4, 5, 22, 'A', TRUE, 5),
(4, 5, 23, 'T', TRUE, 5),
(4, 5, 24, 'B', TRUE, 5),
(4, 5, 25, 'F', TRUE, 5),
(4, 5, 26, 'C', FALSE, 0), -- 答错
-- 为 test_id=5 (操作系统第三章测验) 添加作答记录
-- 学生6 (s_liu) 的作答记录
(5, 6, 27, 'A', TRUE, 10),
(5, 6, 28, 'T', TRUE, 10),
(5, 6, 29, 'C', FALSE, 0), -- 答错
(5, 6, 30, 'T', TRUE, 10),
(5, 6, 31, 'D', TRUE, 10),
-- 为 test_id=6 (软件测试第一单元测验) 添加作答记录
(6, 4, 32, 'B', TRUE, 5),
(6, 4, 33, 'F', TRUE, 5),
(6, 4, 34, 'A', FALSE, 0),
(6, 6, 32, 'B', TRUE, 5),
(6, 6, 33, 'T', FALSE, 0),
(6, 6, 34, 'B', TRUE, 5),
-- 为 test_id=7 (网络安全入门测试) 添加作答记录
(7, 5, 35, 'D', TRUE, 10),
(7, 5, 36, 'T', TRUE, 10);

-- 作业数据
INSERT INTO Homework (course_id, title, description, deadline, weight, requirements) VALUES
(1, '第一次作业：变量与表达式', '掌握变量定义和基本运算', DATE_ADD(NOW(), INTERVAL 5 DAY), 0.1, '提交PDF文件'),
(2, '第二次作业：链表实现', '实现链表的基本功能', DATE_ADD(NOW(), INTERVAL 10 DAY), 0.2, '附加代码说明'),
(6, '第一次作业：数据库设计', '设计一个简单的图书管理系统数据库', DATE_ADD(NOW(), INTERVAL 14 DAY), 0.3, '提交E-R图和SQL脚本');

-- 作业提交记录
INSERT INTO homework_submission (homework_id, student_id, file_name, file_url) VALUES
(1, 4, '变量表达式作业.pdf', '/uploads/hw1_student4.pdf'),
(2, 5, '链表实现作业.zip', '/uploads/hw2_student5.zip'),
(1, 6, '作业一.pdf', '/uploads/hw1_student6.pdf');
