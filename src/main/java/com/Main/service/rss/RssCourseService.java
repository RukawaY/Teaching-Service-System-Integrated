package com.Main.service.rss;

import com.Main.RowMapper.rss.CourseRowMapper;
import com.Main.entity.rss.Course;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RssCourseService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 获取所有课程
     * @return 课程列表
     */
    public List<Course> getAllCourses() {
        String sql = "SELECT * FROM course";
        return jdbcTemplate.query(sql, new CourseRowMapper());
    }

    /**
     * 根据学生ID获取选课列表
     * @param studentId 学生ID
     * @return 课程列表
     */
    public List<Course> getCourseListByStudentId(Integer studentId) {
        String sql = "SELECT c.* FROM course c " +
                "JOIN Section s ON c.course_id = s.course_id " +
                "JOIN course_selection sc ON s.section_id = sc.section_id " +
                "WHERE sc.student_id = ?";
        return jdbcTemplate.query(sql, new CourseRowMapper(), studentId);
    }
}
