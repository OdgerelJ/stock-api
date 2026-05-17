package com.stockapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockapi.dto.AuthResponse;
import com.stockapi.dto.LoginRequest;
import com.stockapi.dto.RefreshRequest;
import com.stockapi.dto.RegisterRequest;
import com.stockapi.security.JwtUtil;
import com.stockapi.security.UserDetailsServiceImpl;
import com.stockapi.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(TestSecurityConfig.class)
class AuthControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean AuthService authService;
    @MockBean JwtUtil jwtUtil;
    @MockBean UserDetailsServiceImpl userDetailsService;

    private static final AuthResponse MOCK_RESPONSE =
        new AuthResponse("access-token", "refresh-token", "odgerel");

    @Test
    void register_validRequest_returns200() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("odgerel");
        req.setPassword("password123");

        when(authService.register(any())).thenReturn(MOCK_RESPONSE);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").value("access-token"))
            .andExpect(jsonPath("$.refreshToken").value("refresh-token"))
            .andExpect(jsonPath("$.username").value("odgerel"));
    }

    @Test
    void register_shortUsername_returns400() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("ab"); // too short — min 3
        req.setPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void register_shortPassword_returns400() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("odgerel");
        req.setPassword("123"); // too short — min 6

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void login_validCredentials_returns200() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setUsername("odgerel");
        req.setPassword("password123");

        when(authService.login(any())).thenReturn(MOCK_RESPONSE);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").value("access-token"))
            .andExpect(jsonPath("$.refreshToken").value("refresh-token"));
    }

    @Test
    void login_badCredentials_returns401() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setUsername("odgerel");
        req.setPassword("wrong");

        when(authService.login(any())).thenThrow(new BadCredentialsException("Bad credentials"));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void refresh_validToken_returns200() throws Exception {
        RefreshRequest req = new RefreshRequest();
        req.setRefreshToken("valid-refresh-token");

        when(authService.refresh(any())).thenReturn(MOCK_RESPONSE);

        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").value("access-token"))
            .andExpect(jsonPath("$.refreshToken").value("refresh-token"));
    }

    @Test
    void refresh_invalidToken_returns401() throws Exception {
        RefreshRequest req = new RefreshRequest();
        req.setRefreshToken("bad-token");

        when(authService.refresh(any())).thenThrow(new BadCredentialsException("Invalid refresh token"));

        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void logout_validToken_returns204() throws Exception {
        RefreshRequest req = new RefreshRequest();
        req.setRefreshToken("some-refresh-token");

        mockMvc.perform(post("/api/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isNoContent());

        verify(authService).logout(any());
    }
}
