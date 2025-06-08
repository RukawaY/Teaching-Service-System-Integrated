package com.Main.service.exam;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import com.Main.entity.exam.TestPublish;

@Service
public class TestPublishService {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Setter used for injecting JdbcTemplate in tests.
     */
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Create a new test_publish record.
     *
     * @param testPublish information for the new test
     * @return generated test_id
     */
    public int createTest(TestPublish testPublish) {
        String sql = "INSERT INTO TestPublish " +
                     "(teacher_id, course_id, test_name, publish_time, deadline, " +
                     "question_count, is_random, question_ids, ratio) " +
                     "VALUES (?,?,?,?,?,?,?,?,?)";

        KeyHolder holder = new GeneratedKeyHolder();

        jdbcTemplate.update(conn -> {
            var ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setObject(1, testPublish.getTeacherId());
            ps.setObject(2, testPublish.getCourseId());
            ps.setString(3, testPublish.getTestName());
            ps.setTimestamp(4, testPublish.getPublishTime() == null
                                   ? null
                                   : Timestamp.valueOf(testPublish.getPublishTime()));
            ps.setTimestamp(5, testPublish.getDeadline() == null
                                   ? null
                                   : Timestamp.valueOf(testPublish.getDeadline()));
            ps.setObject(6, testPublish.getQuestionCount());
            ps.setObject(7, testPublish.getRandom());
            try {
                ps.setString(8, testPublish.getQuestionIds() == null
                                   ? null
                                   : objectMapper.writeValueAsString(testPublish.getQuestionIds()));
            } catch (Exception e) {
                throw new RuntimeException("Failed to serialize question ids", e);
            }
            ps.setObject(9, testPublish.getRatio());
            return ps;
        }, holder);

        return holder.getKey().intValue();
    }

    /**
     * Mark a test as published by setting its publish time to the specified
     * start time. The value is written to the database as provided without any
     * time zone conversion.
     *
     * @param testId      id of the test to publish
     * @param publishTime start time of the test
     * @return number of affected rows
     */
    public int publishTest(int testId, LocalDateTime publishTime) {
        String sql = "UPDATE TestPublish SET publish_time = ? WHERE test_id = ?";
        Timestamp ts = publishTime == null ? null : Timestamp.valueOf(publishTime);
        return jdbcTemplate.update(sql, ts, testId);
    }

    public List<Map<String, Object>> getTestByCourseId(int courseId) {
        String sql = "SELECT test_id, test_name, teacher_id, course_id, " +
                     "DATE_FORMAT(publish_time, '%Y-%m-%d %H:%i:%s') AS publish_time, " +
                     "DATE_FORMAT(deadline, '%Y-%m-%d %H:%i:%s') AS deadline, " +
                     "question_count, is_random, ratio " +
                     "FROM TestPublish WHERE course_id = ? " +
                     "AND (deadline <= NOW() OR deadline IS NULL)";
        return jdbcTemplate.queryForList(sql, courseId);
    }

    public List<Map<String, Object>> getQuestionsByTestId(int testId, boolean isTeacher) {
        String checkTestSql = "SELECT question_ids FROM TestPublish WHERE test_id = ?";
        List<Map<String, Object>> testInfo = jdbcTemplate.queryForList(checkTestSql, testId);

        if (testInfo.isEmpty()) {
            System.out.println("Test with ID " + testId + " does not exist");
            return List.of();
        }

        String questionIds = (String) testInfo.get(0).get("question_ids");
        String cleanIds    = questionIds.replace("[", "").replace("]", "");

        String[] ids = cleanIds.split(",");
        List<String> existingIds = new ArrayList<>();
        for (String id : ids) {
            String chk = "SELECT COUNT(*) FROM QuestionBank WHERE question_id = ?";
            int count  = jdbcTemplate.queryForObject(chk, Integer.class, id.trim());
            if (count > 0) existingIds.add(id.trim());
        }

        if (existingIds.isEmpty()) return List.of();

        String existingIdsStr = String.join(",", existingIds);

        if (isTeacher) {
            String sql = "SELECT q.* FROM QuestionBank q " +
                         "WHERE FIND_IN_SET(q.question_id, ?) > 0";
            return jdbcTemplate.queryForList(sql, existingIdsStr);
        } else {
            String sql = "SELECT q.question_id, q.question_type, q.content, q.options, " +
                         "q.score, q.difficulty " +
                         "FROM QuestionBank q " +
                         "WHERE FIND_IN_SET(q.question_id, ?) > 0";
            return jdbcTemplate.queryForList(sql, existingIdsStr);
        }
    }

    public List<Map<String, Object>> getQuestionsByTestIdIfAnswered(int testId, int studentId) {
        String chkAns = "SELECT COUNT(*) FROM StudentAnswerResult " +
                        "WHERE test_id = ? AND student_id = ?";
        int answered  = jdbcTemplate.queryForObject(chkAns, Integer.class, testId, studentId);
        if (answered == 0) return List.of();

        String idsSql = "SELECT question_ids FROM TestPublish WHERE test_id = ?";
        String qIds   = (String) jdbcTemplate.queryForMap(idsSql, testId).get("question_ids");
        String clean  = qIds.replace("[", "").replace("]", "");

        String[] ids  = clean.split(",");
        List<String> present = new ArrayList<>();
        for (String id : ids) {
            String chk = "SELECT COUNT(*) FROM QuestionBank WHERE question_id = ?";
            int cnt    = jdbcTemplate.queryForObject(chk, Integer.class, id.trim());
            if (cnt > 0) present.add(id.trim());
        }
        if (present.isEmpty()) return List.of();

        String presentIds = String.join(",", present);

        String sql = "SELECT q.*, sar.student_answer, sar.is_correct, sar.score_obtained " +
                     "FROM QuestionBank q " +
                     "JOIN StudentAnswerResult sar ON q.question_id = sar.question_id " +
                     "AND sar.test_id = ? AND sar.student_id = ? " +
                     "WHERE FIND_IN_SET(q.question_id, ?) > 0";
        return jdbcTemplate.queryForList(sql, testId, studentId, presentIds);
    }

    public List<Map<String, Object>> getScoresByTestId(int testId) {
        String chk = "SELECT COUNT(*) FROM TestPublish WHERE test_id = ?";
        if (jdbcTemplate.queryForObject(chk, Integer.class, testId) == 0) return List.of();

        String sql = "SELECT sar.student_id, u.name AS student_name, " +
                     "SUM(sar.score_obtained) AS total_score, " +
                     "COUNT(sar.question_id) AS answered_questions, " +
                     "MAX(sar.answer_time) AS last_answer_time " +
                     "FROM StudentAnswerResult sar " +
                     "JOIN user u ON sar.student_id = u.user_id " +
                     "WHERE sar.test_id = ? " +
                     "GROUP BY sar.student_id, u.name " +
                     "ORDER BY total_score DESC";
        return jdbcTemplate.queryForList(sql, testId);
    }

    public List<Map<String, Object>> getTestForStudent(int studentId, int courseId) {
        String selChk = "SELECT COUNT(*) FROM course_selection sc " +
                        "INNER JOIN section s ON sc.section_id = s.section_id " +
                        "WHERE sc.student_id = ? AND s.course_id = ?";
        int selCnt    = jdbcTemplate.queryForObject(selChk, Integer.class, studentId, courseId);
        System.out.println("选课记录数量 : " + selCnt);

        String testChk = "SELECT COUNT(*) FROM TestPublish WHERE course_id = ?";
        int testCnt    = jdbcTemplate.queryForObject(testChk, Integer.class, courseId);
        System.out.println("课程测试数量 : " + testCnt);

        String validChk = "SELECT COUNT(*) FROM TestPublish WHERE course_id = ? " +
                          "AND publish_time <= NOW() " +
                          "AND (deadline IS NULL OR deadline >= NOW())";
        int validCnt    = jdbcTemplate.queryForObject(validChk, Integer.class, courseId);
        System.out.println("有效测试数量 : " + validCnt);

        String sql = "SELECT * FROM ( " +
                     " SELECT DISTINCT tp.test_id, tp.test_name, " +
                     "  DATE_FORMAT(tp.publish_time, '%Y-%m-%d %H:%i:%s') AS publish_time, " +
                     "  DATE_FORMAT(tp.deadline, '%Y-%m-%d %H:%i:%s') AS deadline, " +
                     "  u.name AS teacher_name, c.course_name, " +
                     "  DATE_FORMAT(tp.publish_time, '%Y-%m-%d %H:%i:%s') AS sort_time " +
                     " FROM TestPublish tp " +
                     " LEFT JOIN user u ON tp.teacher_id = u.user_id " +
                     " LEFT JOIN course c ON tp.course_id = c.course_id " +
                     " INNER JOIN section s ON tp.course_id = s.course_id " +
                     " INNER JOIN course_selection sc ON s.section_id = sc.section_id " +
                     " WHERE sc.student_id = ? AND tp.course_id = ? " +
                     " AND tp.publish_time <= NOW() " +
                     " AND (tp.deadline IS NULL OR tp.deadline >= NOW()) " +
                     ") AS sorted_tests ORDER BY sort_time DESC";

        List<Map<String, Object>> res =
                jdbcTemplate.queryForList(sql, studentId, courseId);
        System.out.println("最终查询结果数量 : " + res.size());
        return res;
    }

    public List<Map<String, Object>> getStudentCourses(int studentId) {
        String sql = "SELECT DISTINCT c.course_id, c.course_name " +
                     "FROM course_selection sc " +
                     "INNER JOIN section s ON sc.section_id = s.section_id " +
                     "INNER JOIN course c ON s.course_id = c.course_id " +
                     "WHERE sc.student_id = ? " +
                     "ORDER BY c.course_id";
        return jdbcTemplate.queryForList(sql, studentId);
    }

    /**
     * 根据测试 ID 获取测试信息
     *
     * @param testId 测试 ID
     * @return 测试信息
     */
    public List<Map<String, Object>> getTestByTestId(int testId) {
        String sql = "SELECT test_id, test_name, teacher_id, course_id, " +
                     "DATE_FORMAT(publish_time, '%Y-%m-%d %H:%i:%s') AS publish_time, " +
                     "DATE_FORMAT(deadline, '%Y-%m-%d %H:%i:%s') AS deadline, " +
                     "question_count, is_random, question_ids, ratio " +
                     "FROM TestPublish WHERE test_id = ?";
        return jdbcTemplate.queryForList(sql, testId);
    }
}
