package com.padudjayaputera.sistem_akuntansi.dto;

import com.padudjayaputera.sistem_akuntansi.model.UserRole;

import lombok.Data;

@Data
public class RegisterRequest {
    private String username;
    private String password;
    private UserRole role;
    private Integer divisionId;
}