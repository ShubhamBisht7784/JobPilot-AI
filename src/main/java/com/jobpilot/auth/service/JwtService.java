package com.jobpilot.auth.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtService {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expiration}")
    private long jwtExpiration;

    // Generate JWT Token
    public String generateToken(String email) {

        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date())
                .expiration(
                        new Date(System.currentTimeMillis() + jwtExpiration)
                )
                .signWith(getSignInKey())
                .compact();
    }

    // Extract email from token
    public String extractUsername(String token) {

        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    // Validate token
    public boolean isTokenValid(String token, UserDetails userDetails) {

        try {
            String username = extractUsername(token);

            return username.equals(userDetails.getUsername());

        } catch (io.jsonwebtoken.JwtException |
                 IllegalArgumentException exception) {

            return false;
        }
    }

    // Generate signing key
    private SecretKey getSignInKey() {

        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException(
                    "JWT is not configured. Set app.jwt.secret to a Base64-encoded secret of at least 32 bytes."
            );
        }

        byte[] keyBytes = Decoders.BASE64.decode(secret);

        return Keys.hmacShaKeyFor(keyBytes);
    }
}