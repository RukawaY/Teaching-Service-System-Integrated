package com.Main.dto.information;

public class PasswordResetDTO {
    private String newPassword;
    
    // 构造函数
    public PasswordResetDTO() {
    }
    
    // Getter和Setter
    public String getNewPassword() {
        return newPassword;
    }
    
    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
} 