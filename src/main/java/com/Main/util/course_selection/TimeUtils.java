package com.Main.util.course_selection;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 时间工具类
 */
public class TimeUtils {
    
    private static final Logger logger = LoggerFactory.getLogger(TimeUtils.class);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_ZONED_DATE_TIME;
    
    /**
     * 检查当前时间是否在指定的时间段内
     * 
     * @param timeList 时间段列表，应包含起止时间点，如 ["2023-01-01 00:00:00", "2023-01-10 00:00:00"]
     * @return 如果当前时间在任意一个时间段内则返回true，否则返回false
     */
    public static boolean isTimeInRanges(List<String> timeList) {
        if (timeList == null || timeList.size() < 2 || timeList.size() % 2 != 0) {
            logger.warn("Invalid time list format: {}", timeList);
            return false;
        }
        
        LocalDateTime now = LocalDateTime.now();
        logger.info("Current time: {}", now.format(FORMATTER));
        
        for (int i = 0; i < timeList.size(); i += 2) {
            try {
                String startTimeStr = timeList.get(i);
                String endTimeStr = timeList.get(i + 1);
                
                LocalDateTime startTime = parseDateTime(startTimeStr);
                LocalDateTime endTime = parseDateTime(endTimeStr);
                
                logger.info("Checking time range: {} to {}", startTimeStr, endTimeStr);
                
                if (now.isAfter(startTime) && now.isBefore(endTime)) {
                    logger.info("Current time is within range");
                    return true;
                }
            } catch (DateTimeParseException e) {
                logger.error("Error parsing time: {}", e.getMessage());
            }
        }
        
        logger.info("Current time is not within any range");
        return false;
    }
    
    /**
     * 解析时间字符串，支持多种格式
     * 
     * @param timeStr 时间字符串
     * @return LocalDateTime对象
     * @throws DateTimeParseException 如果无法解析时间格式
     */
    private static LocalDateTime parseDateTime(String timeStr) throws DateTimeParseException {
        if (timeStr == null || timeStr.trim().isEmpty()) {
            throw new DateTimeParseException("Time string is null or empty", timeStr, 0);
        }
        
        // 尝试解析ISO 8601格式 (2025-05-26T16:00:00.000Z)
        try {
            ZonedDateTime zonedDateTime = ZonedDateTime.parse(timeStr, ISO_FORMATTER);
            return zonedDateTime.toLocalDateTime();
        } catch (DateTimeParseException e) {
            logger.debug("Failed to parse as ISO format: {}", timeStr);
        }
        
        // 尝试解析标准格式 (yyyy-MM-dd HH:mm:ss)
        try {
            return LocalDateTime.parse(timeStr, FORMATTER);
        } catch (DateTimeParseException e) {
            logger.debug("Failed to parse as standard format: {}", timeStr);
        }
        
        // 尝试解析简化的ISO格式 (2025-05-26T16:00:00)
        try {
            return LocalDateTime.parse(timeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (DateTimeParseException e) {
            logger.debug("Failed to parse as ISO local format: {}", timeStr);
        }
        
        // 如果所有格式都失败，抛出异常
        throw new DateTimeParseException("Unable to parse time string with any supported format", timeStr, 0);
    }
} 