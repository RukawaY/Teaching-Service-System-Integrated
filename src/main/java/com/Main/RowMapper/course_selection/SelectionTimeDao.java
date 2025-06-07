package com.Main.RowMapper.course_selection;

import com.Main.entity.course_selection.SelectionTime;

/**
 * 选课时间数据访问接口
 */
public interface SelectionTimeDao {
    
    /**
     * 获取选课系统时间配置
     * 
     * @return 选课系统时间配置
     */
    SelectionTime getSelectionTime();
    
    /**
     * 更新选课系统时间配置
     * 
     * @param selectionTime 选课系统时间配置
     * @return 是否更新成功
     */
    boolean updateSelectionTime(SelectionTime selectionTime);
}