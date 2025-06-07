package com.Main.RowMapper.arrange;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.Main.entity.arrange.User;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.lang.NonNull;

/**
 * this class is only used for debug.
 * after merging, it will be deleted.
 */
public class UserRowMapper implements RowMapper<User> {
    @Override
    public User mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("user_id"));
        user.setName(rs.getString("name"));
        user.setAccount(rs.getString("account"));
        user.setPassword(rs.getString("password")); 
        user.setRole(rs.getString("role"));
        user.setDepartment(rs.getString("department"));
        user.setContact(rs.getString("contact"));
        user.setAvatarPath(rs.getString("avatar_path"));
        return user;
    }
}
