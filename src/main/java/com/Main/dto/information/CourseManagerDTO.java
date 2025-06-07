package com.Main.dto.information;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CourseManagerDTO {
    @JsonProperty("course_name")
    private String course_name;
    @JsonProperty("course_description")
    private String course_description;
    private float credit;
    private String category;
    @JsonProperty("hours_per_week")
    private int hours_per_week;

    public CourseManagerDTO(String course_name, String course_description, float credit, String category, int hours_per_week) {
        this.course_name = course_name;
        this.course_description = course_description;
        this.credit = credit;
        this.category = category;
        this.hours_per_week = hours_per_week;
    }
    public CourseManagerDTO() {}

    // Getters and Setters
    public String getCourseName() {
        return course_name;
    }

    public void setCourseName(String course_name) {
        this.course_name = course_name;
    }

    public String getCourseDescription() {
        return course_description;
    }

    public void setCourseDescription(String course_description) {
        this.course_description = course_description;
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

    public int getHours_per_week() {
        return hours_per_week;
    }

    public void setHours_per_week(int hours_per_week) {
        this.hours_per_week = hours_per_week;
    }

}
