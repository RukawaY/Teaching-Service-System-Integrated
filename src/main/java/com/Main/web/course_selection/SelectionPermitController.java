package com.Main.web.course_selection;

import com.Main.course_selection.config.ConcurrentSelectionConfig;
import com.Main.dto.course_selection.ResponseDTO;
import com.Main.entity.course_selection.SelectionTime;
import com.Main.service.course_selection.SelectionTimeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 选课权限控制器
 */
@RestController
@RequestMapping("/api/course_selection/permit")
public class SelectionPermitController {

    private static final Logger logger = LoggerFactory.getLogger(SelectionPermitController.class);

    @Autowired
    private ConcurrentSelectionConfig concurrentSelectionConfig;
    
    @Autowired
    private SelectionTimeService selectionTimeService;

    /**
     * 获取选课权限
     * @param request 包含学生ID的请求体
     * @return 是否获取成功
     */
    @PostMapping("/acquire")
    public ResponseDTO<?> acquireSelectionPermit(@RequestBody Map<String, Integer> request) {
        Integer studentId = request.get("student_id");
        if (studentId == null) {
            return ResponseDTO.fail("Missing student ID in request");
        }
        
        logger.info("Student {} is requesting selection permit", studentId);
        
        // 首先检查是否在选课时间内
        if (!selectionTimeService.isInFirstSelectionTime() && !selectionTimeService.isInSecondSelectionTime()) {
            return ResponseDTO.fail("Current time is not in course selection period");
        }
        
        boolean acquired = concurrentSelectionConfig.acquireSelectionPermit(studentId);
        
        if (acquired) {
            Map<String, Object> data = new HashMap<>();
            data.put("current_count", concurrentSelectionConfig.getCurrentSelections());
            data.put("max_count", concurrentSelectionConfig.getMaxSelections());
            return ResponseDTO.success(data);
        } else {
            return ResponseDTO.fail("Maximum number of concurrent selections reached, please try again later");
        }
    }

    /**
     * 释放选课权限
     * @param request 包含学生ID的请求体
     * @return 操作结果
     */
    @PostMapping("/release")
    public ResponseDTO<?> releaseSelectionPermit(@RequestBody Map<String, Integer> request) {
        Integer studentId = request.get("student_id");
        if (studentId == null) {
            return ResponseDTO.fail("Missing student ID in request");
        }
        
        logger.info("Student {} is requesting to release selection permit", studentId);
        
        concurrentSelectionConfig.releaseSelectionPermit(studentId);
        
        Map<String, Object> data = new HashMap<>();
        data.put("current_count", concurrentSelectionConfig.getCurrentSelections());
        data.put("max_count", concurrentSelectionConfig.getMaxSelections());
        return ResponseDTO.success(data);
    }
    
    /**
     * 获取选课计数信息
     * @return 选课计数信息
     */
    @GetMapping("/count")
    public ResponseDTO<?> getSelectionCount() {
        Map<String, Object> data = new HashMap<>();
        data.put("current_count", concurrentSelectionConfig.getCurrentSelections());
        data.put("max_count", concurrentSelectionConfig.getMaxSelections());
        
        SelectionTime selectionTime = selectionTimeService.getSelectionTime();
        if (selectionTime != null && selectionTime.getMaxNumber() != null) {
            data.put("configured_max", selectionTime.getMaxNumber());
        }
        
        return ResponseDTO.success(data);
    }
    
    /**
     * 清除学生的选课状态
     * @param request 包含学生ID的请求体
     * @return 操作结果
     */
    @PostMapping("/clear")
    public ResponseDTO<?> clearStudentStatus(@RequestBody Map<String, Integer> request) {
        Integer studentId = request.get("student_id");
        if (studentId == null) {
            return ResponseDTO.fail("Missing student ID in request");
        }
        
        logger.info("Clearing selection status for student {}", studentId);
        concurrentSelectionConfig.clearStudentStatus(studentId);
        return ResponseDTO.success();
    }
    
    /**
     * 手动刷新选课信号量
     * 用于管理员在更新max_number后手动刷新并发限制
     * @return 刷新结果
     */
    @PostMapping("/refresh_semaphore")
    public ResponseDTO<?> refreshSelectionSemaphore() {
        logger.info("Manually refreshing selection semaphore");
        concurrentSelectionConfig.refreshSelectionSemaphore();
        
        // 获取并返回当前的信号量状态
        Map<String, Object> data = new HashMap<>();
        data.put("current_count", concurrentSelectionConfig.getCurrentSelections());
        data.put("max_count", concurrentSelectionConfig.getMaxSelections());
        
        SelectionTime selectionTime = selectionTimeService.getSelectionTime();
        if (selectionTime != null && selectionTime.getMaxNumber() != null) {
            data.put("configured_max", selectionTime.getMaxNumber());
            data.put("applied_max", concurrentSelectionConfig.getMaxSelections());
        }
        
        return ResponseDTO.success(data);
    }
} 