package com.Main.dto.information;

public class UserUpdateDTO {
    private String name;
    private String department;
    private String contact;
    
    // 构造函数
    public UserUpdateDTO() {
    }
    
    public UserUpdateDTO(String name, String department, String contact) {
        this.name = name;
        this.department = department;
        this.contact = contact;
    }
    
    // Getter和Setter
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
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
} 