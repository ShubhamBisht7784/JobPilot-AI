package com.jobpilot.auth.controller;

import com.jobpilot.auth.dto.RegisterRequest;
import com.jobpilot.auth.dto.UserResponse;
import com.jobpilot.auth.dto.LoginRequest;
import com.jobpilot.auth.dto.LoginResponse;
import com.jobpilot.auth.service.AuthService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(
            @Valid @RequestBody RegisterRequest request) {

        String response = authService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {

        LoginResponse response = authService.login(request);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
public ResponseEntity<UserResponse> getCurrentUser(
        Authentication authentication) {

    UserResponse response =
            authService.getCurrentUser(authentication.getName());

    return ResponseEntity.ok(response);
}

}