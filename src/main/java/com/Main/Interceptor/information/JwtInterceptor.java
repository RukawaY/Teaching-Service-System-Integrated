package com.Main.Interceptor.information;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.Main.util.information.JwtTokenUtil;

import org.springframework.core.annotation.Order;

@Component
@Order(1)
public class JwtInterceptor implements HandlerInterceptor {
    
    private final Logger logger = LoggerFactory.getLogger(getClass());
    
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 登录和OPTIONS请求不需要验证
        String requestURI = request.getRequestURI();
        logger.info("JWT拦截器开始处理请求: {}", request.getRequestURI());
        if (requestURI.contains("information/api/v1/auth/login") || request.getMethod().equals("OPTIONS")) {
            return true;
        }
        
        // 从请求头获取Authorization
        String authHeader = request.getHeader("Authorization");
        logger.info("Authorization头: {}", authHeader);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                // 验证令牌有效性
                Integer userId = jwtTokenUtil.getUserIdFromToken(token);
                // 如果需要，可以验证用户ID是否存在
                
                // 获取用户角色，可以进行角色权限验证
                String role = jwtTokenUtil.getRoleFromToken(token);
                
                // 将用户ID和角色存入请求，供后续使用
                request.setAttribute("userId", userId);
                request.setAttribute("userRole", role);
                
                logger.info("JWT验证通过: userId={}, role={}, uri={}", userId, role, requestURI);
                return true;
            } catch (Exception e) {
                logger.error("JWT验证失败: {}", e.getMessage());
            }
        }
        
        // 验证失败，返回401状态码
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":401,\"message\":\"未授权或令牌已过期\",\"data\":null}");
        return false;
    }
} 