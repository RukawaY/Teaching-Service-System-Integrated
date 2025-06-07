package com.Main.web.rss;

import com.Main.entity.rss.Course;
import com.Main.entity.rss.Homework;
import com.Main.service.rss.HomeworkService;
import com.Main.service.rss.RssCourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/teacher")
@CrossOrigin
public class TeacherHomeworkController {
    @Autowired
    private HomeworkService homeworkService;
    @Autowired
    private RssCourseService courseService;

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
}