package com.Main.dto.information;

public class SectionSearchDTO {
    private int sectionId;
    private int courseId;
    private int classroomId;
    private int capacity;
    private String semester;
    private int secYear;
    private String secTime;
    private int available_capacity;
    private String classroom_location;
    private int classroom_capacity;

    // 构造函数
    public SectionSearchDTO() {
    }

    public SectionSearchDTO(int sectionId, int courseId, int classroomId, int capacity, String semester, int secYear, String secTime, int available_capacity, String classroom_location, int classroom_capacity) {
        this.sectionId = sectionId;
        this.courseId = courseId;
        this.classroomId = classroomId;
        this.capacity = capacity;
        this.semester = semester;
        this.secYear = secYear;
        this.secTime = secTime;
        this.available_capacity = available_capacity;
        this.classroom_location = classroom_location;
        this.classroom_capacity = classroom_capacity;
    }

    // Getter and Setter methods
    public int getSectionId() {
        return sectionId;
    }

    public void setSectionId(int sectionId) {
        this.sectionId = sectionId;
    }

    public int getCourseId() {
        return courseId;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

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

    public int getAvailable_capacity() {
        return available_capacity;
    }

    public void setAvailable_capacity(int available_capacity) {
        this.available_capacity = available_capacity;
    }

    public String getClassroom_location() {
        return classroom_location;
    }

    public void setClassroom_location(String classroom_location) {
        this.classroom_location = classroom_location;
    }

    public int getClassroom_capacity() {
        return classroom_capacity;
    }

    public void setClassroom_capacity(int classroom_capacity) {
        this.classroom_capacity = classroom_capacity;
    }

}
