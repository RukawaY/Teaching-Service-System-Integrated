package com.Main.RowMapper.information;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.lang.NonNull;

import com.Main.entity.information.GradeBase;

import java.sql.ResultSet;
import java.sql.SQLException;

public class GradeBaseRowMapper implements RowMapper<GradeBase> {

    @Override
    public GradeBase mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
        GradeBase gradeBase = new GradeBase();
        gradeBase.setGradeId(rs.getInt("grade_id"));
        gradeBase.setStudentId(rs.getInt("student_id"));
        gradeBase.setCourseId(rs.getInt("course_id"));
        gradeBase.setSectionId(rs.getInt("section_id"));
        gradeBase.setScore(rs.getInt("score"));
        gradeBase.setGpa(rs.getFloat("gpa"));
        gradeBase.setSubmitStatus(rs.getInt("submit_status"));
        return gradeBase;
    }
}
