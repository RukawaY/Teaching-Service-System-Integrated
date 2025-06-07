package com.Main.RowMapper.arrange;

import java.sql.ResultSet;
import java.sql.SQLException;
import com.Main.entity.arrange.Classroom;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.lang.NonNull;

public class ClassroomRowMapper implements RowMapper<Classroom> {
    @Override
    public Classroom mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
        Classroom classroom = new Classroom();
        classroom.setId(rs.getInt("classroom_id"));
        classroom.setLocation(rs.getString("location"));
        classroom.setCategory(rs.getString("category"));
        classroom.setCapacity(rs.getInt("capacity"));
        return classroom;
    }
}

