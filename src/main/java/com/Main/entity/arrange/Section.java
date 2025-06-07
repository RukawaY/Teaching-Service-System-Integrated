package com.Main.entity.arrange;

public class Section {
    private int sectionId=-1;
    private int courseId=-1;
    private int classroomId=-1;
    private int capacity=-1;
    private String semester; // 春夏，秋冬
    private int secYear=-1;
    private String secTime; //开课时间，格式暂定为"Monday 1,2; Wednesday 6,7,8"

    //newly added
    private int availableCapacity; // 剩余容量

    public Section(){}

    public Section(int courseId, int classroomId, int capacity, String semester, int secYear, String secTime, int availableCapacity){
        this.courseId = courseId;
        this.classroomId = classroomId;
        this.capacity = capacity;
        this.semester = semester;
        this.secYear = secYear;
        this.secTime = secTime;
        this.availableCapacity = availableCapacity;
    }

    public int getId() {
        return sectionId;
    }

    public void setId(int sectionId) {
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

    public int getAvailableCapacity() {
        return availableCapacity;
    }

    public void setAvailableCapacity(int availableCapacity) {
        this.availableCapacity = availableCapacity;
    }   

    @Override
    public String toString() {
        return String.format("Section[id=%d, courseId=%d, classroomId=%d, capacity=%d, semester='%s', year=%d, time='%s', availableCapacity=%d]", getId(), getCourseId(), getClassroomId(), getCapacity(), getSemester(), getSecYear(), getSecTime(), getAvailableCapacity());
    }
}
