package com.Main.web.rss;

import com.Main.entity.rss.Course;
import com.Main.service.rss.RssCourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/student")
@CrossOrigin
public class StudentCourseController {
    @Autowired
    private RssCourseService courseService;

    @GetMapping("/get_course_list")
    public Map<String, Object> getCourseList(@RequestParam Integer student_id) {
        List<Course> list = courseService.getCourseListByStudentId(student_id);
        return Map.of("code", "200", "message", "success", "data", Map.of("course_list", list));
    }
}