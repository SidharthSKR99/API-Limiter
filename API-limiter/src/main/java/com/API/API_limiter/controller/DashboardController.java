package com.API.API_limiter.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class DashboardController {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private com.API.API_limiter.repository.UserRepository userRepository;

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats(java.security.Principal principal) {
        String username = principal.getName();
        var user = userRepository.findByUsername(username).orElseThrow();

        Map<String, Object> stats = new HashMap<>();
        stats.put("username", user.getUsername());
        stats.put("plan", user.getPlan());
        stats.put("api_key", user.getApiKey());

        String redisKey = user.getApiKey() + ":tokens";
        String tokensStr = redisTemplate.opsForValue().get(redisKey);

        double limit = (user.getPlan() == com.API.API_limiter.model.UserEntity.PlanType.FREE) ? 10.0 : 50.0;
        double remaining = tokensStr != null ? Double.parseDouble(tokensStr) : limit;

        stats.put("limit", limit);
        stats.put("remaining", remaining);

        return ResponseEntity.ok(stats);
    }
}
