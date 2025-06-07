package com.Main.service.information;

import com.Main.RowMapper.information.ClassroomRowMapper;
import com.Main.RowMapper.information.CourseRowMapper;
import com.Main.RowMapper.information.SectionRowMapper;
import com.Main.entity.information.Classroom;
import com.Main.entity.information.Course;
import com.Main.entity.information.Section;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ClassroomService {

    final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    JdbcTemplate jdbcTemplate;

    RowMapper<Course> courseRowMapper = new CourseRowMapper();
    RowMapper<Classroom> classroomRowMapper = new ClassroomRowMapper();
    RowMapper<Section> sectionRowMapper = new SectionRowMapper();

    /**
     * 获取教室信息
     * @param classroom_id 教室ID
     * @return 教室信息
     */
    public String getClassroomInfo(int classroom_id) {
        String sql = "SELECT * FROM classroom WHERE classroom_id = ?";
        return jdbcTemplate.queryForObject(sql, new Object[]{classroom_id}, String.class);
    }

    /**
     * 获取所有教室信息
     * @param sec_year 学年
     * @param semester 学期
     * @param sec_time 上课时间
     * @return 可用教室列表
     */
    public List<Classroom> getAvailableClassrooms(int sec_year, String semester, String sec_time) {
        String sql = "SELECT * FROM classroom  WHERE 1 = 1 ";
        List<Classroom> classrooms = jdbcTemplate.query(sql, classroomRowMapper);
        for (Classroom classroom : classrooms) {
            String checkSql = "SELECT COUNT(*) FROM section WHERE sec_year = ? AND semester = ? AND sec_time = ? AND classroom_id = ?";
            int count = jdbcTemplate.queryForObject(checkSql, new Object[]{sec_year, semester, sec_time, classroom.getClassroom_id()}, Integer.class);
            if (count > 0) {
                classrooms.remove(classroom);
            }
        }
        return classrooms;
    }
}
