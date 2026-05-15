# Action Items & Suggestions

## High Priority Fixes

1. **Fix `ApiLimiterApplicationTests.java`**
   - **Issue:** The default test file will crash if someone runs `mvn test` because it doesn't have Testcontainers configured, but the application context requires PostgreSQL and Redis to load.
   - **Solution:** Either delete this file if it's not needed, or add the `@Testcontainers` setup to it (similar to what is in `ApiLimiterIntegrationTest.java`).

2. **Update CORS Configuration**
   - **Issue:** The `CorsConfig` does not explicitly allow the custom `X-API-KEY` header. While it might work in simple setups, browsers making preflight requests (OPTIONS) from a different origin (like Swagger UI or a detached frontend) may drop the header.
   - **Solution:** Add `.allowedHeaders("X-API-KEY", "Authorization", "Content-Type")` to your `CorsConfig.java`.

## Recommended Enhancements

- **Interactive Dashboard:** Add a "Send Test Request" button on the React dashboard so you can trigger the rate limiter and watch the graph update without leaving the app.
- **Controller Unit Tests:** Add isolated MockMvc tests for `AuthController`, `WeatherController`, and `DashboardController` to round out the test coverage.
- **Logging:** Add SLF4J logging to the `RateLimitInterceptor` to log when requests are blocked. This shows production awareness.
