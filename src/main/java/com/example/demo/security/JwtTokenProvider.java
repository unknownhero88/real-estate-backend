package com.example.demo.security;

import com.example.demo.config.JwtConfig;
import com.example.demo.entity.User;
import com.example.demo.util.JwtUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class JwtTokenProvider {

    private static final Logger log =
        LoggerFactory.getLogger(JwtTokenProvider.class);

    private final JwtConfig jwtConfig;

    public JwtTokenProvider(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
    }

    // ─────────────────────────────────────────
    // GENERATE ACCESS TOKEN
    // ─────────────────────────────────────────
    public String generateAccessToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role",     user.getRole().name());
        claims.put("userId",   user.getId());
        claims.put("fullName", user.getFullName());
        claims.put("type",     "access");

        return JwtUtils.generateToken(
            user.getEmail(),
            claims,
            jwtConfig.getAccessTokenExpiry(),
            jwtConfig.getSecret()
        );
    }

    // ─────────────────────────────────────────
    // GENERATE REFRESH TOKEN
    // ─────────────────────────────────────────
    public String generateRefreshToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh");

        return JwtUtils.generateToken(
            user.getEmail(),
            claims,
            jwtConfig.getRefreshTokenExpiry(),
            jwtConfig.getSecret()
        );
    }

    // ─────────────────────────────────────────
    // VALIDATE TOKEN
    // ─────────────────────────────────────────
    public boolean validateToken(String token) {
        return JwtUtils.validateToken(token, jwtConfig.getSecret());
    }

    // ─────────────────────────────────────────
    // EXTRACT EMAIL
    // ─────────────────────────────────────────
    public String extractEmail(String token) {
        return JwtUtils.extractSubject(token, jwtConfig.getSecret());
    }

    // ─────────────────────────────────────────
    // EXTRACT ROLE
    // ─────────────────────────────────────────
    public String extractRole(String token) {
        return JwtUtils.extractRole(token, jwtConfig.getSecret());
    }

    // ─────────────────────────────────────────
    // EXTRACT USER ID
    // ─────────────────────────────────────────
    public Long extractUserId(String token) {
        return JwtUtils.extractUserId(token, jwtConfig.getSecret());
    }

    // ─────────────────────────────────────────
    // CHECK IF EXPIRED
    // ─────────────────────────────────────────
    public boolean isTokenExpired(String token) {
        return JwtUtils.isTokenExpired(token, jwtConfig.getSecret());
    }

    // ─────────────────────────────────────────
    // GET FAILURE REASON
    // ─────────────────────────────────────────
    public String getFailureReason(String token) {
        return JwtUtils.getValidationFailureReason(
            token, jwtConfig.getSecret());
    }

    // ─────────────────────────────────────────
    // GET REMAINING EXPIRY MINUTES
    // ─────────────────────────────────────────
    public long getRemainingMinutes(String token) {
        return JwtUtils.getRemainingExpiryMinutes(
            token, jwtConfig.getSecret());
    }
}