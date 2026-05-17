package com.stockapi.service;

import com.stockapi.dto.AuthResponse;
import com.stockapi.dto.LoginRequest;
import com.stockapi.dto.RefreshRequest;
import com.stockapi.dto.RegisterRequest;
import com.stockapi.entity.User;
import com.stockapi.repository.UserRepository;
import com.stockapi.security.JwtUtil;
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
    @Mock RefreshTokenService refreshTokenService;

    @InjectMocks AuthService authService;

    @Test
    void register_success() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("odgerel");
        req.setPassword("password123");

        User saved = new User("odgerel", "hashed");
        when(userRepository.existsByUsername("odgerel")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenReturn(saved);
        when(jwtUtil.generate("odgerel")).thenReturn("access-token");
        when(refreshTokenService.generate(saved)).thenReturn("refresh-token");

        AuthResponse res = authService.register(req);

        assertThat(res.getAccessToken()).isEqualTo("access-token");
        assertThat(res.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(res.getUsername()).isEqualTo("odgerel");
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

        User user = new User("odgerel", "hashed");
        when(userRepository.findByUsername("odgerel")).thenReturn(Optional.of(user));
        when(jwtUtil.generate("odgerel")).thenReturn("access-token");
        when(refreshTokenService.generate(user)).thenReturn("refresh-token");

        AuthResponse res = authService.login(req);

        assertThat(res.getAccessToken()).isEqualTo("access-token");
        assertThat(res.getRefreshToken()).isEqualTo("refresh-token");
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

    @Test
    void refresh_validToken_rotatesAndReturnsNewPair() {
        RefreshRequest req = new RefreshRequest();
        req.setRefreshToken("old-refresh-token");

        User user = new User("odgerel", "hashed");
        when(refreshTokenService.validateAndRotate("old-refresh-token")).thenReturn(user);
        when(jwtUtil.generate("odgerel")).thenReturn("new-access-token");
        when(refreshTokenService.generate(user)).thenReturn("new-refresh-token");

        AuthResponse res = authService.refresh(req);

        assertThat(res.getAccessToken()).isEqualTo("new-access-token");
        assertThat(res.getRefreshToken()).isEqualTo("new-refresh-token");
        assertThat(res.getUsername()).isEqualTo("odgerel");
    }

    @Test
    void refresh_invalidToken_throws() {
        RefreshRequest req = new RefreshRequest();
        req.setRefreshToken("bad-token");

        when(refreshTokenService.validateAndRotate("bad-token"))
            .thenThrow(new BadCredentialsException("Invalid refresh token"));

        assertThatThrownBy(() -> authService.refresh(req))
            .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void logout_revokesToken() {
        RefreshRequest req = new RefreshRequest();
        req.setRefreshToken("some-refresh-token");

        authService.logout(req);

        verify(refreshTokenService).revoke("some-refresh-token");
    }
}
