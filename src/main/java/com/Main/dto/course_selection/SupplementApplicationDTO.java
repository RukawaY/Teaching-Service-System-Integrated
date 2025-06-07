package com.Main.dto.course_selection;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 补选申请数据传输对象
 */
public class SupplementApplicationDTO {
    private Integer studentId;
    private Integer sectionId;

    @JsonProperty("student_id")
    public Integer getStudentId() {
        return studentId;
    }

    public void setStudentId(Integer studentId) {
        this.studentId = studentId;
    }

    @JsonProperty("section_id")
    public Integer getSectionId() {
        return sectionId;
    }

    public void setSectionId(Integer sectionId) {
        this.sectionId = sectionId;
    }
}