package com.Main.RowMapper.course_selection.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.Main.RowMapper.course_selection.SelectionTimeDao;
import com.Main.entity.course_selection.SelectionTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class SelectionTimeDaoImpl implements SelectionTimeDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Override
    public SelectionTime getSelectionTime() {
        try {
            String sql = "SELECT * FROM selection_time WHERE id = 1";
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                SelectionTime selectionTime = new SelectionTime();
                selectionTime.setMaxNumber(rs.getInt("max_number"));
                
                try {
                    // 直接获取JSON字符串
                    String firstTimeListJson = rs.getString("first_time_list");
                    String secondTimeListJson = rs.getString("second_time_list");
                    String dropTimeListJson = rs.getString("drop_time_list");
                    
                    // 解析为简单的字符串列表
                    List<String> firstTimeList = objectMapper.readValue(
                            firstTimeListJson, 
                            new TypeReference<List<String>>() {});
                    
                    List<String> secondTimeList = objectMapper.readValue(
                            secondTimeListJson, 
                            new TypeReference<List<String>>() {});
                    
                    List<String> dropTimeList = objectMapper.readValue(
                            dropTimeListJson, 
                            new TypeReference<List<String>>() {});
                    
                    selectionTime.setFirstTimeList(firstTimeList);
                    selectionTime.setSecondTimeList(secondTimeList);
                    selectionTime.setDropTimeList(dropTimeList);
                } catch (Exception e) {
                    e.printStackTrace();
                    // 出现异常时，保留默认的空列表
                }
                
                return selectionTime;
            });
        } catch (Exception e) {
            e.printStackTrace();
            // 如果查询失败，返回默认的配置
            return new SelectionTime();
        }
    }
    
    @Override
    public boolean updateSelectionTime(SelectionTime selectionTime) {
        try {
            // 将时间列表转换为JSON
            String firstTimeListJson = objectMapper.writeValueAsString(selectionTime.getFirstTimeList());
            String secondTimeListJson = objectMapper.writeValueAsString(selectionTime.getSecondTimeList());
            String dropTimeListJson = objectMapper.writeValueAsString(selectionTime.getDropTimeList());
            
            // Check if record exists
            String checkSql = "SELECT COUNT(*) FROM selection_time WHERE id = 1";
            int count = jdbcTemplate.queryForObject(checkSql, Integer.class);
            
            int rowsAffected;
            if (count > 0) {
                // Update existing record
                String updateSql = "UPDATE selection_time SET max_number = ?, first_time_list = ?, second_time_list = ?, drop_time_list = ? WHERE id = 1";
                rowsAffected = jdbcTemplate.update(updateSql, 
                    selectionTime.getMaxNumber(), 
                    firstTimeListJson, 
                    secondTimeListJson, 
                    dropTimeListJson
                );
            } else {
                // Insert new record
                String insertSql = "INSERT INTO selection_time (id, max_number, first_time_list, second_time_list, drop_time_list) VALUES (1, ?, ?, ?, ?)";
                rowsAffected = jdbcTemplate.update(insertSql,
                    selectionTime.getMaxNumber(), 
                    firstTimeListJson, 
                    secondTimeListJson, 
                    dropTimeListJson
                );
            }
            
            return rowsAffected > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}