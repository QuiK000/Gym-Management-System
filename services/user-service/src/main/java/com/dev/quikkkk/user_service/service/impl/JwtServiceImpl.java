package com.dev.quikkkk.user_service.service.impl;

import com.dev.quikkkk.user_service.service.IJwtService;
import com.dev.quikkkk.user_service.utils.KeyUtils;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.NonNull;
import org.springframework.stereotype.Service;

import java.security.PublicKey;
import java.util.Date;
import java.util.List;

import static java.util.concurrent.TimeUnit.MINUTES;

@Service
public class JwtServiceImpl implements IJwtService {
    private static final String USER_ID = "userId";
    private static final String PATH_TO_PUBLIC_KEY = "keys/local-only/public_key.pem";
    private static final PublicKey PUBLIC_KEY;

    private final Cache<@NonNull String, Claims> claimsCache = Caffeine.newBuilder()
            .maximumSize(10000)
            .expireAfterWrite(5, MINUTES)
            .build();

    static {
        try {
            PUBLIC_KEY = KeyUtils.loadPublicKey(PATH_TO_PUBLIC_KEY);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load JWT Keys", e);
        }
    }

    @Override
    public String extractEmail(String token) {
        return getCachedClaims(token).getSubject();
    }

    @Override
    public String extractUserId(String token) {
        return getCachedClaims(token).get(USER_ID).toString();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        return (List<String>) getCachedClaims(token).get("roles");
    }

    @Override
    public boolean isTokenValid(String token, String expectedEmail) {
        String email = extractEmail(token);
        return email.equals(expectedEmail) && !isTokenExpired(token);
    }

    private Claims getCachedClaims(String token) {
        return claimsCache.get(token, this::extractClaimsInternal);
    }

    private Claims extractClaimsInternal(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(PUBLIC_KEY)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException e) {
            throw new RuntimeException("Invalid JWT token", e);
        }
    }

    private Claims extractClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(PUBLIC_KEY)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException e) {
            throw new RuntimeException("Invalid JWT token", e);
        }
    }

    private boolean isTokenExpired(String token) {
        return extractClaims(token)
                .getExpiration()
                .before(new Date());
    }
}
