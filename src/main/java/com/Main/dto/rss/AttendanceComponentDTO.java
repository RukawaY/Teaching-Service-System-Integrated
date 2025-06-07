package com.Main.dto.rss;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AttendanceComponentDTO {
    @JsonProperty("student_id")  // 修改为student_id
    private Integer studentId;  // 学生ID（前端输入）
    
    @JsonProperty("course_name")
    private String courseName;  // 课程名称（前端可提供）
    
    @JsonProperty("attendance_ratio")
    private Integer attendanceRatio;  // 考勤比例（0-100，如 30 代表30%）
    
    @JsonProperty("attendance_score")
    private Integer attendanceScore;  // 考勤得分

    // Getters and Setters（同步修改studentName为studentId）
    public Integer getStudentId() {  // 修改方法名
        return studentId;
    }

    public void setStudentId(Integer studentId) {  // 修改方法名
        this.studentId = studentId;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public Integer getAttendanceRatio() {
        return attendanceRatio;
    }

    public void setAttendanceRatio(Integer attendanceRatio) {
        this.attendanceRatio = attendanceRatio;
    }

    public Integer getAttendanceScore() {
        return attendanceScore;
    }

    public void setAttendanceScore(Integer attendanceScore) {
        this.attendanceScore = attendanceScore;
    }
}