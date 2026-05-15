package com.API.API_limiter.service;

import com.API.API_limiter.model.UserEntity;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class RateLimiterService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final DefaultRedisScript<List> redisScript;

    public RateLimiterService(RedisTemplate<String, Object> redisTemplate, DefaultRedisScript<List> redisScript) {
        this.redisTemplate = redisTemplate;
        this.redisScript = redisScript;
    }

    public record RateLimitResult(boolean allowed, long tokensRemaining) {
    }

    public record RateLimitSnapshot(long limit, long remaining, int refillRate) {
    }

    public RateLimitResult isAllowed(String apiKey, int replenishRate, int burstCapacity) {
        List<String> keys = List.of(apiKey);
        List result = redisTemplate.execute(
                redisScript,
                keys,
                String.valueOf(replenishRate),
                String.valueOf(burstCapacity),
                "1");

        if (result != null && result.size() >= 2) {
            Long allowed = (Long) result.get(0);
            Long remaining = (Long) result.get(1);
            return new RateLimitResult(allowed == 1L, remaining);
        }
        return new RateLimitResult(false, 0);
    }

    public RateLimitSnapshot getSnapshot(String apiKey, UserEntity.PlanType plan) {
        long limit = plan.getLimit();
        int refillRate = plan.getRefillRate();
        Object tokensValue = redisTemplate.opsForValue().get(apiKey + ":tokens");
        Object timestampValue = redisTemplate.opsForValue().get(apiKey + ":ts");

        if (tokensValue == null || timestampValue == null) {
            return new RateLimitSnapshot(limit, limit, refillRate);
        }

        long storedTokens = Long.parseLong(String.valueOf(tokensValue));
        long lastRefreshed = Long.parseLong(String.valueOf(timestampValue));
        long elapsedSeconds = Math.max(0, Instant.now().getEpochSecond() - lastRefreshed);
        long replenishedTokens = Math.min(limit, storedTokens + (elapsedSeconds * refillRate));
        return new RateLimitSnapshot(limit, replenishedTokens, refillRate);
    }
}
