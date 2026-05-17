package com.stockapi.service;

import com.stockapi.dto.AuthResponse;
import com.stockapi.dto.LoginRequest;
import com.stockapi.dto.RefreshRequest;
import com.stockapi.dto.RegisterRequest;
import com.stockapi.entity.User;
import com.stockapi.repository.UserRepository;
import com.stockapi.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authManager;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByUsername(req.getUsername())) {
            throw new IllegalArgumentException("Username already taken");
        }
        String hashed = passwordEncoder.encode(req.getPassword());
        User user = userRepository.save(new User(req.getUsername(), hashed));
        return buildResponse(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest req) {
        authManager.authenticate(
            new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword())
        );
        User user = userRepository.findByUsername(req.getUsername())
            .orElseThrow(() -> new IllegalStateException("User not found"));
        return buildResponse(user);
    }

    @Transactional
    public AuthResponse refresh(RefreshRequest req) {
        User user = refreshTokenService.validateAndRotate(req.getRefreshToken());
        return buildResponse(user);
    }

    @Transactional
    public void logout(RefreshRequest req) {
        refreshTokenService.revoke(req.getRefreshToken());
    }

    private AuthResponse buildResponse(User user) {
        String accessToken = jwtUtil.generate(user.getUsername());
        String refreshToken = refreshTokenService.generate(user);
        return new AuthResponse(accessToken, refreshToken, user.getUsername());
    }
}
