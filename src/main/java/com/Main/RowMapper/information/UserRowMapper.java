package com.Main.RowMapper.information;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.Main.entity.information.User;

public class UserRowMapper implements RowMapper<User> {
    @Override
    public User mapRow(ResultSet rs, int rowNum) throws SQLException {
        User user = new User();
        user.setUser_id(rs.getInt("user_id"));
        user.setName(rs.getString("name"));
        user.setAccount(rs.getString("account"));
        user.setPassword(rs.getString("password"));
        user.setRole(rs.getString("role"));
        
        // 这些字段可能为空，使用hasColumn方法检查
        if (hasColumn(rs, "department")) {
            user.setDepartment(rs.getString("department"));
        }
        if (hasColumn(rs, "contact")) {
            user.setContact(rs.getString("contact"));
        }
        if (hasColumn(rs, "avatar_path")) {
            user.setAvatarPath(rs.getString("avatar_path"));
        }
        
        return user;
    }
    
    // 辅助方法：检查ResultSet是否包含特定列
    private boolean hasColumn(ResultSet rs, String columnName) {
        try {
            rs.findColumn(columnName);
            return true;
        } catch (SQLException e) {
            return false;
        }
    }
}
