package com.Main.web.information;

import com.Main.dto.information.*;
import com.Main.dto.information.ApiResponseDTO;
import com.Main.dto.information.ApiResponseListDTO;
import com.Main.dto.information.ApplyRequestDTO;
import com.Main.dto.information.ApplySearchDTO;
import com.Main.dto.information.GradeDTO;
import com.Main.dto.information.ReturnCourseDTO;
import com.Main.dto.information.ReviewApplyDTO;
import com.Main.entity.information.Apply;
import com.Main.entity.information.Course;
import com.Main.entity.information.GradeBase;
import com.Main.entity.information.User;
import com.Main.service.information.ApplyService;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/information/api/v1")
public class ApplyController {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private GradeService gradeService;
    @Autowired
    private CourseService courseService;
    @Autowired
    private SectionService sectionService;
    @Autowired
    private UserService userService;
    @Autowired
    private ApplyService applyService;

    /**
     * 提交成绩修改申请
     * @param applyrequestDTO  申请DTO
     * @param request   请求对象
     * @return 成绩
     */
    @PostMapping("/teacher/grade-applies")
    public ResponseEntity<ApiResponseDTO<Apply>> applyForGradeChange(
            @RequestBody ApplyRequestDTO applyrequestDTO,
            HttpServletRequest request
    ) {
        int grade_id = applyrequestDTO.getGradeId();
        int old_score = applyrequestDTO.getOldScore();
        int new_score = applyrequestDTO.getNewScore();
        String  reason = applyrequestDTO.getReason();
        logger.info("提交成绩修改申请, grade_id: {}, old_score: {}, new_score: {}, reason: {}", grade_id, old_score, new_score, reason);
        // 验证成绩正确性
        GradeDTO gradeDTO = gradeService.getGradeDetail(grade_id);
        if (gradeDTO.gradeBase.getScore() != old_score) {
            logger.error("成绩修改申请失败，原成绩不正确");
            return ResponseEntity.badRequest().body(ApiResponseDTO.error(404, "原成绩不正确"));
        }
        // 获取学生成绩
        try {
            int teacher_id = (int) request.getAttribute("userId");
            Apply apply = applyService.createApply(teacher_id, grade_id, old_score, new_score, reason);
            return ResponseEntity.ok(ApiResponseDTO.success(apply));
        } catch (Exception e) {
            logger.error("提交成绩修改申请失败", e);
            return ResponseEntity.badRequest().body(ApiResponseDTO.error(500, e.getMessage()));
        }
    }

    /**
     * 查询已发送申请
     * @param audit_status 审核状态
     * @param request 请求对象
     * @return 申请列表
     */
    @GetMapping("/teacher/grade-applies")
    public ResponseEntity<ApiResponseListDTO<ApplySearchDTO>> queryForSentApply(
            @RequestParam(value = "audit_status", required = false) Integer audit_status,
            HttpServletRequest request
    ) {
        logger.info("查询已发送申请, audit_status: {}", audit_status);
        try {
            int teacher_id = (int) request.getAttribute("userId");
            List<Apply> applyList = applyService.getAppliesByTeacher(teacher_id, audit_status);
            List<ApplySearchDTO> applySearchDTOList = new ArrayList<>();
            for (Apply apply : applyList) {
                ApplySearchDTO applySearchDTO = new ApplySearchDTO();
                applySearchDTO.setApply(apply);
                // 获取课程信息
                ReturnCourseDTO course = courseService.getCourseById(apply.getGradeId());
                applySearchDTO.setCourse_name(course.getCourse_name());

                // 获取教师信息
                User teacher = userService.getUserById(apply.getTeacherId());
                applySearchDTO.setTeacher_name(teacher.getName());

                // 获取学生信息
                GradeDTO gradeDTO = gradeService.getGradeDetail(apply.getGradeId());
                User student = userService.getUserById(gradeDTO.getGradeBase().getStudentId());
                applySearchDTO.setStudent_name(student.getName());

                applySearchDTOList.add(applySearchDTO);
            }
            return ResponseEntity.ok(ApiResponseListDTO.success("获取成功",applySearchDTOList));
        } catch (Exception e) {
            logger.error("查询已发送申请失败", e);
            return ResponseEntity.badRequest().body(ApiResponseListDTO.error(500, e.getMessage()));
        }
    }
    /**
     * 管理员查询成绩修改申请
     * @param audit_status 审核状态
     * @param teacher_id 教师ID
     * @param request 请求对象
     * @return 申请列表
     */
    @GetMapping("/admin/grade-applies")
    public ResponseEntity<ApiResponseListDTO<ApplySearchDTO>> queryForAdminApply(
            @RequestParam(value = "audit_status", required = false) Integer audit_status,
            @RequestParam(value = "teacher_id", required = false) Integer teacher_id,
            HttpServletRequest request
    ) {
        logger.info("管理员查询成绩修改申请, audit_status: {}, teacher_id: {}", audit_status, teacher_id);
        try {
            String role = (String) request.getAttribute("userRole");
            if (!role.equals("a") ){
                throw new RuntimeException("没有权限");
            }

            List<Apply> applyList = applyService.getAppliesByAdmin(audit_status, teacher_id);
            List<ApplySearchDTO> applySearchDTOList = new ArrayList<>();
            for (Apply apply : applyList) {
                ApplySearchDTO applySearchDTO = new ApplySearchDTO();
                applySearchDTO.setApply(apply);

                // 获取课程信息
                ReturnCourseDTO course = courseService.getCourseById(apply.getGradeId());
                applySearchDTO.setCourse_name(course.getCourse_name());

                // 获取教师信息
                User teacher = userService.getUserById(apply.getTeacherId());
                applySearchDTO.setTeacher_name(teacher.getName());

                // 获取学生信息
                GradeDTO gradeDTO = gradeService.getGradeDetail(apply.getGradeId());
                User student = userService.getUserById(gradeDTO.getGradeBase().getStudentId());
                applySearchDTO.setStudent_name(student.getName());

                applySearchDTOList.add(applySearchDTO);
            }
            return ResponseEntity.ok(ApiResponseListDTO.success("获取成功", applySearchDTOList));
        } catch (Exception e) {
            logger.error("管理员查询成绩修改申请失败", e);
            return ResponseEntity.badRequest().body(ApiResponseListDTO.error(500, e.getMessage()));
        }
    }

    /** 管理员审核成绩修改申请
     * @param apply_id 申请ID
     * @param reviewApplyDTO 审核DTO
     * @param request 请求对象
     * @return 修改后申请
     */
    @PutMapping("/admin/grade-applies/{apply_id}/review")
    public ResponseEntity<ApiResponseDTO<Apply>> ReviewApply(
            @PathVariable("apply_id") int apply_id,
            @RequestBody ReviewApplyDTO reviewApplyDTO,
            HttpServletRequest request
    ) {
        int audit_status = reviewApplyDTO.getAuditStatus();
        String audit_reason = reviewApplyDTO.getAuditReason();
        logger.info("管理员审核成绩修改申请, apply_id: {}, audit_status: {}", apply_id, audit_status);
        int admin_id = (int) request.getAttribute("userId");
        try {
            Apply apply = applyService.reviewApply(apply_id, admin_id, audit_status,audit_reason);
            return ResponseEntity.ok(ApiResponseDTO.success("审核操作成功",apply));
        } catch (Exception e) {
            logger.error("管理员审核成绩修改申请失败", e);
            return ResponseEntity.badRequest().body(ApiResponseDTO.error(500, e.getMessage()));
        }
    }

}
