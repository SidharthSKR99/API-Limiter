package com.API.API_limiter.controller;

import com.API.API_limiter.model.UserEntity;
import com.API.API_limiter.repository.UserRepository;
import com.API.API_limiter.service.ApiKeyCacheService;
import com.API.API_limiter.util.JwtUtil;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ApiKeyCacheService apiKeyCacheService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username already exists", "status", 400));
        }

        UserEntity user = UserEntity.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .plan(UserEntity.PlanType.valueOf(request.getPlan().toUpperCase()))
                .apiKey(UUID.randomUUID().toString())
                .build();

        userRepository.save(user);
        
        apiKeyCacheService.cacheApiKey(user.getApiKey(), user.getPlan());

        return ResponseEntity.ok(Map.of("message", "User registered successfully", "apiKey", user.getApiKey()));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials", "status", 401));
        }

        UserEntity user = userRepository.findByUsername(request.getUsername()).get();
        String token = jwtUtil.generateToken(user.getUsername());

        return ResponseEntity.ok(Map.of(
                "token", token,
                "apiKey", user.getApiKey(),
                "plan", user.getPlan(),
                "limit", user.getPlan() == UserEntity.PlanType.FREE ? 10 : 50));
    }

    @Data
    public static class RegisterRequest {
        private String username;
        private String password;
        private String plan; // FREE or GOLD
    }

    @Data
    public static class LoginRequest {
        private String username;
        private String password;
    }
}
