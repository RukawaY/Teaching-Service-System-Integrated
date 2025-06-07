package com.Main.web.course_selection;

import com.Main.dto.course_selection.CourseListDTO;
import com.Main.dto.course_selection.CourseTableDTO;
import com.Main.dto.course_selection.CurriculumDTO;
import com.Main.dto.course_selection.ResponseDTO;
import com.Main.service.course_selection.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 课程公共控制器，处理不依赖于特定用户角色的API
 */
@RestController
@RequestMapping("/api/course_selection")
public class CourseController {

    @Autowired
    private StudentService studentService;

    /**
     * 搜索课程
     */
    @GetMapping("/search_course")
    public ResponseDTO<CourseListDTO> searchCourse(
            @RequestParam(required = false, name = "course_name") String courseName,
            @RequestParam(required = false, name = "teacher_name") String teacherName,
            @RequestParam(required = false, name = "course_id") Integer courseId,
            @RequestParam(required = false, name = "student_id") Integer studentId,
            @RequestParam(required = false, name = "need_available") Boolean needAvailable) {
        
        // 调用服务层搜索课程
        CourseListDTO courseListDTO = studentService.searchCourse(courseName, teacherName, courseId, studentId, needAvailable);
        
        // 检查返回值，如果为null说明个人培养方案不存在
        if (courseListDTO == null) {
            return ResponseDTO.fail("个人培养方案未定制或学生不存在");
        }
        
        return ResponseDTO.success(courseListDTO);
    }

    /**
     * 获取课程信息表
     */
    @GetMapping("/search_course_table")
    public ResponseDTO<CourseTableDTO> searchCourseTable(
            @RequestParam(required = false, name = "course_id") Integer courseId,
            @RequestParam(required = false, name = "course_name") String courseName,
            @RequestParam(required = false, name = "category") String category) {
        
        // 调用服务层查询课程表
        CourseTableDTO courseTableDTO = studentService.searchCourseTable(courseId, courseName, category);
        return ResponseDTO.success(courseTableDTO);
    }

    /**
     * 获取专业培养方案
     */
    @GetMapping("/get_curriculum")
    public ResponseDTO<CurriculumDTO> getCurriculum(@RequestParam(name = "major_name") String majorName) {
        
        // 调用服务层获取专业培养方案
        CurriculumDTO curriculumDTO = studentService.getCurriculum(majorName);
        if (curriculumDTO == null) {
            return ResponseDTO.fail("获取专业培养方案失败，专业不存在或服务器错误");
        }
        return ResponseDTO.success(curriculumDTO);
    }
}