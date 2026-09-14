package com.aigrading.model;

import java.io.Serializable;
import java.sql.Timestamp;

public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private int userId;
    private String fullName;
    private String email;
    private String passwordHash;
    private String role;
    private Timestamp createdAt;

    public User() {}

    public User(int userId, String fullName, String email, String passwordHash, String role) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public boolean isTeacher() { return "teacher".equalsIgnoreCase(role); }
    public boolean isStudent() { return "student".equalsIgnoreCase(role); }
    public boolean isAdmin() { return "admin".equalsIgnoreCase(role); }

    @Override
    public String toString() {
        return "User{" + "userId=" + userId + ", fullName='" + fullName + '\'' + ", email='" + email + '\'' + ", role='" + role + '\'' + '}';
    }
}
