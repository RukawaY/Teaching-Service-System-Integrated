package com.Main.web.arrange;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import com.Main.dto.information.ApiResponseDTO;
import com.Main.entity.arrange.Classroom;
import com.Main.entity.arrange.Section;
import com.Main.entity.arrange.LessonScheduleFilter;
import com.Main.service.arrange.LessonScheduler;
import com.Main.service.information.AdminUserService;

import java.util.Map;
import java.net.URLDecoder;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.servlet.http.HttpServletRequest;


@RestController
public class AdminLessonController {

    final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private LessonScheduler lessonScheduler;
    
    @Autowired
    private AdminUserService adminUserService;

    // 管理员权限：添加教室
    @PostMapping("/arrange/api/classrooms")
    public ResponseEntity<?> addClassroom(@RequestBody Classroom classroom, HttpServletRequest request) {
        String userRole = (String) request.getAttribute("userRole");

        try {
            logger.debug("admin controller add classroom: " + classroom);
            lessonScheduler.addClassroom(classroom);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("添加教室失败", e);
            Map<String, String> response = new HashMap<>();
            response.put("error", "添加教室失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // 管理员权限：更新教室
    @PutMapping("/arrange/api/classrooms/{classroom_id}")
    public ResponseEntity<?> updateClassroom(@PathVariable("classroom_id") int classroomId, @RequestBody Classroom updateInfo, HttpServletRequest request) {
        String userRole = (String) request.getAttribute("userRole");

        try {
            logger.debug("admin controller update classroom: " + updateInfo);
            lessonScheduler.updateClassroom(classroomId, updateInfo);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("更新教室失败", e);
            Map<String, String> response = new HashMap<>();
            response.put("error", "更新教室失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // 管理员权限：删除教室
    @DeleteMapping("/arrange/api/classrooms/{classroom_id}")
    public ResponseEntity<?> deleteClassroom(@PathVariable("classroom_id") int classroomId, HttpServletRequest request) {
        String userRole = (String) request.getAttribute("userRole");

        try {
            logger.debug("admin controller delete classroom: " + classroomId);
            lessonScheduler.deleteClassroom(classroomId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("删除教室失败", e);
            Map<String, String> response = new HashMap<>();
            response.put("error", "删除教室失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // 管理员权限：生成课表
    @PostMapping("/arrange/api/schedules/generate")
    public ResponseEntity<?> generateSchedule(@RequestBody LessonScheduleFilter filter, HttpServletRequest request) {
        String userRole = (String) request.getAttribute("userRole");

        try {   
            logger.debug("admin controller generate schedule: " + filter);
            lessonScheduler.generateSchedule(filter);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("生成课表失败", e);
            Map<String, String> response = new HashMap<>();
            response.put("error", "生成课表失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // 管理员权限：添加课程安排
    @PostMapping("/arrange/api/sections")
    public ResponseEntity<?> addSchedule(@RequestBody Section section, HttpServletRequest request) {
        String userRole = (String) request.getAttribute("userRole");

        try {
            logger.debug("admin controller add schedule: " + section);
            lessonScheduler.addSchedule(section);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("添加课表失败", e);
            Map<String, String> response = new HashMap<>();
            response.put("error", "添加课表失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // 管理员权限：更新课程安排
    @PutMapping("/arrange/api/sections/{section_id}")
    public ResponseEntity<?> updateSchedule(@PathVariable("section_id") int sectionId, @RequestBody Section updateInfo, HttpServletRequest request) {
        String userRole = (String) request.getAttribute("userRole");

        try {
            logger.debug("admin controller update section: " + updateInfo);
            lessonScheduler.updateSchedule(sectionId, updateInfo);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("更新课表失败", e);
            Map<String, String> response = new HashMap<>();
            response.put("error", "更新课表失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // 管理员权限：删除课程安排
    @DeleteMapping("/arrange/api/sections/{section_id}")
    public ResponseEntity<?> deleteSchedule(@PathVariable("section_id") int sectionId, HttpServletRequest request) {
        String userRole = (String) request.getAttribute("userRole");

        try {
            logger.debug("admin controller delete schedule: " + sectionId);
            lessonScheduler.deleteSchedule(sectionId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("删除课表失败", e);
            Map<String, String> response = new HashMap<>();
            response.put("error", "删除课表失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // 管理员权限：检查课表
    @GetMapping("/arrange/api/sections/check")
    public ResponseEntity<?> checkSchedule(@RequestParam(required = true) String semester, @RequestParam(required = true) int year, HttpServletRequest request) {
        String userRole = (String) request.getAttribute("userRole");

        try {
            String decodedSemester = URLDecoder.decode(semester, "UTF-8");
            logger.debug("admin controller check schedule: " + decodedSemester + " " + year);
            String result = lessonScheduler.checkSchedule(decodedSemester, year);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("检查课表失败", e);
            Map<String, String> response = new HashMap<>();
            response.put("error", "检查课表失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
} 