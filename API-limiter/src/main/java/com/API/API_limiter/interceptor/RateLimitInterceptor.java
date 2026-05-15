package com.API.API_limiter.interceptor;

import com.API.API_limiter.model.UserEntity;
import com.API.API_limiter.service.ApiKeyCacheService;
import com.API.API_limiter.service.RateLimiterService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimiterService rateLimiterService;
    private final ApiKeyCacheService apiKeyCacheService;

    public RateLimitInterceptor(RateLimiterService rateLimiterService, ApiKeyCacheService apiKeyCacheService) {
        this.rateLimiterService = rateLimiterService;
        this.apiKeyCacheService = apiKeyCacheService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String apiKey = request.getHeader("X-API-KEY");

        if (apiKey == null || apiKey.isEmpty()) {
            writeError(response, HttpStatus.BAD_REQUEST, "{\"error\":\"Missing Header: X-API-KEY\",\"status\":400}");
            return false;
        }

        UserEntity.PlanType plan = apiKeyCacheService.getPlanForApiKey(apiKey);
        if (plan == null) {
            writeError(response, HttpStatus.UNAUTHORIZED, "{\"error\":\"Invalid API Key\",\"status\":401}");
            return false;
        }

        int limit = plan.getLimit();
        int refillRate = plan.getRefillRate();
        RateLimiterService.RateLimitResult result = rateLimiterService.isAllowed(apiKey, refillRate, limit);

        response.setHeader("X-RateLimit-Limit", String.valueOf(limit));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(result.tokensRemaining()));
        response.setHeader("X-RateLimit-Policy", plan.name());

        if (!result.allowed()) {
            int retryAfterSeconds = Math.max(1, limit / refillRate);
            response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));
            writeError(
                    response,
                    HttpStatus.TOO_MANY_REQUESTS,
                    String.format(
                            "{\"error\":\"Rate limit exceeded\",\"plan\":\"%s\",\"retryAfterSeconds\":%d,\"status\":429}",
                            plan.name(),
                            retryAfterSeconds));
            return false;
        }

        return true;
    }

    private void writeError(HttpServletResponse response, HttpStatus status, String payload) throws Exception {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(payload);
    }
}
