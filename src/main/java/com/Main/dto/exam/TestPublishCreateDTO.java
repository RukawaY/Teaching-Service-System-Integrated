package com.Main.dto.exam;

import java.time.LocalDateTime;
import java.util.List;

public class TestPublishCreateDTO {
    private Integer teacherId;
    private Integer courseId;
    private String testName;
    private LocalDateTime publishTime;
    private LocalDateTime deadline;
    private Integer questionCount;
    private Boolean random;
    private List<Integer> questionIds;
    private Integer ratio; // 考试成绩占总成绩的比例

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
        return random;
    }

    public void setRandom(Boolean random) {
        this.random = random;
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
}
