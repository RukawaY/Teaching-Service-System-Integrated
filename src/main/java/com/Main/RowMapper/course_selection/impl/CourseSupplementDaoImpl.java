package com.Main.RowMapper.course_selection.impl;

import com.Main.RowMapper.course_selection.CourseSupplementDao;
import com.Main.entity.course_selection.CourseSupplementApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class CourseSupplementDaoImpl implements CourseSupplementDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public boolean save(CourseSupplementApplication application) {
        String sql = "INSERT INTO course_supplement (student_id, section_id, apply_time, status) VALUES (?, ?, NOW(), 0)";
        return jdbcTemplate.update(sql, application.getStudentId(), application.getSectionId()) > 0;
    }

    @Override
    public List<CourseSupplementApplication> findByStudentId(String studentId) {
        String sql = "SELECT * FROM course_supplement WHERE student_id = ?";
        return jdbcTemplate.query(sql, this::mapRowToApplication, Integer.parseInt(studentId));
    }

    @Override
    public List<CourseSupplementApplication> findBySectionId(Integer sectionId) {
        String sql = "SELECT * FROM course_supplement WHERE section_id = ? AND status = 0";
        return jdbcTemplate.query(sql, this::mapRowToApplication, sectionId);
    }

    @Override
    public List<CourseSupplementApplication> findAll() {
        String sql = "SELECT * FROM course_supplement WHERE status = 0";
        return jdbcTemplate.query(sql, this::mapRowToApplication);
    }

    @Override
    public boolean updateStatus(Integer applicationId, Integer status) {
        String sql = "UPDATE course_supplement SET status = ? WHERE supplement_id = ?";
        return jdbcTemplate.update(sql, status, applicationId) > 0;
    }

    @Override
    public List<CourseSupplementApplication> getPendingSupplements() {
        String sql = "SELECT * FROM course_supplement WHERE status = 0";
        return jdbcTemplate.query(sql, this::mapRowToApplication);
    }
    
    @Override
    public boolean saveSupplementApplication(Integer studentId, Integer sectionId) {
        String sql = "INSERT INTO course_supplement (student_id, section_id, apply_time, status) VALUES (?, ?, NOW(), 0)";
        return jdbcTemplate.update(sql, studentId, sectionId) > 0;
    }

    private CourseSupplementApplication mapRowToApplication(ResultSet rs, int rowNum) throws SQLException {
        CourseSupplementApplication application = new CourseSupplementApplication();
        application.setSupplementId(rs.getInt("supplement_id"));
        application.setStudentId(rs.getInt("student_id"));
        application.setSectionId(rs.getInt("section_id"));
        application.setApplyTime(rs.getTimestamp("apply_time"));
        application.setStatus(rs.getInt("status"));
        return application;
    }

    @Override
    public CourseSupplementApplication findById(Integer supplementId) {
        String sql = "SELECT * FROM course_supplement WHERE supplement_id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, this::mapRowToApplication, supplementId);
        } catch (Exception e) {
            return null; // Return null if no record is found
        }
    }
}