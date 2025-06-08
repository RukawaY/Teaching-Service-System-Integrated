package com.Main.web.rss;

import com.Main.entity.rss.Course;
import com.Main.entity.rss.Homework;
import com.Main.service.rss.AttendanceService;
import com.Main.service.rss.HomeworkService;
import com.Main.service.rss.RssCourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/teacher")
@CrossOrigin
public class TeacherHomeworkController {
    @Autowired
    private HomeworkService homeworkService;
    @Autowired
    private RssCourseService courseService;
    @Autowired
    private AttendanceService attendanceService;

    @GetMapping("/homework_list")
    public Map<String, Object> getHomeworkList() {
        List<Map<String, Object>> result = new ArrayList<>();
        List<Course> courses = courseService.getAllCourses();
        for (Course course : courses) {
            Map<String, Object> map = new HashMap<>();
            map.put("course_id", course.getCourse_id());
            map.put("course_name", course.getCourse_name());
            map.put("homework_list", homeworkService.getHomeworkList(course.getCourse_id()));
            result.add(map);
        }
        return Map.of("code", "200", "message", "success", "data", result);
    }

    @PostMapping("/homework")
    public Map<String, Object> assignHomework(@RequestBody Homework hw) {
        homeworkService.assignHomework(hw);
        return Map.of("code", "200", "message", "success");
    }

    @GetMapping("/homework/courseId")
    public ResponseEntity<?> getCourseId(@RequestParam Integer teacherId) {
        try {
            List<com.Main.entity.course_selection.Course> courses = attendanceService.getCoursesByTeacherId(teacherId);
            // 提取课程名称列表
            List<Integer> courseNames = courses.stream()
                    .map(com.Main.entity.course_selection.Course::getCourseId)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(courseNames);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("查询课程失败: " + e.getMessage());
        }
    }
}