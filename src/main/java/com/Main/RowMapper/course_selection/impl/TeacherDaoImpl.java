package com.Main.RowMapper.course_selection.impl;

import com.Main.RowMapper.course_selection.TeacherDao;
import com.Main.entity.course_selection.Course;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class TeacherDaoImpl implements TeacherDao {

    private static final Logger logger = LoggerFactory.getLogger(TeacherDaoImpl.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Override
    public List<Course> findCoursesByTeacherId(Integer teacherId) {
        String sql = "SELECT * FROM course WHERE teacher_id = ?";
        logger.debug("Querying courses for teacher ID: {}", teacherId);
        return jdbcTemplate.query(sql, this::mapRowToCourse, teacherId);
    }
    
    @Override
    public boolean existsById(Integer teacherId) {
        String sql = "SELECT COUNT(1) FROM user WHERE user_id = ? AND role = 't'";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, teacherId);
        return count != null && count > 0;
    }
    
    private Course mapRowToCourse(ResultSet rs, int rowNum) throws SQLException {
        try {
            Course course = new Course();
            course.setCourseId(rs.getInt("course_id"));
            course.setCourseName(rs.getString("course_name"));
            course.setCourseDescription(rs.getString("course_description"));
            course.setTeacherId(rs.getInt("teacher_id"));
            course.setCredit(rs.getDouble("credit"));
            
            // 这些字段在course表中不存在，设置默认值避免空指针异常
            course.setClassTime("N/A");
            course.setClassroom("N/A");
            course.setCapacity(0);
            course.setAvailableCapacity(0);
            
            logger.debug("Mapped course: {}, ID: {}", course.getCourseName(), course.getCourseId());
            return course;
        } catch (SQLException e) {
            logger.error("Error mapping course: {}", e.getMessage());
            throw e;
        }
    }
}