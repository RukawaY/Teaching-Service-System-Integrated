package com.Main.RowMapper.information;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.lang.NonNull;

import com.Main.entity.information.Course;

public class CourseRowMapper implements RowMapper<Course> {
    @Override
    public Course mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
        Course course = new Course();
        course.setId(rs.getInt("course_id"));
        course.setName(rs.getString("course_name"));
        course.setDescription(rs.getString("course_description"));
        course.setCategory(rs.getString("category"));
        course.setCredit(rs.getFloat("credit"));
        course.setTeacherId(rs.getInt("teacher_id"));
        course.setHours_per_week(rs.getInt("hours_per_week"));
        return course;
    }
}
