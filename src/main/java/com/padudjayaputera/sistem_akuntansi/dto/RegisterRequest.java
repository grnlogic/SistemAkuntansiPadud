package com.padudjayaputera.sistem_akuntansi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.padudjayaputera.sistem_akuntansi.model.UserRole;

import lombok.Data;

@Data
public class RegisterRequest {
    @JsonProperty("username")
    private String username;
    
    @JsonProperty("password") 
    private String password;
    
    @JsonProperty("role")
    private UserRole role;
    
    @JsonProperty("divisionId")
    private Integer divisionId;
    
    // Constructor untuk debugging
    public RegisterRequest() {
        System.out.println("RegisterRequest created");
    }
    
    // Override setter untuk debugging
    public void setPassword(String password) {
        System.out.println("Setting password: " + (password != null ? "[HIDDEN]" : "NULL"));
        this.password = password;
    }
}