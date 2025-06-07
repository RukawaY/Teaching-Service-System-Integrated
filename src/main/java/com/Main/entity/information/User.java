package com.Main.entity.information;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class User {

    private int user_id;
    private String name;
    private String account;

    @JsonIgnore
    private String password;
    
    private String role; // s-学生, t-教师, a-管理员
    private String department;
    private String contact;
    private String avatarPath;
    
    // 默认构造函数
    public User() {
    }
    
    // 带参数的构造函数
    public User(String name, String account, String password, String role, 
                String department, String contact, String avatarPath) {
        this.name = name;
        this.account = account;
        this.password = password;
        this.role = role;
        this.department = department;
        this.contact = contact;
        this.avatarPath = avatarPath;
    }
    
    // Getters and Setters
    public int getUser_id() {
        return user_id;
    }
    
    public void setUser_id(int user_id) {
        this.user_id = user_id;
            }
    
    public String getName() {
        return name;
        }
    
    public void setName(String name) {
        this.name = name;
    }

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

    public String getDepartment() {
        return department;
    }
    
    public void setDepartment(String department) {
        this.department = department;
    }
    
    public String getContact() {
        return contact;
    }
    
    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getAvatarPath() {
        return avatarPath;
    }

    public void setAvatarPath(String avatarPath) {
        this.avatarPath = avatarPath;
    }

    @Override
    public String toString() {
        return String.format("User[user_id=%d, account='%s', name='%s', role='%s', department='%s', contact='%s']",
                user_id, account, name, role, department, contact);
    }
}
