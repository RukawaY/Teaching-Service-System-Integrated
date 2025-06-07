package com.Main.dto.information;

import java.util.List;

public class StudentAnalyseDTO {
    private double overall_gpa; // 总体GPA
    private double average_score; // 平均分
    private int total_credits_earned; // 已获得学分
    private int total_credits_taken; // 已修学分
    private List<GradeDistributionDTO> grade_distribution_by_course; // 按课程的成绩分布
    private List<PerformanceTrendDTO> performance_trend; // 表现趋势

    public StudentAnalyseDTO() {}

    public double getOverall_gpa() {
        return overall_gpa;
    }

    public void setOverall_gpa(double overall_gpa) {
        this.overall_gpa = overall_gpa;
    }

    public double getAverage_score() {
        return average_score;
    }

    public void setAverage_score(double average_score) {
        this.average_score = average_score;
    }

    public int getTotal_credits_earned() {
        return total_credits_earned;
    }

    public void setTotal_credits_earned(int total_credits_earned) {
        this.total_credits_earned = total_credits_earned;
    }

    public int getTotal_credits_taken() {
        return total_credits_taken;
    }

    public void setTotal_credits_taken(int total_credits_taken) {
        this.total_credits_taken = total_credits_taken;
    }

    public List<GradeDistributionDTO> getGrade_distribution_by_course() {
        return grade_distribution_by_course;
    }

    public void setGrade_distribution_by_course(List<GradeDistributionDTO> grade_distribution_by_course) {
        this.grade_distribution_by_course = grade_distribution_by_course;
    }

    public List<PerformanceTrendDTO> getPerformance_trend() {
        return performance_trend;
    }

    public void setPerformance_trend(List<PerformanceTrendDTO> performance_trend) {
        this.performance_trend = performance_trend;
    }
}
