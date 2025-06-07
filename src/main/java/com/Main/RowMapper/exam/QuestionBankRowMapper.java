package com.Main.RowMapper.exam;

import com.Main.entity.exam.QuestionBank;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.RowMapper;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class QuestionBankRowMapper implements RowMapper<QuestionBank> {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public QuestionBank mapRow(ResultSet rs, int rowNum) throws SQLException {
        QuestionBank question = new QuestionBank();
        question.setQuestionId(rs.getInt("question_id"));
        question.setCourseId(rs.getInt("course_id"));
        question.setChapterId(rs.getInt("chapter_id"));

        // 处理枚举类型
        String questionTypeStr = rs.getString("question_type");
        if (questionTypeStr != null) {
            question.setQuestionType(QuestionBank.QuestionType.valueOf(questionTypeStr));
        }

        question.setContent(rs.getString("content"));

        // 处理JSON格式的options
        try {
            String optionsJson = rs.getString("options");
            if (optionsJson != null && !optionsJson.isEmpty()) {
                List<String> options = objectMapper.readValue(optionsJson, new TypeReference<List<String>>() {});
                question.setOptions(options);
            }
        } catch (Exception e) {
            throw new SQLException("Failed to parse options JSON", e);
        }

        question.setAnswer(rs.getString("answer"));

        // 处理可能为NULL的数值字段
        question.setScore(rs.getInt("score"));
        if (rs.wasNull()) {
            question.setScore(null);
        }

        question.setDifficulty(rs.getInt("difficulty"));
        if (rs.wasNull()) {
            question.setDifficulty(null);
        }

        question.setCreatedAt(rs.getTimestamp("created_at"));

        return question;
    }
}