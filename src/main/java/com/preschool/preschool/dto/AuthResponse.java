package com.preschool.preschool.dto;

public class AuthResponse {
    private String status;
    private String message;
    private String token;
    private String username;
    private String role;

    public AuthResponse(String status, String message, String token, String username, String role) {
        this.status = status;
        this.message = message;
        this.token = token;
        this.username = username;
        this.role = role;
    }

    public AuthResponse(String status, String message) {
        this.status = status;
        this.message = message;
    }

    // Getters and Setters

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
