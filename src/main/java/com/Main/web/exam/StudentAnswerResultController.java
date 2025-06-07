package com.Main.web.exam;

import com.Main.RowMapper.exam.StudentAnswerResultRowMapper;
import com.Main.entity.exam.StudentAnswerResult;
import com.Main.dto.exam.StudentAnswerDTO;
import com.Main.service.exam.StudentAnswerResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/test/answer")
public class StudentAnswerResultController {

    @Autowired
    private StudentAnswerResultService service;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /** Setter for injection in tests */
    public void setService(StudentAnswerResultService service) {
        this.service = service;
    }

    /** Setter for injection in tests */
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /** 单题提交 */
    @PostMapping("/submit")
    public StudentAnswerResult submit(@RequestBody StudentAnswerDTO dto) {
        service.submitAnswer(
                dto.getTestId(),
                dto.getStudentId(),
                dto.getQuestionId(),
                dto.getStudentAnswer());

        String sql = "SELECT * FROM StudentAnswerResult " +
                     "WHERE test_id = ? AND student_id = ? AND question_id = ?";
        return jdbcTemplate.queryForObject(
                sql,
                new StudentAnswerResultRowMapper(),
                dto.getTestId(),
                dto.getStudentId(),
                dto.getQuestionId());
    }

    /** 批量提交 */
    @PostMapping("/submitBatch")
    public List<StudentAnswerResult> submitBatch(@RequestBody List<StudentAnswerDTO> answers) {
        List<StudentAnswerResult> saved = new ArrayList<>();

        String sql = "SELECT * FROM StudentAnswerResult " +
                     "WHERE test_id = ? AND student_id = ? AND question_id = ?";

        for (StudentAnswerDTO ans : answers) {
            service.submitAnswer(
                    ans.getTestId(),
                    ans.getStudentId(),
                    ans.getQuestionId(),
                    ans.getStudentAnswer());

            StudentAnswerResult persisted = jdbcTemplate.queryForObject(
                    sql,
                    new StudentAnswerResultRowMapper(),
                    ans.getTestId(),
                    ans.getStudentId(),
                    ans.getQuestionId());

            saved.add(persisted);
        }
        return saved;
    }
}
