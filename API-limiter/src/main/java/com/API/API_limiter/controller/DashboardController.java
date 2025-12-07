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

    // Hardcoded keys for MVP visualization
    private final String FREE_KEY = "free_key_123";
    private final String GOLD_KEY = "gold_key_456";

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        // 1. Fetch current tokens from Redis
        // Keys: "{api_key}:tokens"
        String freeTokensStr = redisTemplate.opsForValue().get(FREE_KEY + ":tokens");
        String goldTokensStr = redisTemplate.opsForValue().get(GOLD_KEY + ":tokens");

        // 2. Parse and Handle nulls
        // If null, it means the bucket hasn't been blocked/created yet, so it's full.
        stats.put("free_plan_limit", 10);
        stats.put("free_plan_remaining", freeTokensStr != null ? Double.parseDouble(freeTokensStr) : 10);

        stats.put("gold_plan_limit", 50);
        stats.put("gold_plan_remaining", goldTokensStr != null ? Double.parseDouble(goldTokensStr) : 50);

        return ResponseEntity.ok(stats);
    }
}
