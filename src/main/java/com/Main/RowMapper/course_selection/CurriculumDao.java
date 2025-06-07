package com.Main.RowMapper.course_selection;

import com.Main.dto.course_selection.CurriculumDTO;

/**
 * 培养方案数据访问接口
 */
public interface CurriculumDao {
    
    /**
     * 检查专业是否存在
     * 
     * @param majorName 专业名称
     * @return 是否存在
     */
    boolean checkMajorExists(String majorName);
    
    /**
     * 删除专业培养方案
     * 
     * @param majorId 专业ID
     */
    void deleteCurriculumByMajorId(Integer majorId);
    
    /**
     * 删除专业培养方案（通过专业名称）
     * 
     * @param majorName 专业名称
     */
    void deleteCurriculumByMajorName(String majorName);

    
    /**
     * 保存专业培养方案
     * 
     * @param majorName 专业名称
     * @param curriculumJson 培养方案JSON
     * @return 是否保存成功
     */
    boolean saveCurriculum(String majorName, String curriculumJson);
    
}