package com.Main.RowMapper.course_selection.impl;

import com.Main.RowMapper.course_selection.GradeBaseDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class GradeBaseDaoImpl implements GradeBaseDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public boolean insertInitialGrade(Integer studentId, Integer courseId, Integer sectionId) {
        // Check if grade record already exists
        if (existsGrade(studentId, courseId, sectionId)) {
            return true; // Already exists, consider as success
        }
        
        String sql = "INSERT INTO GradeBase (student_id, course_id, section_id, score, gpa, submit_status) " +
                    "VALUES (?, ?, ?, 0, 0.0, '0')";
        try {
            int rowsAffected = jdbcTemplate.update(sql, studentId, courseId, sectionId);
            return rowsAffected > 0;
        } catch (Exception e) {
            // Log error but don't fail the course selection process
            System.err.println("Failed to insert initial grade record: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean existsGrade(Integer studentId, Integer courseId, Integer sectionId) {
        String sql = "SELECT COUNT(1) FROM GradeBase WHERE student_id = ? AND course_id = ? AND section_id = ?";
        try {
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, studentId, courseId, sectionId);
            return count != null && count > 0;
        } catch (Exception e) {
            return false;
        }
    }
} 