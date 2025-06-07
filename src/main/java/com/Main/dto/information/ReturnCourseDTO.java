package com.Main.dto.information;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ReturnCourseDTO {

    @JsonProperty("course_id")
    private int course_id; // 课程ID

    @JsonProperty("course_name")
    private String course_name; // 课程名称

    @JsonProperty("course_description")
    private String course_description; // 课程描述

    @JsonProperty("teacher_id")
    private int teacher_id; // 教师ID

    @JsonProperty("teacher_name")
    private String teacher_name; // 教师姓名

    private float credit; // 学分

    private String category; // 课程类别

    @JsonProperty("hours_per_week")
    private int hoursPerWeek; // 每周小时数

    public ReturnCourseDTO() {}

    public int getCourseId() {
        return course_id;
    }

    public void setCourseId(int courseId) {
        this.course_id = courseId;
    }

    public String getCourse_name() {
        return course_name;
    }

    public void setCourse_name(String course_name) {
        this.course_name = course_name;
    }

    public String getCourse_description() {
        return course_description;
    }

    public void setCourse_description(String course_description) {
        this.course_description = course_description;
    }

    public int getTeacher_id() {
        return teacher_id;
    }

    public void setTeacher_id(int teacher_id) {
        this.teacher_id = teacher_id;
    }

    public String getTeacher_name() {
        return teacher_name;
    }

    public void setTeacher_name(String teacher_name) {
        this.teacher_name = teacher_name;
    }

    public float getCredit() {
        return credit;
    }

    public void setCredit(float credit) {
        this.credit = credit;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getHoursPerWeek() {
        return hoursPerWeek;
    }

    public void setHoursPerWeek(int hoursPerWeek) {
        this.hoursPerWeek = hoursPerWeek;
    }
}
