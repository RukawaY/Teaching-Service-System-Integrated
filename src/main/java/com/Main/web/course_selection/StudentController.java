package com.Main.web.course_selection;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.Main.dto.course_selection.CourseListDTO;
import com.Main.dto.course_selection.CurriculumDTO;
import com.Main.dto.course_selection.ResponseDTO;
import com.Main.dto.course_selection.SupplementApplicationDTO;
import com.Main.dto.course_selection.SupplementResultListDTO;
import com.Main.service.course_selection.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 学生控制器
 */
@RestController
@RequestMapping("/api/course_selection/student")
public class StudentController {

    @Autowired
    private StudentService studentService;

    /**
     * 2. 学生选择课程（通过请求参数）
     * 注意：course_id参数实际上是section_id，代表具体的开课信息
     */
    @PostMapping(value = "/choose_course", params = {"student_id", "course_id"})
    public ResponseDTO<?> chooseCourseWithParams(
            @RequestParam(name = "student_id") Integer studentId, 
            @RequestParam(name = "course_id") Integer courseId,
            HttpServletRequest request) {
        
        // 调用服务层选择课程，courseId实际上是sectionId
        String message = studentService.chooseCourse(studentId, courseId);
        if (message.equals("success")) {
            return ResponseDTO.success();
        } else {
            return ResponseDTO.fail(message);
        }
    }
    
    /**
     * 2. 学生选择课程（通过请求体）
     * 注意：course_id参数实际上是section_id，代表具体的开课信息
     */
    @PostMapping("/choose_course")
    public ResponseDTO<?> chooseCourse(@RequestBody StudentCourseRequest request, HttpServletRequest httpRequest) {
        
        // 调用服务层选择课程，courseId实际上是sectionId
        String message = studentService.chooseCourse(request.getStudentId(), request.getCourseId());
        if (message.equals("success")) {
            return ResponseDTO.success();
        } else {
            return ResponseDTO.fail(message);
        }
    }

    /**
     * 学生退课
     * 注意：course_id参数实际上是section_id，代表具体的开课信息
     */
    @PostMapping("/drop_course")
    public ResponseDTO<?> dropCourse(@RequestBody StudentCourseRequest request, HttpServletRequest httpRequest) {
        
        // 调用服务层退课，courseId实际上是sectionId
        boolean success = studentService.dropCourse(request.getStudentId(), request.getCourseId());
        if (success) {
            return ResponseDTO.success();
        } else {
            return ResponseDTO.fail("退课失败，请检查是否已选择该课程");
        }
    }

    /**
     * 3. 查看已选课程
     */
    @GetMapping("/show_selected_courses")
    public ResponseDTO<CourseListDTO> showSelectedCourses(@RequestParam(name = "student_id") Integer studentId, HttpServletRequest request) {
        
        // 调用服务层获取已选课程
        CourseListDTO courseListDTO = studentService.getSelectedCourses(studentId);
        if (courseListDTO == null) {
            return ResponseDTO.fail("用户不是学生或用户不存在");
        }
        return ResponseDTO.success(courseListDTO);
    }

    /**
     * 5. 查询个人培养方案
     */
    @GetMapping("/get_personal_curriculum")
    public ResponseDTO<CurriculumDTO> getPersonalCurriculum(@RequestParam(name = "student_id") Integer studentId, HttpServletRequest request) {
        
        // 调用服务层获取个人培养方案
        CurriculumDTO curriculumDTO = studentService.getPersonalCurriculum(studentId);
        if (curriculumDTO.getStudentId() == null) {
            return ResponseDTO.fail("获取个人培养方案失败,学生不存在或未定制培养方案");
        }
        return ResponseDTO.success(curriculumDTO);
    }

    /**
     * 6. 定制个人培养方案
     */
    @PostMapping("/set_personal_curriculum")
    public ResponseDTO<?> setPersonalCurriculum(@RequestBody CurriculumDTO curriculumDTO, HttpServletRequest request) {
        
        // 调用服务层设置个人培养方案
        String message = studentService.setPersonalCurriculum(curriculumDTO.getStudentId(), curriculumDTO);
        if (message.equals("success")) {
            return ResponseDTO.success();
        } else {
            return ResponseDTO.fail(message);
        }
    }

    /**
     * 学生申请补选
     */
    @PostMapping("/apply_supplement")
    public ResponseDTO<?> applySupplementCourse(@RequestBody SupplementApplicationDTO supplementApplicationDTO, HttpServletRequest request) {
        
        // 调用服务层提交补选申请
        String message = studentService.applySupplementCourse(
                supplementApplicationDTO.getStudentId(), 
                supplementApplicationDTO.getSectionId()
        );
        
        if (message.equals("success")) {
            return ResponseDTO.success();
        } else {
            return ResponseDTO.fail(message);
        }
    }

    /**
     * 查看补选结果
     */
    @GetMapping("/get_supp_result")
    public ResponseDTO<SupplementResultListDTO> getSupplementResult(@RequestParam(name = "student_id") Integer studentId, HttpServletRequest request) {
        
        // 调用服务层获取补选结果
        SupplementResultListDTO resultDTO = studentService.getSupplementResult(studentId);
        return ResponseDTO.success(resultDTO);
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