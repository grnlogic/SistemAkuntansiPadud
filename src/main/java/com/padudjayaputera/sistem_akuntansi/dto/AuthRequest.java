package com.padudjayaputera.sistem_akuntansi.dto;

import lombok.Data;

@Data
public class AuthRequest {
    private String username;
    private String password;
}