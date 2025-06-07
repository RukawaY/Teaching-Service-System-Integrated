package com.Main.RowMapper.rss;

import com.Main.entity.rss.Homework;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.lang.NonNull;

import java.sql.ResultSet;
import java.sql.SQLException;

public class HomeworkRowMapper implements RowMapper<Homework> {

    @Override
    public Homework mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
        Homework hw = new Homework();
        hw.setHomework_id(rs.getInt("homework_id"));
        hw.setCourse_id(rs.getInt("course_id"));
        hw.setTitle(rs.getString("title"));
        hw.setDescription(rs.getString("description"));
        hw.setDeadline(rs.getTimestamp("deadline")); // 若Homework.deadline是Date类型可直接用
        hw.setWeight(rs.getDouble("weight"));
        hw.setRequirements(rs.getString("requirements"));
        return hw;
    }
}