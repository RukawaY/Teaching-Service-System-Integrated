package com.Main.service.course_selection;

import com.Main.dto.course_selection.CourseListDTO;
import com.Main.dto.course_selection.CourseTableDTO;
import com.Main.dto.course_selection.CurriculumDTO;
import com.Main.dto.course_selection.SupplementResultListDTO;

/**
 * 学生服务接口
 */
public interface StudentService {

    /**
     * 搜索课程
     *
     * @param courseName  课程名称
     * @param teacherName 教师名称
     * @param courseId    课程ID
     * @param studentId   学生ID
     * @param needAvailable 若为true，仅返回有余量的课程
     * @return 课程列表
     */
    CourseListDTO searchCourse(String courseName, String teacherName, Integer courseId, Integer studentId, Boolean needAvailable);

    /**
     * 搜索课程表
     *
     * @param courseId   课程ID
     * @param courseName 课程名称  
     * @param category   课程类别
     * @return 课程表数据
     */
    CourseTableDTO searchCourseTable(Integer courseId, String courseName, String category);

    /**
     * 学生选择课程
     *
     * @param studentId 学生ID
     * @param courseId  课程ID
     * @return 是否选课成功
     */
    String chooseCourse(Integer studentId, Integer courseId);

    /**
     * 学生退课
     *
     * @param studentId 学生ID
     * @param courseId  课程ID
     * @return 是否退课成功
     *
     */
    boolean dropCourse(Integer studentId, Integer courseId);

    /**
     * 获取学生已选课程
     *
     * @param studentId 学生ID
     * @return 已选课程列表
     */
    CourseListDTO getSelectedCourses(Integer studentId);

    /**
     * 获取专业培养方案
     *
     * @param majorName 专业名称
     * @return 专业培养方案
     */
    CurriculumDTO getCurriculum(String majorName);

    /**
     * 获取个人培养方案
     *
     * @param studentId 学生ID
     * @return 个人培养方案
     */
    CurriculumDTO getPersonalCurriculum(Integer studentId);

    /**
     * 设置个人培养方案
     *
     * @param studentId    学生ID
     * @param curriculumDTO 培养方案DTO
     * @return 操作结果信息
     */
    String setPersonalCurriculum(Integer studentId, CurriculumDTO curriculumDTO);

    /**
     * 学生申请补选课程
     *
     * @param studentId 学生ID
     * @param sectionId 开课ID
     * @return 操作结果信息
     */
    String applySupplementCourse(Integer studentId, Integer sectionId);

    /**
     * 获取学生补选结果
     *
     * @param studentId 学生ID
     * @return 补选结果列表
     */
    SupplementResultListDTO getSupplementResult(Integer studentId);
}