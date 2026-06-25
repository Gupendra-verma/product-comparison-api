package com.example.ProductComparison.Security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {

    private final SecretKey key;
    private final long expirySeconds;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiry-seconds:3600}") long expirySeconds
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirySeconds = expirySeconds;
    }

    // ─── Token Generation ────────────────────────────────────────────────────

    public String generateToken(String username, String role) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(username)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(expirySeconds)))
                .claims(Map.of("role", role))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    // ─── Core: parse once, reuse everywhere ──────────────────────────────────

    /**
     * Single parse point. All other methods call this.
     * Throws JwtException subtypes naturally — callers decide how to handle.
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // ─── Public Extractors ───────────────────────────────────────────────────

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public String extractRole(String token) {
        Object role = extractAllClaims(token).get("role");
        return role == null ? null : role.toString();
    }

    // ─── Validation ──────────────────────────────────────────────────────────

    /**
     * Returns true only if the token has a valid signature AND is not expired.
     * Logs the specific reason for failure instead of silently returning false.
     */
    public boolean isTokenValid(String token) {
        try {
            extractAllClaims(token);
            System.out.println("✓ Token is VALID");
            return true;
        } catch (ExpiredJwtException e) {
            System.out.println("✗ JWT EXPIRED - Token has expired");
            System.out.println("  Error: " + e.getMessage());
        } catch (JwtException e) {
            System.out.println("✗ JWT INVALID - Signature or parsing error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("✗ JWT ERROR: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
}