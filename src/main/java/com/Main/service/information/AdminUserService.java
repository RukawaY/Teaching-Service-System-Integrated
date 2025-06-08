package com.Main.service.information;

import com.Main.RowMapper.information.UserRowMapper;
import com.Main.dto.information.PageResponseDTO;
import com.Main.entity.information.User;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class AdminUserService {
    
    private final Logger logger = LoggerFactory.getLogger(getClass());
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    private UserService userService;
    
    /**
     * 分页查询用户列表（支持多条件筛选）
     * @param page 页码（从1开始）
     * @param size 每页记录数
     * @param name 用户姓名（模糊匹配）
     * @param account 账户名
     * @param role 角色
     * @param department 部门
     * @return 分页结果
     */
    public PageResponseDTO<User> getUserList(int page, int size, String name, 
                                             String account, String role, String department) {
        logger.info("查询用户列表: page={}, size={}, name={}, account={}, role={}, department={}",
                page, size, name, account, role, department);
        
        // 规范化参数
        if (page < 1) page = 1;
        if (size < 1) size = 10;
        if (size > 100) size = 100; // 限制最大记录数
        
        // 构建基础SQL和参数
        StringBuilder countSql = new StringBuilder("SELECT COUNT(*) FROM User WHERE 1=1");
        StringBuilder querySql = new StringBuilder("SELECT * FROM User WHERE 1=1");
        List<Object> params = new ArrayList<>();
        
        // 添加筛选条件
        if (name != null && !name.isEmpty()) {
            countSql.append(" AND name LIKE ?");
            querySql.append(" AND name LIKE ?");
            params.add("%" + name + "%");
        }
        
        if (account != null && !account.isEmpty()) {
            countSql.append(" AND account = ?");
            querySql.append(" AND account = ?");
            params.add(account);
        }
        
        if (role != null && !role.isEmpty()) {
            countSql.append(" AND role = ?");
            querySql.append(" AND role = ?");
            params.add(role);
        }
        
        if (department != null && !department.isEmpty()) {
            countSql.append(" AND department LIKE ?");
            querySql.append(" AND department LIKE ?");
            params.add("%" + department + "%");
        }
        
        // 添加排序和分页
        querySql.append(" ORDER BY user_id ASC LIMIT ? OFFSET ?");
        
        // 计算偏移量
        int offset = (page - 1) * size;
        
        // 添加分页参数
        List<Object> queryParams = new ArrayList<>(params);
        queryParams.add(size);
        queryParams.add(offset);
        
        // 查询总记录数
        int totalItems = jdbcTemplate.queryForObject(countSql.toString(), Integer.class, params.toArray());
        
        // 计算总页数
        int totalPages = (totalItems + size - 1) / size;
        
        // 查询当前页数据
        List<User> users = new ArrayList<>();
        if (totalItems > 0) {
            users = jdbcTemplate.query(querySql.toString(), new UserRowMapper(), queryParams.toArray());
            
            // 安全处理：不返回密码字段
            for (User user : users) {
                user.setPassword(null);
            }
        }
        
        // 构建分页响应
        return new PageResponseDTO<>(totalItems, totalPages, page, users);
    }
    
    /**
     * 根据用户ID获取用户信息
     * @param userId 用户ID
     * @return 用户信息
     */
    public User getUser(int userId) {
        logger.info("获取用户信息: userId={}", userId);
        
        try {
            String sql = "SELECT * FROM User WHERE user_id = ?";
            User user = jdbcTemplate.queryForObject(sql, new UserRowMapper(), userId);
            
            // 安全处理：不返回密码字段
            if (user != null) {
                user.setPassword(null);
            }
            
            return user;
        } catch (Exception e) {
            logger.error("获取用户信息失败: {}", e.getMessage());
            throw new RuntimeException("用户不存在或查询失败");
        }
    }
    
    /**
     * 检查当前用户是否为管理员
     * @param userRole 用户角色
     * @return 是否管理员
     */
    public boolean isAdmin(String userRole) {
        return "a".equals(userRole);
    }
    
    /**
     * 创建新用户
     * @param name 姓名
     * @param account 账号
     * @param password 密码
     * @param role 角色
     * @param department 部门
     * @param contact 联系方式
     * @return 创建的用户
     */
    public User createUser(String name, String account, String password, 
                           String role, String department, String contact) {
        logger.info("创建新用户: name={}, account={}, role={}", name, account, role);
        
        // 参数验证
        if (name == null || name.isEmpty()) {
            throw new RuntimeException("姓名不能为空");
        }
        if (account == null || account.isEmpty()) {
            throw new RuntimeException("账号不能为空");
        }
        if (password == null || password.isEmpty()) {
            throw new RuntimeException("密码不能为空");
        }
        if (role == null || role.isEmpty()) {
            throw new RuntimeException("角色不能为空");
        }
        
        // 验证角色是否有效
        if (!role.equals("s") && !role.equals("t") && !role.equals("a")) {
            throw new RuntimeException("无效的角色值，有效值为：s(学生), t(教师), a(管理员)");
        }
        
        // 判断账号是否已存在
        try {
            String checkSql = "SELECT COUNT(*) FROM User WHERE account = ?";
            Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, account);
            if (count != null && count > 0) {
                throw new RuntimeException("账号已存在");
            }
        } catch (Exception e) {
            if (!(e instanceof RuntimeException)) {
                logger.error("检查账号是否存在时发生错误: {}", e.getMessage());
                throw new RuntimeException("创建用户失败：系统错误");
            } else {
                throw e;
            }
        }
        
        // 创建用户
        try {
            String sql = "INSERT INTO User (name, account, password, role, department, contact) VALUES (?, ?, ?, ?, ?, ?)";
            Object[] params = {name, account, password, role, department, contact};
            int result = jdbcTemplate.update(sql, params);
            
            if (result <= 0) {
                throw new RuntimeException("创建用户失败");
            }
            
            // 查询新创建的用户
            String querySql = "SELECT * FROM User WHERE account = ?";
            User newUser = jdbcTemplate.queryForObject(querySql, new UserRowMapper(), account);
            
            // 安全处理
            if (newUser != null) {
                newUser.setPassword(null);
            }
            
            logger.info("用户创建成功: userId={}, account={}", 
                    newUser != null ? newUser.getUser_id() : "unknown", account);
            return newUser;
        } catch (Exception e) {
            if (!(e instanceof RuntimeException)) {
                logger.error("创建用户过程中发生错误: {}", e.getMessage());
                throw new RuntimeException("创建用户失败：" + e.getMessage());
            } else {
                throw e;
            }
        }
    }
    
    /**
     * 更新指定用户信息
     * @param userId 用户ID
     * @param name 姓名
     * @param role 角色
     * @param department 部门
     * @param contact 联系方式
     * @return 更新后的用户
     */
    public User updateUser(int userId, String name, String role, 
                           String department, String contact) {
        logger.info("更新用户信息: userId={}, name={}, role={}, department={}, contact={}", 
                userId, name, role, department, contact);
        
        // 验证用户是否存在
        try {
            getUser(userId); // 如果用户不存在，会抛出异常
        } catch (RuntimeException e) {
            throw new RuntimeException("用户不存在");
        }
        
        // 验证角色是否有效
        if (role != null && !role.isEmpty() && 
                !role.equals("s") && !role.equals("t") && !role.equals("a")) {
            throw new RuntimeException("无效的角色值，有效值为：s(学生), t(教师), a(管理员)");
        }
        
        // 构建更新SQL
        StringBuilder updateSql = new StringBuilder("UPDATE User SET");
        List<Object> params = new ArrayList<>();
        boolean needComma = false;
        
        if (name != null && !name.isEmpty()) {
            updateSql.append(" name = ?");
            params.add(name);
            needComma = true;
        }
        
        if (role != null && !role.isEmpty()) {
            if (needComma) {
                updateSql.append(",");
            }
            updateSql.append(" role = ?");
            params.add(role);
            needComma = true;
        }
        
        if (department != null) {
            if (needComma) {
                updateSql.append(",");
            }
            updateSql.append(" department = ?");
            params.add(department);
            needComma = true;
        }
        
        if (contact != null) {
            if (needComma) {
                updateSql.append(",");
            }
            updateSql.append(" contact = ?");
            params.add(contact);
        }
        
        // 如果没有需要更新的字段，直接返回用户信息
        if (params.isEmpty()) {
            return getUser(userId);
        }
        
        // 添加WHERE条件
        updateSql.append(" WHERE user_id = ?");
        params.add(userId);
        
        // 执行更新
        try {
            int result = jdbcTemplate.update(updateSql.toString(), params.toArray());
            
            if (result <= 0) {
                throw new RuntimeException("更新用户失败");
            }
            
            // 返回更新后的用户信息
            return getUser(userId);
        } catch (Exception e) {
            if (!(e instanceof RuntimeException)) {
                logger.error("更新用户过程中发生错误: {}", e.getMessage());
                throw new RuntimeException("更新用户失败: " + e.getMessage());
            } else {
                throw e;
            }
        }
    }
    
    /**
     * 删除指定用户
     * @param userId 用户ID
     * @return 删除结果
     */
    public boolean deleteUser(int userId) {
        logger.info("删除用户: userId={}", userId);
        
        // 验证用户是否存在
        try {
            getUser(userId); // 如果用户不存在，会抛出异常
        } catch (RuntimeException e) {
            throw new RuntimeException("用户不存在");
        }
        
        // 执行删除
        try {
            String sql = "DELETE FROM User WHERE user_id = ?";
            int result = jdbcTemplate.update(sql, userId);
            
            if (result <= 0) {
                throw new RuntimeException("删除用户失败");
            }
            
            return true;
        } catch (Exception e) {
            if (!(e instanceof RuntimeException)) {
                logger.error("删除用户过程中发生错误: {}", e.getMessage());
                throw new RuntimeException("删除用户失败: " + e.getMessage());
            } else {
                throw e;
            }
        }
    }
    
    /**
     * 重置用户密码
     * @param userId 用户ID
     * @param newPassword 新密码（如果为null或空，则生成随机密码）
     * @return 新密码信息
     */
    public String resetUserPassword(int userId, String newPassword) {
        logger.info("重置用户密码: userId={}", userId);
        
        // 验证用户是否存在
        try {
            getUser(userId); // 如果用户不存在，会抛出异常
        } catch (RuntimeException e) {
            throw new RuntimeException("用户不存在");
        }
        
        // 如果未提供新密码，则生成随机密码
        if (newPassword == null || newPassword.isEmpty()) {
            newPassword = generateRandomPassword();
        }
        
        // 更新用户密码
        try {
            String sql = "UPDATE User SET password = ? WHERE user_id = ?";
            int result = jdbcTemplate.update(sql, newPassword, userId);
            
            if (result <= 0) {
                throw new RuntimeException("重置密码失败");
            }
            
            // 返回新密码信息
            return newPassword;
        } catch (Exception e) {
            if (!(e instanceof RuntimeException)) {
                logger.error("重置密码过程中发生错误: {}", e.getMessage());
                throw new RuntimeException("重置密码失败: " + e.getMessage());
            } else {
                throw e;
            }
        }
    }
    
    /**
     * 生成随机密码
     * @return 随机密码
     */
    private String generateRandomPassword() {
        // 简单的随机密码生成逻辑，实际应用可能需要更复杂的算法
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        java.util.Random random = new java.util.Random();
        
        // 生成8位随机密码
        for (int i = 0; i < 8; i++) {
            int index = random.nextInt(chars.length());
            sb.append(chars.charAt(index));
        }
        
        return sb.toString();
    }

    /**
     * 批量创建用户
     * @param jsonFile JSON文件路径
     * @return 创建结果
     */
    public List<User> batchCreateUsers(File jsonFile) {
        logger.info("批量创建用户: 文件路径={}", jsonFile.getAbsolutePath());
        
        try {
            // 解析JSON文件
            ObjectMapper objectMapper = new ObjectMapper();
            List<Map<String, String>> userList = objectMapper.readValue(jsonFile, List.class);
            
            List<User> createdUsers = new ArrayList<>();
            
            for (Map<String, String> userData : userList) {
                String name = userData.get("name");
                String account = userData.get("account");
                String password = userData.get("password");
                String role = userData.get("role");
                String department = userData.get("department");
                String contact = userData.get("contact");
                
                try {
                    // 调用现有的创建用户方法
                    User newUser = createUser(name, account, password, role, department, contact);
                    createdUsers.add(newUser);
                } catch (RuntimeException e) {
                    logger.error("创建用户失败: {}", e.getMessage());
                }
            }
            
            return createdUsers;
        } catch (Exception e) {
            logger.error("批量创建用户失败: {}", e.getMessage());
            throw new RuntimeException("批量创建用户失败：" + e.getMessage());
        }
    }
}