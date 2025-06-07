package com.Main.service.course_selection;

import com.Main.dto.course_selection.TeacherCourseListDTO;

/**
 * 教师服务接口
 */
public interface TeacherService {
    
    /**
     * 获取教师课程及选课学生信息
     *
     * @param teacherId 教师ID
     * @return 教师课程及选课学生列表
     */
    TeacherCourseListDTO getCourseStudents(Integer teacherId);
}