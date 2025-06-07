package com.Main.service.course_selection;

import com.Main.dto.course_selection.SupplementListDTO;
import com.Main.dto.course_selection.SupplementApplicationDTO;
import com.Main.dto.course_selection.CurriculumDTO;
import com.Main.entity.course_selection.SelectionTime;

/**
 * 管理员服务接口
 */
public interface ManagerService {
    
    /**
     * 更新选课系统时间
     *
     * @param selectionTime 选课系统时间配置
     * @return 是否更新成功
     */
    boolean updateSelectionTime(SelectionTime selectionTime);
    
    /**
     * 设置专业培养方案
     *
     * @param curriculumDTO 培养方案DTO
     * @return 是否设置成功
     */
    String setCurriculum(CurriculumDTO curriculumDTO);
    
    /**
     * 获取选课系统时间配置
     *
     * @return 选课系统时间配置
     */
    SelectionTime getSelectionTime();
    
    /**
     * 管理员为学生选课
     *
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @return 是否选课成功
     */
    String chooseCourseForStudent(Integer studentId, Integer courseId);
    
    /**
     * 获取补选申请列表
     *
     * @return 补选申请列表
     */
    SupplementListDTO getSupplementApplications();
    
    /**
     * 获取补选申请列表，可按课程ID筛选
     *
     * @param courseId 课程ID，可为null
     * @return 补选申请列表
     */
    SupplementListDTO getSupplementApplications(Integer courseId);
    
    /**
     * 处理补选申请
     *
     * @param supplementId 补选申请ID
     * @param approved 是否批准
     * @return 是否处理成功
     */
    String processSupplementApplication(Integer supplementId, Boolean approved);
}