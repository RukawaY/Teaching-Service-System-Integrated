package com.Main.RowMapper.rss;

import com.Main.entity.rss.HomeworkSubmission;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.lang.NonNull;

import java.sql.ResultSet;
import java.sql.SQLException;

public class HomeworkSubmissionRowMapper implements RowMapper<HomeworkSubmission> {

    @Override
    public HomeworkSubmission mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
        HomeworkSubmission sub = new HomeworkSubmission();
        sub.setSubmission_id(rs.getInt("submission_id"));
        sub.setHomework_id(rs.getInt("homework_id"));
        sub.setStudent_id(rs.getInt("student_id"));
        sub.setSubmit_time(rs.getTimestamp("submit_time"));
        sub.setFile_name(rs.getString("file_name"));
        sub.setFile_url(rs.getString("file_url"));
        sub.setScore(rs.getDouble("score"));
        sub.setComment(rs.getString("comment"));
        return sub;
    }
}