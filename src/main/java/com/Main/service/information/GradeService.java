package com.Main.service.information;

import com.Main.RowMapper.information.*;
import com.Main.RowMapper.information.CourseRowMapper;
import com.Main.RowMapper.information.GradeBaseRowMapper;
import com.Main.RowMapper.information.GradeComponentRowMapper;
import com.Main.RowMapper.information.SectionRowMapper;
import com.Main.RowMapper.information.UserRowMapper;
import com.Main.dto.information.*;
import com.Main.dto.information.GradeDTO;
import com.Main.dto.information.GradeDistributionDTO;
import com.Main.dto.information.GradeDistributionSegmentDTO;
import com.Main.dto.information.PerformanceTrendDTO;
import com.Main.dto.information.ScoreRankingDTO;
import com.Main.dto.information.SectionAnalyseDTO;
import com.Main.dto.information.SectionGradeDTO;
import com.Main.dto.information.StudentAnalyseDTO;
import com.Main.dto.information.StudentGradeDTO;
import com.Main.entity.information.Course;
import com.Main.entity.information.GradeBase;
import com.Main.entity.information.GradeComponent;
import com.Main.entity.information.Section;
import com.Main.entity.information.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class GradeService{

    final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    JdbcTemplate jdbcTemplate;

    GradeBaseRowMapper gradeBaseRowMapper = new GradeBaseRowMapper();
    GradeComponentRowMapper gradeComponentRowMapper = new GradeComponentRowMapper();
    SectionRowMapper sectionRowMapper = new SectionRowMapper();
    UserRowMapper userRowMapper = new UserRowMapper();


    /**
     * 学生查询成绩列表
     * @param studentId 学生ID
     * @param semester 学期
     * @param secYear 学年
     * @param courseName 课程名字
     * @return StudentGradeListDTO
     */
    public List<StudentGradeDTO> getStudentGradeList(int studentId, String semester, int secYear, String courseName) {
        List<Object> params = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT gb.* FROM GradeBase gb WHERE gb.student_id = ? AND gb.submit_status = '1' ");
        params.add(studentId);

        if (semester != null && !semester.isEmpty()) {
            sql.append(" AND EXISTS (SELECT 1 FROM Section s WHERE s.section_id = gb.section_id AND s.semester = ?)");
            params.add(semester);
        }

        if (secYear > 0) {
            sql.append(" AND EXISTS (SELECT 1 FROM Section s WHERE s.section_id = gb.section_id AND s.sec_year = ?)");
            params.add(secYear);
        }

        if (courseName != null && !courseName.isEmpty()) {
            sql.append(" AND EXISTS (SELECT 1 FROM Course c WHERE gb.course_id = c.course_id AND c.course_name LIKE ?)");
            params.add("%" + courseName + "%");
        }

        logger.info("SQL: {}, Params: {}", sql.toString(), params);

        List<GradeBase> gradeBases = jdbcTemplate.query(sql.toString(), gradeBaseRowMapper, params.toArray());

        if (gradeBases.isEmpty()) {
            logger.info("没找到学生的成绩"); // 没有找到成绩，返回空列表
        }

        List<StudentGradeDTO> studentGradeDTOList = new ArrayList<>();
        for (GradeBase gradeBase : gradeBases) {
            StudentGradeDTO studentGradeDTO = new StudentGradeDTO();
            studentGradeDTO.setGrade_id(gradeBase.getGradeId());
            studentGradeDTO.setCourse_id(gradeBase.getCourseId());
            studentGradeDTO.setSection_id(gradeBase.getSectionId());
            studentGradeDTO.setScore(gradeBase.getScore());
            studentGradeDTO.setGpa(gradeBase.getGpa());
            studentGradeDTO.setSubmit_status(gradeBase.getSubmitStatus());

            // 查询Grade Components
            String componentSql = "SELECT * FROM GradeComponent WHERE grade_id = ?";
            List<GradeComponent> components;
            try {
                components = jdbcTemplate.query(componentSql, gradeComponentRowMapper, gradeBase.getGradeId()); // 直接传入gradeId
            } catch (DataAccessException e) {
                logger.warn("No GradeComponent found for gradeId: {}", gradeBase.getGradeId());
                components = Collections.emptyList(); // 不抛出异常，允许成绩没有组件
            }
            studentGradeDTO.setGradeComponents(components);

            studentGradeDTOList.add(studentGradeDTO);
        }

        return studentGradeDTOList;
    }


    /**
     * 学生查询成绩详情
     * @param studentId 学生ID
     * @param gradeId 成绩ID
     * @return StudentGradeDTO
     */
    public StudentGradeDTO getStudentGradeDetail(int studentId, int gradeId) {
        String sql = "SELECT * FROM GradeBase  WHERE student_id = ? AND grade_id = ?";
        try {
            // 查询 GradeBase
            GradeBase gradeBase = jdbcTemplate.queryForObject(sql, new Object[]{studentId, gradeId}, gradeBaseRowMapper);

            // 创建 StudentGradeDTO 并设置基本信息
            StudentGradeDTO studentGradeDTO = new StudentGradeDTO();
            studentGradeDTO.setGrade_id(gradeBase.getGradeId());
            studentGradeDTO.setCourse_id(gradeBase.getCourseId());
            studentGradeDTO.setSection_id(gradeBase.getSectionId());
            studentGradeDTO.setScore(gradeBase.getScore());
            studentGradeDTO.setGpa(gradeBase.getGpa());
            studentGradeDTO.setSubmit_status(gradeBase.getSubmitStatus());

            // 查询 GradeComponent 列表
            String componentSql = "SELECT * FROM GradeComponent WHERE grade_id = ?";
            List<GradeComponent> components;
            try {
                components = jdbcTemplate.query(componentSql, new Object[]{gradeId}, gradeComponentRowMapper);
            } catch (DataAccessException e) {
                logger.warn("找不到该gradeID的grade component");
                throw new DataAccessException("找不到该gradeID的grade component") {};
            }
            studentGradeDTO.setGradeComponents(components);

            return studentGradeDTO;
        } catch (DataAccessException e) {
            logger.warn("找不到Grade");
            throw new DataAccessException("找不到Grade") {};
        }
    }

    /**
     * 教师提交学生成绩
     * @param sectionId 班级ID
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @param score 成绩
     * @param gpa GPA
     * @return boolean
     */
    public boolean submitStudentGrades(int sectionId, int studentId, int courseId, int score, float gpa) {
        // 检查成绩是否已存在
        String checkSql = "SELECT COUNT(*) FROM GradeBase WHERE student_id = ? AND section_id = ? AND course_id = ?";
        Integer count = jdbcTemplate.queryForObject(checkSql, new Object[]{studentId, sectionId, courseId}, Integer.class);

        String sql;
        if (count != null && count > 0) {
            // 更新成绩
            sql = "UPDATE GradeBase SET score = ?, gpa = ?, submit_status = \"1\" WHERE student_id = ? AND section_id = ? AND course_id = ?";
        } else {
            // 插入新成绩
            sql = "INSERT INTO GradeBase (student_id, section_id, course_id, score, gpa, submit_status) " +
                    "VALUES (?, ?, ?, ?, ?, 1)";
        }

        try {
            int rowsAffected;
            if (count != null && count > 0) {
                rowsAffected = jdbcTemplate.update(sql, score, gpa, studentId, sectionId, courseId);
            } else {
                rowsAffected = jdbcTemplate.update(sql, studentId, sectionId, courseId, score, gpa);
            }
            if (rowsAffected <= 0) {
                logger.warn("Failed to update/insert grade for sectionId: {} and studentId: {}", sectionId, studentId);
                return false;
            }
        } catch (DataAccessException e) {
            logger.error("SQL Error while submitting grades for sectionId: {} and studentId: {}. Error: {}", sectionId, studentId, e.getMessage(), e);
            return false;
        }
        return true;
    }

    /**
     * 教师查询班级成绩
     * @param sectionId 班级ID
     * @param studentName 学生姓名
     * @param studentId 学生ID
     * @return SectionGradeDTO
     */
    public SectionGradeDTO getSectionStudentGrades(int sectionId, String studentName, int studentId) {
        SectionGradeDTO sectionGradeDTO = new SectionGradeDTO();
        List<User> userList = new ArrayList<>();
        List<GradeDTO> gradeList = new ArrayList<>();

        try {
            // 1. 首先获取该section下所有学生的成绩记录
            String gradeSql = "SELECT * FROM GradeBase WHERE section_id = ? ";
            List<Object> gradeParams = new ArrayList<>();
            gradeParams.add(sectionId);
            
            if (studentId > 0) {
                gradeSql += " AND student_id = ?";
                gradeParams.add(studentId);
            }
            
            List<GradeBase> allGrades = jdbcTemplate.query(gradeSql, gradeParams.toArray(), gradeBaseRowMapper);
            logger.info("查询到section_id={}的成绩记录数: {}", sectionId, allGrades.size());
            
            if (allGrades.isEmpty()) {
                logger.info("该section没有任何成绩记录");
                return sectionGradeDTO; // 返回空结果
            }
            
            // 2. 提取所有学生ID
            Set<Integer> studentIds = allGrades.stream()
                .map(GradeBase::getStudentId)
                .collect(Collectors.toSet());
            
            // 3. 查询这些学生的信息
            StringBuilder userSql = new StringBuilder("SELECT * FROM User WHERE role = 's' AND user_id IN (");
            for (int i = 0; i < studentIds.size(); i++) {
                userSql.append(i == 0 ? "?" : ", ?");
            }
            userSql.append(")");
            
            // 如果有姓名筛选，添加条件
            if (studentName != null && !studentName.isEmpty()) {
                userSql.append(" AND name LIKE ?");
            }
            
            List<Object> userParams = new ArrayList<>();
            studentIds.forEach(userParams::add);
            
            if (studentName != null && !studentName.isEmpty()) {
                userParams.add("%" + studentName + "%");
            }
            
            userList = jdbcTemplate.query(userSql.toString(), userParams.toArray(), userRowMapper);
            logger.info("查询到符合条件的学生数量: {}", userList.size());
            
            // 4. 为每个学生关联成绩记录
            for (User user : userList) {
                List<GradeBase> studentGrades = allGrades.stream()
                    .filter(grade -> grade.getStudentId() == user.getUser_id())
                    .collect(Collectors.toList());
                
                for (GradeBase gradeBase : studentGrades) {
                    GradeDTO gradeDTO = new GradeDTO();
                    gradeDTO.setGradeBase(gradeBase);
                    
                    // 查询成绩组件
                    if (gradeBase.getGradeId() > 0) {
                        try {
                            List<GradeComponent> components = jdbcTemplate.query(
                                "SELECT * FROM GradeComponent WHERE grade_id = ?", 
                                new Object[]{gradeBase.getGradeId()}, 
                                gradeComponentRowMapper
                            );
                            gradeDTO.setGradeComponent(components);
                        } catch (DataAccessException e) {
                            logger.warn("查询成绩组件失败: {}", e.getMessage());
                            gradeDTO.setGradeComponent(new ArrayList<>());
                        }
                    } else {
                        gradeDTO.setGradeComponent(new ArrayList<>());
                    }
                    
                    gradeList.add(gradeDTO);
                }
            }
            
            sectionGradeDTO.setUser(userList);
            sectionGradeDTO.setGrade(gradeList);
            
        } catch (Exception e) {
            logger.error("获取班级成绩失败: {}", e.getMessage(), e);
        }
        
        return sectionGradeDTO;
    }

    /**
     * 学生成绩分析
     * @param studentId 学生ID
     * @param startSecYear 开始学年
     * @param startSemester 开始学期
     * @param endSecYear 结束学年
     * @param endSemester 结束学期
     * @return StudentAnalyseDTO
     */
    public StudentAnalyseDTO getStudentGradeAnalysis(int studentId, int startSecYear, String startSemester, int endSecYear, String endSemester) {
        StudentAnalyseDTO analysisResult = new StudentAnalyseDTO();
        logger.info("studentId={}, 开始时间：{}年{}学期, 结束时间：{}年{}学期", 
                   studentId, startSecYear, startSemester, endSecYear, endSemester);
        
        // 学期顺序定义
        List<String> semesters = Arrays.asList("春夏", "秋冬");
        Map<String, Integer> semesterOrder = new HashMap<>();
        for (int i = 0; i < semesters.size(); i++) {
            semesterOrder.put(semesters.get(i), i);
        }

        // 获取学生所有成绩记录
        List<StudentGradeDTO> gradeListDTO = getStudentGradeList(studentId, null, 0, null);

        // 初始化总 GPA、总分数和课程计数
        double totalGpa = 0.0;
        double totalScore = 0.0;
        int totalCourses = 0;
        double totalCreditsEarned = 0.0;
        double totalCreditsTaken = 0.0;

        // 成绩分布列表
        List<GradeDistributionDTO> gradeDistribution = new ArrayList<>();

        // 学期和学年 GPA/分数 Map
        Map<String, PerformanceTrendDTO> semesterDataMap = new HashMap<>();
        // 学期课程数量 Map
        Map<String, Integer> semesterCourseCountMap = new HashMap<>();

        // 遍历成绩记录
        for (StudentGradeDTO studentGradeDTO : gradeListDTO) {
            Section section = jdbcTemplate.queryForObject("SELECT * FROM Section WHERE section_id = ?",sectionRowMapper,studentGradeDTO.getSection_id());
            int year = section.getSecYear();
            String semester = section.getSemester();
            logger.info("成绩学期：year={}, semester={}", year, semester);
            
            // 检查是否在范围内
            if (year > 0) {
                // 创建组合键（年份*100 + 学期索引），便于直接比较
                int currentTermValue = year * 100 + (semesterOrder.getOrDefault(semester, 0));
                int startTermValue = (startSecYear > 0) ? 
                    startSecYear * 100 + (semesterOrder.getOrDefault(startSemester, 0)) : 0;
                int endTermValue = (endSecYear > 0) ? 
                    endSecYear * 100 + (semesterOrder.getOrDefault(endSemester, 0)) : Integer.MAX_VALUE;
                
                logger.info("比较值：当前学期={}, 开始学期={}, 结束学期={}", 
                           currentTermValue, startTermValue, endTermValue);
                
                // 直接比较数值大小，简化逻辑
                if (currentTermValue < startTermValue || currentTermValue > endTermValue) {
                    logger.info("学期{}年{}不在范围内，跳过", year, semester);
                    continue; // 跳过范围外的学期
                }
            }
            
            // 累加 GPA 和分数
            totalGpa += studentGradeDTO.getGpa();
            totalScore += studentGradeDTO.getScore();
            totalCourses++;

            String sql = "SELECT * FROM Course WHERE course_id = ?";
            Course course = jdbcTemplate.queryForObject(sql, new CourseRowMapper(), studentGradeDTO.getCourse_id());

            // 创建成绩分布对象
            GradeDistributionDTO distribution = new GradeDistributionDTO();
            distribution.setCourse_name(course.getName());
            distribution.setScore(studentGradeDTO.getScore());
            distribution.setGpa(studentGradeDTO.getGpa());
            distribution.setCredit(course.getCredit());
            gradeDistribution.add(distribution);

            // 累加学分
            totalCreditsTaken += course.getCredit();
            if (studentGradeDTO.getSubmit_status() == 1) {
                totalCreditsEarned += course.getCredit();
            }

            // 构建学期和学年 Map
            String semesterKey = year + "-" + semester;
            PerformanceTrendDTO performanceTrendDTO = semesterDataMap.getOrDefault(semesterKey, new PerformanceTrendDTO());
            performanceTrendDTO.setSec_year(year);
            performanceTrendDTO.setSemester(semester);
            performanceTrendDTO.setGpa(performanceTrendDTO.getGpa() + studentGradeDTO.getGpa());
            performanceTrendDTO.setAverage_score(performanceTrendDTO.getAverage_score() + studentGradeDTO.getScore());
            semesterDataMap.put(semesterKey, performanceTrendDTO);

            // 增加课程计数
            semesterCourseCountMap.put(semesterKey, semesterCourseCountMap.getOrDefault(semesterKey, 0) + 1);
        }

        // 计算平均 GPA 和分数
        if (totalCourses > 0) {
            analysisResult.setOverall_gpa(totalGpa / totalCourses);
            analysisResult.setAverage_score(totalScore / totalCourses);
        } else {
            analysisResult.setOverall_gpa(0.0);
            analysisResult.setAverage_score(0.0);
        }

        // 设置总学分
        analysisResult.setTotal_credits_earned((int) totalCreditsEarned);
        analysisResult.setTotal_credits_taken((int) totalCreditsTaken);

        // 设置成绩分布
        analysisResult.setGrade_distribution_by_course(gradeDistribution);

        // 生成表现趋势
        List<PerformanceTrendDTO> performanceTrend = new ArrayList<>();
        for (Map.Entry<String, PerformanceTrendDTO> entry : semesterDataMap.entrySet()) {
            PerformanceTrendDTO trend = entry.getValue();
            int count = semesterCourseCountMap.get(entry.getKey()); // 使用该学期的课程数量
            if (count > 0) {
                trend.setGpa(trend.getGpa() / count);
                trend.setAverage_score(trend.getAverage_score() / count);
            }
            performanceTrend.add(trend);
        }

        // 按照学年和学期顺序排序
        performanceTrend.sort(Comparator.comparingInt(PerformanceTrendDTO::getSec_year)
                .thenComparing(trend -> semesterOrder.get(trend.getSemester())));

        analysisResult.setPerformance_trend(performanceTrend);

        return analysisResult;
    }

    /**
     * 教师查询开课班级成绩分析
     * @param sectionId 班级ID
     * @return SectionAnalyseDTO 班级成绩分析
     */
    public SectionAnalyseDTO getSectionGradeAnalysis(int sectionId) {
        SectionAnalyseDTO sectionAnalyseDTO = new SectionAnalyseDTO();

        Section section = jdbcTemplate.queryForObject("SELECT * FROM Section WHERE section_id = ?", new Object[]{sectionId}, sectionRowMapper);//根据 sectionId 获取所有 student_id
        sectionAnalyseDTO.setSection(section);

        String studentIdSql = "SELECT * FROM GradeBase WHERE section_id = ? AND SUBMIT_STATUS = '1' ";
        List<GradeBase> gradeBases = jdbcTemplate.query(studentIdSql, new Object[]{sectionId}, gradeBaseRowMapper);

        List<StudentGradeDTO> StudentGradeListDTO = new ArrayList<>();

        if (gradeBases.isEmpty()) {
            // 如果没有成绩记录，返回默认值
            return sectionAnalyseDTO;
        }

        List<Double> scores = new ArrayList<>();
        int passCount = 0;
        int excellentCount = 0;

        for (GradeBase gradebase : gradeBases) {
            double score = gradebase.getScore();
            scores.add(score);

            // 统计及格和优秀人数
            if (score >= 60) {
                passCount++;
            }
            if (score >= 90) {
                excellentCount++;
            }
        }

        // 计算平均分
        double averageScore = scores.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        sectionAnalyseDTO.setAverageScore(averageScore);

        // 计算最高分和最低分
        sectionAnalyseDTO.setHighestScore(Collections.max(scores));
        sectionAnalyseDTO.setLowestScore(Collections.min(scores));

        // 计算中位数
        Collections.sort(scores);
        double medianScore;
        int total_size = scores.size();
        if (total_size % 2 == 0) {
            medianScore = (scores.get(total_size / 2 - 1) + scores.get(total_size / 2)) / 2.0;
        } else {
            medianScore = scores.get(total_size / 2);
        }
        sectionAnalyseDTO.setMedianScore(medianScore);

        // 计算及格率和优秀率
        double passRate = (double) passCount / gradeBases.size() * 100;
        double excellentRate = (double) excellentCount / gradeBases.size() * 100;
        sectionAnalyseDTO.setPassRate(passRate);
        sectionAnalyseDTO.setExcellentRate(excellentRate);

        // 生成成绩分布
        List<GradeDistributionSegmentDTO> gradeDistributionSegments = new ArrayList<>();
        int[] ranges = {0, 60, 70, 80, 90, 101}; // 分数区间
        int size = scores.size();
        for (int i = 0; i < ranges.length - 1; i++) {
            int lowerBound = ranges[i];
            int upperBound = ranges[i + 1];
            int count = 0;
            // 统计该区间人数
            for (double score : scores) {
                if (score >= lowerBound && score < upperBound) {
                    count++;
                }
            }
            double percentage = (double) count / size * 100;
            GradeDistributionSegmentDTO segment = new GradeDistributionSegmentDTO();
            segment.setSegment(lowerBound + "-" + upperBound);
            segment.setCount(count);
            segment.setPercentage(percentage);
            gradeDistributionSegments.add(segment);
        }
        sectionAnalyseDTO.setGradeDistributionSegments(gradeDistributionSegments);

        // 生成学生排名
        List<ScoreRankingDTO> scoreRanking = new ArrayList<>();
        for (GradeBase gradebase : gradeBases) {
            ScoreRankingDTO rankingDTO = new ScoreRankingDTO();
            int studentid = gradebase.getStudentId();
            User user = jdbcTemplate.queryForObject("SELECT * FROM User WHERE user_id = ?", new Object[]{studentid}, userRowMapper);
            String studentname = user.getName();
            rankingDTO.setStudentId(studentid);
            rankingDTO.setScore(gradebase.getScore());
            rankingDTO.setStudentName(studentname);
            scoreRanking.add(rankingDTO);
        }

        // 排序学生排名
        scoreRanking.sort(Comparator.comparingDouble(ScoreRankingDTO::getScore).reversed());
        for (int i = 0; i < scoreRanking.size(); i++) {
            scoreRanking.get(i).setRank(i + 1);
        }
        sectionAnalyseDTO.setScoreRanking(scoreRanking);

        return sectionAnalyseDTO;
    }

    /**
     * 获取成绩详情
     * @param gradeId 成绩ID
     * @return GradeDTO
     */
    public GradeDTO getGradeDetail(int gradeId) {
        String sql = "SELECT * FROM GradeBase WHERE grade_id = ?";
        try {
            GradeBase gradeBase = jdbcTemplate.queryForObject(sql, new Object[]{gradeId}, gradeBaseRowMapper);
            GradeDTO gradeDTO = new GradeDTO();
            gradeDTO.setGradeBase(gradeBase);

            // 查询 GradeComponent 列表
            String componentSql = "SELECT * FROM GradeComponent WHERE grade_id = ?";
            List<GradeComponent> components;
            try {
                components = jdbcTemplate.query(componentSql, new Object[]{gradeId}, gradeComponentRowMapper);
            } catch (DataAccessException e) {
                logger.warn("No GradeComponent found for gradeId: {}", gradeId);
                components = new ArrayList<>();
            }
            gradeDTO.setGradeComponent(components);

            return gradeDTO;
        } catch (DataAccessException e) {
            logger.warn("找不到成绩");
            throw new DataAccessException("找不到成绩") {};
        }
    }
}
