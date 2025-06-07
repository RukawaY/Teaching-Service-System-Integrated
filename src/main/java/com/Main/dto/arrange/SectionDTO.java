package com.Main.dto.arrange;

import com.Main.entity.arrange.Section;

public class SectionDTO extends Section {
    private String teacherName;
    private String courseName;
    private String classroomName;

    public SectionDTO(Section section) {
        this.setId(section.getId());
        this.setCourseId(section.getCourseId());
        this.setClassroomId(section.getClassroomId());
        this.setCapacity(section.getCapacity());
        this.setSemester(section.getSemester());
        this.setSecYear(section.getSecYear());
        this.setSecTime(section.getSecTime());
        this.setAvailableCapacity(section.getAvailableCapacity());
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public void setClassroomName(String classroomName){
        this.classroomName = classroomName;
    }

    public String getClassroomName() {
        return classroomName;
    }
} 