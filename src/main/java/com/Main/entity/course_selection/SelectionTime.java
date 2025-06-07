package com.Main.entity.course_selection;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * 选课系统时间配置实体类
 */
public class SelectionTime {
    private Integer maxNumber;
    private List<String> firstTimeList;
    private List<String> secondTimeList;
    private List<String> dropTimeList;
    
    @JsonProperty("max_number")
    public Integer getMaxNumber() {
        return maxNumber;
    }
    
    public void setMaxNumber(Integer maxNumber) {
        this.maxNumber = maxNumber;
    }
    
    @JsonProperty("first_time_list")
    public List<String> getFirstTimeList() {
        return firstTimeList;
    }
    
    public void setFirstTimeList(List<String> firstTimeList) {
        this.firstTimeList = firstTimeList;
    }
    
    @JsonProperty("second_time_list")
    public List<String> getSecondTimeList() {
        return secondTimeList;
    }
    
    public void setSecondTimeList(List<String> secondTimeList) {
        this.secondTimeList = secondTimeList;
    }
    
    @JsonProperty("drop_time_list")
    public List<String> getDropTimeList() {
        return dropTimeList;
    }
    
    public void setDropTimeList(List<String> dropTimeList) {
        this.dropTimeList = dropTimeList;
    }
}