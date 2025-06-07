package com.Main.entity.exam;

import java.sql.Timestamp;
import java.util.Objects;

public class StudentAnswerResult {
    private Integer resultId;
    private Integer testId;
    private Integer studentId;
    private Integer questionId;
    private String studentAnswer;
    private Boolean isCorrect;
    private Integer scoreObtained;
    private Timestamp answerTime;

    // Getters and Setters
    public Integer getResultId() {
        return resultId;
    }

    public void setResultId(Integer resultId) {
        this.resultId = resultId;
    }

    public Integer getTestId() {
        return testId;
    }

    public void setTestId(Integer testId) {
        this.testId = testId;
    }

    public Integer getStudentId() {
        return studentId;
    }

    public void setStudentId(Integer studentId) {
        this.studentId = studentId;
    }

    public Integer getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Integer questionId) {
        this.questionId = questionId;
    }

    public String getStudentAnswer() {
        return studentAnswer;
    }

    public void setStudentAnswer(String studentAnswer) {
        this.studentAnswer = studentAnswer;
    }

    public Boolean getIsCorrect() {
        return isCorrect;
    }

    public void setIsCorrect(Boolean correct) {
        isCorrect = correct;
    }

    public Integer getScoreObtained() {
        return scoreObtained;
    }

    public void setScoreObtained(Integer scoreObtained) {
        this.scoreObtained = scoreObtained;
    }

    public Timestamp getAnswerTime() {
        return answerTime;
    }

    public void setAnswerTime(Timestamp answerTime) {
        this.answerTime = answerTime;
    }

    // toString
    @Override
    public String toString() {
        return "StudentAnswerResult{" +
                "resultId=" + resultId +
                ", testId=" + testId +
                ", studentId=" + studentId +
                ", questionId=" + questionId +
                ", studentAnswer='" + studentAnswer + '\'' +
                ", isCorrect=" + isCorrect +
                ", scoreObtained=" + scoreObtained +
                ", answerTime=" + answerTime +
                '}';
    }

    // equals and hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StudentAnswerResult that = (StudentAnswerResult) o;
        return Objects.equals(resultId, that.resultId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(resultId);
    }
}