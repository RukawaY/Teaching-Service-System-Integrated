package com.Main.entity.course_selection;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;

/**
 * 课程补选申请实体类
 */
public class CourseSupplementApplication {
    private Integer supplementId;
    private Integer studentId;
    private Integer sectionId;
    private Date applyTime;
    private Integer status; // 0-待处理, 1-已同意, 2-已拒绝

    @JsonProperty("supplement_id")
    public Integer getSupplementId() {
        return supplementId;
    }

    public void setSupplementId(Integer supplementId) {
        this.supplementId = supplementId;
    }

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

    @JsonProperty("apply_time")
    public Date getApplyTime() {
        return applyTime;
    }

    public void setApplyTime(Date applyTime) {
        this.applyTime = applyTime;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}