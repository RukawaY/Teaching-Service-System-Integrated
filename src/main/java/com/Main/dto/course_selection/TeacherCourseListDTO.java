package com.Main.dto.course_selection;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * 教师课程列表数据传输对象
 */
public class TeacherCourseListDTO {
    private List<TeacherCourseDTO> courseList;

    @JsonProperty("course_list")
    public List<TeacherCourseDTO> getCourseList() {
        return courseList;
    }

    public void setCourseList(List<TeacherCourseDTO> courseList) {
        this.courseList = courseList;
    }

    /**
     * 教师课程DTO
     */
    public static class TeacherCourseDTO {
        private String courseName;
        private String courseTime;
        private String courseClassroom;
        private List<StudentInfo> studentList;

        @JsonProperty("course_name")
        public String getCourseName() {
            return courseName;
        }

        public void setCourseName(String courseName) {
            this.courseName = courseName;
        }

        @JsonProperty("course_time")
        public String getCourseTime() {
            return courseTime;
        }

        public void setCourseTime(String courseTime) {
            this.courseTime = courseTime;
        }

        @JsonProperty("course_classroom")
        public String getCourseClassroom() {
            return courseClassroom;
        }

        public void setCourseClassroom(String courseClassroom) {
            this.courseClassroom = courseClassroom;
        }

        @JsonProperty("student_list")
        public List<StudentInfo> getStudentList() {
            return studentList;
        }

        public void setStudentList(List<StudentInfo> studentList) {
            this.studentList = studentList;
        }
    }

    /**
     * 学生信息DTO
     */
    public static class StudentInfo {
        private String name;
        private Integer ID;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @JsonProperty("id")
        public Integer getID() {
            return ID;
        }

        public void setID(Integer ID) {
            this.ID = ID;
        }
    }
}