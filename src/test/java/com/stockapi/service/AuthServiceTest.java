package com.stockapi.service;

import com.stockapi.dto.AuthResponse;
import com.stockapi.dto.LoginRequest;
import com.stockapi.dto.RegisterRequest;
import com.stockapi.entity.User;
import com.stockapi.repository.UserRepository;
import com.stockapi.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtUtil jwtUtil;
    @Mock AuthenticationManager authManager;

    @InjectMocks AuthService authService;

    @Test
    void register_success() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("odgerel");
        req.setPassword("password123");

        when(userRepository.existsByUsername("odgerel")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed");
        when(jwtUtil.generate("odgerel")).thenReturn("mock-jwt-token");

        AuthResponse res = authService.register(req);

        assertThat(res.getToken()).isEqualTo("mock-jwt-token");
        assertThat(res.getUsername()).isEqualTo("odgerel");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_duplicateUsername_throws() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("odgerel");
        req.setPassword("password123");

        when(userRepository.existsByUsername("odgerel")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(req))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("already taken");
    }

    @Test
    void login_success() {
        LoginRequest req = new LoginRequest();
        req.setUsername("odgerel");
        req.setPassword("password123");

        when(jwtUtil.generate("odgerel")).thenReturn("mock-jwt-token");
        AuthResponse res = authService.login(req);

        assertThat(res.getToken()).isEqualTo("mock-jwt-token");
        assertThat(res.getUsername()).isEqualTo("odgerel");
        verify(authManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void login_badCredentials_throws() {
        LoginRequest req = new LoginRequest();
        req.setUsername("odgerel");
        req.setPassword("wrongpassword");

        doThrow(new BadCredentialsException("Bad credentials"))
            .when(authManager).authenticate(any());

        assertThatThrownBy(() -> authService.login(req))
            .isInstanceOf(BadCredentialsException.class);
    }
}
