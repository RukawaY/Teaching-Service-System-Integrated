package com.Main.dto.information;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ReviewApplyDTO {
    @JsonProperty("audit_status")
    private int auditStatus; // 0-待审核, 1-通过, 2-拒绝
    @JsonProperty("audit_reason")
    private String auditReason;

    public ReviewApplyDTO() {
    }
    // Getter 和 Setter
    public int getAuditStatus() {
        return auditStatus;
    }

    public void setAuditStatus(int auditStatus) {
        this.auditStatus = auditStatus;
    }

    public String getAuditReason() {
        return auditReason;
    }

    public void setAuditReason(String auditReason) {
        this.auditReason = auditReason;
    }
}
