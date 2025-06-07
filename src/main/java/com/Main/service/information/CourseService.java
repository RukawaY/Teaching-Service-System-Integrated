package com.Main.service.information;

import com.Main.RowMapper.information.CourseRowMapper;
import com.Main.RowMapper.information.UserRowMapper;
import com.Main.dto.information.PageResponseDTO;
import com.Main.dto.information.ReturnCourseDTO;
import com.Main.entity.information.Course;
import com.Main.entity.information.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@Component
public class CourseService {

    final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    JdbcTemplate jdbcTemplate;

    RowMapper<Course> courseRowMapper = new CourseRowMapper();
    RowMapper<User> userRowMapper = new UserRowMapper();

    /**
     * 获取课程列表（支持分页和筛选）
     * @param page 页码
     * @param size 每页数量
     * @param courseName 按课程名字模糊查询
     * @param teacherId 按教师ID查询
     * @param teacherName 按教师名字模糊筛选
     * @param category 按课程类别筛选
     * @return 分页课程列表
     */
    public PageResponseDTO<ReturnCourseDTO> getCourses(Integer page, Integer size, String courseName, Integer teacherId, String teacherName, String category) {
        logger.info("查询课程列表: page={}, size={}, courseName={}, teacherId={}, teacherName={}, category={}",
                page, size, courseName, teacherId, teacherName, category);

        // 规范化参数
        if (page == null || page < 1) page = 1;
        if (size == null || size < 1) size = 10;
        if (size > 100) size = 100; // 限制最大记录数

        // 构建基础SQL和参数
        StringBuilder countSql = new StringBuilder("SELECT COUNT(*) FROM Course c JOIN User u ON c.teacher_id = u.user_id WHERE 1=1");
        StringBuilder querySql = new StringBuilder("SELECT c.* FROM Course c JOIN User u ON c.teacher_id = u.user_id WHERE 1=1");
        List<Object> params = new ArrayList<>();

        // 添加筛选条件
        if (courseName != null && !courseName.isEmpty()) {
            countSql.append(" AND c.course_name LIKE ?");
            querySql.append(" AND c.course_name LIKE ?");
            params.add("%" + courseName + "%");
        }

        if (teacherId != null) {
            countSql.append(" AND c.teacher_id = ?");
            querySql.append(" AND c.teacher_id = ?");
            params.add(teacherId);
        }

        if (teacherName != null && !teacherName.isEmpty()) {
            countSql.append(" AND u.name LIKE ?");
            querySql.append(" AND u.name LIKE ?");
            params.add("%" + teacherName + "%");
        }

        if (category != null && !category.isEmpty()) {
            countSql.append(" AND c.category = ?");
            querySql.append(" AND c.category = ?");
            params.add(category);
        }

        // 添加排序和分页
        querySql.append(" ORDER BY c.course_id ASC LIMIT ? OFFSET ?");

        // 计算偏移量
        int offset = (page - 1) * size;

        // 添加分页参数
        List<Object> queryParams = new ArrayList<>(params);
        queryParams.add(size);
        queryParams.add(offset);

        // 查询总记录数
        int totalItems = jdbcTemplate.queryForObject(countSql.toString(), Integer.class, params.toArray());

        // 计算总页数
        int totalPages = (totalItems + size - 1) / size;

        // 查询当前页数据
        List<ReturnCourseDTO> courses = new ArrayList<>();
        if (totalItems > 0) {
            List<Course> courseList = jdbcTemplate.query(querySql.toString(), courseRowMapper, queryParams.toArray());

            // 将 Course 对象转换为 ReturnCourseDTO 对象
            for (Course course : courseList) {
                int teacher_id = course.getTeacherId();
                User teacher = jdbcTemplate.queryForObject("SELECT * FROM User WHERE user_id = ?", new UserRowMapper(), teacher_id);
                String name = teacher.getName();
                ReturnCourseDTO returnCourseDTO = new ReturnCourseDTO();
                returnCourseDTO.setCourseId(course.getId());
                returnCourseDTO.setCourse_name(course.getName());
                returnCourseDTO.setCourse_description(course.getDescription());
                returnCourseDTO.setTeacher_id(course.getTeacherId());
                returnCourseDTO.setTeacher_name(name);
                returnCourseDTO.setCredit(course.getCredit());
                returnCourseDTO.setCategory(course.getCategory());
                returnCourseDTO.setHoursPerWeek(course.getHours_per_week());
                courses.add(returnCourseDTO);
            }
        }

        // 构建分页响应
        return new PageResponseDTO<>(totalItems, totalPages, page, courses);
    }

    /**
     * 获取课程详细信息（支持分页和筛选）
     * @param courseId 课程信息
     * @return 课程详细信息
     */
    public ReturnCourseDTO getCourseById(int courseId) {
        logger.info("获取课程信息: courseId={}", courseId);

        try {
            String sql = "SELECT * FROM Course WHERE course_id = ?";
            Course course = jdbcTemplate.queryForObject(sql, new CourseRowMapper(), courseId);
            String userSql = "SELECT * FROM User WHERE user_id = ?";
            User teacher = jdbcTemplate.queryForObject(userSql, new UserRowMapper(), course.getTeacherId());
            String teacher_name = teacher.getName();
            ReturnCourseDTO returnCourseDTO = new ReturnCourseDTO();
            returnCourseDTO.setCourseId(course.getId());
            returnCourseDTO.setCourse_name(course.getName());
            returnCourseDTO.setCourse_description(course.getDescription());
            returnCourseDTO.setTeacher_id(course.getTeacherId());
            returnCourseDTO.setTeacher_name(teacher_name);
            returnCourseDTO.setCredit(course.getCredit());
            returnCourseDTO.setCategory(course.getCategory());
            returnCourseDTO.setHoursPerWeek(course.getHours_per_week());
            return returnCourseDTO;
        } catch (DataAccessException e) {
            logger.warn("课程获取失败: courseId={}, message={}", courseId, e.getMessage());
            throw new RuntimeException("用户不存在或查询失败");
        }
    }

    /**
     * 创建课程
     * @param courseName 按课程名字模糊查询
     * @param credit 学分
     * @param courseDescription 课程描述
     * @param category 按课程类别筛选
     * @param teacherId 按教师查询
     * @return 课程信息
     */
    public Course createCourse(String courseName, String courseDescription, float credit, String category, int teacherId,int hours_per_week) {
        Course course = new Course();
        course.setName(courseName);
        course.setDescription(courseDescription);
        course.setCredit(credit);
        course.setCategory(category);
        course.setTeacherId(teacherId);
        course.setHours_per_week(hours_per_week);

        KeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            if (1 != jdbcTemplate.update((conn) -> {
                var ps = conn.prepareStatement("INSERT INTO Course(course_name, course_description, credit, category, teacher_id, hours_per_week) VALUES(?, ?, ?, ?, ?, ?)",
                        Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, course.getName());
                ps.setString(2, course.getDescription());
                ps.setDouble(3, course.getCredit());
                ps.setString(4, course.getCategory());
                ps.setInt(5, course.getTeacherId());
                ps.setInt(6,course.getHours_per_week());
                return ps;
            }, keyHolder)) {
                throw new RuntimeException("sql语句插入失败.");
            }
        } catch (DataAccessException e) {
            logger.error("SQL Error: " + e.getMessage(), e);
            throw new RuntimeException("未知原因插入失败.", e);
        }

        course.setId(keyHolder.getKey().intValue());
        return course;
    }

    /**
     * 修改课程信息
     * @param courseId 课程ID
     * @param courseName 修改后课程名字
     * @param credit 修改后学分
     * @param courseDescription 修改后课程描述
     * @param category 修改后课程类别
     * @param teacherId 教师ID
     * @return 课程信息
     */
    public Course updateCourse(int courseId, String courseName, String courseDescription, double credit, String category, int teacherId, int hours_per_week) {
        // 检查课程是否存在
        String checkCourseSql = "SELECT COUNT(*) FROM Course WHERE course_id = ?";
        int courseCount = jdbcTemplate.queryForObject(checkCourseSql, new Object[]{courseId}, Integer.class);

        if (courseCount == 0) {
            logger.warn("Course {} 不存在.", courseId);
            throw new RuntimeException("课程不存在");
        }

        // 检查该课程是否属于指定的教师
        String checkTeacherSql = "SELECT COUNT(*) FROM Course WHERE course_id = ? AND teacher_id = ?";
        int teacherCount = jdbcTemplate.queryForObject(checkTeacherSql, new Object[]{courseId, teacherId}, Integer.class);

        if (teacherCount == 0) {
            logger.warn("Teacher {} 没有权限更新课程 {}.", teacherId, courseId);
            throw new RuntimeException("该教师没有权限更新此课程");
        }

        // 如果课程存在且教师有权限，动态构建更新 SQL
        StringBuilder sql = new StringBuilder("UPDATE Course SET ");
        List<Object> params = new ArrayList<>();

        if (courseName != null && !courseName.isEmpty()) {
            sql.append("course_name = ?, ");
            params.add(courseName);
        }
        if (courseDescription != null && !courseDescription.isEmpty()) {
            sql.append("course_description = ?, ");
            params.add(courseDescription);
        }
        if (credit > 0) {
            sql.append("credit = ?, ");
            params.add(credit);
        }
        if (category != null && !category.isEmpty()) {
            sql.append("category = ?, ");
            params.add(category);
        }
        if (hours_per_week > 0) {
            sql.append("hours_per_week = ?, ");
            params.add(hours_per_week);
        }

        // 移除最后多余的逗号和空格
        if (params.isEmpty()) {
            throw new RuntimeException("没有提供任何更新字段");
        }
        sql.setLength(sql.length() - 2);

        // 添加 WHERE 条件
        sql.append(" WHERE course_id = ? AND teacher_id = ?");
        params.add(courseId);
        params.add(teacherId);

        // 执行更新操作
        int rowsAffected = jdbcTemplate.update(sql.toString(), params.toArray());

        if (rowsAffected > 0) {
            logger.info("Course {} 成功更新.", courseId);
        } else {
            logger.warn("Course {} 更新失败.", courseId);
            throw new RuntimeException("更新课程失败：系统错误");
        }

        // 查询更新后的课程信息
        String getCourseSql = "SELECT * FROM Course WHERE course_id = ?";
        Course updatedCourse = jdbcTemplate.queryForObject(getCourseSql, new Object[]{courseId}, (rs, rowNum) -> {
            Course course = new Course();
            course.setId(rs.getInt("course_id"));
            course.setName(rs.getString("course_name"));
            course.setDescription(rs.getString("course_description"));
            course.setCredit(rs.getFloat("credit"));
            course.setCategory(rs.getString("category"));
            course.setTeacherId(rs.getInt("teacher_id"));
            course.setHours_per_week(rs.getInt("hours_per_week"));
            return course;
        });

        return updatedCourse;
    }


    /**
     * 删除课程
     * @param courseId 课程ID
     * @param teacherId 教师ID
     * @return 课程信息
     */
    public boolean deleteCourse(int courseId, int teacherId) {
        // 检查是否有开课信息或学生选课
        String sql = "SELECT COUNT(*) FROM Section WHERE course_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, new Object[]{courseId}, Integer.class);
        if ( count > 0) {
            logger.warn("无法删除课程 {} 因为有学生选课.", courseId);
            return false;
        }
        // 删除课程
        String sql_delete = "DELETE FROM Course WHERE course_id = ? AND teacher_id = ?";
        int rowsAffected = jdbcTemplate.update(sql_delete, courseId, teacherId);
        if (rowsAffected > 0) {
            logger.info("Course {} 成功删除.", courseId);
            return true;
        } else {
            logger.warn("Course {} 删除失败.", courseId);
            return false;
        }
    }
}
