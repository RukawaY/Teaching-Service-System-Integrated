package com.Main.RowMapper.course_selection.impl;

import com.Main.RowMapper.course_selection.CourseSelectionDao;
import com.Main.entity.course_selection.CourseSelection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class CourseSelectionDaoImpl implements CourseSelectionDao {

    @Autowired private JdbcTemplate jdbcTemplate;

    @Override
    public boolean isStudentSelectedCourse(Integer studentId, Integer sectionId) {
        String sql = "SELECT COUNT(1) FROM course_selection WHERE student_id = ? AND section_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, studentId, sectionId);
        return count != null && count > 0;
    }

    @Override
    public boolean saveSelection(Integer studentId, Integer sectionId) {
        String sql = "INSERT INTO course_selection (student_id, section_id, select_time) VALUES (?, ?, NOW())";
        return jdbcTemplate.update(sql, studentId, sectionId) > 0;
    }

    @Override
    public boolean existsByStudentIdAndSectionId(Integer studentId, Integer sectionId) {
        return isStudentSelectedCourse(studentId, sectionId);
    }

    @Override
    public boolean insertSelection(Integer studentId, Integer sectionId) {
        return saveSelection(studentId, sectionId);
    }

    @Override
    public List<CourseSelection> findByStudentId(Integer studentId) {
        String sql = "SELECT * FROM course_selection WHERE student_id = ?";
        return jdbcTemplate.query(sql, new CourseSelectionRowMapper(), studentId);
    }

    @Override
    public List<CourseSelection> findBySectionId(Integer sectionId) {
        String sql = "SELECT * FROM course_selection WHERE section_id = ?";
        return jdbcTemplate.query(sql, new CourseSelectionRowMapper(), sectionId);
    }

    @Override
    public boolean deleteSelection(Integer studentId, Integer sectionId) {
        String sql = "DELETE FROM course_selection WHERE student_id = ? AND section_id = ?";
        return jdbcTemplate.update(sql, studentId, sectionId) > 0;
    }

    @Override
    public int countBySectionId(Integer sectionId) {
        String sql = "SELECT COUNT(1) FROM course_selection WHERE section_id = ?";
        return jdbcTemplate.queryForObject(sql, Integer.class, sectionId);
    }

    @Override
    public boolean hasStudentSelectedCourse(Integer studentId, Integer courseId) {
        String sql = "SELECT COUNT(1) FROM course_selection cs " +
                    "JOIN section s ON cs.section_id = s.section_id " +
                    "WHERE cs.student_id = ? AND s.course_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, studentId, courseId);
        return count != null && count > 0;
    }

    private static class CourseSelectionRowMapper implements RowMapper<CourseSelection> {
        @Override
        public CourseSelection mapRow(ResultSet rs, int rowNum) throws SQLException {
            CourseSelection selection = new CourseSelection();
            selection.setSelectionId(rs.getInt("id"));
            selection.setStudentId(rs.getInt("student_id"));
            selection.setSectionId(rs.getInt("section_id"));
            selection.setSelectionTime(rs.getTimestamp("select_time"));
            return selection;
        }
    }
}