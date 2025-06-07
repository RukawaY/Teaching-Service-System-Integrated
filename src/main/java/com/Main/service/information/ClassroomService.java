package com.Main.service.information;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class ClassroomService {

    final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    JdbcTemplate jdbcTemplate;
    /**
     * 获取教室信息
     * @param classroom_id 教室ID
     * @return 教室信息
     */
    public String getClassroomInfo(int classroom_id) {
        String sql = "SELECT * FROM classroom WHERE classroom_id = ?";
        return jdbcTemplate.queryForObject(sql, new Object[]{classroom_id}, String.class);
    }
}
