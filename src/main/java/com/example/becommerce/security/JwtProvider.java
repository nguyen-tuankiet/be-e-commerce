package com.example.becommerce.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;

/**
 * Central JWT utility — handles generation and validation of
 * both access tokens and refresh tokens.
 *
 * <p>Access tokens are short-lived (15 min) and carry user claims.
 * Refresh tokens are long-lived (7 days) and are validated against DB records.
 */
@Component
@Slf4j
public class JwtProvider {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;   // ms

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;  // ms

    // ---- Token generation ------------------------------------------

    /**
     * Generate a signed access token.
     *
     * @param email  subject (stored in JWT "sub" claim)
     * @param claims additional claims (e.g., role, userId)
     */
    public String generateAccessToken(String email, Map<String, Object> claims) {
        return buildToken(email, claims, accessTokenExpiration);
    }

    /**
     * Generate a signed refresh token.
     * Minimal claims — just the subject.
     */
    public String generateRefreshToken(String email) {
        return buildToken(email, Map.of(), refreshTokenExpiration);
    }

    private String buildToken(String subject, Map<String, Object> claims, long expiration) {
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    // ---- Token validation ------------------------------------------

    /**
     * Returns true if the token has a valid signature and is not expired.
     */
    public boolean isTokenValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Invalid JWT: {}", e.getMessage());
            return false;
        }
    }

    // ---- Claim extraction ------------------------------------------

    public String extractEmail(String token) {
        return parseClaims(token).getSubject();
    }

    public Date extractExpiration(String token) {
        return parseClaims(token).getExpiration();
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // ---- Internal helpers ------------------------------------------

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public long getRefreshTokenExpiration() {
        return refreshTokenExpiration;
    }
}
