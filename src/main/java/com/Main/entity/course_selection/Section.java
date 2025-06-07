package com.Main.entity.course_selection;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 开课信息实体类
 */
public class Section {
    private Integer sectionId;
    private Integer courseId;
    private Integer classroomId;
    private Integer capacity;
    private Integer availableCapacity;
    private String semester;
    private Integer secYear;
    private String secTime;
    
    @JsonProperty("section_id")
    public Integer getSectionId() {
        return sectionId;
    }
    
    public void setSectionId(Integer sectionId) {
        this.sectionId = sectionId;
    }
    
    @JsonProperty("course_id")
    public Integer getCourseId() {
        return courseId;
    }
    
    public void setCourseId(Integer courseId) {
        this.courseId = courseId;
    }
    
    @JsonProperty("classroom_id")
    public Integer getClassroomId() {
        return classroomId;
    }
    
    public void setClassroomId(Integer classroomId) {
        this.classroomId = classroomId;
    }
    
    public Integer getCapacity() {
        return capacity;
    }
    
    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }
    
    @JsonProperty("available_capacity")
    public Integer getAvailableCapacity() {
        return availableCapacity;
    }
    
    public void setAvailableCapacity(Integer availableCapacity) {
        this.availableCapacity = availableCapacity;
    }
    
    public String getSemester() {
        return semester;
    }
    
    public void setSemester(String semester) {
        this.semester = semester;
    }
    
    @JsonProperty("sec_year")
    public Integer getSecYear() {
        return secYear;
    }
    
    public void setSecYear(Integer secYear) {
        this.secYear = secYear;
    }
    
    @JsonProperty("sec_time")
    public String getSecTime() {
        return secTime;
    }
    
    public void setSecTime(String secTime) {
        this.secTime = secTime;
    }
} 