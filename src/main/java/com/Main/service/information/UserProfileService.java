package com.Main.service.information;

import com.Main.entity.information.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class UserProfileService {
    
    private final Logger logger = LoggerFactory.getLogger(getClass());
    
    @Autowired
    private UserService userService;
    
    // 头像存储的基础路径，默认为项目根目录下的avatars文件夹
    @Value("${avatar.upload.path:src/main/webapp/avatars}")
    private String avatarUploadPath;
    
    /**
     * 更新用户密码
     * @param userId 用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 是否更新成功
     */
    public boolean updatePassword(Integer userId, String oldPassword, String newPassword) {
        logger.info("更新用户密码: userId={}", userId);
        
        // 验证参数
        if (oldPassword == null || newPassword == null) {
            throw new RuntimeException("密码不能为空");
        }
        
        if (newPassword.length() < 6) {
            throw new RuntimeException("新密码长度不能小于6位");
        }
        
        // 获取用户信息并验证旧密码
        User user = userService.getUserById(userId);
        if (!oldPassword.equals(user.getPassword())) {
            logger.error("旧密码验证失败: userId={}", userId);
            throw new RuntimeException("原密码不正确");
        }
        
        // 更新密码
        try {
            // 构建SQL
            String sql = "UPDATE User SET password = ? WHERE user_id = ?";
            int rows = userService.getJdbcTemplate().update(sql, newPassword, userId);
            
            if (rows == 0) {
                logger.error("更新密码失败: userId={}", userId);
                throw new RuntimeException("更新密码失败");
            }
            
            logger.info("更新密码成功: userId={}", userId);
            return true;
        } catch (Exception e) {
            logger.error("更新密码过程中发生错误: userId={}, error={}", userId, e.getMessage());
            throw new RuntimeException("更新密码失败：" + e.getMessage());
        }
    }
    
    /**
     * 上传并更新用户头像
     * @param userId 用户ID
     * @param file 头像文件
     * @return 头像访问路径
     */
    public String uploadAvatar(Integer userId, MultipartFile file) {
        logger.info("上传用户头像: userId={}, 文件名={}, 大小={}", 
                userId, file.getOriginalFilename(), file.getSize());
        
        if (file.isEmpty()) {
            throw new RuntimeException("上传的文件不能为空");
        }
        
        // 验证文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new RuntimeException("仅支持上传图片文件");
        }
        
        try {
            // 确保目录存在
            File uploadDir = new File(avatarUploadPath);
            if (!uploadDir.exists()) {
                if (!uploadDir.mkdirs()) {
                    throw new RuntimeException("创建头像存储目录失败");
                }
            }
            
            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String newFilename = userId + "_" + UUID.randomUUID().toString() + fileExtension;
            
            // 构建完整文件路径
            Path filePath = Paths.get(avatarUploadPath, newFilename);
            
            // 保存文件
            Files.copy(file.getInputStream(), filePath);
            
            // 文件保存成功后，更新数据库
            String avatarPath = "/avatars/" + newFilename;
            updateUserAvatarPath(userId, avatarPath);
            
            logger.info("头像上传成功: userId={}, 路径={}", userId, avatarPath);
            return avatarPath;
        } catch (IOException e) {
            logger.error("头像文件保存失败: userId={}, error={}", userId, e.getMessage());
            throw new RuntimeException("头像文件保存失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新用户头像路径
     * @param userId 用户ID
     * @param avatarPath 新的头像路径
     */
    private void updateUserAvatarPath(Integer userId, String avatarPath) {
        try {
            String sql = "UPDATE User SET avatar_path = ? WHERE user_id = ?";
            int rows = userService.getJdbcTemplate().update(sql, avatarPath, userId);
            
            if (rows == 0) {
                throw new RuntimeException("更新头像路径失败：用户不存在");
            }
        } catch (Exception e) {
            logger.error("更新用户头像路径失败: userId={}, error={}", userId, e.getMessage());
            throw new RuntimeException("更新头像路径失败: " + e.getMessage());
        }
    }
} 