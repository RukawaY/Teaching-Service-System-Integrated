package com.Main.RowMapper.course_selection;

import com.Main.entity.course_selection.Section;
import java.util.List;

/**
 * 开课信息DAO接口
 */
public interface SectionDao {
    /**
     * 根据开课ID查询开课信息
     *
     * @param sectionId 开课ID
     * @return 开课信息对象
     */
    Section findById(Integer sectionId);
    
    /**
     * 检查开课信息是否存在
     *
     * @param sectionId 开课ID
     * @return 是否存在
     */
    boolean existsById(Integer sectionId);
    
    /**
     * 检查开课是否已满
     *
     * @param sectionId 开课ID
     * @return 是否已满
     */
    boolean isFull(Integer sectionId);
    
    /**
     * 减少开课可用容量
     *
     * @param sectionId 开课ID
     * @return 是否更新成功
     */
    boolean decreaseAvailableCapacity(Integer sectionId);
    
    /**
     * 增加开课可用容量
     *
     * @param sectionId 开课ID
     * @return 是否更新成功
     */
    boolean increaseAvailableCapacity(Integer sectionId);
    
    /**
     * 获取开课时间
     *
     * @param sectionId 开课ID
     * @return 开课时间
     */
    String getSectionTime(Integer sectionId);
    
    /**
     * 根据课程ID查询所有开课信息
     *
     * @param courseId 课程ID
     * @return 开课信息列表
     */
    List<Section> findByCourseId(Integer courseId);
} 