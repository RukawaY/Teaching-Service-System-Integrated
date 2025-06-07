package com.Main.dto.course_selection;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * 补选申请列表数据传输对象
 */
public class SupplementListDTO {
    private List<SupplementItemDTO> supplementList;
    
    @JsonProperty("supplement_list")
    public List<SupplementItemDTO> getSupplementList() {
        return supplementList;
    }
    
    public void setSupplementList(List<SupplementItemDTO> supplementList) {
        this.supplementList = supplementList;
    }
    
    /**
     * 补选申请项数据传输对象
     */
    public static class SupplementItemDTO {
        private Integer supplementId;
        private String studentName;
        private String courseName;
        
        @JsonProperty("supplement_id")
        public Integer getSupplementId() {
            return supplementId;
        }
        
        public void setSupplementId(Integer supplementId) {
            this.supplementId = supplementId;
        }
        
        @JsonProperty("student_name")
        public String getStudentName() {
            return studentName;
        }
        
        public void setStudentName(String studentName) {
            this.studentName = studentName;
        }
        
        @JsonProperty("course_name")
        public String getCourseName() {
            return courseName;
        }
        
        public void setCourseName(String courseName) {
            this.courseName = courseName;
        }
    }
}