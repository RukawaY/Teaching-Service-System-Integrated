package com.Main.web.course_selection;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.Main.dto.course_selection.CurriculumDTO;
import com.Main.dto.course_selection.ResponseDTO;
import com.Main.dto.course_selection.SupplementListDTO;
import com.Main.dto.course_selection.SupplementResultDTO;
import com.Main.entity.course_selection.SelectionTime;
import com.Main.service.course_selection.ManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 管理员控制器
 */
@RestController
@RequestMapping("/api/course_selection/manager")
public class ManagerController {

    @Autowired
    private ManagerService managerService;

    /**
     * 8. 管理员设计选课系统时间
     */
    @PostMapping("/update_time")
    public ResponseDTO<?> updateTime(@RequestBody SelectionTime selectionTime, HttpServletRequest request) {
        
        // 调用服务层更新选课系统时间
        boolean success = managerService.updateSelectionTime(selectionTime);
        if (success) {
            // 返回更详细的成功信息，包括新的选课人数上限
            Map<String, Object> data = new HashMap<>();
            data.put("max_number", selectionTime.getMaxNumber());
            return ResponseDTO.success(data);
        } else {
            return ResponseDTO.fail("Failed to update selection time settings");
        }
    }

    /**
     * 9. 定制培养方案
     */
    @PostMapping("/set_curriculum")
    public ResponseDTO<?> setCurriculum(@RequestBody CurriculumDTO curriculumDTO, HttpServletRequest request) {
        
        // 调用服务层设置培养方案
        String message = managerService.setCurriculum(curriculumDTO);
        if (message == "success") {
            return ResponseDTO.success();
        } else {
            return ResponseDTO.fail(message);
        }
    }

    /**
     * 10. 管理员获取选课系统的时间
     */
    @GetMapping("/get_time")
    public ResponseDTO<SelectionTime> getTime(HttpServletRequest request) {
        
        // 调用服务层获取选课系统时间
        SelectionTime selectionTime = managerService.getSelectionTime();
        return ResponseDTO.success(selectionTime);
    }

    /**
     * 11. 管理员为学生选课
     */
    @PostMapping("/choose_course")
    public ResponseDTO<?> chooseCourse(@RequestBody StudentCourseRequest request, HttpServletRequest httpRequest) {
        
        // 调用服务层为学生选课
        String response = managerService.chooseCourseForStudent(request.getStudentId(), request.getCourseId());
        if ("success".equals(response)) {
            return ResponseDTO.success();
        } else {
            return ResponseDTO.fail(response);
        }
    }

    /**
     * 管理员获取补选申请
     */
    @GetMapping("/get_supplement")
    public ResponseDTO<SupplementListDTO> getSupplement(@RequestParam(name = "course_id", required = false) Integer courseId, HttpServletRequest request) {
        
        // 调用服务层获取补选申请列表
        SupplementListDTO supplementListDTO = managerService.getSupplementApplications(courseId);
        return ResponseDTO.success(supplementListDTO);
    }

    /**
     * 管理员提交补选结果
     */
    @PostMapping("/submit_supplement")
    public ResponseDTO<?> submitSupplement(@RequestBody SupplementResultDTO supplementResultDTO, HttpServletRequest request) {
        
        // 调用服务层处理补选申请
        String message = managerService.processSupplementApplication(
                supplementResultDTO.getSupplementId(),
                supplementResultDTO.getResult()
        );
        
        if (message == "success") {
            return ResponseDTO.success();
        } else {
            return ResponseDTO.fail(message);
        }
    }

    /**
     * 学生课程请求DTO
     */
    public static class StudentCourseRequest {
        private Integer studentId;
        private Integer courseId;

        @JsonProperty("student_id")
        public Integer getStudentId() {
            return studentId;
        }

        public void setStudentId(Integer studentId) {
            this.studentId = studentId;
        }

        @JsonProperty("course_id")
        public Integer getCourseId() {
            return courseId;
        }

        public void setCourseId(Integer courseId) {
            this.courseId = courseId;
        }
    }
}