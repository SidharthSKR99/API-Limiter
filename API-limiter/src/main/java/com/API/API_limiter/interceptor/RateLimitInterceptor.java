package com.API.API_limiter.interceptor;

import com.API.API_limiter.service.RateLimiterService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    @Autowired
    private RateLimiterService rateLimiterService;

    @Autowired
    private com.API.API_limiter.service.ApiKeyCacheService apiKeyCacheService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        // 1. Get the API Key from the header
        String apiKey = request.getHeader("X-API-KEY");

        if (apiKey == null || apiKey.isEmpty()) {
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Missing Header: X-API-KEY\", \"status\": 400}");
            return false;
        }

        // 2. Fetch User Plan from Cache/DB
        var plan = apiKeyCacheService.getPlanForApiKey(apiKey);
        if (plan == null) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Invalid API Key\", \"status\": 401}");
            return false;
        }

        int limit = 0;
        int refillRate = 1;

        if (plan == com.API.API_limiter.model.UserEntity.PlanType.FREE) {
            limit = 10;
            refillRate = 1;
        } else if (plan == com.API.API_limiter.model.UserEntity.PlanType.GOLD) {
            limit = 50;
            refillRate = 5;
        }

        // 3. Call the Redis Engine
        var result = rateLimiterService.isAllowed(apiKey, refillRate, limit);

        // Always set rate limit headers
        response.setHeader("X-RateLimit-Limit", String.valueOf(limit));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(result.tokensRemaining()));
        response.setHeader("X-RateLimit-Policy", plan.name());

        if (!result.allowed()) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value()); // 429 Error
            response.setContentType("application/json");
            response.getWriter().write(String.format(
                "{\"error\": \"Rate limit exceeded\", \"plan\": \"%s\", \"retry_after_seconds\": %d, \"status\": 429}",
                plan.name(), (limit / refillRate)
            ));
            return false;
        }

        // 4. Success! Let the request pass to the Controller
        return true;
    }
}
