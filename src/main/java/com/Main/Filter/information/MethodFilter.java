package com.Main.Filter.information;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.Main.service.information.UserService;

import java.io.IOException;

@Component
//@WebFilter(value = "/api/*", filterName = "MethodFilter") // Springboot 对Filter配置拦截资源路径的方法。
public class MethodFilter implements Filter {

    final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    UserService userService;

    public void init(FilterConfig filterConfig) throws ServletException {
        System.out.println("init MethodFilter");
    }


    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        String url = req.getRequestURI();
        String method = req.getMethod();
        System.out.println("url: " + url + ", method: " + method);
        try {

        } catch (RuntimeException e) {
            logger.warn("login by authorization header failed.", e);
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy(){
        System.out.println("destroy MethodFilter");
    }
}
