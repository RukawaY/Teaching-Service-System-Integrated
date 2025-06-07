package com.Main.RowMapper.information;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.lang.NonNull;

import com.Main.entity.information.Apply;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ApplyRowMapper implements RowMapper<Apply> {

    @Override
    public Apply mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
        Apply apply = new Apply();
        apply.setApplyId(rs.getInt("apply_id"));
        apply.setTeacherId(rs.getInt("teacher_id"));

        // Handle potential null for admin_id
        Integer adminId = rs.getObject("admin_id") != null ? rs.getInt("admin_id") : null;
        apply.setAdminId(adminId);

        apply.setGradeId(rs.getInt("grade_id"));
        apply.setOldScore(rs.getInt("old_score"));
        apply.setNewScore(rs.getInt("new_score"));
        apply.setReason(rs.getString("reason"));
        apply.setAudit_reason(rs.getString("audit_reason"));
        apply.setAuditStatus(rs.getInt("audit_status"));

        // Handle Timestamp for apply_time and review_time
        apply.setApplyTime(rs.getTimestamp("apply_time"));
        apply.setReviewTime(rs.getTimestamp("review_time"));

        return apply;
    }
}
