package com.Main.dto.information;

public class ScoreRankingDTO {
    private int studentId; // 学生ID
    private String studentName; // 学生姓名
    private double score; // 分数
    private int rank; // 排名

    public ScoreRankingDTO() {}

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }
}