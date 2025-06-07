package com.Main.entity.arrange;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/*
    CREATE TABLE User (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(20) NOT NULL,
    account VARCHAR(100) NOT NULL,
    password VARCHAR(100) NOT NULL,
    role ENUM('s', 't', 'a') NOT NULL DEFAULT 's',
    department VARCHAR(20) NOT NULL,
    contact VARCHAR(50) NOT NULL,
    avatar_path VARCHAR(200)
);
 */

/**
 * this class is only used for debug.
 * after merging, it will be deleted.
 */
public class User {
    private int userId;
    private String name;
    private String account;
    private String password;
    private String role;
    private String department;
    private String contact;
    private String avatarPath;

    public User() {}

    public User(String name, String account, String password, String role, String department, String contact, String avatarPath) {
        this.name = name;
        this.account = account;
        this.password = password;
        this.role = role;
        this.department = department;
        this.contact = contact;
        this.avatarPath = avatarPath;
    }

    public int getId() {
        return userId;
    }

    public void setId(int userId) {
        this.userId = userId;
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
        return String.format("User[id=%d, name='%s', account='%s', role='%s', department='%s', contact='%s', avatarPath='%s']", 
            getId(), getName(), getAccount(), getRole(), getDepartment(), getContact(), getAvatarPath());
    }
}
