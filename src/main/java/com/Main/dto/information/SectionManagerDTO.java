package com.Main.dto.information;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SectionManagerDTO {

    @JsonProperty("classroom_id")
    private int classroomId;

    @JsonProperty("capacity")
    private int capacity;

    @JsonProperty("semester")
    private String semester;

    @JsonProperty("sec_year")
    private int secYear;

    @JsonProperty("sec_time")
    private String secTime;

    // 默认构造函数
    public SectionManagerDTO() {}

    // 带参数的构造函数
    public SectionManagerDTO(int classroomId, int capacity, String semester, int secYear, String secTime) {
        this.classroomId = classroomId;
        this.capacity = capacity;
        this.semester = semester;
        this.secYear = secYear;
        this.secTime = secTime;
    }

    // Getters 和 Setters
    public int getClassroomId() {
        return classroomId;
    }

    public void setClassroomId(int classroomId) {
        this.classroomId = classroomId;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public int getSecYear() {
        return secYear;
    }

    public void setSecYear(int secYear) {
        this.secYear = secYear;
    }

    public String getSecTime() {
        return secTime;
    }

    public void setSecTime(String secTime) {
        this.secTime = secTime;
    }
}
