package com.Main.web.information;

import com.Main.dto.information.ApiResponseDTO;
import com.Main.dto.information.LoginRequestDTO;
import com.Main.dto.information.LoginResponseDTO;
import com.Main.service.information.AuthService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/information/api/v1/auth")
public class AuthController {
    
    private final Logger logger = LoggerFactory.getLogger(getClass());
    
    @Autowired
    private AuthService authService;
    
    /**
     * 用户登录接口
     * @param loginRequest 登录请求DTO
     * @return 包含token和用户信息的响应
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponseDTO<LoginResponseDTO>> login(@RequestBody LoginRequestDTO loginRequest) {
        try {
            logger.info("接收到登录请求：{}", loginRequest.getAccount());
            LoginResponseDTO loginResponse = authService.login(loginRequest);
            return ResponseEntity.ok(ApiResponseDTO.success("登录成功", loginResponse));
        } catch (RuntimeException e) {
            logger.error("登录失败：{}", e.getMessage());
            return ResponseEntity.ok(ApiResponseDTO.error(1001, e.getMessage()));
        } catch (Exception e) {
            logger.error("登录过程中发生未知错误", e);
            return ResponseEntity.ok(ApiResponseDTO.error(500, "服务器内部错误"));
        }
    }
} 