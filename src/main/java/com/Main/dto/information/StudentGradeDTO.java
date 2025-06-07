package com.Main.dto.information;

import com.Main.entity.information.GradeBase;
import com.Main.entity.information.GradeComponent;

import java.util.List;

public class StudentGradeDTO {
    private int grade_id;
    private int course_id;
    private String course_name;
    private int section_id;
    private String semester;
    private int sec_year;
    private String teacher_name;
    private int score;
    private float gpa;
    private float credit;
    private int submit_status;
    private List<GradeComponent> gradeComponents;

    public StudentGradeDTO() {}

    public int getGrade_id() {
        return grade_id;
    }

    public void setGrade_id(int grade_id) {
        this.grade_id = grade_id;
    }

    public int getCourse_id() {
        return course_id;
    }

    public void setCourse_id(int course_id) {
        this.course_id = course_id;
    }

    public String getCourse_name() {
        return course_name;
    }

    public void setCourse_name(String course_name) {
        this.course_name = course_name;
    }

    public int getSection_id() {
        return section_id;
    }

    public void setSection_id(int section_id) {
        this.section_id = section_id;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public int getSec_year() {
        return sec_year;
    }

    public void setSec_year(int sec_year) {
        this.sec_year = sec_year;
    }

    public String getTeacher_name() {
        return teacher_name;
    }

    public void setTeacher_name(String teacher_name) {
        this.teacher_name = teacher_name;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public float getCredit() {
        return credit;
    }

    public void setCredit(float credit) {
        this.credit = credit;
    }

    public float getGpa() {
        return gpa;
    }

    public void setGpa(float gpa) {
        this.gpa = gpa;
    }

    public int getSubmit_status() {
        return submit_status;
    }

    public void setSubmit_status(int submit_status) {
        this.submit_status = submit_status;
    }

    public List<GradeComponent> getGradeComponents() {
        return gradeComponents;
    }

    public void setGradeComponents(List<GradeComponent> gradeComponents) {
        this.gradeComponents = gradeComponents;
    }

}
