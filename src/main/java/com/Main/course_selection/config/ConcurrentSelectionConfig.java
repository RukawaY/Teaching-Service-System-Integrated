package com.Main.course_selection.config;

import com.Main.entity.course_selection.SelectionTime;
import com.Main.service.course_selection.SelectionTimeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 并发选课配置，用于控制同时选课人数
 */
@Component
public class ConcurrentSelectionConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(ConcurrentSelectionConfig.class);
    
    // 当前正在选课的人数
    private final AtomicInteger currentSelections = new AtomicInteger(0);
    
    // 用于控制同时选课人数的信号量
    private Semaphore selectionSemaphore;
    
    // 学生选课状态缓存，记录每个学生是否正在选课中
    private final Map<Integer, Boolean> studentSelectionStatus = new ConcurrentHashMap<>();
    
    @Autowired
    private SelectionTimeService selectionTimeService;
    
    @PostConstruct
    public void init() {
        refreshSelectionSemaphore();
    }
    
    /**
     * 刷新选课信号量，根据数据库中的max_number值设置并发数
     */
    public void refreshSelectionSemaphore() {
        SelectionTime selectionTime = selectionTimeService.getSelectionTime();
        int maxSelections = selectionTime != null && selectionTime.getMaxNumber() != null ? 
                selectionTime.getMaxNumber() : 10; // 默认值为10
        
        // 更新信号量
        selectionSemaphore = new Semaphore(maxSelections, true);
        logger.info("Selection semaphore refreshed, max concurrent selections: {}", maxSelections);
    }
    
    /**
     * 学生开始选课，获取选课资格
     * @param studentId 学生ID
     * @return 是否获取到选课资格
     */
    public boolean acquireSelectionPermit(Integer studentId) {
        // 如果学生已经在选课中，直接返回true
        if (Boolean.TRUE.equals(studentSelectionStatus.get(studentId))) {
            return true;
        }
        
        try {
            // 尝试获取信号量，最多等待1秒
            boolean acquired = selectionSemaphore.tryAcquire(1000, java.util.concurrent.TimeUnit.MILLISECONDS);
            if (acquired) {
                currentSelections.incrementAndGet();
                studentSelectionStatus.put(studentId, true);
                logger.info("Student {} acquired selection permit, current selections: {}", studentId, currentSelections.get());
                return true;
            } else {
                logger.warn("Student {} failed to acquire selection permit, maximum limit reached: {}", studentId, currentSelections.get());
                return false;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Interrupted while acquiring selection permit", e);
            return false;
        }
    }
    
    /**
     * 学生完成选课，释放选课资格
     * @param studentId 学生ID
     */
    public void releaseSelectionPermit(Integer studentId) {
        // 只有当学生确实在选课中时才释放
        if (Boolean.TRUE.equals(studentSelectionStatus.get(studentId))) {
            studentSelectionStatus.put(studentId, false);
            currentSelections.decrementAndGet();
            selectionSemaphore.release();
            logger.info("Student {} released selection permit, current selections: {}", studentId, currentSelections.get());
        }
    }
    
    /**
     * 获取当前选课人数
     * @return 当前选课人数
     */
    public int getCurrentSelections() {
        return currentSelections.get();
    }
    
    /**
     * 获取最大允许选课人数
     * @return 最大允许选课人数
     */
    public int getMaxSelections() {
        return selectionSemaphore.availablePermits() + currentSelections.get();
    }
    
    /**
     * 清除学生选课状态
     * @param studentId 学生ID
     */
    public void clearStudentStatus(Integer studentId) {
        // 移除学生状态并释放可能占用的资源
        Boolean status = studentSelectionStatus.remove(studentId);
        if (Boolean.TRUE.equals(status)) {
            currentSelections.decrementAndGet();
            selectionSemaphore.release();
            logger.info("Cleared selection status for student {}, resources released", studentId);
        }
    }
} 