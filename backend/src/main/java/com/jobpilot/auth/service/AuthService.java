package com.jobpilot.auth.service;

import com.jobpilot.auth.dto.RegisterRequest;
import com.jobpilot.auth.dto.UserResponse;
import com.jobpilot.auth.dto.LoginRequest;
import com.jobpilot.auth.dto.LoginResponse;
import com.jobpilot.auth.entity.Role;
import com.jobpilot.auth.entity.User;
import com.jobpilot.auth.repository.UserRepository;
import com.jobpilot.common.exception.EmailAlreadyExistsException;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public String register(RegisterRequest request) {

        String normalizedEmail = request.getEmail().trim().toLowerCase(Locale.ROOT);

       if (userRepository.existsByEmail(normalizedEmail)) {
    throw new EmailAlreadyExistsException(
            "Email already registered"
    );
}
        User user = new User();
        user.setName(request.getName());
        user.setEmail(normalizedEmail);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.USER);

        userRepository.save(user);

        return "User registered successfully";
    }

    public LoginResponse login(LoginRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail().trim().toLowerCase(Locale.ROOT),
                        request.getPassword()
                )
        );

        String token = jwtService.generateToken(
                request.getEmail().trim().toLowerCase(Locale.ROOT)
        );

        return new LoginResponse(token, "Bearer");
    }

    public UserResponse getCurrentUser(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(()
                        -> new RuntimeException("User not found"));

        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole()
        );
    }
}
