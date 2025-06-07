package com.Main.service.exam;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import java.sql.Statement;
import java.util.Map;
import java.util.List;

@Service
public class StudentAnswerResultService {

   @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Setter used for injecting JdbcTemplate in tests.
     */
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Submit an answer for the given test question. If the student has already
     * answered this question for the test, the existing record will be updated.
     *
     * @param testId        test identifier
     * @param studentId     id of the student
     * @param questionId    id of the question
     * @param studentAnswer answer provided by the student
     * @return generated result_id when inserting or number of affected rows when updating
     */
    public int submitAnswer(int testId, int studentId, int questionId, String studentAnswer) {
        String queryQuestion = "SELECT answer, score FROM QuestionBank WHERE question_id = ?";
        Map<String, Object> question = jdbcTemplate.queryForMap(queryQuestion, questionId);
        String correctAnswer = (String) question.get("answer");
        Integer score = ((Number) question.get("score")).intValue();

        boolean isCorrect = studentAnswer != null && studentAnswer.equals(correctAnswer);
        int scoreObtained = isCorrect ? score : 0;

        String checkSql = "SELECT result_id FROM StudentAnswerResult " +
                          "WHERE test_id = ? AND student_id = ? AND question_id = ?";
        var existing = jdbcTemplate.query(checkSql,
                (rs, rowNum) -> rs.getInt("result_id"),
                testId, studentId, questionId);

        int resultId;
        if (!existing.isEmpty()) {
            String updateSql =
                "UPDATE StudentAnswerResult SET student_answer = ?, is_correct = ?, " +
                "score_obtained = ?, answer_time = NOW() " +
                "WHERE test_id = ? AND student_id = ? AND question_id = ?";
            jdbcTemplate.update(updateSql,
                    studentAnswer, isCorrect, scoreObtained,
                    testId, studentId, questionId);
            resultId = existing.get(0);
        } else {
            String insertSql = "INSERT INTO StudentAnswerResult " +
                               "(test_id, student_id, question_id, student_answer, " +
                               "is_correct, score_obtained, answer_time) " +
                               "VALUES (?,?,?,?,?,?,NOW())";
            KeyHolder holder = new GeneratedKeyHolder();
            jdbcTemplate.update(conn -> {
                var ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
                ps.setObject(1, testId);
                ps.setObject(2, studentId);
                ps.setObject(3, questionId);
                ps.setString(4, studentAnswer);
                ps.setObject(5, isCorrect);
                ps.setObject(6, scoreObtained);
                return ps;
            }, holder);
            resultId = holder.getKey().intValue();
        }
        
        // 更新成绩
        updateGrade(testId, studentId);
        
        return resultId;
    }
    
    /**
     * 更新学生考试相关成绩
     * 
     * @param testId 考试ID
     * @param studentId 学生ID
     */
    private void updateGrade(int testId, int studentId) {
        try {
            // 1. 获取考试信息
            String testSql = "SELECT tp.course_id, tp.test_name, tp.ratio FROM TestPublish tp WHERE tp.test_id = ?";
            Map<String, Object> testInfo = jdbcTemplate.queryForMap(testSql, testId);
            
            Integer courseId = ((Number) testInfo.get("course_id")).intValue();
            String testName = (String) testInfo.get("test_name");
            Integer ratio = testInfo.get("ratio") != null ? ((Number) testInfo.get("ratio")).intValue() : 0;
            
            // 如果ratio为0，不进行成绩更新
            if (ratio == 0) {
                return;
            }
            
            // 2. 获取学生在此考试中的总分和所有题目总分
            String scoreSql = "SELECT SUM(sar.score_obtained) AS obtained, " +
                              "(SELECT SUM(qb.score) FROM QuestionBank qb " +
                              " JOIN TestPublish tp ON JSON_CONTAINS(tp.question_ids, CAST(qb.question_id AS JSON)) " + 
                              " WHERE tp.test_id = ?) AS total " +
                              "FROM StudentAnswerResult sar " +
                              "WHERE sar.test_id = ? AND sar.student_id = ?";
            Map<String, Object> scoreInfo = jdbcTemplate.queryForMap(scoreSql, testId, testId, studentId);
            
            Integer obtained = scoreInfo.get("obtained") != null ? ((Number) scoreInfo.get("obtained")).intValue() : 0;
            Integer total = scoreInfo.get("total") != null ? ((Number) scoreInfo.get("total")).intValue() : 100; // 默认100分
            
            // 3. 计算考试得分占比
            double scoreRatio = (double) obtained / total;
            int finalScore = (int) Math.round(scoreRatio * ratio); // 取整
            
            // 4. 查找对应的section_id和grade_id
            String sectionSql = "SELECT s.section_id FROM Section s " + 
                               "JOIN course_selection cs ON s.section_id = cs.section_id " +
                               "WHERE s.course_id = ? AND cs.student_id = ? LIMIT 1";
            List<Map<String, Object>> sectionList = jdbcTemplate.queryForList(sectionSql, courseId, studentId);
            
            if (sectionList.isEmpty()) {
                // 如果没有找到选课记录，不更新成绩
                return;
            }
            
            Integer sectionId = ((Number) sectionList.get(0).get("section_id")).intValue();
            
            // 5. 检查GradeBase是否存在此记录
            String checkGradeSql = "SELECT grade_id, score FROM GradeBase WHERE student_id = ? AND course_id = ? AND section_id = ?";
            List<Map<String, Object>> gradeList = jdbcTemplate.queryForList(checkGradeSql, studentId, courseId, sectionId);
            
            Integer gradeId;
            Integer oldTotalScore = 0;
            
            if (gradeList.isEmpty()) {
                // 如果不存在，创建GradeBase记录
                String insertGradeSql = "INSERT INTO GradeBase (student_id, course_id, section_id, score, submit_status) VALUES (?, ?, ?, ?, '0')";
                KeyHolder gradeHolder = new GeneratedKeyHolder();
                jdbcTemplate.update(conn -> {
                    var ps = conn.prepareStatement(insertGradeSql, Statement.RETURN_GENERATED_KEYS);
                    ps.setInt(1, studentId);
                    ps.setInt(2, courseId); 
                    ps.setInt(3, sectionId);
                    ps.setInt(4, finalScore);
                    return ps;
                }, gradeHolder);
                gradeId = gradeHolder.getKey().intValue();
            } else {
                // 如果存在，更新GradeBase记录
                gradeId = ((Number) gradeList.get(0).get("grade_id")).intValue();
                oldTotalScore = gradeList.get(0).get("score") != null ? ((Number) gradeList.get(0).get("score")).intValue() : 0;
                
                // 检查是否已有相同测试的GradeComponent
                String checkComponentSql = "SELECT component_id, score FROM GradeComponent WHERE grade_id = ? AND component_name = ?";
                List<Map<String, Object>> componentList = jdbcTemplate.queryForList(checkComponentSql, gradeId, testName);
                
                if (!componentList.isEmpty()) {
                    // 如果已有相同测试的成绩组成，获取旧分数
                    Integer componentId = ((Number) componentList.get(0).get("component_id")).intValue();
                    Integer oldComponentScore = componentList.get(0).get("score") != null ? 
                                             ((Number) componentList.get(0).get("score")).intValue() : 0;
                    
                    // 更新总分：加上新组件分，减去旧组件分
                    int newTotalScore = oldTotalScore - oldComponentScore + finalScore;
                    
                    // 更新GradeBase
                    String updateGradeSql = "UPDATE GradeBase SET score = ? WHERE grade_id = ?";
                    jdbcTemplate.update(updateGradeSql, newTotalScore, gradeId);
                    
                    // 更新GradeComponent
                    String updateComponentSql = "UPDATE GradeComponent SET score = ? WHERE component_id = ?";
                    jdbcTemplate.update(updateComponentSql, finalScore, componentId);
                    
                    return;
                }
                
                // 更新GradeBase的总分
                String updateGradeSql = "UPDATE GradeBase SET score = score + ? WHERE grade_id = ?";
                jdbcTemplate.update(updateGradeSql, finalScore, gradeId);
            }
            
            // 6. 创建GradeComponent记录
            String insertComponentSql = "INSERT INTO GradeComponent (component_name, grade_id, component_type, ratio, score) VALUES (?, ?, '1', ?, ?)";
            jdbcTemplate.update(insertComponentSql, testName, gradeId, ratio, finalScore);
            
        } catch (Exception e) {
            // 记录错误但不中断流程，确保答案保存成功
            System.err.println("更新成绩时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
