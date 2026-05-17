package com.stockapi.service;

import com.stockapi.entity.RefreshToken;
import com.stockapi.entity.User;
import com.stockapi.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    @Transactional
    public String generate(User user) {
        String rawToken = generateSecureToken();
        refreshTokenRepository.save(
            new RefreshToken(sha256(rawToken), user, Instant.now().plusMillis(refreshExpirationMs))
        );
        return rawToken;
    }

    /**
     * Validates the token, revokes it, and returns the owning User.
     * Caller must immediately issue a new token pair (rotation).
     */
    @Transactional
    public User validateAndRotate(String rawToken) {
        RefreshToken token = refreshTokenRepository.findByTokenHash(sha256(rawToken))
            .orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));

        if (token.isRevoked() || token.getExpiresAt().isBefore(Instant.now())) {
            throw new BadCredentialsException("Refresh token expired or revoked");
        }

        token.setRevoked(true);
        return token.getUser();
    }

    @Transactional
    public void revoke(String rawToken) {
        refreshTokenRepository.findByTokenHash(sha256(rawToken))
            .ifPresent(t -> t.setRevoked(true));
    }

    @Transactional
    public void purgeExpiredAndRevoked() {
        refreshTokenRepository.deleteExpiredAndRevoked(Instant.now());
    }

    private static String generateSecureToken() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
