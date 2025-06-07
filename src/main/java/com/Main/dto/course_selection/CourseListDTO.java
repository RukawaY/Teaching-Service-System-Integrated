package com.Main.dto.course_selection;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import com.Main.entity.course_selection.Course;

/**
 * 课程列表数据传输对象
 */
public class CourseListDTO {
    private List<CourseDTO> courseList;
    
    @JsonProperty("course_list")
    public List<CourseDTO> getCourseList() {
        return courseList;
    }
    
    public void setCourseList(List<CourseDTO> courseList) {
        this.courseList = courseList;
    }
    
    /**
     * 详细课程信息DTO
     */
    public static class CourseDTO {
        private Integer courseId;
        private Integer sectionId;
        private String courseName;
        private String courseDescription;
        private String teacherName;
        private String classTime;
        private String classroom;
        private Integer availableCapacity;
        private Integer totalCapacity;
        private Double credit;
        
        @JsonProperty("course_id")
        public Integer getCourseId() {
            return courseId;
        }
        
        public void setCourseId(Integer courseId) {
            this.courseId = courseId;
        }
        
        @JsonProperty("section_id")
        public Integer getSectionId() {
            return sectionId;
        }
        
        public void setSectionId(Integer sectionId) {
            this.sectionId = sectionId;
        }
        
        @JsonProperty("course_name")
        public String getCourseName() {
            return courseName;
        }
        
        public void setCourseName(String courseName) {
            this.courseName = courseName;
        }
        
        @JsonProperty("course_description")
        public String getCourseDescription() {
            return courseDescription;
        }
        
        public void setCourseDescription(String courseDescription) {
            this.courseDescription = courseDescription;
        }
        
        @JsonProperty("teacher_name")
        public String getTeacherName() {
            return teacherName;
        }
        
        public void setTeacherName(String teacherName) {
            this.teacherName = teacherName;
        }
        
        @JsonProperty("class_time")
        public String getClassTime() {
            return classTime;
        }
        
        public void setClassTime(String classTime) {
            this.classTime = classTime;
        }
        
        public String getClassroom() {
            return classroom;
        }
        
        public void setClassroom(String classroom) {
            this.classroom = classroom;
        }
        
        @JsonProperty("available_capacity")
        public Integer getAvailableCapacity() {
            return availableCapacity;
        }
        
        public void setAvailableCapacity(Integer availableCapacity) {
            this.availableCapacity = availableCapacity;
        }
        
        @JsonProperty("total_capacity")
        public Integer getTotalCapacity() {
            return totalCapacity;
        }
        
        public void setTotalCapacity(Integer totalCapacity) {
            this.totalCapacity = totalCapacity;
        }
        
        public Double getCredit() {
            return credit;
        }
        
        public void setCredit(Double credit) {
            this.credit = credit;
        }
    }
}