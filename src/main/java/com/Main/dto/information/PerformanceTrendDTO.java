package com.Main.dto.information;

public class PerformanceTrendDTO {
    private int sec_year;
    private String semester; // 学期
    private double gpa; // GPA
    private double average_score; // 平均分

    public PerformanceTrendDTO() {}

    public int getSec_year() {
        return sec_year;
    }

    public void setSec_year(int sec_year) {
        this.sec_year = sec_year;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public double getGpa() {
        return gpa;
    }

    public void setGpa(double gpa) {
        this.gpa = gpa;
    }

    public double getAverage_score() {
        return average_score;
    }

    public void setAverage_score(double average_score) {
        this.average_score = average_score;
    }
}