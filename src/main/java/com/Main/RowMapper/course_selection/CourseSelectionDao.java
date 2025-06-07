package com.Main.RowMapper.course_selection;

import com.Main.entity.course_selection.CourseSelection;
import java.util.List;

/**
 * 选课记录数据访问接口
 */
public interface CourseSelectionDao {
    /**
     * 检查学生是否已选某开课
     */
    boolean isStudentSelectedCourse(Integer studentId, Integer sectionId);

    /**
     * 保存选课记录
     */
    boolean saveSelection(Integer studentId, Integer sectionId);

    /**
     * 检查选课记录是否存在
     */
    boolean existsByStudentIdAndSectionId(Integer studentId, Integer sectionId);

    /**
     * 插入选课记录
     */
    boolean insertSelection(Integer studentId, Integer sectionId);

    /**
     * 根据学生ID获取选课列表
     */
    List<CourseSelection> findByStudentId(Integer studentId);

    /**
     * 根据开课ID获取选课列表
     */
    List<CourseSelection> findBySectionId(Integer sectionId);

    /**
     * 删除选课记录
     */
    boolean deleteSelection(Integer studentId, Integer sectionId);

    /**
     * 获取选课记录总数
     */
    int countBySectionId(Integer sectionId);

    /**
     * 检查学生是否已选某课程的任何开课
     */
    boolean hasStudentSelectedCourse(Integer studentId, Integer courseId);
}