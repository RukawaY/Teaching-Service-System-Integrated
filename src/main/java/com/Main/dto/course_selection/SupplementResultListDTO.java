package com.Main.dto.course_selection;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * 学生补选结果列表DTO
 */
public class SupplementResultListDTO {
    
    private List<ResultItem> resultList;
    
    @JsonProperty("result_list")
    public List<ResultItem> getResultList() {
        return resultList;
    }
    
    public void setResultList(List<ResultItem> resultList) {
        this.resultList = resultList;
    }
    
    /**
     * 补选结果项
     */
    public static class ResultItem {
        private Integer courseId;
        private String courseName;
        private String teacherName;
        private String classTime;
        private String classroom;
        private Double credit;
        private String result;
        
        @JsonProperty("course_id")
        public Integer getCourseId() {
            return courseId;
        }
        
        public void setCourseId(Integer courseId) {
            this.courseId = courseId;
        }
        
        @JsonProperty("course_name")
        public String getCourseName() {
            return courseName;
        }
        
        public void setCourseName(String courseName) {
            this.courseName = courseName;
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
        
        public Double getCredit() {
            return credit;
        }
        
        public void setCredit(Double credit) {
            this.credit = credit;
        }
        
        public String getResult() {
            return result;
        }
        
        public void setResult(String result) {
            this.result = result;
        }
    }
}