package com.Main.web.information;

import com.Main.dto.information.AdminUserUpdateDTO;
import com.Main.dto.information.ApiResponseDTO;
import com.Main.dto.information.PageResponseDTO;
import com.Main.dto.information.PasswordResetDTO;
import com.Main.dto.information.UserCreateDTO;
import com.Main.entity.information.User;
import com.Main.service.information.AdminUserService;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.util.List;

@RestController
@RequestMapping("/information/api/v1/admin")
public class AdminUserController {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    
    @Autowired
    private AdminUserService adminUserService;
    
    /**
     * 获取用户列表（支持分页和筛选）
     * @param page 页码
     * @param size 每页数量
     * @param name 按姓名模糊查询
     * @param account 按账户名查询
     * @param role 按角色筛选
     * @param department 按部门筛选
     * @param request HTTP请求对象
     * @return 分页用户列表
     */
    @GetMapping("/users")
    public ResponseEntity<ApiResponseDTO<PageResponseDTO<User>>> getUserList(
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String account,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String department,
            HttpServletRequest request) {
        
        try {
            // 获取当前用户角色（由JWT拦截器设置）
            String userRole = (String) request.getAttribute("userRole");
            
            // 检查权限
            if (!adminUserService.isAdmin(userRole)) {
                logger.warn("非管理员尝试访问管理接口: userRole={}", userRole);
                return ResponseEntity.ok(ApiResponseDTO.error(403, "权限不足"));
            }
            
            // 获取用户列表
            PageResponseDTO<User> pageResponse = adminUserService.getUserList(
                    page, size, name, account, role, department);
            
            return ResponseEntity.ok(ApiResponseDTO.success("获取成功", pageResponse));
        } catch (Exception e) {
            logger.error("获取用户列表失败: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponseDTO.error(500, "服务器内部错误: " + e.getMessage()));
        }
    }
    
    /**
     * 获取指定用户信息
     * @param userId 用户ID
     * @param request HTTP请求对象
     * @return 用户信息
     */
    @GetMapping("/users/{userId}")
    public ResponseEntity<ApiResponseDTO<User>> getUser(
            @PathVariable int userId,
            HttpServletRequest request) {
        
        try {
            // 获取当前用户角色（由JWT拦截器设置）
            String userRole = (String) request.getAttribute("userRole");
            
            // 检查权限
            if (!adminUserService.isAdmin(userRole)) {
                logger.warn("非管理员尝试获取用户信息: userRole={}", userRole);
                return ResponseEntity.ok(ApiResponseDTO.error(403, "权限不足"));
            }
            
            // 获取用户信息
            User user = adminUserService.getUser(userId);
            
            return ResponseEntity.ok(ApiResponseDTO.success("获取成功", user));
        } catch (RuntimeException e) {
            logger.error("获取用户信息失败: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponseDTO.error(404, e.getMessage()));
        } catch (Exception e) {
            logger.error("获取用户信息过程中发生未知错误: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponseDTO.error(500, "服务器内部错误"));
        }
    }
    
    /**
     * 创建新用户
     * @param createDTO 用户创建DTO
     * @param request HTTP请求对象
     * @return 创建的用户信息
     */
    @PostMapping("/users")
    public ResponseEntity<ApiResponseDTO<User>> createUser(
            @RequestBody UserCreateDTO createDTO,
            HttpServletRequest request) {
        
        try {
            // 获取当前用户角色（由JWT拦截器设置）
            String userRole = (String) request.getAttribute("userRole");
            
            // 检查权限
            if (!adminUserService.isAdmin(userRole)) {
                logger.warn("非管理员尝试创建用户: userRole={}", userRole);
                return ResponseEntity.ok(ApiResponseDTO.error(403, "权限不足"));
            }
            
            // 创建用户
            User newUser = adminUserService.createUser(
                    createDTO.getName(),
                    createDTO.getAccount(),
                    createDTO.getPassword(),
                    createDTO.getRole(),
                    createDTO.getDepartment(),
                    createDTO.getContact(),
                    createDTO.getMajorId() // 添加专业ID
            );
            
            return ResponseEntity.ok(ApiResponseDTO.success("用户创建成功", newUser));
        } catch (RuntimeException e) {
            logger.error("创建用户失败: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponseDTO.error(400, e.getMessage()));
        } catch (Exception e) {
            logger.error("创建用户过程中发生未知错误: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponseDTO.error(500, "服务器内部错误"));
        }
    }

    /**
     * 修改指定用户信息
     * @param userId 用户ID
     * @param updateDTO 用户更新DTO
     * @param request HTTP请求对象
     * @return 更新后的用户信息
     */
    @PutMapping("/users/{userId}")
    public ResponseEntity<ApiResponseDTO<User>> updateUser(
            @PathVariable int userId,
            @RequestBody AdminUserUpdateDTO updateDTO,
            HttpServletRequest request) {
        
        try {
            // 获取当前用户角色（由JWT拦截器设置）
            String userRole = (String) request.getAttribute("userRole");
            
            // 检查权限
            if (!adminUserService.isAdmin(userRole)) {
                logger.warn("非管理员尝试修改用户信息: userRole={}", userRole);
                return ResponseEntity.ok(ApiResponseDTO.error(403, "权限不足"));
            }
            
            // 更新用户信息
            User updatedUser = adminUserService.updateUser(
                    userId,
                    updateDTO.getName(),
                    updateDTO.getRole(),
                    updateDTO.getDepartment(),
                    updateDTO.getContact(),
                    updateDTO.getMajorId() // 添加专业ID
            );
            
            return ResponseEntity.ok(ApiResponseDTO.success("更新成功", updatedUser));
        } catch (RuntimeException e) {
            logger.error("更新用户信息失败: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponseDTO.error(400, e.getMessage()));
        } catch (Exception e) {
            logger.error("更新用户信息过程中发生未知错误: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponseDTO.error(500, "服务器内部错误"));
        }
    }

    /**
     * 删除指定用户
     * @param userId 用户ID
     * @param request HTTP请求对象
     * @return 删除结果
     */
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteUser(
            @PathVariable int userId,
            HttpServletRequest request) {
        
        try {
            // 获取当前用户角色（由JWT拦截器设置）
            String userRole = (String) request.getAttribute("userRole");
            
            // 检查权限
            if (!adminUserService.isAdmin(userRole)) {
                logger.warn("非管理员尝试删除用户: userRole={}", userRole);
                return ResponseEntity.ok(ApiResponseDTO.error(403, "权限不足"));
            }
            
            // 删除用户
            adminUserService.deleteUser(userId);
            
            return ResponseEntity.ok(ApiResponseDTO.success("删除成功", null));
        } catch (RuntimeException e) {
            logger.error("删除用户失败: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponseDTO.error(400, e.getMessage()));
        } catch (Exception e) {
            logger.error("删除用户过程中发生未知错误: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponseDTO.error(500, "服务器内部错误"));
        }
    }

    /**
     * 重置用户密码
     * @param userId 用户ID
     * @param resetDTO 密码重置DTO
     * @param request HTTP请求对象
     * @return 重置结果
     */
    @PostMapping("/users/{userId}/reset-password")
    public ResponseEntity<ApiResponseDTO<Map<String, String>>> resetUserPassword(
            @PathVariable int userId,
            @RequestBody(required = false) PasswordResetDTO resetDTO,
            HttpServletRequest request) {
        
        try {
            // 获取当前用户角色（由JWT拦截器设置）
            String userRole = (String) request.getAttribute("userRole");
            
            // 检查权限
            if (!adminUserService.isAdmin(userRole)) {
                logger.warn("非管理员尝试重置用户密码: userRole={}", userRole);
                return ResponseEntity.ok(ApiResponseDTO.error(403, "权限不足"));
            }
            
            // 获取新密码（如果提供）
            String newPassword = (resetDTO != null) ? resetDTO.getNewPassword() : null;
            
            // 重置密码
            String passwordInfo = adminUserService.resetUserPassword(userId, newPassword);
            
            // 构建响应数据
            Map<String, String> responseData = new HashMap<>();
            responseData.put("new_password_info", passwordInfo);
            
            return ResponseEntity.ok(ApiResponseDTO.success("密码重置成功", responseData));
        } catch (RuntimeException e) {
            logger.error("重置用户密码失败: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponseDTO.error(400, e.getMessage()));
        } catch (Exception e) {
            logger.error("重置用户密码过程中发生未知错误: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponseDTO.error(500, "服务器内部错误"));
        }
    }

    /**
     * 批量创建用户
     * @param file JSON文件
     * @param request HTTP请求对象
     * @return 创建的用户列表
     */
    @PostMapping("/users/batch-create")
    public ResponseEntity<ApiResponseDTO<List<User>>> batchCreateUsers(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) {
        try {
            // 获取当前用户角色（由JWT拦截器设置）
            String userRole = (String) request.getAttribute("userRole");

            // 检查权限
            if (!adminUserService.isAdmin(userRole)) {
                logger.warn("非管理员尝试批量创建用户: userRole={}", userRole);
                return ResponseEntity.ok(ApiResponseDTO.error(403, "权限不足"));
            }

            // 将文件保存到临时目录
            File tempFile = File.createTempFile("users", ".json");
            file.transferTo(tempFile);

            // 调用服务层批量创建用户
            List<User> createdUsers = adminUserService.batchCreateUsers(tempFile);

            return ResponseEntity.ok(ApiResponseDTO.success("批量创建用户成功", createdUsers));
        } catch (Exception e) {
            logger.error("批量创建用户失败: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponseDTO.error(500, "服务器内部错误: " + e.getMessage()));
        }
    }
}