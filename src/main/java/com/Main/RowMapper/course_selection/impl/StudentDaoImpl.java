package com.Main.RowMapper.course_selection.impl;

import com.Main.RowMapper.course_selection.StudentDao;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;


@Repository
public class StudentDaoImpl implements StudentDao {

    @Autowired private JdbcTemplate jdbcTemplate;

    @Override
    public boolean existsById(Integer studentId) {
        String sql = "SELECT COUNT(1) FROM user WHERE user_id = ? AND role = 's'";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, studentId);
        return count != null && count > 0;
    }
}