package com.Main.Filter.information.rss;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ResourceFilter implements Filter {
    private static final Logger logger = LoggerFactory.getLogger(ResourceFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        logger.info("ResourceFilter initialized");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // 设置响应头，处理跨域请求
        httpResponse.setHeader("Access-Control-Allow-Origin", "*");
        httpResponse.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        httpResponse.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        httpResponse.setHeader("Access-Control-Max-Age", "3600");

        // 添加安全相关的响应头
        httpResponse.setHeader("X-Content-Type-Options", "nosniff");
        httpResponse.setHeader("X-Frame-Options", "DENY");
        httpResponse.setHeader("X-XSS-Protection", "1; mode=block");

        // 设置缓存控制
        if (isResourceRequest(httpRequest)) {
            // 资源文件缓存1小时
            httpResponse.setHeader("Cache-Control", "public, max-age=3600");
        } else {
            // 其他请求不缓存
            httpResponse.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            httpResponse.setHeader("Pragma", "no-cache");
            httpResponse.setHeader("Expires", "0");
        }

        // 记录请求信息
        logRequestInfo(httpRequest);

        // 继续过滤器链的处理
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        logger.info("ResourceFilter destroyed");
    }

    /**
     * 判断是否是资源文件请求
     */
    private boolean isResourceRequest(HttpServletRequest request) {
        String uri = request.getRequestURI().toLowerCase();
        return uri.endsWith(".pdf") || uri.endsWith(".doc") || uri.endsWith(".docx") ||
               uri.endsWith(".xls") || uri.endsWith(".xlsx") || uri.endsWith(".ppt") ||
               uri.endsWith(".pptx") || uri.endsWith(".txt") || uri.endsWith(".jpg") ||
               uri.endsWith(".jpeg") || uri.endsWith(".png") || uri.endsWith(".gif");
    }

    /**
     * 记录请求信息
     */
    private void logRequestInfo(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String method = request.getMethod();
        String userAgent = request.getHeader("User-Agent");
        String remoteAddr = request.getRemoteAddr();

        logger.debug("Resource Request - URI: {}, Method: {}, IP: {}, User-Agent: {}",
                    uri, method, remoteAddr, userAgent);
    }
}
