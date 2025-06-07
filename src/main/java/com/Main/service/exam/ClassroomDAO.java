package com.Main.service.exam;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import com.Main.RowMapper.information.ClassroomRowMapper;
import com.Main.entity.information.Classroom;

//FIXME: used for test only
@Component
public class ClassroomDAO {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<Classroom> getAllClassrooms() {
        return jdbcTemplate.query("SELECT * FROM Classroom", new ClassroomRowMapper());
    }
}
