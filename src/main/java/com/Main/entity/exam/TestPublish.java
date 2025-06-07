package com.Main.entity.exam;

import java.time.LocalDateTime;
import java.util.List;

public class TestPublish {
    private Integer testId;
    private Integer teacherId;
    private Integer courseId;
    private String testName;
    private LocalDateTime publishTime;
    private LocalDateTime deadline;
    private Integer questionCount;
    private Boolean isRandom;
    private List<Integer> questionIds;
    private Integer ratio; // 考试成绩占总成绩的比例

    // Getters and Setters
    public Integer getTestId() {
        return testId;
    }

    public void setTestId(Integer testId) {
        this.testId = testId;
    }

    public Integer getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(Integer teacherId) {
        this.teacherId = teacherId;
    }

    public Integer getCourseId() {
        return courseId;
    }

    public void setCourseId(Integer courseId) {
        this.courseId = courseId;
    }

    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public LocalDateTime getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(LocalDateTime publishTime) {
        this.publishTime = publishTime;
    }

    public LocalDateTime getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDateTime deadline) {
        this.deadline = deadline;
    }

    public Integer getQuestionCount() {
        return questionCount;
    }

    public void setQuestionCount(Integer questionCount) {
        this.questionCount = questionCount;
    }

    public Boolean getRandom() {
        return isRandom;
    }

    public void setRandom(Boolean random) {
        isRandom = random;
    }

    public List<Integer> getQuestionIds() {
        return questionIds;
    }

    public void setQuestionIds(List<Integer> questionIds) {
        this.questionIds = questionIds;
    }
    
    public Integer getRatio() {
        return ratio;
    }
    
    public void setRatio(Integer ratio) {
        this.ratio = ratio;
    }

    // toString
    @Override
    public String toString() {
        return "TestPublish{" +
                "testId=" + testId +
                ", teacherId=" + teacherId +
                ", courseId=" + courseId +
                ", testName='" + testName + '\'' +
                ", publishTime=" + publishTime +
                ", deadline=" + deadline +
                ", questionCount=" + questionCount +
                ", isRandom=" + isRandom +
                ", questionIds=" + questionIds +
                ", ratio=" + ratio +
                '}';
    }

}