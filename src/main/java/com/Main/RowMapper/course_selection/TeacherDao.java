package com.Main.RowMapper.course_selection;

import com.Main.entity.course_selection.Course;
import java.util.List;

/**
 * 教师数据访问接口
 */
public interface TeacherDao {
    
    /**
     * 根据教师ID查询其教授的课程
     *
     * @param teacherId 教师ID
     * @return 课程列表
     */
    List<Course> findCoursesByTeacherId(Integer teacherId);
    
    /**
     * 检查教师是否存在
     *
     * @param teacherId 教师ID
     * @return 是否存在
     */
    boolean existsById(Integer teacherId);
}