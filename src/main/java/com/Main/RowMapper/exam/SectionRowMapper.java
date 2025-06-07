package com.Main.RowMapper.exam;

import java.sql.ResultSet;
import java.sql.SQLException;
import com.Main.entity.arrange.Section;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.lang.NonNull;

public class SectionRowMapper implements RowMapper<Section> {
    @Override
    public Section mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
        Section section = new Section();
        section.setId(rs.getInt("section_id"));
        section.setCourseId(rs.getInt("course_id"));
        section.setClassroomId(rs.getInt("classroom_id"));
        section.setCapacity(rs.getInt("capacity"));
        section.setSemester(rs.getString("semester"));
        section.setSecYear(rs.getInt("sec_year"));
        section.setSecTime(rs.getString("sec_time"));
        return section;
    }
    
}
