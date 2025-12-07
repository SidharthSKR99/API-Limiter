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

    // Hardcoded simple "Database" of keys for Phase 2
    private static final String FREE_KEY = "free_key_123";
    private static final String GOLD_KEY = "gold_key_456";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        // 1. Get the API Key from the header
        String apiKey = request.getHeader("X-API-KEY");

        if (apiKey == null || apiKey.isEmpty()) {
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            response.getWriter().write("Missing Header: X-API-KEY");
            return false;
        }

        // 2. Determine Plan Limits based on the key
        int limit = 0;
        int refillRate = 1; // Default refill

        if (FREE_KEY.equals(apiKey)) {
            limit = 10; // 10 requests max capacity
            refillRate = 1; // 1 token per second
        } else if (GOLD_KEY.equals(apiKey)) {
            limit = 50; // 50 requests max capacity
            refillRate = 5; // 5 tokens per second
        } else {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write("Invalid API Key");
            return false;
        }

        // 3. Call the Redis Engine (Phase 1 code)
        boolean allowed = rateLimiterService.isAllowed(apiKey, refillRate, limit);

        if (!allowed) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value()); // 429 Error
            response.getWriter().write("Rate limit exceeded. Upgrade to Gold Plan!");
            return false;
        }

        // 4. Success! Let the request pass to the Controller
        return true;
    }
}
