package com.Main.entity.course_selection;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 学生实体类
 */
public class Student {
    private Integer studentId;
    private String studentName;
    private Integer majorId;
    
    @JsonProperty("student_id")
    public Integer getStudentId() {
        return studentId;
    }

    public void setStudentId(Integer studentId) {
        this.studentId = studentId;
    }

    @JsonProperty("student_name")
    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    @JsonProperty("major_id")
    public Integer getMajorId() {
        return majorId;
    }

    public void setMajorId(Integer majorId) {
        this.majorId = majorId;
    }
}