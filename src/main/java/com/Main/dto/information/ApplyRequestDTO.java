package com.Main.dto.information;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ApplyRequestDTO {
    @JsonProperty("grade_id")
    private int gradeId;      // 要修改的成绩记录ID (GradeBase)
    @JsonProperty("old_score")
    private int oldScore;   // 原成绩 (后端可校验)
    @JsonProperty("new_score")
    private int newScore;   // 申请的新成绩
    private String reason;     // 修改理由

    // Getter 和 Setter 方法
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
}
