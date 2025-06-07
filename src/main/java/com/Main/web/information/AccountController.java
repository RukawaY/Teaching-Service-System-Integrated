package com.Main.web.information;

import com.Main.dto.information.ApiResponseDTO;
import com.Main.dto.information.AvatarResponseDTO;
import com.Main.dto.information.PasswordUpdateDTO;
import com.Main.dto.information.UserUpdateDTO;
import com.Main.entity.information.User;
import com.Main.service.information.UserProfileService;
import com.Main.service.information.UserService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/information/api/v1/account")
public class AccountController {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private UserProfileService userProfileService;
    
    /**
     * 获取当前用户信息
     * @param request HTTP请求对象
     * @return 用户信息（不含密码）
     */
    @GetMapping("/profile")
    public ResponseEntity<ApiResponseDTO<User>> getProfile(HttpServletRequest request) {
        try {
            // 从请求属性中获取用户ID（由JWT拦截器设置）
            Integer userId = (Integer) request.getAttribute("userId");
            logger.info("获取用户个人信息: userId={}", userId);
            
            // 查询用户信息
            User user = userService.getUserById(userId);
            
            // 安全处理：不返回密码
            user.setPassword(null);
            
            return ResponseEntity.ok(ApiResponseDTO.success("获取成功", user));
        } catch (RuntimeException e) {
            logger.error("获取用户信息失败: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponseDTO.error(404, e.getMessage()));
        } catch (Exception e) {
            logger.error("获取用户信息过程中发生未知错误", e);
            return ResponseEntity.ok(ApiResponseDTO.error(500, "服务器内部错误"));
        }
    }
    
    /**
     * 修改当前用户信息
     * @param updateDTO 要修改的用户信息
     * @param request HTTP请求对象
     * @return 更新后的用户信息
     */
    @PutMapping("/profile")
    public ResponseEntity<ApiResponseDTO<User>> updateProfile(
            @RequestBody UserUpdateDTO updateDTO, 
            HttpServletRequest request) {
        try {
            // 从请求属性中获取用户ID
            Integer userId = (Integer) request.getAttribute("userId");
            logger.info("修改用户信息: userId={}, updateData={}", userId, updateDTO);
            
            // 更新用户信息
            User updatedUser = userService.updateUserProfile(
                userId, 
                updateDTO.getName(), 
                updateDTO.getDepartment(), 
                updateDTO.getContact()
            );
            
            // 安全处理：不返回密码
            updatedUser.setPassword(null);
            
            return ResponseEntity.ok(ApiResponseDTO.success("更新成功", updatedUser));
        } catch (RuntimeException e) {
            logger.error("更新用户信息失败: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponseDTO.error(400, e.getMessage()));
        } catch (Exception e) {
            logger.error("更新用户信息过程中发生未知错误", e);
            return ResponseEntity.ok(ApiResponseDTO.error(500, "服务器内部错误"));
        }
    }
    
    /**
     * 修改当前用户密码
     * @param passwordDTO 包含旧密码和新密码
     * @param request HTTP请求对象
     * @return 修改结果
     */
    @PutMapping("/password")
    public ResponseEntity<ApiResponseDTO<Void>> updatePassword(
            @RequestBody PasswordUpdateDTO passwordDTO,
            HttpServletRequest request) {
        try {
            // 从请求属性中获取用户ID
            Integer userId = (Integer) request.getAttribute("userId");
            logger.info("修改用户密码: userId={}", userId);
            
            // 更新密码
            boolean success = userProfileService.updatePassword(
                userId,
                passwordDTO.getOldPassword(),
                passwordDTO.getNewPassword()
            );
            
            if (success) {
                return ResponseEntity.ok(ApiResponseDTO.success("密码修改成功", null));
            } else {
                return ResponseEntity.ok(ApiResponseDTO.error(400, "密码修改失败"));
            }
        } catch (RuntimeException e) {
            logger.error("修改密码失败: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponseDTO.error(400, e.getMessage()));
        } catch (Exception e) {
            logger.error("修改密码过程中发生未知错误", e);
            return ResponseEntity.ok(ApiResponseDTO.error(500, "服务器内部错误"));
        }
    }
    
    /**
     * 上传/修改当前用户头像
     * @param avatar 头像文件
     * @param request HTTP请求对象
     * @return 头像访问路径
     */
    @PostMapping("/avatar")
    public ResponseEntity<ApiResponseDTO<AvatarResponseDTO>> uploadAvatar(
            @RequestParam("avatar") MultipartFile avatar,
            HttpServletRequest request) {
        try {
            // 从请求属性中获取用户ID
            Integer userId = (Integer) request.getAttribute("userId");
            logger.info("上传用户头像: userId={}, 文件名={}", userId, avatar.getOriginalFilename());
            
            // 上传头像
            String avatarPath = userProfileService.uploadAvatar(userId, avatar);
            
            // 构造返回数据
            AvatarResponseDTO responseData = new AvatarResponseDTO(avatarPath);
            
            return ResponseEntity.ok(ApiResponseDTO.success("头像上传成功", responseData));
        } catch (RuntimeException e) {
            logger.error("头像上传失败: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponseDTO.error(400, e.getMessage()));
        } catch (Exception e) {
            logger.error("头像上传过程中发生未知错误", e);
            return ResponseEntity.ok(ApiResponseDTO.error(500, "服务器内部错误"));
        }
    }
} 