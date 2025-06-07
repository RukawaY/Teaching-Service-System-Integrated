package com.Main.service.course_selection.impl;

import com.Main.RowMapper.course_selection.TeacherDao;
import com.Main.dto.course_selection.TeacherCourseListDTO;
import com.Main.entity.course_selection.Course;
import com.Main.entity.course_selection.Section;
import com.Main.entity.course_selection.Student;
import com.Main.service.course_selection.TeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 教师服务实现类
 */
@Service
public class TeacherServiceImpl implements TeacherService {

    private static final Logger logger = LoggerFactory.getLogger(TeacherServiceImpl.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    private TeacherDao teacherDao;

    @Override
    public TeacherCourseListDTO getCourseStudents(Integer teacherId) {
        logger.info("Getting courses and students for teacher ID: {}", teacherId);
        
        // 检查教师是否存在
        if (teacherId == null || !teacherDao.existsById(teacherId)) {
            logger.warn("Teacher with ID {} not found or invalid", teacherId);
            return new TeacherCourseListDTO();
        }
        
        // 创建返回对象
        TeacherCourseListDTO resultDTO = new TeacherCourseListDTO();
        List<TeacherCourseListDTO.TeacherCourseDTO> courseDTOList = new ArrayList<>();
        
        // 1. 获取教师的所有课程
        List<Course> courses = teacherDao.findCoursesByTeacherId(teacherId);
        logger.info("Found {} courses for teacher ID: {}", courses.size(), teacherId);
        
        // 处理每一门课程
        for (Course course : courses) {
            logger.info("Processing course: {}, ID: {}", course.getCourseName(), course.getCourseId());
            
            // 2. 根据course_id查询所有section
            String sectionSql = "SELECT * FROM section WHERE course_id = ?";
            List<Section> sections = jdbcTemplate.query(
                sectionSql,
                (rs, rowNum) -> {
                    Section section = new Section();
                    section.setSectionId(rs.getInt("section_id"));
                    section.setCourseId(rs.getInt("course_id"));
                    section.setClassroomId(rs.getInt("classroom_id"));
                    section.setCapacity(rs.getInt("capacity"));
                    section.setAvailableCapacity(rs.getInt("available_capacity"));
                    section.setSemester(rs.getString("semester"));
                    section.setSecYear(rs.getInt("sec_year"));
                    section.setSecTime(rs.getString("sec_time"));
                    return section;
                },
                course.getCourseId()
            );
            
            logger.info("Found {} sections for course ID: {}", sections.size(), course.getCourseId());
            
            // 处理每个section
            for (Section section : sections) {
                TeacherCourseListDTO.TeacherCourseDTO courseDTO = new TeacherCourseListDTO.TeacherCourseDTO();
                courseDTO.setCourseName(course.getCourseName());
                
                // 获取教室位置信息
                String classroomSql = "SELECT location FROM classroom WHERE classroom_id = ?";
                String classroom = "未知教室";
                try {
                    classroom = jdbcTemplate.queryForObject(classroomSql, String.class, section.getClassroomId());
                } catch (Exception e) {
                    logger.error("Error getting classroom location for ID {}: {}", section.getClassroomId(), e.getMessage());
                }
                
                courseDTO.setCourseTime(section.getSecTime());
                courseDTO.setCourseClassroom(classroom);
                
                // 3. 根据section_id查询选课学生
                String studentSql = "SELECT u.* FROM user u " +
                                   "JOIN course_selection cs ON u.user_id = cs.student_id " +
                                   "WHERE cs.section_id = ? AND u.role = 's'";
                
                List<Student> students = jdbcTemplate.query(
                    studentSql, 
                    (rs, rowNum) -> {
                        Student student = new Student();
                        student.setStudentId(rs.getInt("user_id"));
                        student.setStudentName(rs.getString("name"));
                        return student;
                    },
                    section.getSectionId()
                );
                
                logger.info("Found {} students for section ID: {}", students.size(), section.getSectionId());
                
                // 转换为学生信息DTO列表
                List<TeacherCourseListDTO.StudentInfo> studentInfoList = new ArrayList<>();
                for (Student student : students) {
                    TeacherCourseListDTO.StudentInfo studentInfo = new TeacherCourseListDTO.StudentInfo();
                    studentInfo.setName(student.getStudentName());
                    studentInfo.setID(student.getStudentId());
                    studentInfoList.add(studentInfo);
                }
                
                courseDTO.setStudentList(studentInfoList);
                courseDTOList.add(courseDTO);
            }
        }
        
        resultDTO.setCourseList(courseDTOList);
        logger.info("Returning {} course DTOs for teacher ID: {}", courseDTOList.size(), teacherId);
        return resultDTO;
    }
}