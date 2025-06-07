package com.Main.entity.information;

import java.sql.Timestamp;

public class Apply {
    private int applyId;    // 申请ID (主键)
    private int teacherId;   // 申请教师ID (外键, 关联User表)
    private Integer adminId;     // 审核管理员ID (外键, 关联User表, 可为空表示未审核)
    private int gradeId;    // 成绩ID (外键, 关联GradeBase表)
    private int oldScore;    // 原成绩
    private int newScore;   // 申请修改后的新成绩
    private String reason;   // 修改理由
    private String audit_reason; // 管理员审核理由
    private int auditStatus; // 审核状态 (例如: 0-待审核, 1-已通过, 2-已拒绝)
    private Timestamp applyTime;  // 申请提交时间 (后端自动生成)
    private Timestamp reviewTime; // 审核时间 (管理员审核时后端自动生成)

    public int getApplyId() {
        return applyId;
    }

    public void setApplyId(int applyId) {
        this.applyId = applyId;
    }

    public int getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(int teacherId) {
        this.teacherId = teacherId;
    }

    public Integer getAdminId() {
        return adminId;
    }

    public void setAdminId(Integer adminId) {
        this.adminId = adminId;
    }

    public int getGradeId() {
        return gradeId;
    }

    public void setGradeId(int gradeId) {
        this.gradeId = gradeId;
    }

    public int getOldScore() {
        return oldScore;
    }

    public void setOldScore(int oldScore) {
        this.oldScore = oldScore;
    }

    public int getNewScore() {
        return newScore;
    }

    public void setNewScore(int newScore) {
        this.newScore = newScore;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public int getAuditStatus() {
        return auditStatus;
    }

    public void setAuditStatus(int auditStatus) {
        this.auditStatus = auditStatus;
    }

    public Timestamp getApplyTime() {
        return applyTime;
    }

    public void setApplyTime(Timestamp applyTime) {
        this.applyTime = applyTime;
    }

    public Timestamp getReviewTime() {
        return reviewTime;
    }

    public void setReviewTime(Timestamp reviewTime) {
        this.reviewTime = reviewTime;
    }

    public String getAudit_reason() {
        return audit_reason;
    }

    public void setAudit_reason(String audit_reason) {
        this.audit_reason = audit_reason;
    }

    @Override
    public String toString() {
        return String.format("Apply[applyId=%d, teacherId=%d, adminId=%d, gradeId=%d, oldScore=%d, newScore=%d, reason='%s',audit_reason='%s', auditStatus='%s', applyTime='%s', reviewTime='%s']",
                applyId, teacherId, adminId, gradeId, oldScore, newScore, reason, audit_reason, auditStatus, applyTime, reviewTime);
    }
}
