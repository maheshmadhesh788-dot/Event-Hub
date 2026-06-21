package com.example.demo.dto;

public class AuthRequest {
    private String username; // rollNumber for students, name for department, "admin" for admin
    private String password;
    private String role; // "STUDENT", "DEPARTMENT", "ADMIN"

    public AuthRequest() {}

    public AuthRequest(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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
