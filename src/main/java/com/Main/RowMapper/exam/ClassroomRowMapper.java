package com.Main.RowMapper.exam;

import java.sql.ResultSet;
import java.sql.SQLException;
import com.Main.entity.information.Classroom;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.lang.NonNull;

public class ClassroomRowMapper implements RowMapper<Classroom> {
    @Override
    public Classroom mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
        Classroom classroom = new Classroom();
        classroom.setId(rs.getInt("classroom_id"));
        classroom.setLocation(rs.getString("location"));
        classroom.setCapacity(rs.getInt("capacity"));
        return classroom;
    }
}

