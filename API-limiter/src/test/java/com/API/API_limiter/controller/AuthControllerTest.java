package com.API.API_limiter.controller;

import com.API.API_limiter.model.UserEntity;
import com.API.API_limiter.repository.UserRepository;
import com.API.API_limiter.service.ApiKeyCacheService;
import com.API.API_limiter.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private ApiKeyCacheService apiKeyCacheService;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private JwtUtil jwtUtil;

    @Test
    public void testRegisterDuplicateUsername() throws Exception {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(new UserEntity()));

        String requestBody = "{\"username\":\"testuser\", \"password\":\"pass\", \"plan\":\"FREE\"}";

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testRegisterSuccess() throws Exception {
        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());

        String requestBody = "{\"username\":\"newuser\", \"password\":\"pass\", \"plan\":\"FREE\"}";

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk());
    }
}
