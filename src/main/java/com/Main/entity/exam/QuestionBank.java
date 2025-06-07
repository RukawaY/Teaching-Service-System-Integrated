package com.Main.entity.exam;

import java.sql.Timestamp;
import java.util.List;

public class QuestionBank {
    private Integer questionId;
    private Integer courseId;
    private Integer chapterId;
    private QuestionType questionType;
    private String content;
    private List<String> options;
    private String answer;
    private Integer score;
    private Integer difficulty;
    private Timestamp createdAt;

    public enum QuestionType {
        MC, TF
    }

    // Getters and Setters
    public Integer getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Integer questionId) {
        this.questionId = questionId;
    }

    public Integer getCourseId() {
        return courseId;
    }

    public void setCourseId(Integer courseId) {
        this.courseId = courseId;
    }

    public Integer getChapterId() {
        return chapterId;
    }

    public void setChapterId(Integer chapterId) {
        this.chapterId = chapterId;
    }

    public QuestionType getQuestionType() {
        return questionType;
    }

    public void setQuestionType(QuestionType questionType) {
        this.questionType = questionType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public Integer getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Integer difficulty) {
        this.difficulty = difficulty;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    // toString
    @Override
    public String toString() {
        return "QuestionBank{" +
                "questionId=" + questionId +
                ", courseId=" + courseId +
                ", chapterId=" + chapterId +
                ", questionType=" + questionType +
                ", content='" + content + '\'' +
                ", options=" + options +
                ", answer='" + answer + '\'' +
                ", score=" + score +
                ", difficulty=" + difficulty +
                ", createdAt=" + createdAt +
                '}';
    }

}
