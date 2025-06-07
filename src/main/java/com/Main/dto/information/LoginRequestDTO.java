package com.Main.dto.information;

public class LoginRequestDTO {
    private String account;
    private String password;
    private String role; // 0-非管理员，1-管理员

    // 构造函数
    public LoginRequestDTO() {
    }

    public LoginRequestDTO(String account, String password, String role) {
        this.account = account;
        this.password = password;
        this.role = role;
    }

    // Getter和Setter方法
    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
} 