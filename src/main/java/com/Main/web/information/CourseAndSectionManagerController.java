package com.Main.web.information;

import com.Main.dto.information.*;
import com.Main.dto.information.ApiResponseDTO;
import com.Main.dto.information.ApiResponseListDTO;
import com.Main.dto.information.CourseManagerDTO;
import com.Main.dto.information.PageResponseDTO;
import com.Main.dto.information.ReturnCourseDTO;
import com.Main.dto.information.SectionManagerDTO;
import com.Main.dto.information.SectionSearchDTO;
import com.Main.entity.information.Classroom;
import com.Main.entity.information.Course;
import com.Main.entity.information.Section;
import com.Main.service.information.ClassroomService;
import com.Main.service.information.CourseService;
import com.Main.service.information.SectionService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/information/api/v1/teacher")
public class CourseAndSectionManagerController {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private CourseService courseService;
    @Autowired
    private SectionService sectionService;
    @Autowired
    private ClassroomService classroomService;

    /**
     * 创建课程
     * @param request HTTP请求对象
     * @param courseManagerDTO 课程创建/修改DTO
     * @return 新创建的课程
     */
    @PostMapping("/courses")
    public ResponseEntity<ApiResponseDTO<Course>> createCourse(
            HttpServletRequest request,
            @RequestBody CourseManagerDTO courseManagerDTO
    ){
        try {
            String course_name = courseManagerDTO.getCourseName();
            String course_description = courseManagerDTO.getCourseDescription();
            float credit = courseManagerDTO.getCredit();
            String category = courseManagerDTO.getCategory();
            int hours_per_week = courseManagerDTO.getHours_per_week();
            // 从请求属性中获取用户ID（由JWT拦截器设置）
            Integer userId = (Integer) request.getAttribute("userId");
            logger.info("创建课程: userId={}, course_name={}, course_description={}, credit={}, category={}, hours_per_week={}",
                    userId, course_name, course_description, credit, category, hours_per_week);

            // 创建课程
            Course course = courseService.createCourse(course_name, course_description, credit, category, userId, hours_per_week);
            return ResponseEntity.ok(ApiResponseDTO.success("课程创建成功", course));
        } catch (Exception e) {
            logger.error("创建课程过程中发生未知错误", e);
            return ResponseEntity.ok(ApiResponseDTO.error(500, "服务器内部错误"));
        }
    }

    /**
     * 修改课程信息
     * @param request HTTP请求对象
     * @param courseManagerDTO 课程创建/修改DTO
     * @return 新创建的课程节
     */
    @PutMapping("/courses/{course_id}")
    public ResponseEntity<ApiResponseDTO<Course>> updateCourse(
            HttpServletRequest request,
            @PathVariable("course_id") Integer course_id,
            @RequestBody CourseManagerDTO courseManagerDTO
    ){
        try {
            // 从请求属性中获取用户ID和角色（由JWT拦截器设置）
            Integer userId = (Integer) request.getAttribute("userId");
            String role = (String) request.getAttribute("userRole");

            // 验证用户身份是否为教师
            if (!"t".equals(role)) {
                logger.warn("无权限修改课程: userId={}, role={}", userId, role);
                return ResponseEntity.ok(ApiResponseDTO.error(403, "无权限修改课程"));
            }
            String course_name = courseManagerDTO.getCourseName();
            String course_description = courseManagerDTO.getCourseDescription();
            float credit = courseManagerDTO.getCredit();
            String category = courseManagerDTO.getCategory();
            int hours_per_week = courseManagerDTO.getHours_per_week();
            logger.info("修改课程: userId={}, course_id={}, course_name={}, course_description={}, credit={}, category={}, hours_per_week={}",
                    userId, course_id, course_name, course_description, credit, category, hours_per_week);

            // 修改课程
            Course course = courseService.updateCourse(course_id, course_name, course_description, credit, category, userId, hours_per_week);
            return ResponseEntity.ok(ApiResponseDTO.success("课程更新成功", course));
        } catch (Exception e) {
            logger.error("修改课程过程中发生未知错误", e);
            return ResponseEntity.ok(ApiResponseDTO.error(500, "服务器内部错误"));
        }
    }

    /**
     * 删除课程
     * @param request HTTP请求对象
     * @param course_id 课程ID
     * @return 删除结果
     */
    @DeleteMapping("/courses/{course_id}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteCourse(
            HttpServletRequest request,
            @PathVariable("course_id") Integer course_id
    ){
        try {
            // 从请求属性中获取用户ID（由JWT拦截器设置）
            Integer userId = (Integer) request.getAttribute("userId");
            logger.info("删除课程: userId={}, course_id={}", userId, course_id);

            // 检查开课表是否为空
            List<SectionSearchDTO> section = sectionService.getSections(course_id,null,0);
            if (section != null && !section.isEmpty()) {
                logger.warn("课程存在开课信息，无法删除: course_id={}", course_id);
                return ResponseEntity.ok(ApiResponseDTO.error(400, "课程存在开课信息，无法删除"));
            }

            // 删除课程
            courseService.deleteCourse(course_id,userId);
            return ResponseEntity.ok(ApiResponseDTO.success("课程删除成功", null));
        } catch (Exception e) {
            logger.error("删除课程过程中发生未知错误", e);
            return ResponseEntity.ok(ApiResponseDTO.error(500, "服务器内部错误"));
        }

    }

    /**
     * 创建开课信息
     * @param request HTTP请求对象
     * @param sectionManagerDTO 开课信息创建/修改DTO
     * @return 新创建的开课信息
     */
    @PostMapping("/courses/{course_id}/sections")
    public ResponseEntity<ApiResponseDTO<Section>> createSection(
            HttpServletRequest request,
            @PathVariable("course_id") Integer course_id,
            @RequestBody SectionManagerDTO sectionManagerDTO
    ){
        try {
            // 从请求属性中获取用户ID（由JWT拦截器设置）
            Integer userId = (Integer) request.getAttribute("userId");
            int classroom_id = sectionManagerDTO.getClassroomId();
            int sec_year = sectionManagerDTO.getSecYear();
            String sec_time = sectionManagerDTO.getSecTime();
            String semester = sectionManagerDTO.getSemester();
            int capacity = sectionManagerDTO.getCapacity();
            logger.info("创建开课信息:  classroom_id={}, sec_year={}, sec_time={}, semester={}, capacity={}",
                    classroom_id, sec_year, sec_time, semester, capacity);
            String role = (String) request.getAttribute("userRole");
            if(!role.equals("t")){
                throw new RuntimeException("无权限创建开课信息");
            }
            // 检查课程是否存在
            ReturnCourseDTO course = courseService.getCourseById(course_id);
            if (course == null) {
                logger.warn("课程不存在，无法创建开课信息: course_id={}", course_id);
                return ResponseEntity.ok(ApiResponseDTO.error(404, "课程不存在"));
            }
            else if (course.getTeacher_id() != userId){
                logger.warn("无权限创建开课信息: userId={}, course_id={}", userId, course_id);
                return ResponseEntity.ok(ApiResponseDTO.error(403, "无权限创建开课信息"));
            }
            // 创建开课信息
            Section section = sectionService.createSection(course_id, classroom_id, capacity, semester, sec_year, sec_time);
            return ResponseEntity.ok(ApiResponseDTO.success("开课信息创建成功", section));
        } catch (Exception e) {
            logger.error("创建开课信息过程中发生未知错误", e);
            return ResponseEntity.ok(ApiResponseDTO.error(500, "服务器内部错误"));
        }
    }
    /**
     * 修改开课信息
     * @param request HTTP请求对象
     * @param sectionManagerDTO 开课信息创建/修改DTO
     * @return 修改后的开课信息
     */
    @PutMapping("/sections/{section_id}")
    public ResponseEntity<ApiResponseDTO<Section>> updateSection(
            HttpServletRequest request,
            @PathVariable("section_id") Integer section_id,
            @RequestBody SectionManagerDTO sectionManagerDTO
    ){
        try {
            // 从请求属性中获取用户ID（由JWT拦截器设置）
            Integer userId = (Integer) request.getAttribute("userId");
            int classroom_id = sectionManagerDTO.getClassroomId();
            int sec_year = sectionManagerDTO.getSecYear();
            String sec_time = sectionManagerDTO.getSecTime();
            String semester = sectionManagerDTO.getSemester();
            int capacity = sectionManagerDTO.getCapacity();
            logger.info("修改开课信息: section_id={}, classroom_id={}, sec_year={}, sec_time={}, semester={}, capacity={}",
                    section_id, classroom_id, sec_year, sec_time, semester, capacity);
            String role = (String) request.getAttribute("userRole");
            if(!role.equals("t")){
                throw new RuntimeException("无权限修改开课信息");
            }
            // 检查课程是否存在
            SectionSearchDTO oldsection = sectionService.getSectionById(section_id);
            int course_id = oldsection.getCourseId();
            ReturnCourseDTO course = courseService.getCourseById(course_id);
            if( course == null) {
                logger.warn("课程不存在，无法修改开课信息: course_id={}", course_id);
                return ResponseEntity.ok(ApiResponseDTO.error(404, "课程不存在"));
            }
            else if (course.getTeacher_id() != userId){
                logger.warn("无权限修改开课信息: userId={}, course_id={}", userId, course_id);
                return ResponseEntity.ok(ApiResponseDTO.error(403, "无权限修改开课信息"));
            }
            // 计算容量变化
            if (capacity < oldsection.getCapacity() - oldsection.getAvailable_capacity()) {
                logger.warn("修改后的容量小于已选学生数量，无法修改开课信息: section_id={}, new_capacity={}", section_id, capacity);
                return ResponseEntity.ok(ApiResponseDTO.error(400, "修改后的容量小于已选学生数量，无法修改开课信息"));
            }
            int offset = capacity - oldsection.getCapacity();
            int available_capacity = oldsection.getAvailable_capacity() + offset;
            // 修改开课信息
            Section section = sectionService.updateSection(section_id, classroom_id, capacity, semester, sec_year, sec_time, available_capacity);
            return ResponseEntity.ok(ApiResponseDTO.success("开课信息更新成功", section));
        } catch (Exception e) {
            logger.error("修改开课信息过程中发生未知错误", e);
            return ResponseEntity.ok(ApiResponseDTO.error(500, "服务器内部错误"));
        }
    }

    /**
     * 删除开课信息
     * @param request HTTP请求对象
     * @param section_id 开课信息ID
     * @return 删除结果
     */
    @DeleteMapping("/sections/{section_id}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteSection(
            HttpServletRequest request,
            @PathVariable("section_id") Integer section_id
    ){
        try {
            // 从请求属性中获取用户ID（由JWT拦截器设置）
            Integer userId = (Integer) request.getAttribute("userId");
            logger.info("删除开课信息: section_id={}", section_id);
            String role = (String) request.getAttribute("userRole");
            if(!role.equals("t")){
                throw new RuntimeException("无权限删除开课信息");
            }

            // 检查课程是否存在
            SectionSearchDTO oldsection = sectionService.getSectionById(section_id);
            int course_id = oldsection.getCourseId();
            ReturnCourseDTO course = courseService.getCourseById(course_id);
            if( course == null) {
                logger.warn("课程不存在，无法删除开课信息: course_id={}", course_id);
                return ResponseEntity.ok(ApiResponseDTO.error(404, "课程不存在"));
            }
            else if (course.getTeacher_id() != userId){
                logger.warn("无权限删除开课信息: userId={}, course_id={}", userId, course_id);
                return ResponseEntity.ok(ApiResponseDTO.error(403, "无权限删除开课信息"));
            }

            // 检查是否有学生选课
            if (oldsection.getCapacity() != oldsection.getAvailable_capacity()) {
                logger.warn("开课信息存在选课学生，无法删除: section_id={}", section_id);
                return ResponseEntity.ok(ApiResponseDTO.error(400, "开课信息存在选课学生，无法删除"));
            }
            // 删除开课信息
            sectionService.deleteSection(section_id);
            return ResponseEntity.ok(ApiResponseDTO.success("开课信息删除成功",null));
        } catch (Exception e) {
            logger.error("删除开课信息过程中发生未知错误", e);
            return ResponseEntity.ok(ApiResponseDTO.error(500, "服务器内部错误"));
        }
    }
    /**
     * 获取开设课程列表（支持分页和筛选）
     * @param page 页码
     * @param size 每页数量
     * @param course_name 按课程名字模糊查询
     * @param teacher_name 按教师名字模糊筛选
     * @param category 按课程类别筛选
     * @param request HTTP请求对象
     * @return 分页课程列表
     */
    @GetMapping("/my-courses")
    public ResponseEntity<ApiResponseDTO<PageResponseDTO<ReturnCourseDTO>>> getTeacherCourses(
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size,
            @RequestParam(required = false) String course_name,
            @RequestParam(required = false) String teacher_name,
            @RequestParam(required = false) String category,
            HttpServletRequest request){
        try {
            // 从请求属性中获取用户ID（由JWT拦截器设置）
            Integer userId = (Integer) request.getAttribute("userId");
            logger.info("获取课程列表: userId={}, page={}, size={}, course_name={}, teacher_name={}, category={}",
                    userId, page, size, course_name, teacher_name, category);

            // 获取课程列表
            PageResponseDTO<ReturnCourseDTO> courses = courseService.getCourses(page, size, course_name, userId, teacher_name, category);
            return ResponseEntity.ok(ApiResponseDTO.success("获取成功", courses));
        } catch (Exception e) {
            logger.error("获取课程列表过程中发生未知错误", e);
            return ResponseEntity.ok(ApiResponseDTO.error(500, "服务器内部错误"));
        }
    }

    /**
     * 获取开课列表
     * @param courseId 课程ID
     * @param semester  开课学期
     * @param sec_year 开课学年
     * @param request HTTP请求对象
     * @return 开课列表
     */
    @GetMapping("/my-courses/{course_id}/sections")
    public ResponseEntity<ApiResponseListDTO<SectionSearchDTO>> getTeacherSections(
            @PathVariable("course_id") Integer courseId,
            @RequestParam(value = "semester", required = false) String semester,
            @RequestParam(value = "sec_year", required = false) Integer sec_year,
            HttpServletRequest request)
    {
        try{
            if(sec_year == null) sec_year = 0;
            // 从请求属性中获取用户ID（由JWT拦截器设置）
            Integer userId = (Integer) request.getAttribute("userId");
            logger.info("获取开课列表: userId={}, course_id={}, semester={}, sec_year={}",
                    userId, courseId, semester, sec_year);
            // 检查课程是否存在
            ReturnCourseDTO course = courseService.getCourseById(courseId);
            if (course == null) {
                logger.warn("课程不存在，无法获取开课列表: course_id={}", courseId);
                return ResponseEntity.ok(ApiResponseListDTO.error(404, "课程不存在"));
            }
            else if (course.getTeacher_id() != userId){
                logger.warn("无权限获取开课列表: userId={}, course_id={}", userId, courseId);
                return ResponseEntity.ok(ApiResponseListDTO.error(403, "无权限获取开课列表"));
            }
            // 获取开课列表
            List<SectionSearchDTO> searchDTO = sectionService.getSections(courseId, semester, sec_year);
            return ResponseEntity.ok(ApiResponseListDTO.success("获取成功",searchDTO));
        } catch (Exception e) {
            logger.error("获取开课列表过程中发生未知错误", e);
            return ResponseEntity.ok(ApiResponseListDTO.error(500, "服务器内部错误"));
        }
    }
}
