package com.stockapi.service;

import com.stockapi.dto.AuthResponse;
import com.stockapi.dto.LoginRequest;
import com.stockapi.dto.RegisterRequest;
import com.stockapi.entity.User;
import com.stockapi.repository.UserRepository;
import com.stockapi.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authManager;

    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByUsername(req.getUsername())) {
            throw new IllegalArgumentException("Username already taken");
        }
        String hashed = passwordEncoder.encode(req.getPassword());
        userRepository.save(new User(req.getUsername(), hashed));
        String token = jwtUtil.generate(req.getUsername());
        return new AuthResponse(token, req.getUsername());
    }

    public AuthResponse login(LoginRequest req) {
        authManager.authenticate(
            new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword())
        );
        String token = jwtUtil.generate(req.getUsername());
        return new AuthResponse(token, req.getUsername());
    }
}
