package com.Main.entity.course_selection;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 教师实体类
 */
public class Teacher {
    private Integer teacherId;
    private String teacherName;
    
    @JsonProperty("teacher_id")
    public Integer getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(Integer teacherId) {
        this.teacherId = teacherId;
    }

    @JsonProperty("teacher_name")
    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }
}