package com.Main.web.arrange;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import com.Main.dto.arrange.SectionDTO;
import com.Main.entity.arrange.Classroom;
import com.Main.entity.arrange.Section;
import com.Main.service.arrange.LessonScheduler;
import com.Main.entity.arrange.Course;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.URLDecoder;

@RestController
public class LessonController {

    final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private LessonScheduler lessonScheduler;

    // 查询教室（普通用户权限）
    @GetMapping("/arrange/api/classrooms/query")
    public ResponseEntity<?> queryClassrooms(
            @RequestParam(required = false) Integer id,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Integer capacity,
            @RequestParam(required = false) String category) {
        try {
            //category 需要识别:%E5%AE%9E%E9%AA%8C  %E6%99%AE%E9%80%9A %E4%BD%93%E8%82%B2
            Classroom filter = new Classroom();
            if (id != null) filter.setId(id);
            if (location != null) filter.setLocation(location);
            if (capacity != null) filter.setCapacity(capacity);
            if (category != null){
                filter.setCategory(category);
                //decode the category
                filter.setCategory(URLDecoder.decode(category, "UTF-8"));
            }
            
            List<Classroom> classrooms = lessonScheduler.queryClassrooms(filter);
            return ResponseEntity.ok(classrooms);
        } catch (Exception e) {
            logger.error("查询教室失败", e);
            Map<String, String> response = new HashMap<>();
            response.put("error", "查询教室失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // 查询课程安排（普通用户权限）
    @GetMapping("/arrange/api/sections/query")
    public ResponseEntity<?> getSchedules(
            @RequestParam(required = false) Integer section_id,
            @RequestParam(required = false) Integer course_id,
            @RequestParam(required = false) Integer classroom_id,
            @RequestParam(required = false) Integer capacity,
            @RequestParam(required = false) String semester,
            @RequestParam(required = false) Integer sec_year,
            @RequestParam(required = false) String sec_time,
            @RequestParam(required = false) Integer teacher_id
            ) {
        try {
            Section section = new Section();
            SectionDTO sectionFilter = new SectionDTO(section);
            List<SectionDTO> schedules;
            if (section_id != null) sectionFilter.setId(section_id);
            if (course_id != null) sectionFilter.setCourseId(course_id);
            if (classroom_id != null) sectionFilter.setClassroomId(classroom_id);
            if (capacity != null) sectionFilter.setCapacity(capacity);
            if (semester != null) {
                sectionFilter.setSemester(semester);
                if (semester.equals("spring_summer")) {
                    sectionFilter.setSemester("春夏");
                } else if (semester.equals("autumn_winter")) {
                    sectionFilter.setSemester("秋冬");
                }
                else{
                    //decode the semester
                    sectionFilter.setSemester(URLDecoder.decode(semester, "UTF-8"));
                }
            }
            if (sec_year != null) sectionFilter.setSecYear(sec_year);
            if (sec_time != null) sectionFilter.setSecTime(sec_time);
            if (teacher_id != null) 
                schedules = lessonScheduler.showSchedule(sectionFilter,teacher_id);
            else
                schedules = lessonScheduler.showSchedule(sectionFilter);
            return ResponseEntity.ok(schedules);
        } catch (Exception e) {
            logger.error("获取课表失败", e);
            Map<String, String> response = new HashMap<>();
            response.put("error", "获取课表失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // 获取课程列表（普通用户权限）
    @GetMapping("/arrange/api/courses")
    public ResponseEntity<?> getCourses() {
        try {
            List<Course> courses = lessonScheduler.showCourses();
            return ResponseEntity.ok(courses);
        } catch (Exception e) {
            logger.error("获取课程失败", e);
            Map<String, String> response = new HashMap<>();
            response.put("error", "获取课程失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}