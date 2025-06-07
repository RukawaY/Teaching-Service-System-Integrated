package com.Main.service.course_selection.impl;

import com.Main.RowMapper.course_selection.SelectionTimeDao;
import com.Main.entity.course_selection.SelectionTime;
import com.Main.service.course_selection.SelectionTimeService;
import com.Main.util.course_selection.TimeUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 选课时间服务实现类
 */
@Service
public class SelectionTimeServiceImpl implements SelectionTimeService {

    private static final Logger logger = LoggerFactory.getLogger(SelectionTimeServiceImpl.class);
    
    @Autowired
    private SelectionTimeDao selectionTimeDao;
    
    @Override
    public SelectionTime getSelectionTime() {
        return selectionTimeDao.getSelectionTime();
    }
    
    @Override
    public boolean updateSelectionTime(SelectionTime selectionTime) {
        return selectionTimeDao.updateSelectionTime(selectionTime);
    }
    
    @Override
    public boolean isInFirstSelectionTime() {
        SelectionTime selectionTime = getSelectionTime();
        if (selectionTime == null || selectionTime.getFirstTimeList() == null) {
            logger.warn("Selection time or first time list is null");
            return false;
        }
        
        return TimeUtils.isTimeInRanges(selectionTime.getFirstTimeList());
    }
    
    @Override
    public boolean isInSecondSelectionTime() {
        SelectionTime selectionTime = getSelectionTime();
        if (selectionTime == null || selectionTime.getSecondTimeList() == null) {
            logger.warn("Selection time or second time list is null");
            return false;
        }
        
        return TimeUtils.isTimeInRanges(selectionTime.getSecondTimeList());
    }
    
    @Override
    public boolean isInDropTime() {
        SelectionTime selectionTime = getSelectionTime();
        if (selectionTime == null || selectionTime.getDropTimeList() == null) {
            logger.warn("Selection time or drop time list is null");
            return false;
        }
        
        return TimeUtils.isTimeInRanges(selectionTime.getDropTimeList());
    }
} 