package com.mediapp.security_service.controller;

import com.mediapp.security_service.service.AppUserService;
import com.mediapp.security_service.service.AuthService;
import com.mediapp.security_service.service.dto.AuthResponse;
import com.mediapp.security_service.service.dto.LoginRequest;
import com.mediapp.security_service.service.dto.RegisterRequest;
import com.mediapp.security_service.service.dto.RegisterResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final AppUserService appUserService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.authenticate(request));
    }

    /**
     * Registers a new user for authentication.
     * This endpoint is called by other services (e.g., user-service) to create auth
     * credentials.
     * 
     * @param request the registration request
     * @return the created user's auth ID and details
     */
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = appUserService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
