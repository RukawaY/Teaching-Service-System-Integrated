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
        classroom.setClassroom_id(rs.getInt("classroom_id"));
        classroom.setClassroom_location(rs.getString("location"));
        classroom.setClassroom_capacity(rs.getInt("capacity"));
        return classroom;
    }
}

