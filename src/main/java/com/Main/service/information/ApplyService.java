package com.Main.service.information;

import com.Main.RowMapper.information.ApplyRowMapper;
import com.Main.entity.information.Apply;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class ApplyService {

    final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    JdbcTemplate jdbcTemplate;

    RowMapper<Apply> applyRowMapper = new ApplyRowMapper();

    /** 创建申请
     * @param teacherId 教师ID
     * @param gradeId  成绩ID
     * @param oldScore  旧成绩
     * @param newScore  新成绩
     * @param reason    申请理由
     * @return Apply 申请数据
     */
    public Apply createApply(int teacherId, int gradeId, int oldScore, int newScore, String reason) {
        Apply apply = new Apply();
        apply.setTeacherId(teacherId);
        apply.setGradeId(gradeId);
        apply.setOldScore(oldScore);
        apply.setNewScore(newScore);
        apply.setReason(reason);
        apply.setAuditStatus(0); // 0-待审核
        apply.setApplyTime(new Timestamp(System.currentTimeMillis()));

        KeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            if (1 != jdbcTemplate.update((conn) -> {
                var ps = conn.prepareStatement("INSERT INTO Apply(teacher_id, grade_id, old_score, new_score, reason, audit_status, apply_time) VALUES(?, ?, ?, ?, ?, ?, ?)",
                        Statement.RETURN_GENERATED_KEYS);
                ps.setInt(1, apply.getTeacherId());
                ps.setInt(2, apply.getGradeId());
                ps.setInt(3, apply.getOldScore());
                ps.setInt(4, apply.getNewScore());
                ps.setString(5, apply.getReason());
                ps.setString(6, String.valueOf(apply.getAuditStatus()));
                ps.setTimestamp(7, apply.getApplyTime());
                return ps;
            }, keyHolder)) {
                throw new RuntimeException("Insert apply failed.");
            }
        } catch (DataAccessException e) {
            logger.error("SQL Error: " + e.getMessage(), e);
            throw new RuntimeException("Insert apply failed due to SQL error.", e);
        }

        apply.setApplyId(keyHolder.getKey().intValue());
        return apply;
    }


    /**
     * 查询教师的成绩修改申请列表
     * @param teacherId 教师ID
     * @param auditStatus 审核状态
     * @return 申请列表
     */
    public List<Apply> getAppliesByTeacher(int teacherId, Integer auditStatus) {
        List<Object> params = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT a.*, s.name, c.course_name FROM Apply a " +
                "INNER JOIN GradeBase gb ON a.grade_id = gb.grade_id " +
                "INNER JOIN User s ON gb.student_id = s.user_Id " +
                "INNER JOIN Course c ON gb.course_id = c.course_id " +
                "WHERE a.teacher_id = ? ");
        params.add(teacherId);

        if (auditStatus != null) {
            sql.append("AND a.audit_status = ? ");
            params.add(auditStatus);
        }

        sql.append("ORDER BY a.apply_time DESC");

        logger.info("SQL: {}, Params: {}", sql.toString(), params);
        return jdbcTemplate.query(sql.toString(), applyRowMapper, params.toArray());
    }

    /**
     * 管理员查询成绩修改申请列表
     * @param auditStatus 审核状态
     * @param teacherId 教师ID
     * @return 申请列表
     */
    public List<Apply> getAppliesByAdmin(Integer auditStatus, Integer teacherId) {
        List<Object> params = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT a.*, t.name AS teacher_name, s.name, c.course_name FROM Apply a " +
                "INNER JOIN GradeBase gb ON a.grade_id = gb.grade_id " +
                "INNER JOIN User t ON a.teacher_id = t.user_id " +
                "INNER JOIN User s ON gb.student_id = s.user_id " +
                "INNER JOIN Course c ON gb.course_id = c.course_id " +
                "WHERE 1=1 ");

        if (auditStatus != null) {
            sql.append("AND a.audit_status = ? ");
            params.add(String.valueOf(auditStatus));
        }

        if (teacherId != null) {
            sql.append("AND a.teacher_id = ? ");
            params.add(teacherId);
        }

        sql.append("ORDER BY a.apply_time DESC");

        logger.info("SQL: {}, Params: {}", sql.toString(), params);
        return jdbcTemplate.query(sql.toString(), applyRowMapper, params.toArray());
    }

    /**
     * 审核成绩修改申请
     * @param applyId 申请ID
     * @param adminId 管理员ID
     * @param auditStatus 审核状态
     * @return Apply 申请数据
     */
    public Apply reviewApply(int applyId, int adminId, int auditStatus, String audit_Reason) {
        Apply apply = null;
        logger.info("Reviewing apply with ID: {}, Admin ID: {}, Audit Status: {}", applyId, adminId, auditStatus);
        try {
            apply = jdbcTemplate.queryForObject("SELECT * FROM Apply WHERE apply_id = ?",applyRowMapper, applyId);
        } catch (DataAccessException e) {
            logger.warn("Apply not found with id: {}", applyId);
            return null;
        }
        if (apply == null) {
            logger.warn("Apply not found with id: {}", applyId);
            return null;
        }

        apply.setAdminId(adminId);
        apply.setAuditStatus(auditStatus);
        apply.setReviewTime(new Timestamp(System.currentTimeMillis()));

        String sql = "UPDATE Apply SET admin_id = ?, audit_status = ?, review_time = ? , audit_reason = ? WHERE apply_id = ?";
        int rowsAffected = jdbcTemplate.update(sql, adminId, String.valueOf(auditStatus), apply.getReviewTime(),audit_Reason , applyId);

        if (rowsAffected > 0) {
            logger.info("Apply {} reviewed successfully.", applyId);

            // 审核通过后，更新 GradeBase 表
            if (auditStatus == 1) {
                updateGradeBase(apply.getGradeId(), apply.getNewScore());
            }
            // 重新查询 Apply 以获取最新状态
            try {
                return jdbcTemplate.queryForObject("SELECT * FROM Apply WHERE apply_id = ?",applyRowMapper,applyId);
            } catch (DataAccessException e) {
                logger.error("Error retrieving updated Apply after review: {}", applyId, e);
                return null;
            }
        } else {
            logger.warn("Apply {} review failed.", applyId);
            return null;
        }
    }

    /**
     * 更新成绩基础表
     * @param gradeId 成绩ID
     * @param newScore 新成绩
     */
    private void updateGradeBase(int gradeId, int newScore) {
        String sql = "UPDATE GradeBase SET score = ?, gpa = ? WHERE grade_id = ?";

        // GPA 计算
        float gpa;
        if (newScore >= 90) {
            gpa = 4.0f;
        } else if (newScore >= 80) {
            gpa = 3.0f;
        } else if (newScore >= 70) {
            gpa = 2.0f;
        } else if (newScore >= 60) {
            gpa = 1.0f;
        } else {
            gpa = 0.0f;
        }

        int rowsAffected = jdbcTemplate.update(sql, newScore, gpa, gradeId);

        if (rowsAffected > 0) {
            logger.info("GradeBase {} updated successfully with new score and GPA.", gradeId);
        } else {
            logger.warn("GradeBase {} update failed.", gradeId);
        }
    }

}
