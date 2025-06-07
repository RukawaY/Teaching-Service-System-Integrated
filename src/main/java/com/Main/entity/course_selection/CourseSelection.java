package com.Main.entity.course_selection;

import java.util.Date;

/**
 * 选课记录实体类
 */
public class CourseSelection {
    private Integer selectionId; // 选课记录ID
    private Integer studentId;   // 学生ID
    private Integer sectionId;   // 开课ID
    private Date selectionTime;  // 选课时间

    // 无参构造函数
    public CourseSelection() {}

    // 带参构造函数
    public CourseSelection(Integer studentId, Integer sectionId) {
        this.studentId = studentId;
        this.sectionId = sectionId;
        this.selectionTime = new Date();
    }

    // Getters and Setters
    public Integer getSelectionId() { return selectionId; }

    public void setSelectionId(Integer selectionId) { this.selectionId = selectionId; }

    public Integer getStudentId() { return studentId; }

    public void setStudentId(Integer studentId) { this.studentId = studentId; }

    public Integer getSectionId() { return sectionId; }

    public void setSectionId(Integer sectionId) { this.sectionId = sectionId; }

    public Date getSelectionTime() { return selectionTime; }

    public void setSelectionTime(Date selectionTime) { this.selectionTime = selectionTime; }

    @Override
    public String toString() {
        return "CourseSelection{"
            + "selectionId=" + selectionId + ", studentId=" + studentId + ", sectionId=" + sectionId + ", selectionTime=" + selectionTime + '}';
    }
}