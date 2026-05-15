package com.API.API_limiter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
public class ApiLimiterIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);
        registry.add("app.jwt.secret", () -> "a-very-long-test-secret-key-that-is-at-least-32-bytes");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        // Clean up or setup if necessary (Flyway runs automatically)
    }

    @Test
    void registerAndLoginFlow() throws Exception {
        String username = "integrationUser";
        String password = "password123";

        // 1. Invalid body registration (400)
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"\", \"password\":\"\", \"plan\":\"INVALID\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());

        // 2. Successful registration
        MvcResult regResult = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"" + username + "\", \"password\":\"" + password + "\", \"plan\":\"FREE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.apiKey").exists())
                .andReturn();

        String apiKey = objectMapper.readTree(regResult.getResponse().getContentAsString()).get("apiKey").asText();

        // 3. Duplicate registration (400)
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"" + username + "\", \"password\":\"" + password + "\", \"plan\":\"FREE\"}"))
                .andExpect(status().isBadRequest());

        // 4. Invalid Login
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"" + username + "\", \"password\":\"wrongpass\"}"))
                .andExpect(status().isUnauthorized());

        // 5. Successful Login
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"" + username + "\", \"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn();

        String token = objectMapper.readTree(loginResult.getResponse().getContentAsString()).get("token").asText();

        // 6. Dashboard JWT Protection
        mockMvc.perform(get("/api/admin/stats"))
                .andExpect(status().isUnauthorized()); // Missing JWT returns 401

        mockMvc.perform(get("/api/admin/stats")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.limit").value(10));

        // 7. Missing API Key
        mockMvc.perform(get("/api/weather/current"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Missing Header: X-API-KEY"));

        // 8. Invalid API Key
        mockMvc.perform(get("/api/weather/current")
                .header("X-API-KEY", "invalid-key"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid API Key"));

        // 9. Within-limit success + Rate limit headers
        mockMvc.perform(get("/api/weather/current")
                .header("X-API-KEY", apiKey))
                .andExpect(status().isOk())
                .andExpect(header().string("X-RateLimit-Limit", "10"))
                .andExpect(header().exists("X-RateLimit-Remaining"))
                .andExpect(header().string("X-RateLimit-Policy", "FREE"));

        // 10. Exhaustion (429)
        for (int i = 0; i < 15; i++) {
            mockMvc.perform(get("/api/weather/current")
                    .header("X-API-KEY", apiKey));
        }

        mockMvc.perform(get("/api/weather/current")
                .header("X-API-KEY", apiKey))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.error").value("Rate limit exceeded"))
                .andExpect(header().exists("X-RateLimit-Remaining"));

        // 11. Token refill recovery (sleep for 2 seconds to refill at 1 req/sec)
        Thread.sleep(2000);
        mockMvc.perform(get("/api/weather/current")
                .header("X-API-KEY", apiKey))
                .andExpect(status().isOk());
    }
}
