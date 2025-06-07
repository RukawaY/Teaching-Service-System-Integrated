package com.Main.web.course_selection;

import com.Main.dto.course_selection.ResponseDTO;
import com.Main.dto.course_selection.TeacherCourseListDTO;
import com.Main.service.course_selection.TeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 教师控制器
 */
@RestController
@RequestMapping("/api/course_selection/teacher")
public class TeacherController {

    @Autowired
    private TeacherService teacherService;

    /**
     * 7. 教师获得选课学生信息
     */
    @GetMapping("/getresult")
    public ResponseDTO<TeacherCourseListDTO> getResult(@RequestParam(name = "teacher_id") Integer teacherId, HttpServletRequest request) {
        
        // 调用服务层获取教师课程和选课学生信息
        TeacherCourseListDTO teacherCourseListDTO = teacherService.getCourseStudents(teacherId);
        return ResponseDTO.success(teacherCourseListDTO);
    }
}