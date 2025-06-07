package com.Main.web.rss;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.Main.dto.rss.AttendanceComponentDTO;
import com.Main.entity.course_selection.Course;
import com.Main.service.rss.AttendanceService;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    @Autowired
    private AttendanceService attendanceService;

    // 处理考勤成绩的接口（修改参数传递）
    @PostMapping("/process")
    public ResponseEntity<?> processAttendance(@RequestBody AttendanceComponentDTO dto) {
        try {
            // 调用服务层方法（传递dto.getStudentId()）
            boolean result = attendanceService.processAttendance(
                dto.getStudentId(),  // 获取studentId
                dto.getCourseName(),
                dto.getAttendanceScore(),
                dto.getAttendanceRatio()
            );
            
            if (result) {
                return ResponseEntity.ok("考勤成绩处理成功");
            } else {
                return ResponseEntity.badRequest().body("考勤成绩处理失败");
            }
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 通过教师ID获取所授课程名称列表的接口
    @GetMapping("/courses/teacher")
    public ResponseEntity<?> getTeacherCourses(@RequestParam Integer teacherId) {
        try {
            List<Course> courses = attendanceService.getCoursesByTeacherId(teacherId);
            // 提取课程名称列表
            List<String> courseNames = courses.stream()
                    .map(Course::getCourseName)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(courseNames);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("查询课程失败: " + e.getMessage());
        }
    }
}
