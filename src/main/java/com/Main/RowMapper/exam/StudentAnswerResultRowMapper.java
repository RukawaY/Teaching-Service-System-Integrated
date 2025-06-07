package com.Main.RowMapper.exam;

import com.Main.entity.exam.StudentAnswerResult;
import org.springframework.jdbc.core.RowMapper;
import java.sql.ResultSet;
import java.sql.SQLException;

public class StudentAnswerResultRowMapper implements RowMapper<StudentAnswerResult> {
    @Override
    public StudentAnswerResult mapRow(ResultSet rs, int rowNum) throws SQLException {
        StudentAnswerResult result = new StudentAnswerResult();
        result.setResultId(rs.getInt("result_id"));
        result.setTestId(rs.getInt("test_id"));
        result.setStudentId(rs.getInt("student_id"));
        result.setQuestionId(rs.getInt("question_id"));
        result.setStudentAnswer(rs.getString("student_answer"));

        // 处理可能为NULL的布尔值
        int isCorrectInt = rs.getInt("is_correct");
        if (!rs.wasNull()) {
            result.setIsCorrect(isCorrectInt == 1);
        }

        // 处理可能为NULL的数值
        result.setScoreObtained(rs.getInt("score_obtained"));
        if (rs.wasNull()) {
            result.setScoreObtained(null);
        }

        result.setAnswerTime(rs.getTimestamp("answer_time"));
        return result;
    }
}