package com.Main.dto.course_selection;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.Main.entity.course_selection.Curriculum;
import java.util.List;

/**
 * 培养方案数据传输对象
 */
public class CurriculumDTO {
    private String majorName;
    private List<SectionDTO> sections;
    private Integer studentId;

    @JsonProperty("major_name")
    public String getMajorName() {
        return majorName;
    }

    public void setMajorName(String majorName) {
        this.majorName = majorName;
    }

    public List<SectionDTO> getSections() {
        return sections;
    }

    public void setSections(List<SectionDTO> sections) {
        this.sections = sections;
    }

    @JsonProperty("student_id")
    public Integer getStudentId() {
        return studentId;
    }

    public void setStudentId(Integer studentId) {
        this.studentId = studentId;
    }

    /**
     * 培养方案子版块DTO
     */
    public static class SectionDTO {
        private Double sectionCredit;
        private List<CourseCreditDTO> courseList;
        private String sectionName;

        @JsonProperty("section_credit")
        public Double getSectionCredit() {
            return sectionCredit;
        }

        public void setSectionCredit(Double sectionCredit) {
            this.sectionCredit = sectionCredit;
        }

        @JsonProperty("course_list")
        public List<CourseCreditDTO> getCourseList() {
            return courseList;
        }

        public void setCourseList(List<CourseCreditDTO> courseList) {
            this.courseList = courseList;
        }

        @JsonProperty("section_name")
        public String getSectionName() {
            return sectionName;
        }

        public void setSectionName(String sectionName) {
            this.sectionName = sectionName;
        }
    }

    /**
     * 课程学分DTO
     */
    public static class CourseCreditDTO {
        private String courseName;
        private Double credit;

        @JsonProperty("course_name")
        public String getCourseName() {
            return courseName;
        }

        public void setCourseName(String courseName) {
            this.courseName = courseName;
        }

        public Double getCredit() {
            return credit;
        }

        public void setCredit(Double credit) {
            this.credit = credit;
        }
    }
}