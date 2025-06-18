package com.padudjayaputera.sistem_akuntansi.dto;

import com.padudjayaputera.sistem_akuntansi.model.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String token;
    private User user; // Add user data to response
}