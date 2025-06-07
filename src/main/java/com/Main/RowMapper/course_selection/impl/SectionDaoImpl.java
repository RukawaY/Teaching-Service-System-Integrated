package com.Main.RowMapper.course_selection.impl;

import com.Main.RowMapper.course_selection.SectionDao;
import com.Main.entity.course_selection.Section;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class SectionDaoImpl implements SectionDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public Section findById(Integer sectionId) {
        String sql = "SELECT * FROM section WHERE section_id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, this::mapRowToSection, sectionId);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public boolean existsById(Integer sectionId) {
        String sql = "SELECT COUNT(1) FROM section WHERE section_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, sectionId);
        return count != null && count > 0;
    }

    @Override
    public boolean isFull(Integer sectionId) {
        // 直接检查 available_capacity 字段
        String sql = "SELECT available_capacity FROM section WHERE section_id = ?";
        Integer availableCapacity = jdbcTemplate.queryForObject(sql, Integer.class, sectionId);
        return availableCapacity != null && availableCapacity <= 0;
    }

    @Override
    public boolean decreaseAvailableCapacity(Integer sectionId) {
        // 减少开课可用容量
        String sql = "UPDATE section SET available_capacity = available_capacity - 1 WHERE section_id = ? AND available_capacity > 0";
        return jdbcTemplate.update(sql, sectionId) > 0;
    }

    @Override
    public boolean increaseAvailableCapacity(Integer sectionId) {
        // 增加开课可用容量
        String sql = "UPDATE section SET available_capacity = available_capacity + 1 WHERE section_id = ? AND available_capacity < capacity";
        return jdbcTemplate.update(sql, sectionId) > 0;
    }

    @Override
    public String getSectionTime(Integer sectionId) {
        String sql = "SELECT sec_time FROM section WHERE section_id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, String.class, sectionId);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public List<Section> findByCourseId(Integer courseId) {
        String sql = "SELECT * FROM section WHERE course_id = ?";
        return jdbcTemplate.query(sql, this::mapRowToSection, courseId);
    }
    
    private Section mapRowToSection(ResultSet rs, int rowNum) throws SQLException {
        Section section = new Section();
        section.setSectionId(rs.getInt("section_id"));
        section.setCourseId(rs.getInt("course_id"));
        section.setClassroomId(rs.getInt("classroom_id"));
        section.setCapacity(rs.getInt("capacity"));
        section.setAvailableCapacity(rs.getInt("available_capacity"));
        section.setSemester(rs.getString("semester"));
        section.setSecYear(rs.getInt("sec_year"));
        section.setSecTime(rs.getString("sec_time"));
        return section;
    }
} 