package com.Main.RowMapper.course_selection;

/**
 * GradeBase数据访问接口
 */
public interface GradeBaseDao {
    /**
     * 插入初始成绩记录
     * 
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @param sectionId 开课ID
     * @return 是否插入成功
     */
    boolean insertInitialGrade(Integer studentId, Integer courseId, Integer sectionId);
    
    /**
     * 检查成绩记录是否已存在
     * 
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @param sectionId 开课ID
     * @return 是否存在
     */
    boolean existsGrade(Integer studentId, Integer courseId, Integer sectionId);
} 