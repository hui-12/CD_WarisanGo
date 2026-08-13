package com.warisango.dto;

import jakarta.validation.constraints.NotBlank;

public class LoginRequest {

    @NotBlank(message = "ID Token is required")
    private String idToken;

    @NotBlank(message = "Role is required")
    private String role;

    public String getIdToken() { return idToken; }
    public void setIdToken(String idToken) { this.idToken = idToken; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}