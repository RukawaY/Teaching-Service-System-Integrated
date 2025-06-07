package com.Main.web.information;

import com.Main.dto.information.ApiResponseDTO;
import com.Main.dto.information.PageResponseDTO;
import com.Main.dto.information.ReturnCourseDTO;
import com.Main.entity.information.Course;
import com.Main.service.information.CourseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/information/api/v1/courses")
public class CourseInformationController {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private CourseService courseService;

    /**
     * 获取课程列表（支持分页和筛选）
     * @param page 页码
     * @param size 每页数量
     * @param course_name 按课程名字模糊查询
     * @param teacher_id 按教师查询
     * @param teacher_name 按教师名字模糊筛选
     * @param category 按课程类别筛选
     * @return 分页课程列表
     */
    @GetMapping
    public ResponseEntity<ApiResponseDTO<PageResponseDTO<ReturnCourseDTO>>> getCourseList(
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size,
            @RequestParam(required = false) String course_name,
            @RequestParam(required = false) Integer teacher_id,
            @RequestParam(required = false) String teacher_name,
            @RequestParam(required = false) String category)
    {

            try{
                PageResponseDTO<ReturnCourseDTO> courses = courseService.getCourses(page, size, course_name, teacher_id, teacher_name, category);
                return ResponseEntity.ok(ApiResponseDTO.success("获取成功",courses));
            }catch (Exception e){
                logger.error("获取课程列表失败: {}", e.getMessage());
                return ResponseEntity.ok(ApiResponseDTO.error(500, "服务器内部错误: " + e.getMessage()));
            }
    }

    /**
     * 获取课程信息
     * @param course_id 页码
     * @return 课程信息
     */
    @GetMapping("/{course_id}")
    public ResponseEntity<ApiResponseDTO<ReturnCourseDTO>> getCourseById(
            @PathVariable int course_id)
    {
        try{
            ReturnCourseDTO course = courseService.getCourseById(course_id);
            return ResponseEntity.ok(ApiResponseDTO.success("获取成功",course));
        }catch (RuntimeException e) {
            logger.error("获取课程信息失败: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponseDTO.error(404, e.getMessage()));
        } catch (Exception e){
            logger.error("获取课程列表失败: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponseDTO.error(500, "服务器内部错误: " + e.getMessage()));
        }
    }

}
