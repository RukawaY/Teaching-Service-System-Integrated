package com.Main.service.rss;

import com.Main.RowMapper.rss.HomeworkRowMapper;
import com.Main.entity.rss.Homework;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HomeworkService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 布置新作业
     * @param hw 作业对象
     */
    public void assignHomework(Homework hw) {
        String sql = "INSERT INTO homework (course_id, title, description, deadline, weight, requirements) VALUES (?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                hw.getCourse_id(),
                hw.getTitle(),
                hw.getDescription(),
                hw.getDeadline(),
                hw.getWeight(),
                hw.getRequirements());
    }

    /**
     * 获取某课程下的所有作业
     * @param course_id 课程ID
     * @return 作业列表
     */
    public List<Homework> getHomeworkList(Integer course_id) {
        String sql = "SELECT * FROM homework WHERE course_id = ?";
        return jdbcTemplate.query(sql, new HomeworkRowMapper(), course_id);
    }

    /**
     * 根据作业ID获取作业详情
     * @param id 作业ID
     * @return 作业对象
     */
    public Homework getById(Integer id) {
        String sql = "SELECT * FROM homework WHERE homework_id = ?";
        return jdbcTemplate.queryForObject(sql, new HomeworkRowMapper(), id);
    }
}