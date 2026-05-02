package com.API.API_limiter.service;

import com.API.API_limiter.model.UserEntity;
import com.API.API_limiter.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
public class ApiKeyCacheService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private UserRepository userRepository;

    private static final String CACHE_PREFIX = "apikey:plan:";
    private static final long CACHE_TTL_MINUTES = 30;

    /**
     * Get user's plan from Redis cache, falling back to DB on cache miss.
     * Returns null if the API key doesn't exist.
     */
    public UserEntity.PlanType getPlanForApiKey(String apiKey) {
        String cached = redisTemplate.opsForValue().get(CACHE_PREFIX + apiKey);
        if (cached != null) {
            return UserEntity.PlanType.valueOf(cached);
        }
        
        // Cache miss — query DB and cache
        Optional<UserEntity> userOpt = userRepository.findByApiKey(apiKey);
        if (userOpt.isEmpty()) {
            return null;
        }

        UserEntity.PlanType plan = userOpt.get().getPlan();
        redisTemplate.opsForValue().set(
                CACHE_PREFIX + apiKey, 
                plan.name(),
                CACHE_TTL_MINUTES, 
                TimeUnit.MINUTES
        );
        return plan;
    }

    /** Called on registration to warm the cache */
    public void cacheApiKey(String apiKey, UserEntity.PlanType plan) {
        redisTemplate.opsForValue().set(
                CACHE_PREFIX + apiKey, 
                plan.name(),
                CACHE_TTL_MINUTES, 
                TimeUnit.MINUTES
        );
    }
}
