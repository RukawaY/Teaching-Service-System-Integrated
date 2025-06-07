package com.Main.entity.rss;

import java.util.Date;

public class HomeworkSubmission {
    private Integer submission_id;
    private Integer homework_id;
    private Integer student_id;
    private Date submit_time;
    private String file_name;
    private String file_url;
    private Double score;
    private String comment;

    public Integer getSubmission_id() {
        return submission_id;
    }

    public void setSubmission_id(Integer submission_id) {
        this.submission_id = submission_id;
    }

    public Integer getHomework_id() {
        return homework_id;
    }

    public void setHomework_id(Integer homework_id) {
        this.homework_id = homework_id;
    }

    public Integer getStudent_id() {
        return student_id;
    }

    public void setStudent_id(Integer student_id) {
        this.student_id = student_id;
    }

    public Date getSubmit_time() {
        return submit_time;
    }

    public void setSubmit_time(Date submit_time) {
        this.submit_time = submit_time;
    }

    public String getFile_name() {
        return file_name;
    }

    public void setFile_name(String file_name) {
        this.file_name = file_name;
    }

    public String getFile_url() {
        return file_url;
    }

    public void setFile_url(String file_url) {
        this.file_url = file_url;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public String toString() {
        return "HomeworkSubmission{" +
                "submission_id=" + submission_id +
                ", homework_id=" + homework_id +
                ", student_id=" + student_id +
                ", submit_time=" + submit_time +
                ", file_name='" + file_name + '\'' +
                ", file_url='" + file_url + '\'' +
                ", score=" + score +
                ", comment='" + comment + '\'' +
                '}';
    }
}