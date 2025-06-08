package com.Main.dto.information;

public class AdminUserUpdateDTO {
    private String name;
    private String role;
    private String department;
    private String contact;
    private Integer majorId; // 添加专业ID字段，可为空
    
    // 构造函数
    public AdminUserUpdateDTO() {
    }
    
    // Getter和Setter
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
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

    public Integer getMajorId() {
        return majorId;
    }

    public void setMajorId(Integer majorId) {
        this.majorId = majorId;
    }
}