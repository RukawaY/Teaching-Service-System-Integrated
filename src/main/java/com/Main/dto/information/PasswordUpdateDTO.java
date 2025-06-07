package com.Main.dto.information;

public class PasswordUpdateDTO {
    private String oldPassword;
    private String newPassword;
    
    // 构造函数
    public PasswordUpdateDTO() {
    }
    
    public PasswordUpdateDTO(String oldPassword, String newPassword) {
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
    }
    
    // Getter和Setter
    public String getOldPassword() {
        return oldPassword;
    }
    
    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }
    
    public String getNewPassword() {
        return newPassword;
    }
    
    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
} 