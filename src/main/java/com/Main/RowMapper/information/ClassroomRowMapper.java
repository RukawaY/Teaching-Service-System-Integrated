package com.Main.RowMapper.information;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.lang.NonNull;

import com.Main.entity.information.Classroom;

import java.sql.ResultSet;
import java.sql.SQLException;

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
