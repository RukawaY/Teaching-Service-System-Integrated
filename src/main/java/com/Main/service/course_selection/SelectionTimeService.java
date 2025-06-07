package com.Main.service.course_selection;

import com.Main.entity.course_selection.SelectionTime;

/**
 * 选课时间服务接口
 */
public interface SelectionTimeService {
    
    /**
     * 获取选课时间配置
     * 
     * @return 选课系统时间配置
     */
    SelectionTime getSelectionTime();
    
    /**
     * 更新选课时间配置
     * 
     * @param selectionTime 选课系统时间配置
     * @return 是否更新成功
     */
    boolean updateSelectionTime(SelectionTime selectionTime);
    
    /**
     * 检查当前时间是否在正选时间段内
     * 
     * @return 是否在正选时间段内
     */
    boolean isInFirstSelectionTime();
    
    /**
     * 检查当前时间是否在补选时间段内
     * 
     * @return 是否在补选时间段内
     */
    boolean isInSecondSelectionTime();
    
    /**
     * 检查当前时间是否在退课时间段内
     * 
     * @return 是否在退课时间段内
     */
    boolean isInDropTime();
} 