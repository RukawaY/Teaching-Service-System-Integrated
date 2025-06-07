package com.Main.RowMapper.exam;
import com.Main.entity.exam.TestPublish;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.RowMapper;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;


public class TestPublishRowMapper implements RowMapper<TestPublish> {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public TestPublish mapRow(ResultSet rs, int rowNum) throws SQLException {
        TestPublish testPublish = new TestPublish();
        testPublish.setTestId(rs.getInt("test_id"));
        testPublish.setTeacherId(rs.getInt("teacher_id"));
        testPublish.setCourseId(rs.getInt("course_id"));
        testPublish.setPublishTime(rs.getObject("publish_time", LocalDateTime.class));
        testPublish.setDeadline(rs.getObject("deadline", LocalDateTime.class));
        testPublish.setQuestionCount(rs.getInt("question_count"));
        testPublish.setRandom(rs.getBoolean("is_random"));
        testPublish.setRatio(rs.getInt("ratio"));

        // 处理JSON格式的questionIds
        try {
            String questionIdsJson = rs.getString("question_ids");
            if (questionIdsJson != null && !questionIdsJson.isEmpty()) {
                List<Integer> questionIds = objectMapper.readValue(questionIdsJson, new TypeReference<List<Integer>>() {});
                testPublish.setQuestionIds(questionIds);
            }
        } catch (Exception e) {
            throw new SQLException("Failed to parse question_ids JSON", e);
        }

        return testPublish;
    }
}