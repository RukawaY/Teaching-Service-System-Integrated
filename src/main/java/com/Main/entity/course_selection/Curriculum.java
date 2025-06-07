package com.Main.entity.course_selection;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * 培养方案实体类
 */
public class Curriculum {
    private Integer majorId;
    private String majorName;
    private List<CurriculumSection> sections;
    
    @JsonProperty("major_id")
    public Integer getMajorId() {
        return majorId;
    }

    public void setMajorId(Integer majorId) {
        this.majorId = majorId;
    }

    @JsonProperty("major_name")
    public String getMajorName() {
        return majorName;
    }

    public void setMajorName(String majorName) {
        this.majorName = majorName;
    }

    public List<CurriculumSection> getSections() {
        return sections;
    }

    public void setSections(List<CurriculumSection> sections) {
        this.sections = sections;
    }
    
    /**
     * 培养方案子版块
     */
    public static class CurriculumSection {
        private Double sectionCredit;
        private List<CourseCredit> courseList;
        private String sectionName;

        @JsonProperty("section_credit")
        public Double getSectionCredit() {
            return sectionCredit;
        }

        public void setSectionCredit(Double sectionCredit) {
            this.sectionCredit = sectionCredit;
        }

        @JsonProperty("course_list")
        public List<CourseCredit> getCourseList() {
            return courseList;
        }

        public void setCourseList(List<CourseCredit> courseList) {
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
     * 课程学分信息
     */
    public static class CourseCredit {
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