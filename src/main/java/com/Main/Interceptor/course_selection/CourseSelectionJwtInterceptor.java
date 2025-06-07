package com.Main.Interceptor.course_selection;

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
@Order(3)
public class CourseSelectionJwtInterceptor implements HandlerInterceptor {
    
    private final Logger logger = LoggerFactory.getLogger(getClass());
    
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();
        logger.info("Course Selection JWT拦截器开始处理请求: {}", requestURI);
        
        // OPTIONS请求直接放行
        if (request.getMethod().equals("OPTIONS")) {
            return true;
        }
        
        // 不需要JWT验证的路径
        if (requestURI.equals("/course_selection/search_course") || 
            requestURI.equals("/course_selection/search_course_table") ||
            requestURI.equals("/course_selection/get_curriculum") ||
            requestURI.contains("/course_selection") ||
            requestURI.contains("/course_selection/permit")) {
            logger.info("无需JWT验证的路径: {}", requestURI);
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
                String role = jwtTokenUtil.getRoleFromToken(token);
                
                // 角色权限验证
                if (!hasPermission(requestURI, role)) {
                    logger.warn("用户角色 {} 无权访问路径: {}", role, requestURI);
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"code\":403,\"message\":\"权限不足\",\"data\":null}");
                    return false;
                }
                
                // 将用户ID和角色存入请求，供后续使用
                request.setAttribute("userId", userId);
                request.setAttribute("userRole", role);
                
                logger.info("Course Selection JWT验证通过: userId={}, role={}, uri={}", userId, role, requestURI);
                return true;
            } catch (Exception e) {
                logger.error("Course Selection JWT验证失败: {}", e.getMessage());
            }
        }
        
        // 验证失败，返回401状态码
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":401,\"message\":\"未授权或令牌已过期\",\"data\":null}");
        return false;
    }
    
    /**
     * 检查用户角色是否有权限访问指定路径
     */
    private boolean hasPermission(String requestURI, String role) {
        // 学生权限：只能访问student路径
        if (requestURI.contains("/course_selection/student")) {
            return "s".equals(role);
        }
        
        // 教师权限：只能访问teacher路径
        if (requestURI.contains("/course_selection/teacher")) {
            return "t".equals(role);
        }
        
        // 管理员权限：只能访问manager路径
        if (requestURI.contains("/course_selection/manager")) {
            return "a".equals(role);
        }
        
        // 其他路径默认拒绝
        return false;
    }
} 