package com.Main.service.exam;

import com.Main.entity.exam.QuestionBank;
import com.Main.entity.information.Course;
import com.Main.RowMapper.exam.QuestionBankRowMapper;
import com.Main.RowMapper.information.CourseRowMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Timestamp;

@Service
public class QuestionBankService {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 添加题目
     * @param questionBank 题目对象，包含所有必要字段
     */
    public void add_question(QuestionBank questionBank) {
        String sql = "INSERT INTO QuestionBank (course_id, chapter_id, question_type, content, options, answer, score, difficulty, created_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try {
            String optionsJson = questionBank.getOptions() != null ? 
                objectMapper.writeValueAsString(questionBank.getOptions()) : null;
            
            jdbcTemplate.update(sql,
                questionBank.getCourseId(),
                questionBank.getChapterId(),
                questionBank.getQuestionType() != null ? questionBank.getQuestionType().name() : null,
                questionBank.getContent(),
                optionsJson,
                questionBank.getAnswer(),
                questionBank.getScore(),
                questionBank.getDifficulty(),
                new Timestamp(System.currentTimeMillis())
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to add question bank", e);
        }
    }

    /**
     * 更新题目
     * @param questionBank 题目对象，包含要更新的字段
     * @return 更新的记录数，通常为1表示更新成功，0表示未找到题目
     */
    public int update_question(QuestionBank questionBank) {
        String sql = "UPDATE QuestionBank SET " +
                     "course_id = ?, chapter_id = ?, question_type = ?, " +
                     "content = ?, options = ?, answer = ?, " +
                     "score = ?, difficulty = ? " +
                     "WHERE question_id = ?";
        
        try {
            String optionsJson = questionBank.getOptions() != null ? 
                objectMapper.writeValueAsString(questionBank.getOptions()) : null;
            
            return jdbcTemplate.update(sql,
                questionBank.getCourseId(),
                questionBank.getChapterId(),
                questionBank.getQuestionType() != null ? questionBank.getQuestionType().name() : null,
                questionBank.getContent(),
                optionsJson,
                questionBank.getAnswer(),
                questionBank.getScore(),
                questionBank.getDifficulty(),
                questionBank.getQuestionId() // WHERE条件
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to update question bank", e);
        }
    }

    /**
     * 删除题目
     * @param question_id 题目ID
     */
    public void del_question(int question_id) {
        String sql = "DELETE FROM QuestionBank WHERE question_id = ?";
        jdbcTemplate.update(sql, question_id);
    }

    /**
     * 获取教师的所有课程
     * @param teacherId 教师ID
     * @return 课程列表，包含course_id和course_name
     */
    public List<Map<String, Object>> get_course(int teacherId) {
        String sql = "SELECT course_id, course_name FROM course WHERE teacher_id = ?";
        return jdbcTemplate.queryForList(sql, teacherId);
    }

    /**
     * 获取指定课程下的题目
     * @param courseId 课程ID
     * @param chapterId 章节ID（可选，如果为0则表示查询所有章节）
     * @return 题目列表
     */
    public List<Map<String, Object>> get_question_by_course(int courseId, int chapterId) {
        StringBuilder sql = new StringBuilder("SELECT question_id, course_id, chapter_id, question_type, content, options, answer, score, difficulty, created_at FROM QuestionBank WHERE course_id = ?");
        
        if (chapterId > 0) {
            sql.append(" AND chapter_id = ?");
            return jdbcTemplate.queryForList(sql.toString(), courseId, chapterId);
        }
        return jdbcTemplate.queryForList(sql.toString(), courseId);
    }

    /**
     * 搜索题目
     * @param courseId 课程ID
     * @param chapterId 章节ID
     * @param content 题目内容（可选）
     * @param questionType 题目类型（可选）
     * @return 题目列表
     */
    public List<Map<String, Object>> search_que(int courseId, int chapterId, String content, String questionType) {
        StringBuilder sql = new StringBuilder("SELECT question_id, course_id, chapter_id, question_type, content, options, answer, score, difficulty, created_at FROM QuestionBank WHERE course_id = ? AND chapter_id = ?");
        
        if (content != null && !content.isEmpty()) {
            sql.append(" AND content LIKE ?");
        }
        if (questionType != null && !questionType.isEmpty()) {
            sql.append(" AND question_type = ?");
        }

        if (content != null && !content.isEmpty() && questionType != null && !questionType.isEmpty()) {
            return jdbcTemplate.queryForList(sql.toString(), courseId, chapterId, "%" + content + "%", questionType);
        } else if (content != null && !content.isEmpty()) {
            return jdbcTemplate.queryForList(sql.toString(), courseId, chapterId, "%" + content + "%");
        } else if (questionType != null && !questionType.isEmpty()) {
            return jdbcTemplate.queryForList(sql.toString(), courseId, chapterId, questionType);
        } else {
            return jdbcTemplate.queryForList(sql.toString(), courseId, chapterId);
        }
    }
}
