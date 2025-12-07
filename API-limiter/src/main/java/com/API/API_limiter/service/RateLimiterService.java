package com.API.API_limiter.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class RateLimiterService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private DefaultRedisScript<List> redisScript;

    public boolean isAllowed(String apiKey, int replenishRate, int burstCapacity) {
        List<String> keys = Arrays.asList(apiKey);

        // Execute the Lua script
        List result = redisTemplate.execute(
                redisScript,
                keys,
                String.valueOf(replenishRate),
                String.valueOf(burstCapacity),
                "1" // Requesting 1 token
        );

        // Result[0] is 1 if allowed, 0 if denied
        if (result != null && !result.isEmpty()) {
            Long allowed = (Long) result.get(0);
            return allowed == 1L;
        }
        return false;
    }
}