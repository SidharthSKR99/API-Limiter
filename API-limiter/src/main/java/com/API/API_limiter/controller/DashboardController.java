package com.API.API_limiter.controller;

import com.API.API_limiter.model.UserEntity;
import com.API.API_limiter.repository.UserRepository;
import com.API.API_limiter.service.RateLimiterService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class DashboardController {

    private final RateLimiterService rateLimiterService;
    private final UserRepository userRepository;

    public DashboardController(RateLimiterService rateLimiterService, UserRepository userRepository) {
        this.rateLimiterService = rateLimiterService;
        this.userRepository = userRepository;
    }

    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsResponse> getDashboardStats(java.security.Principal principal) {
        String username = principal.getName();
        UserEntity user = userRepository.findByUsername(username).orElseThrow();
        UserEntity.PlanType plan = user.getPlan();
        RateLimiterService.RateLimitSnapshot snapshot = rateLimiterService.getSnapshot(user.getApiKey(), plan);

        return ResponseEntity.ok(new DashboardStatsResponse(
                user.getUsername(),
                plan.name(),
                user.getApiKey(),
                snapshot.limit(),
                snapshot.remaining(),
                snapshot.refillRate(),
                "ACTIVE"));
    }

    public record DashboardStatsResponse(
            String username,
            String plan,
            String apiKey,
            long limit,
            long remaining,
            int refillRate,
            String status) {
    }
}
