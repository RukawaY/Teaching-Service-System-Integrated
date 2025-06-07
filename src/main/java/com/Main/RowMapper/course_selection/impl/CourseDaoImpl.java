package com.Main.RowMapper.course_selection.impl;

import com.Main.RowMapper.course_selection.CourseDao;
import com.Main.entity.course_selection.Course;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Repository
public class CourseDaoImpl implements CourseDao {

    private static final Logger logger = LoggerFactory.getLogger(CourseDaoImpl.class);

    @Autowired private JdbcTemplate jdbcTemplate;

    @Override
    public Course findById(Integer courseId) {
        String sql = "SELECT * FROM course WHERE course_id = ?";
        try {
            logger.debug("Querying course with ID: {}", courseId);
            return jdbcTemplate.queryForObject(sql, this::mapRowToCourse, courseId);
        } catch (Exception e) {
            // 如果查询不到结果，返回null
            logger.error("Error finding course with ID {}: {}", courseId, e.getMessage());
            return null;
        }
    }

    @Override
    public boolean existsById(Integer courseId) {
        String sql = "SELECT COUNT(1) FROM course WHERE course_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, courseId);
        return count != null && count > 0;
    }

    @Override
    public List<Course> searchCourseTable(Integer courseId, String courseName, String category) {
        StringBuilder sql = new StringBuilder("SELECT * FROM course WHERE 1=1");
        List<Object> params = new ArrayList<>();
        
        // Add filters based on provided parameters
        if (courseId != null) {
            sql.append(" AND course_id = ?");
            params.add(courseId);
        }
        
        if (courseName != null && !courseName.trim().isEmpty()) {
            sql.append(" AND course_name LIKE ?");
            params.add("%" + courseName.trim() + "%");
        }
        
        if (category != null && !category.trim().isEmpty()) {
            sql.append(" AND category LIKE ?");
            params.add("%" + category.trim() + "%");
        }
        
        try {
            logger.debug("Executing course table search query: {} with params: {}", sql.toString(), params);
            return jdbcTemplate.query(sql.toString(), this::mapRowToCourseTable, params.toArray());
        } catch (Exception e) {
            logger.error("Error searching course table: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    private Course mapRowToCourse(ResultSet rs, int rowNum) throws SQLException {
        try {
            Course course = new Course();
            course.setCourseId(rs.getInt("course_id"));
            course.setCourseName(rs.getString("course_name"));
            course.setCourseDescription(rs.getString("course_description"));
            course.setTeacherId(rs.getInt("teacher_id"));
            course.setCredit(rs.getDouble("credit"));
            course.setCategory(rs.getString("category"));
            
            // 这些字段在新的数据库结构中已经移到section表了
            // 设置默认值以避免空指针异常
            course.setClassTime("N/A");
            course.setClassroom("N/A");
            course.setCapacity(0);
            course.setAvailableCapacity(0);
            
            logger.debug("Mapped course: id={}, name={}", course.getCourseId(), course.getCourseName());
            return course;
        } catch (SQLException e) {
            logger.error("Error mapping course row: {}", e.getMessage());
            throw e;
        }
    }
    
    private Course mapRowToCourseTable(ResultSet rs, int rowNum) throws SQLException {
        try {
            Course course = new Course();
            course.setCourseId(rs.getInt("course_id"));
            course.setCourseName(rs.getString("course_name"));
            course.setCourseDescription(rs.getString("course_description"));
            course.setTeacherId(rs.getInt("teacher_id"));
            course.setCredit(rs.getDouble("credit"));
            course.setCategory(rs.getString("category"));
            
            logger.debug("Mapped course table entry: id={}, name={}, category={}", 
                        course.getCourseId(), course.getCourseName(), course.getCategory());
            return course;
        } catch (SQLException e) {
            logger.error("Error mapping course table row: {}", e.getMessage());
            throw e;
        }
    }
}