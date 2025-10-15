package com.darum.ng.auth_service.util;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);
    private final Environment environment;

    @Value("${jwt.secret:}")
    private String configuredSecret;

    @Value("${jwt.expiration:86400000}") // Default 24 hours in milliseconds
    private Long expiration;

    // Default secret for development only - NEVER use in production
    private static final String DEFAULT_SECRET = "dev-default-insecure-secret-change-in-production-2024";

    public JwtUtil(Environment environment) {
        this.environment = environment;
    }

    private SecretKey getSigningKey() {
        String secretToUse = getValidSecret();
        logger.info("JWT Configuration: Using secret key with {} characters", secretToUse.length());

        // Log environment source for debugging
        String[] activeProfiles = environment.getActiveProfiles();
        if (activeProfiles.length > 0) {
            logger.info("Active Spring profiles: {}", String.join(", ", activeProfiles));
        }

        return Keys.hmacShaKeyFor(secretToUse.getBytes());
    }
    private String getValidSecret() {
        // Priority 1: JWT_SECRET environment variable (most secure)
        String envSecret = System.getenv("JWT_SECRET");
        if (envSecret != null && !envSecret.trim().isEmpty()) {
            if (envSecret.length() < 32) {
                logger.warn("⚠️  JWT Secret Warning: Environment variable JWT_SECRET is less than 32 characters");
            }
            logger.info("JWT Source: Environment variable JWT_SECRET");
            return envSecret;
        }

        // Priority 2: Spring property (from config server)
        if (configuredSecret != null && !configuredSecret.trim().isEmpty() &&
                !configuredSecret.contains("default-insecure")) {
            logger.info("JWT Source: Configuration property jwt.secret");
            return configuredSecret;
        }

        // Priority 3: Default with security warning
        logger.error(" SECURITY ALERT: Using default JWT secret! This is INSECURE for production.");
        logger.error(" Set JWT_SECRET environment variable with:");
        logger.error(" export JWT_SECRET=$(openssl rand -base64 64)");

        return DEFAULT_SECRET;
    }

    // Extract username from token
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Extract expiration date from token
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // Extract specific claim from token
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    // Extract all claims from token
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    // Check if token is expired
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // Generate token for user
    public String generateToken(String username, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        claims.put("iss", "employee-management-system");
        claims.put("aud", "employee-management-client");
        return createToken(claims, username);
    }
    // Create token with claims
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Validate token with username
    public Boolean validateToken(String token, String username) {
        try {
            final String extractedUsername = extractUsername(token);
            return (extractedUsername.equals(username) && !isTokenExpired(token));
        } catch (Exception e) {
            logger.error("Token validation failed for user {}: {}", username, e.getMessage());
            return false;
        }
    }

    // Validate token without username (just signature and expiration)
    public Boolean validateToken(String token) {
        try {
            extractAllClaims(token); // This will verify signature
            return !isTokenExpired(token);
        } catch (Exception e) {
            logger.error("Token validation failed: {}", e.getMessage());
            return false;
        }
    }
    // Get remaining time until token expiration
    public Long getRemainingTime(String token) {
        try {
            Date expiration = extractExpiration(token);
            return expiration.getTime() - System.currentTimeMillis();
        } catch (Exception e) {
            logger.error("Failed to get remaining time: {}", e.getMessage());
            return -1L;
        }
    }

    // Check if token will expire soon (within specified milliseconds)
    public Boolean isTokenExpiringSoon(String token, Long thresholdMs) {
        Long remainingTime = getRemainingTime(token);
        return remainingTime > 0 && remainingTime <= thresholdMs;
    }
}
