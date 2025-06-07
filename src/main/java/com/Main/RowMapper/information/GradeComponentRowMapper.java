package com.Main.RowMapper.information;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.lang.NonNull;

import com.Main.entity.information.GradeComponent;

import java.sql.ResultSet;
import java.sql.SQLException;

public class GradeComponentRowMapper implements RowMapper<GradeComponent> {

    @Override
    public GradeComponent mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
        GradeComponent gradeComponent = new GradeComponent();
        gradeComponent.setComponentId(rs.getInt("component_id"));
        gradeComponent.setComponentName(rs.getString("component_name"));
        gradeComponent.setGradeId(rs.getInt("grade_id"));
        gradeComponent.setComponentType(rs.getInt("component_type"));
        gradeComponent.setRatio(rs.getInt("ratio"));
        gradeComponent.setScore(rs.getInt("score"));
        return gradeComponent;
    }
}
