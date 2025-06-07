package com.Main.dto.course_selection;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Course table data transfer object for search_course_table API
 */
public class CourseTableDTO {
    
    @JsonProperty("course_list")
    private List<CourseInfoDTO> courseList;
    
    public CourseTableDTO() {}
    
    public CourseTableDTO(List<CourseInfoDTO> courseList) {
        this.courseList = courseList;
    }
    
    // Getters and Setters
    public List<CourseInfoDTO> getCourseList() {
        return courseList;
    }
    
    public void setCourseList(List<CourseInfoDTO> courseList) {
        this.courseList = courseList;
    }
    
    /**
     * Inner class for course information
     */
    public static class CourseInfoDTO {
        
        @JsonProperty("course_id")
        private Integer courseId;
        
        @JsonProperty("course_name")
        private String courseName;
        
        @JsonProperty("course_description")
        private String courseDescription;
        
        @JsonProperty("credit")
        private Double credit;
        
        @JsonProperty("category")
        private String category;
        
        public CourseInfoDTO() {}
        
        public CourseInfoDTO(Integer courseId, String courseName, String courseDescription, Double credit, String category) {
            this.courseId = courseId;
            this.courseName = courseName;
            this.courseDescription = courseDescription;
            this.credit = credit;
            this.category = category;
        }
        
        // Getters and Setters
        public Integer getCourseId() {
            return courseId;
        }
        
        public void setCourseId(Integer courseId) {
            this.courseId = courseId;
        }
        
        public String getCourseName() {
            return courseName;
        }
        
        public void setCourseName(String courseName) {
            this.courseName = courseName;
        }
        
        public String getCourseDescription() {
            return courseDescription;
        }
        
        public void setCourseDescription(String courseDescription) {
            this.courseDescription = courseDescription;
        }
        
        public Double getCredit() {
            return credit;
        }
        
        public void setCredit(Double credit) {
            this.credit = credit;
        }
        
        public String getCategory() {
            return category;
        }
        
        public void setCategory(String category) {
            this.category = category;
        }
    }
} 