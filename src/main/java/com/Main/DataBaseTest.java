package com.Main;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class DataBaseTest {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void init() {
        // 查看 users 表的数据
        // jdbcTemplate.queryForList("SELECT * FROM User")
        //        .forEach(row -> System.out.println(row));

    }
}
