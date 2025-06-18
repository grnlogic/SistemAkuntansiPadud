package com.padudjayaputera.sistem_akuntansi.service;

import com.padudjayaputera.sistem_akuntansi.dto.AuthResponse;
import com.padudjayaputera.sistem_akuntansi.dto.RegisterRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
}