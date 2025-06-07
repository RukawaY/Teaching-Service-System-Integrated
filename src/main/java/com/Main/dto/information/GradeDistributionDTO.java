package com.Main.dto.information;

public class GradeDistributionDTO {
    private String course_name; // 课程名称
    private double score; // 分数
    private double gpa; // GPA
    private float credit; // 学分

    public GradeDistributionDTO() {}

    public String getCourse_name() {
        return course_name;
    }

    public void setCourse_name(String course_name) {
        this.course_name = course_name;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public double getGpa() {
        return gpa;
    }

    public void setGpa(double gpa) {
        this.gpa = gpa;
    }

    public float getCredit() {
        return credit;
    }

    public void setCredit(float credit) {
        this.credit = credit;
    }
}