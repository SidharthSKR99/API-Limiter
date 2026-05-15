package com.API.API_limiter.controller;

import com.API.API_limiter.model.UserEntity;
import com.API.API_limiter.repository.UserRepository;
import com.API.API_limiter.service.ApiKeyCacheService;
import com.API.API_limiter.util.JwtUtil;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final ApiKeyCacheService apiKeyCacheService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthController(
            AuthenticationManager authenticationManager,
            UserRepository userRepository,
            ApiKeyCacheService apiKeyCacheService,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.apiKeyCacheService = apiKeyCacheService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username already exists", "status", 400));
        }

        UserEntity.PlanType plan = UserEntity.PlanType.from(request.getPlan());
        UserEntity user = UserEntity.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .plan(plan)
                .apiKey(UUID.randomUUID().toString())
                .build();

        userRepository.save(user);
        apiKeyCacheService.cacheApiKey(user.getApiKey(), user.getPlan());

        return ResponseEntity.ok(Map.of(
                "message", "User registered successfully",
                "apiKey", user.getApiKey(),
                "plan", user.getPlan()));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        } catch (Exception exception) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials", "status", 401));
        }

        UserEntity user = userRepository.findByUsername(request.getUsername()).orElseThrow();
        String token = jwtUtil.generateToken(user.getUsername());

        return ResponseEntity.ok(Map.of(
                "token", token,
                "apiKey", user.getApiKey(),
                "plan", user.getPlan(),
                "limit", user.getPlan().getLimit()));
    }

    @Data
    public static class RegisterRequest {
        @NotBlank(message = "Username is required")
        @Size(max = 50, message = "Username must be at most 50 characters")
        private String username;

        @NotBlank(message = "Password is required")
        @Size(max = 255, message = "Password must be at most 255 characters")
        private String password;

        @NotBlank(message = "Plan is required")
        @Pattern(regexp = "(?i)FREE|GOLD", message = "Plan must be FREE or GOLD")
        private String plan;
    }

    @Data
    public static class LoginRequest {
        @NotBlank(message = "Username is required")
        private String username;

        @NotBlank(message = "Password is required")
        private String password;
    }
}
