package com.Main.web.rss;

import com.Main.entity.rss.Homework;
import com.Main.service.rss.HomeworkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/student")
@CrossOrigin
public class StudentHomeworkController {
    @Autowired
    private HomeworkService homeworkService;

    @GetMapping("/get_homework_list")
    public Map<String, Object> getHomeworkList(@RequestParam Integer course_id) {
        List<Homework> list = homeworkService.getHomeworkList(course_id);
        return Map.of("code", "200", "message", "success", "data", Map.of("homework_list", list));
    }
}