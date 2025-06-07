package com.Main.RowMapper.course_selection.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.Main.RowMapper.course_selection.CurriculumDao;
import com.Main.dto.course_selection.CurriculumDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CurriculumDaoImpl implements CurriculumDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    private ObjectMapper objectMapper; // 可以在Spring配置中添加这个Bean
    
    @Override
    public boolean checkMajorExists(String majorName) {
        String sql = "SELECT COUNT(1) FROM major WHERE major_name = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, majorName);
        return count != null && count > 0;
    }
    
    @Override
    public void deleteCurriculumByMajorId(Integer majorId) {
        String sql = "DELETE FROM curriculum WHERE major_id = ?";
        jdbcTemplate.update(sql, majorId);
    }

    @Override
    public void deleteCurriculumByMajorName(String majorName) {
        String findMajorIdSql = "SELECT major_id FROM major WHERE major_name = ?";
        Integer majorId = jdbcTemplate.queryForObject(findMajorIdSql, Integer.class, majorName);
        
        if (majorId != null) {
            String deleteSql = "DELETE FROM curriculum WHERE major_id = ?";
            jdbcTemplate.update(deleteSql, majorId);
        }
    }
    
    @Override
    public boolean saveCurriculum(String majorName, String curriculumJson) {
        // First find the major ID from the major name
        String findMajorIdSql = "SELECT major_id FROM major WHERE major_name = ?";
        Integer majorId = jdbcTemplate.queryForObject(findMajorIdSql, Integer.class, majorName);
        
        if (majorId != null) {
            String sql = "INSERT INTO curriculum (major_id, curriculum_json) VALUES (?, ?)";
            return jdbcTemplate.update(sql, majorId, curriculumJson) > 0;
        }
        return false;
    }
    
}