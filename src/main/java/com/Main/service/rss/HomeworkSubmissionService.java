package com.Main.service.rss;

import com.Main.RowMapper.rss.HomeworkSubmissionRowMapper;
import com.Main.entity.rss.HomeworkSubmission;
import com.Main.entity.information.GradeComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;

@Service
public class HomeworkSubmissionService {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    private HomeworkService homeworkService;

    public void submit(HomeworkSubmission submission) {
        String sql = "INSERT INTO homework_submission(homework_id, student_id, submit_time, file_name, file_url) VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                submission.getHomework_id(),
                submission.getStudent_id(),
                submission.getSubmit_time() != null ? new Timestamp(submission.getSubmit_time().getTime()) : null,
                submission.getFile_name(),
                submission.getFile_url()
        );
    }

    public List<HomeworkSubmission> getSubmissions(Integer homework_id) {
        String sql = "SELECT * FROM homework_submission WHERE homework_id = ?";
        return jdbcTemplate.query(sql, new HomeworkSubmissionRowMapper(), homework_id);
    }

    /**
     * 评分并更新学生总成绩
     * @param submission_id 提交ID
     * @param score 作业得分
     * @param comment 评语
     */
    @Transactional
    public void grade(Integer submission_id, Double score, String comment) {
        // 更新作业提交记录的分数和评语
        String sql = "UPDATE homework_submission SET score = ?, comment = ? WHERE submission_id = ?";
        jdbcTemplate.update(sql, score, comment, submission_id);
        
        // 获取提交信息
        HomeworkSubmission submission = getById(submission_id);
        if (submission == null) {
            throw new RuntimeException("未找到作业提交记录");
        }
        
        // 获取作业信息，包括权重
        Integer homeworkId = submission.getHomework_id();
        Double weight = getHomeworkWeight(homeworkId);
        if (weight == null) {
            throw new RuntimeException("未找到作业权重信息");
        }
        
        // 获取课程ID
        Integer courseId = getCourseIdByHomeworkId(homeworkId);
        if (courseId == null) {
            throw new RuntimeException("未找到对应课程信息");
        }
        
        // 获取学生对应课程的成绩记录ID
        Integer studentId = submission.getStudent_id();
        Integer gradeId = getGradeIdByCourseAndStudent(courseId, studentId);
        if (gradeId == null) {
            throw new RuntimeException("未找到该学生对应课程的成绩记录");
        }
        
        // 更新GradeBase的总分
        updateGradeBaseScore(gradeId, score, weight);
        
        // 插入或更新GradeComponent记录
        insertOrUpdateGradeComponent(gradeId, homeworkId, score, weight);
    }
    
    /**
     * 获取作业权重
     */
    private Double getHomeworkWeight(Integer homeworkId) {
        try {
            String sql = "SELECT weight FROM Homework WHERE homework_id = ?";
            return jdbcTemplate.queryForObject(sql, Double.class, homeworkId);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 通过作业ID获取课程ID
     */
    private Integer getCourseIdByHomeworkId(Integer homeworkId) {
        try {
            String sql = "SELECT course_id FROM Homework WHERE homework_id = ?";
            return jdbcTemplate.queryForObject(sql, Integer.class, homeworkId);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 通过course_id和student_id获取grade_id
     */
    private Integer getGradeIdByCourseAndStudent(int courseId, int studentId) {
        String sql = "SELECT grade_id FROM GradeBase WHERE course_id = ? AND student_id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, Integer.class, courseId, studentId);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 更新GradeBase的score
     */
    private void updateGradeBaseScore(int gradeId, double score, double weight) {
        // 获取当前总得分
        String currentScoreSql = "SELECT score FROM GradeBase WHERE grade_id = ?";
        Integer currentScore = jdbcTemplate.queryForObject(currentScoreSql, Integer.class, gradeId);
        
        // 计算新增分数（作业得分 * 权重）
        int addedScore = (int) (score * weight);
        int newScore = currentScore + addedScore;
        
        // 更新总得分
        String updateSql = "UPDATE GradeBase SET score = ? WHERE grade_id = ?";
        jdbcTemplate.update(updateSql, newScore, gradeId);
    }
    
    /**
     * 插入或更新GradeComponent记录
     */
    private void insertOrUpdateGradeComponent(int gradeId, int homeworkId, double score, double weight) {
        // 获取作业标题
        String homeworkTitle = getHomeworkTitle(homeworkId);
        if (homeworkTitle == null) {
            homeworkTitle = "作业-" + homeworkId; // 获取失败时使用默认名称
        }
        
        // 检查是否已存在该作业的成绩组成
        String checkSql = "SELECT component_id FROM GradeComponent WHERE grade_id = ? AND component_name = ?";
        String componentName = homeworkTitle;
        
        List<Integer> componentIds = jdbcTemplate.queryForList(checkSql, Integer.class, gradeId, componentName);
        
        if (componentIds.isEmpty()) {
            // 不存在，插入新记录
            String insertSql = "INSERT INTO GradeComponent (grade_id, component_name, component_type, ratio, score) VALUES (?, ?, ?, ?, ?)";
            jdbcTemplate.update(insertSql,
                    gradeId,
                    componentName,
                    2, // 作业类型为2
                    (int)(weight * 100), // 权重转为百分比
                    (int)score);
        } else {
            // 已存在，更新记录
            String updateSql = "UPDATE GradeComponent SET score = ? WHERE component_id = ?";
            jdbcTemplate.update(updateSql, (int)score, componentIds.get(0));
        }
    }
    
    /**
     * 获取作业标题
     */
    private String getHomeworkTitle(int homeworkId) {
        try {
            String sql = "SELECT title FROM Homework WHERE homework_id = ?";
            return jdbcTemplate.queryForObject(sql, String.class, homeworkId);
        } catch (Exception e) {
            return null;
        }
    }

    public HomeworkSubmission getById(Integer submission_id) {
        String sql = "SELECT * FROM homework_submission WHERE submission_id = ?";
        List<HomeworkSubmission> list = jdbcTemplate.query(sql, new HomeworkSubmissionRowMapper(), submission_id);
        return list.isEmpty() ? null : list.get(0);
    }

    public HomeworkSubmission getByHomeworkIdAndStudentId(Integer homework_id, Integer student_id) {
        String sql = "SELECT * FROM homework_submission WHERE homework_id = ? AND student_id = ? LIMIT 1";
        List<HomeworkSubmission> list = jdbcTemplate.query(sql, new HomeworkSubmissionRowMapper(), homework_id, student_id);
        return list.isEmpty() ? null : list.get(0);
    }
}