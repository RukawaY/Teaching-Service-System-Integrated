package com.Main.RowMapper.exam;

import com.Main.entity.exam.Chapter;
import org.springframework.jdbc.core.RowMapper;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ChapterRowMapper implements RowMapper<Chapter> {
    @Override
    public Chapter mapRow(ResultSet rs, int rowNum) throws SQLException {
        Chapter chapter = new Chapter();
        chapter.setChapterId(rs.getInt("chapter_id"));
        chapter.setCourseId(rs.getInt("course_id"));
        chapter.setChapterName(rs.getString("chapter_name"));
        chapter.setSequence(rs.getInt("sequence"));

        // 处理可能的NULL值
        if (rs.wasNull()) {
            chapter.setSequence(null);
        }

        return chapter;
    }
}