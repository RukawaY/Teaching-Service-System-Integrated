package com.Main.service.information;

import com.Main.dto.information.LoginRequestDTO;
import com.Main.dto.information.LoginResponseDTO;
import com.Main.entity.information.User;
import com.Main.util.information.JwtTokenUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    
    private final Logger logger = LoggerFactory.getLogger(getClass());
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    
    /**
     * 用户登录
     * @param loginRequest 登录请求DTO
     * @return 登录响应DTO，包含令牌和用户信息
     */
    public LoginResponseDTO login(LoginRequestDTO loginRequest) {
        logger.info("尝试登录账户: {}", loginRequest.getAccount());
        
        // 1. 通过账号查找用户
        User user = userService.getUserByAccount(loginRequest.getAccount());
        
        // 2. 验证密码
        if (!user.getPassword().equals(loginRequest.getPassword())) {
            logger.error("账户密码不正确: {}", loginRequest.getAccount());
            throw new RuntimeException("用户名或密码错误");
        }
        
        // 3. 验证角色（如果需要）
        String expectedRole = "s"; // 默认为学生
        if ("a".equals(loginRequest.getRole())) {
            // 如果请求的是管理员角色
            expectedRole = "a";
        }
        if ("t".equals(loginRequest.getRole())) {
            expectedRole = "t";
        }
        if (!expectedRole.equals(user.getRole())) {
            logger.error("角色不匹配, 账户: {}, 预期角色: {}, 实际角色: {}", 
                loginRequest.getAccount(), expectedRole, user.getRole());
            throw new RuntimeException("用户角色不匹配");
        }
        
        // 4. 生成JWT令牌
        String token = jwtTokenUtil.generateToken(user.getUser_id(), user.getRole());
        
        // 5. 构造响应对象
        LoginResponseDTO response = new LoginResponseDTO();
        response.setToken(token);
        
        // 设置用户信息（密码不应返回给客户端）
        // 这里创建新对象进行脱敏
        User userInfo = new User();
        userInfo.setUser_id(user.getUser_id());
        userInfo.setName(user.getName());
        userInfo.setAccount(user.getAccount());
        userInfo.setRole(user.getRole());
        userInfo.setDepartment(user.getDepartment());
        userInfo.setContact(user.getContact());
        userInfo.setAvatarPath(user.getAvatarPath());
        
        response.setUserInfo(userInfo);
        
        logger.info("登录成功: {}", loginRequest.getAccount());
        return response;
    }
} 