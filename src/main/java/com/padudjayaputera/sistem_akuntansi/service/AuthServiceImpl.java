package com.padudjayaputera.sistem_akuntansi.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.padudjayaputera.sistem_akuntansi.dto.AuthResponse;
import com.padudjayaputera.sistem_akuntansi.dto.RegisterRequest;
import com.padudjayaputera.sistem_akuntansi.exception.DivisionAccessException;
import com.padudjayaputera.sistem_akuntansi.model.Division;
import com.padudjayaputera.sistem_akuntansi.model.User;
import com.padudjayaputera.sistem_akuntansi.model.UserRole;
import com.padudjayaputera.sistem_akuntansi.repository.DivisionRepository;
import com.padudjayaputera.sistem_akuntansi.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final DivisionRepository divisionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    public AuthResponse register(RegisterRequest request) {
        // Validasi akses divisi untuk Admin Divisi
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            User currentUser = (User) authentication.getPrincipal();
            
            if (currentUser.getRole() == UserRole.ADMIN_DIVISI) {
                if (!request.getDivisionId().equals(currentUser.getDivision().getId())) {
                    throw new DivisionAccessException("Admin Divisi hanya boleh membuat akun untuk divisinya sendiri");
                }
            }
        }
        
        Division division = divisionRepository.findById(request.getDivisionId())
                .orElseThrow(() -> new RuntimeException("Division not found"));

        var user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword())); // Enkripsi password!
        user.setRole(request.getRole());
        user.setDivision(division);

        userRepository.save(user);
        var jwtToken = jwtService.generateToken(user);
        return AuthResponse.builder().token(jwtToken).build();
    }
}