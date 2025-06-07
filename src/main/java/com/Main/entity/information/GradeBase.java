package com.Main.entity.information;

public class GradeBase {
    private int gradeId;  // 成绩ID (主键)
    private int studentId;// 学生ID (外键, 关联User表)
    private int courseId; // 课程ID (外键, 关联Course表)
    private int sectionId;// 开课ID (外键, 关联Section表)
    private int score;  // 百分制成绩
    private float gpa;   // GPA绩点
    private int submitStatus; // 提交状态 (例如: 未提交 0 /已提交 1，默认未提交 0)

    public int getGradeId() {
        return gradeId;
    }

    public void setGradeId(int gradeId) {
        this.gradeId = gradeId;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public int getCourseId() {
        return courseId;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    public int getSectionId() {
        return sectionId;
    }

    public void setSectionId(int sectionId) {
        this.sectionId = sectionId;
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

    public int getSubmitStatus() {
        return submitStatus;
    }

    public void setSubmitStatus(int submitStatus) {
        this.submitStatus = submitStatus;
    }

    @Override
    public String toString() {
        return String.format("GradeBase[gradeId=%d, studentId=%d, courseId=%d, sectionId=%d, score=%d, gpa=%f, submitStatus='%s']",
                gradeId, studentId, courseId, sectionId, score, gpa, submitStatus);
    }
}

