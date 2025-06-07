package com.Main.web.information;

import com.Main.dto.information.*;
import com.Main.dto.information.ApiResponseDTO;
import com.Main.dto.information.SectionAnalyseDTO;
import com.Main.dto.information.StudentAnalyseDTO;
import com.Main.entity.information.Course;
import com.Main.entity.information.User;
import com.Main.service.information.CourseService;
import com.Main.service.information.GradeService;
import com.Main.service.information.SectionService;
import com.Main.service.information.UserService;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/information/api/v1")
public class AnalyseController {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private GradeService gradeService;
    @Autowired
    private CourseService courseService;
    @Autowired
    private SectionService sectionService;
    @Autowired
    private UserService userService;

    /**
     * 学生成绩分析
     * @param start_sec_year 开始学年
     * @param start_semester 开始学期
     * @param end_sec_year 结束学年
     * @param end_semester 结束学期
     * @param request 请求对象
     * @return 成绩
     */
    @GetMapping("/student/grades/analysis")
    public ResponseEntity<ApiResponseDTO<StudentAnalyseDTO>> getGrades(
            @RequestParam(value = "start_sec_year", required = false, defaultValue = "0") int start_sec_year,
            @RequestParam(value = "start_semester", required = false) String start_semester,
            @RequestParam(value = "end_sec_year", required = false, defaultValue = "0") int end_sec_year,
            @RequestParam(value = "end_semester", required = false) String end_semester,
            HttpServletRequest request
    ) {
        logger.info("获取学生成绩分析, start_sec_year: {}, start_semester: {}, end_sec_year: {}, end_semester: {}",
                start_sec_year, start_semester, end_sec_year, end_semester);

        // 验证输入参数
        if ((start_sec_year != 0 && start_semester == null) || (start_sec_year == 0 && !(start_semester==null) )) {
            return ResponseEntity.ok(ApiResponseDTO.error(500, "开始学年和开始学期必须同时输入或同时不输入"));
        }

        if ((end_sec_year != 0 && end_semester == null) || (end_sec_year == 0 && !(end_semester == null))) {
            return ResponseEntity.ok(ApiResponseDTO.error(500, "结束学年和结束学期必须同时输入或同时不输入"));
        }

        // 获取学生成绩
        try {
            int student_id = (int) request.getAttribute("userId");
            String role = (String) request.getAttribute("userRole");
            if(!role.equals("s")) {
                logger.error("该用户不是学生");
                throw new Exception();
            }
            StudentAnalyseDTO studentAnalyseDTO = gradeService.getStudentGradeAnalysis(student_id, start_sec_year, start_semester, end_sec_year, end_semester);

            return ResponseEntity.ok(ApiResponseDTO.success("分析成功",studentAnalyseDTO));
        } catch (Exception e) {
            logger.error("获取学生成绩分析失败", e);
            return ResponseEntity.ok(ApiResponseDTO.error(500, "获取失败"));
        }
    }

    /**
     * 教师课程分析
     * @param section_id 开课ID
     * @return SectionAnalyseDTO 课程分析
     */
    @GetMapping("/teacher/sections/{section_id}/grades/analysis")
    public ResponseEntity<ApiResponseDTO<SectionAnalyseDTO>> getSectionGrades(
            @PathVariable("section_id") int section_id) {
        logger.info("获取课程成绩分析, section_id: {}", section_id);

        // 获取课程成绩分析
        try {
            SectionAnalyseDTO sectionAnalyseDTO = gradeService.getSectionGradeAnalysis(section_id);
            return ResponseEntity.ok(ApiResponseDTO.success("分析成功", sectionAnalyseDTO));
        } catch (Exception e) {
            logger.error("获取课程成绩分析失败", e);
            return ResponseEntity.ok(ApiResponseDTO.error(500, "获取失败"));
        }
    }

}
