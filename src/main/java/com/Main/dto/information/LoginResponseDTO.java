package com.Main.dto.information;

import com.Main.entity.information.User;

public class LoginResponseDTO {
    private String token;
    private User userInfo;

    // 构造函数
    public LoginResponseDTO() {
    }

    public LoginResponseDTO(String token, User userInfo) {
        this.token = token;
        this.userInfo = userInfo;
    }

    // Getter和Setter方法
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public User getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(User userInfo) {
        this.userInfo = userInfo;
    }
} 