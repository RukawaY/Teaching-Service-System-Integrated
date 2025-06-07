package com.Main.service.information;

import com.Main.RowMapper.information.ClassroomRowMapper;
import com.Main.RowMapper.information.SectionRowMapper;
import com.Main.dto.information.ApiResponseDTO;
import com.Main.dto.information.PageResponseDTO;
import com.Main.dto.information.SectionSearchDTO;
import com.Main.dto.information.SectionSearchListDTO;
import com.Main.entity.information.Classroom;
import com.Main.entity.information.Section;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@Component
public class SectionService {

    final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    JdbcTemplate jdbcTemplate;

    RowMapper<Section> sectionRowMapper = new SectionRowMapper();
    RowMapper<Classroom> classroomRowMapper = new ClassroomRowMapper();
    /**
     * 获取开课信息列表（支持分页）
     * @param courseId 课程ID
     * @param semester 学期
     * @param secYear 学年
     * @return 开课信息
     */
    public List<SectionSearchDTO> getSections(int courseId, String semester, int secYear) {
        logger.info("查询开课信息列表: courseId={}, semester={}, secYear={}", courseId, semester, secYear);

        // 构建基础SQL和参数
        StringBuilder querySql = new StringBuilder("SELECT * FROM Section WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        // 添加筛选条件
        if (courseId > 0) {
            querySql.append("AND course_id = ? ");
            params.add(courseId);
        }
        if (semester != null && !semester.isEmpty()) {
            querySql.append("AND semester = ? ");
            params.add(semester);
        }
        if (secYear > 0) {
            querySql.append("AND sec_year = ? ");
            params.add(secYear);
        }

        // 添加排序
        querySql.append(" ORDER BY section_id ASC");

        // 查询数据
        List<Section> sections = jdbcTemplate.query(querySql.toString(), params.toArray(), sectionRowMapper);
        List<SectionSearchDTO> sectionSearchDTOS = new ArrayList<>();
        for (Section section : sections) {
            Classroom classroom = jdbcTemplate.queryForObject("SELECT * FROM Classroom WHERE classroom_id = ?",classroomRowMapper,section.getClassroomId());
            SectionSearchDTO sectionSearchDTO = new SectionSearchDTO();
            sectionSearchDTO.setSectionId(section.getId());
            sectionSearchDTO.setCourseId(section.getCourseId());
            sectionSearchDTO.setClassroomId(section.getClassroomId());
            sectionSearchDTO.setCapacity(section.getCapacity());
            sectionSearchDTO.setSemester(section.getSemester());
            sectionSearchDTO.setSecYear(section.getSecYear());
            sectionSearchDTO.setSecTime(section.getSecTime());
            sectionSearchDTO.setClassroom_location(classroom.getLocation());
            sectionSearchDTO.setClassroom_capacity(classroom.getCapacity());
            sectionSearchDTO.setAvailable_capacity(section.getCapacity());
            sectionSearchDTOS.add(sectionSearchDTO);
        }
        return sectionSearchDTOS;
    }

    /**
     * 获取开课详细信息
     * @param sectionId 开课Id
     * @return 开课信息
     */
    public SectionSearchDTO getSectionById(int sectionId) {
        logger.info("获取课程信息: sectionId={}", sectionId);
        try {
            Section section = jdbcTemplate.queryForObject("SELECT * FROM Section WHERE section_id = ?", new Object[]{sectionId}, sectionRowMapper);
            Classroom classroom = jdbcTemplate.queryForObject("SELECT * FROM Classroom WHERE classroom_id = ?",classroomRowMapper,section.getClassroomId());
            SectionSearchDTO sectionSearchDTO = new SectionSearchDTO();
            sectionSearchDTO.setSectionId(section.getId());
            sectionSearchDTO.setCourseId(section.getCourseId());
            sectionSearchDTO.setClassroomId(section.getClassroomId());
            sectionSearchDTO.setCapacity(section.getCapacity());
            sectionSearchDTO.setSemester(section.getSemester());
            sectionSearchDTO.setSecYear(section.getSecYear());
            sectionSearchDTO.setSecTime(section.getSecTime());
            sectionSearchDTO.setClassroom_location(classroom.getLocation());
            sectionSearchDTO.setAvailable_capacity(section.getAvailableCapacity());
            sectionSearchDTO.setClassroom_capacity(classroom.getCapacity());
            return sectionSearchDTO;
        } catch (DataAccessException e) {
            logger.error("SQL Error: " + e.getMessage(), e);
            throw new RuntimeException("获取开课信息失败.", e);
        }
    }

    /**
     * 创建开课信息
     * @param courseId 课程ID
     * @param classroomId 教室ID
     * @param capacity 容量
     * @param semester 学期
     * @param secYear 学年
     * @param secTime 上课时间
     * @return 创建的开课信息
     */
    public Section createSection(int courseId, int classroomId, int capacity, String semester, int secYear, String secTime) {
        logger.info("创建开课信息: courseId={}, classroomId={}, capacity={}, semester={}, secYear={}, secTime={}",
                courseId, classroomId, capacity, semester, secYear, secTime);

        // 参数验证
        if (courseId <= 0) {
            logger.warn("Invalid courseId: {}", courseId);
            throw new IllegalArgumentException("课程ID必须大于0");
        }
        if (classroomId <= 0) {
            logger.warn("Invalid classroomId: {}", classroomId);
            throw new IllegalArgumentException("教室ID必须大于0");
        }
        if (capacity <= 0) {
            logger.warn("Invalid capacity: {}", capacity);
            throw new IllegalArgumentException("容量必须大于0");
        }
        if (semester == null || semester.isEmpty()) {
            logger.warn("Invalid semester: {}", semester);
            throw new IllegalArgumentException("学期不能为空");
        }
        if (secYear <= 0) {
            logger.warn("Invalid secYear: {}", secYear);
            throw new IllegalArgumentException("学年必须大于0");
        }
        if (secTime == null || secTime.isEmpty()) {
            logger.warn("Invalid secTime: {}", secTime);
            throw new IllegalArgumentException("上课时间不能为空");
        }

        // 创建开课信息对象
        Section section = new Section();
        section.setCourseId(courseId);
        section.setClassroomId(classroomId);
        section.setCapacity(capacity);
        section.setSemester(semester);
        section.setSecYear(secYear);
        section.setSecTime(secTime);
        section.setAvailableCapacity(capacity);

        KeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            if (1 != jdbcTemplate.update((conn) -> {
                var ps = conn.prepareStatement("INSERT INTO Section(course_id, classroom_id, capacity, semester, sec_year, sec_time, available_capacity) VALUES(?, ?, ?, ?, ?, ?, ?)",
                        Statement.RETURN_GENERATED_KEYS);
                ps.setInt(1, section.getCourseId());
                ps.setInt(2, section.getClassroomId());
                ps.setInt(3, section.getCapacity());
                ps.setString(4, section.getSemester());
                ps.setInt(5, section.getSecYear());
                ps.setString(6, section.getSecTime());
                ps.setInt(7, section.getAvailableCapacity());
                return ps;
            }, keyHolder)) {
                logger.error("Failed to insert section.");
                throw new RuntimeException("创建开课信息失败.");
            }
        } catch (DataAccessException e) {
            logger.error("SQL Error: " + e.getMessage(), e);
            throw new RuntimeException("创建开课信息失败：数据库错误.", e);
        }

        section.setId(keyHolder.getKey().intValue());
        logger.info("Section created successfully with ID: {}", section.getId());
        return section;
    }

    /**
     * 修改开课信息
     * @param sectionId 开课ID
     * @param classroomId 教室ID
     * @param capacity 容量
     * @param semester 学期
     * @param secYear 学年
     * @param secTime 上课时间
     * @return 修改后的开课信息
     */
    public Section updateSection(int sectionId, int classroomId, int capacity, String semester, int secYear, String secTime, int available_capacity) {
        logger.info("修改开课信息: sectionId={}, classroomId={}, capacity={}, semester={}, secYear={}, secTime={}",
                sectionId, classroomId, capacity, semester, secYear, secTime);

        // 参数验证
        if (sectionId <= 0) {
            logger.warn("Invalid sectionId: {}", sectionId);
            throw new IllegalArgumentException("开课ID必须大于0");
        }
        if (classroomId <= 0) {
            logger.warn("Invalid classroomId: {}", classroomId);
            throw new IllegalArgumentException("教室ID必须大于0");
        }
        if (capacity <= 0) {
            logger.warn("Invalid capacity: {}", capacity);
            throw new IllegalArgumentException("容量必须大于0");
        }
        if (semester == null || semester.isEmpty()) {
            logger.warn("Invalid semester: {}", semester);
            throw new IllegalArgumentException("学期不能为空");
        }
        if (secYear <= 0) {
            logger.warn("Invalid secYear: {}", secYear);
            throw new IllegalArgumentException("学年必须大于0");
        }
        if (secTime == null || secTime.isEmpty()) {
            logger.warn("Invalid secTime: {}", secTime);
            throw new IllegalArgumentException("上课时间不能为空");
        }
        Classroom classroom = jdbcTemplate.queryForObject("select * from classroom where classroom_id = ?",classroomRowMapper, classroomId);
        if(capacity > classroom.getCapacity()) {
            logger.warn("Invalid capacity: {}, 教室容量为: {}", capacity, classroom.getCapacity());
            throw new IllegalArgumentException("开课容量不能小于教室容量");
        }

        String sql = "UPDATE Section SET classroom_id = ?, capacity = ?, semester = ?, sec_year = ?, sec_time = ? , available_capacity = ? WHERE section_id = ?";
        int rowsAffected = jdbcTemplate.update(sql, classroomId, capacity, semester, secYear, secTime, available_capacity, sectionId);

        if (rowsAffected > 0) {
            logger.info("Section {} 成功更新", sectionId);
        } else {
            logger.warn("Section {} 更新失败", sectionId);
            throw new RuntimeException("更新开课信息失败：开课信息不存在");
        }

        // 查询更新后的开课信息
        String getSectionSql = "SELECT * FROM Section WHERE section_id = ?";
        Section updatedSection = jdbcTemplate.queryForObject(getSectionSql, new Object[]{sectionId}, (rs, rowNum) -> {
            Section section = new Section();
            section.setId(rs.getInt("section_id"));
            section.setCourseId(rs.getInt("course_id"));
            section.setClassroomId(rs.getInt("classroom_id"));
            section.setCapacity(rs.getInt("capacity"));
            section.setSemester(rs.getString("semester"));
            section.setSecYear(rs.getInt("sec_year"));
            section.setSecTime(rs.getString("sec_time"));
            return section;
        });

        return updatedSection;
    }

    /**
     * 删除开课信息
     * @param sectionId 开课ID
     * @return 是否删除成功
     */
    public boolean deleteSection(int sectionId) {
        logger.info("删除开课信息: sectionId={}", sectionId);

        // 参数验证
        if (sectionId < 0) {
            logger.warn("无效的sectionId: {}", sectionId);
            throw new IllegalArgumentException("无效的sectionId");
        }

        // 删除开课信息
        String sql_delete = "DELETE FROM Section WHERE section_id = ?";
        int rowsAffected = jdbcTemplate.update(sql_delete, sectionId);
        if (rowsAffected > 0) {
            logger.info("Section {} 成功删除.", sectionId);
            return true;
        } else {
            logger.warn("Section {} 删除失败", sectionId);
            throw new RuntimeException("开课信息删除失败");
        }
    }

}
