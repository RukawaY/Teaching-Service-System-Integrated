package com.Main.RowMapper.course_selection;

import com.Main.entity.course_selection.CourseSupplementApplication;
import java.util.List;

/**
 * 课程补选DAO接口
 */
public interface CourseSupplementDao {
    /**
     * 保存补选申请
     *
     * @param application 补选申请对象
     * @return 是否保存成功
     */
    boolean save(CourseSupplementApplication application);

    /**
     * 根据学生ID获取补选申请列表
     *
     * @param studentId 学生ID
     * @return 补选申请列表
     */
    List<CourseSupplementApplication> findByStudentId(String studentId);

    /**
     * 获取所有补选申请
     *
     * @return 补选申请列表
     */
    List<CourseSupplementApplication> findAll();

    /**
     * 更新补选申请状态
     *
     * @param applicationId 补选申请ID
     * @param status 状态（1-同意，0-拒绝）
     * @return 是否更新成功
     */
    boolean updateStatus(Integer applicationId, Integer status);
    
    /**
     * 获取待处理的补选申请列表
     *
     * @return 待处理的补选申请列表
     */
    List<CourseSupplementApplication> getPendingSupplements();
    
    /**
     * 保存学生的补选申请
     *
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @return 是否保存成功
     */
    boolean saveSupplementApplication(Integer studentId, Integer courseId);
    
    /**
     * 根据课程ID获取补选申请列表
     * 
     * @param courseId 课程ID
     * @return 补选申请列表
     */
    List<CourseSupplementApplication> findBySectionId(Integer sectionId);

    /**
     * 根据申请ID获取补选申请
     *
     * @param applicationId 补选申请ID
     * @return 补选申请对象，如果不存在则返回null
     */
    CourseSupplementApplication findById(Integer supplementId);
}