package com.Main.service.rss;

import com.Main.RowMapper.rss.HomeworkSubmissionRowMapper;
import com.Main.entity.rss.HomeworkSubmission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;

@Service
public class HomeworkSubmissionService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

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

    public void grade(Integer submission_id, Double score, String comment) {
        String sql = "UPDATE homework_submission SET score = ?, comment = ? WHERE submission_id = ?";
        jdbcTemplate.update(sql, score, comment, submission_id);
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