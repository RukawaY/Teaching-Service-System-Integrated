package com.Main.service.information;

import com.Main.RowMapper.information.UserRowMapper;
import com.Main.entity.information.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;

@Component
public class UserService {

    final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    JdbcTemplate jdbcTemplate;

    RowMapper<User> userRowMapper = new BeanPropertyRowMapper<>(User.class);

    public List<User> getUsers(){
        return jdbcTemplate.query("SELECT * FROM User", new UserRowMapper());
    }

    public User getUserById(long id) {
        try {
            return jdbcTemplate.queryForObject("SELECT * FROM User WHERE user_id = ?", 
                new UserRowMapper(), id);
        } catch (EmptyResultDataAccessException e) {
            logger.error("User not found with id: {}", id);
            throw new RuntimeException("用户不存在");
        }
    }

    public User getUserByAccount(String account) {
        try {
            return jdbcTemplate.queryForObject("SELECT * FROM User WHERE account = ?", 
                new UserRowMapper(), account);
        } catch (EmptyResultDataAccessException e) {
            logger.error("User not found with account: {}", account);
            throw new RuntimeException("用户不存在");
        }
    }

    public User signin(String email, String password) {
        logger.info("try login by {}...", email);
        User user = getUserByAccount(email);
        System.out.println("User found: " + user);
        if (user.getPassword().equals(password)) {
            System.out.println("Password is correct");
            return user;
        }
        throw new RuntimeException("login failed.");
    }

    public User register(String account, String password, String name, String role, 
                         String department, String contact) {
        logger.info("Attempting to register user with account: {}", account);
        User user = new User();
        user.setAccount(account);
        user.setPassword(password);
        user.setName(name);
        user.setRole(role != null ? role : "s"); // 默认为学生角色
        user.setDepartment(department);
        user.setContact(contact);
        
        KeyHolder holder = new GeneratedKeyHolder();
        try {
            if (1 != jdbcTemplate.update((conn) -> {
                var ps = conn.prepareStatement(
                    "INSERT INTO User(name, account, password, role, department, contact) " +
                    "VALUES(?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
                ps.setObject(1, user.getName());
                ps.setObject(2, user.getAccount());
                ps.setObject(3, user.getPassword());
                ps.setObject(4, user.getRole());
                ps.setObject(5, user.getDepartment());
                ps.setObject(6, user.getContact());
                return ps;
            }, holder)) {
                throw new RuntimeException("Insert failed.");
            }
        } catch (DataAccessException e) {
            logger.error("SQL Error: " + e.getMessage(), e);
            throw new RuntimeException("Insert failed due to SQL error.", e);
        }

        user.setUser_id(holder.getKey().intValue());
        return user;
    }

    public void updateUser(User user) {
        if (1 != jdbcTemplate.update(
                "UPDATE User SET name = ?, department = ?, contact = ? WHERE user_id = ?", 
                user.getName(), user.getDepartment(), user.getContact(), user.getUser_id())) {
            throw new RuntimeException("User not found by id");
        }
    }
    
    public void updatePassword(String account, String newPassword) {
        int rowsAffected = jdbcTemplate.update(
            "UPDATE User SET password = ? WHERE account = ?", 
            newPassword, account);
            
        if (rowsAffected <= 0) {
            throw new RuntimeException("Failed to update password for user with account: " + account);
        }
    }
    
    public User updateAvatar(String account, String avatarPath) {
        int rowsAffected = jdbcTemplate.update(
            "UPDATE User SET avatar_path = ? WHERE account = ?", 
            avatarPath, account);
            
        if (rowsAffected <= 0) {
            throw new RuntimeException("Failed to update avatar for user with account: " + account);
        }
        
        return getUserByAccount(account);
    }

    public User ChangeUserPwd(String newpassword, String email) {
        // 增强日志，记录尝试修改密码的详细信息
        logger.info("Attempting to change password for user with email: {}", email);

        // SQL更新语句
        String sql = "UPDATE User SET UserPwd = ? WHERE UserEmail = ?";

        // 执行更新，rowsAffected表示受影响的行数
        int rowsAffected = jdbcTemplate.update(sql, newpassword, email);

        // 增强日志，记录是否更新成功
        if (rowsAffected > 0) {
            logger.info("Password successfully changed for user with email: {}", email);
        } else {
            logger.error("Failed to update password for user with email: {}", email);
            throw new RuntimeException("Failed to update the password for user with email: " + email);
        }

        // 返回更新后的用户信息
        // 这里假设你有一个方法能够根据email从数据库中重新加载用户信息queryForObjectl = ?";
        String query = "SELECT * FROM User WHERE UserEmail = ?";
        User updatedUser = jdbcTemplate.queryForObject(query, new Object[] { email }, new UserRowMapper()); // 假设UserRowMapper是一个适当的RowMapper
        return updatedUser;
    }
    public User ChangeUserName(String newName, String email) {
        // 增强日志，记录尝试修改密码的详细信息
        logger.info("Attempting to change password for user with email: {}", email);

        // SQL更新语句
        String sql = "UPDATE User SET UserName = ? WHERE UserEmail = ?";

        // 执行更新，rowsAffected表示受影响的行数
        int rowsAffected = jdbcTemplate.update(sql, newName, email);

        // 增强日志，记录是否更新成功
        if (rowsAffected > 0) {
            logger.info("Name successfully changed for user with email: {}", email);
        } else {
            logger.error("Failed to update name for user with email: {}", email);
            throw new RuntimeException("Failed to update the password for user with email: " + email);
        }

        // 返回更新后的用户信息
        // 这里假设你有一个方法能够根据email从数据库中重新加载用户信息queryForObjectl = ?";
        String query = "SELECT * FROM User WHERE UserEmail = ?";
        User updatedUser = jdbcTemplate.queryForObject(query, new Object[] { email }, new UserRowMapper()); // 假设UserRowMapper是一个适当的RowMapper
        return updatedUser;
    }
    public User ChangeUserEmail(String newEmail, String email) {
        // 增强日志，记录尝试修改密码的详细信息
        logger.info("Attempting to change email for user with email: {}", email);

        // SQL更新语句
        String sql = "UPDATE User SET UserEmail = ? WHERE UserEmail = ?";

        // 执行更新，rowsAffected表示受影响的行数
        int rowsAffected = jdbcTemplate.update(sql, newEmail, email);

        // 增强日志，记录是否更新成功
        if (rowsAffected > 0) {
            logger.info("Email successfully changed for user with email: {}", email);
        } else {
            logger.error("Failed to update email for user with email: {}", email);
            throw new RuntimeException("Failed to update the password for user with email: " + email);
        }

        // 返回更新后的用户信息
        // 这里假设你有一个方法能够根据email从数据库中重新加载用户信息queryForObjectl = ?";
        String query = "SELECT * FROM User WHERE UserEmail = ?";
        User updatedUser = jdbcTemplate.queryForObject(query, new Object[] { email }, new UserRowMapper()); // 假设UserRowMapper是一个适当的RowMapper
        return updatedUser;
    }

    public User ChangeUserAvatar(String avatarUrl, String email) {
        logger.info("Attempting to change avatarURL for user with email: {}", email);
        String sql = "UPDATE User SET avatarUrl = ? WHERE UserEmail = ?";
        int rowsAffected = jdbcTemplate.update(sql, avatarUrl, email);
        if (rowsAffected > 0) {
            logger.info("Avatar successfully changed for user with email: {}", email);
        } else {
            logger.error("Failed to update avatarURL for user with email: {}", email);
            throw new RuntimeException("Failed to update the avatarURL for user with email: " + email);
        }
        String query = "SELECT * FROM User WHERE UserEmail = ?";
        User updatedUser = jdbcTemplate.queryForObject(query, new Object[] { email }, new UserRowMapper()); // 假设UserRowMapper是一个适当的RowMapper
        return updatedUser;
    }

    public User ChangeUserLocation(String address, String email) {
        logger.info("Attempting to change Address for user with email: {}", email);
        String sql = "UPDATE User SET Location = ? WHERE UserEmail = ?";
        int rowsAffected = jdbcTemplate.update(sql, address, email);
        if (rowsAffected > 0) {
            logger.info("Address successfully changed for user with email: {}", email);
        }else{
            logger.error("Failed to update address for user with email: {}", email);
            throw new RuntimeException("Failed to update the address for user with email: " + email);
        }
        String query = "SELECT * FROM User WHERE UserEmail = ?";
        User updatedUser = jdbcTemplate.queryForObject(query, new Object[] { email }, new UserRowMapper());
        return updatedUser;
    }

    public void SetMember(int userid) {
        jdbcTemplate.update("update User set Member=true where UserId=?", userid);
    }

    public boolean getmember(int id) {
        boolean result = jdbcTemplate.queryForObject("SELECT member FROM User WHERE UserId = ?", new Object[]{id}, Boolean.class);
        return result;
    }
    public User FindByName(String name) {
        User user = jdbcTemplate.queryForObject(
                "SELECT * FROM User WHERE UserName = ?",
                new Object[]{name},
                new BeanPropertyRowMapper<>(User.class)
        );
        return user;
    }
    public void ChangePassword(String email, String password) {
        jdbcTemplate.update("UPDATE User SET UserPwd = ? WHERE UserEmail = ?", password, email);
    }

    /**
     * 更新用户信息
     * @param userId 用户ID
     * @param name 姓名
     * @param department 部门/院系
     * @param contact 联系方式
     * @return 更新后的用户信息
     */
    public User updateUserProfile(Integer userId, String name, String department, String contact) {
        logger.info("更新用户信息: userId={}", userId);
        
        // 构建SQL
        StringBuilder sql = new StringBuilder("UPDATE User SET ");
        boolean needComma = false;
        java.util.List<Object> params = new java.util.ArrayList<>();
        
        // 动态添加要更新的字段
        if (name != null && !name.isEmpty()) {
            sql.append("name = ?");
            params.add(name);
            needComma = true;
        }
        
        if (department != null) {
            if (needComma) sql.append(", ");
            sql.append("department = ?");
            params.add(department);
            needComma = true;
        }
        
        if (contact != null) {
            if (needComma) sql.append(", ");
            sql.append("contact = ?");
            params.add(contact);
        }
        
        // 添加WHERE条件
        sql.append(" WHERE user_id = ?");
        params.add(userId);
        
        // 执行更新操作
        int rows = jdbcTemplate.update(sql.toString(), params.toArray());
        if (rows == 0) {
            logger.error("用户不存在或更新失败: userId={}", userId);
            throw new RuntimeException("更新失败：用户不存在");
        }
        
        // 返回更新后的用户信息
        return getUserById(userId);
    }

    /**
     * 获取JdbcTemplate
     * @return jdbcTemplate实例
     */
    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }
}
