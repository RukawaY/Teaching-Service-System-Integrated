package com.Main.dto.information;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ScoreUpdateDTO {
    @JsonProperty("student_id")
    private int studentId; // 学生ID
    private int score;     // 总分
    private float gpa;     // 绩点

    public ScoreUpdateDTO() {
    }

    public ScoreUpdateDTO(int studentId, int score, float gpa) {
        this.studentId = studentId;
        this.score = score;
        this.gpa = gpa;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public float getGpa() {
        return gpa;
    }

    public void setGpa(float gpa) {
        this.gpa = gpa;
    }
}
