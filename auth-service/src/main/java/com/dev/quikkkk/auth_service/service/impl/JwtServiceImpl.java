package com.dev.quikkkk.auth_service.service.impl;

import com.dev.quikkkk.auth_service.entity.Role;
import com.dev.quikkkk.auth_service.entity.UserCredentials;
import com.dev.quikkkk.auth_service.service.IJwtService;
import com.dev.quikkkk.auth_service.utils.KeyUtils;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static java.util.concurrent.TimeUnit.MINUTES;

@Service
public class JwtServiceImpl implements IJwtService {
    private static final String TOKEN_TYPE = "token_type";
    private static final String USER_ID = "userId";
    private static final String PATH_TO_PRIVATE_KEY = "keys/local-only/private_key.pem";
    private static final String PATH_TO_PUBLIC_KEY = "keys/local-only/public_key.pem";
    private static final PrivateKey PRIVATE_KEY;
    private static final PublicKey PUBLIC_KEY;

    private final Cache<@NonNull String, Claims> claimsCache = Caffeine.newBuilder()
            .maximumSize(10000)
            .expireAfterWrite(5, MINUTES)
            .build();

    static {
        try {
            PRIVATE_KEY = KeyUtils.loadPrivateKey(PATH_TO_PRIVATE_KEY);
            PUBLIC_KEY = KeyUtils.loadPublicKey(PATH_TO_PUBLIC_KEY);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load JWT Keys", e);
        }
    }

    @Value("${app.security.jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Value("${app.security.jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    @Override
    public String generateAccessToken(UserCredentials userCredentials) {
        Map<String, Object> claims = Map.of(
                TOKEN_TYPE, "ACCESS_TOKEN",
                USER_ID, userCredentials.getId(),
                "roles", userCredentials.getRoles().stream().map(Role::getName).toList()
        );

        return buildToken(userCredentials.getUsername(), claims, accessTokenExpiration);
    }

    @Override
    public String generateRefreshToken(UserCredentials userCredentials) {
        Map<String, Object> claims = Map.of(
                TOKEN_TYPE, "REFRESH_TOKEN",
                USER_ID, userCredentials.getId(),
                "roles", userCredentials.getRoles().stream().map(Role::getName).toList()
        );

        return buildToken(userCredentials.getUsername(), claims, refreshTokenExpiration);
    }

    @Override
    public String refreshAccessToken(String refreshToken) {
        Claims claims = extractClaims(refreshToken);

        if (!"REFRESH_TOKEN".equals(claims.get(TOKEN_TYPE))) throw new RuntimeException("Invalid token type");
        if (isTokenExpired(refreshToken)) throw new RuntimeException("Token expired");

        String email = claims.getSubject();
        String userId = claims.get(USER_ID).toString();

        Map<String, Object> claimsForNewToken = Map.of(
                TOKEN_TYPE, "ACCESS_TOKEN",
                USER_ID, userId,
                "roles", claims.get("roles")
        );


        return buildToken(email, claimsForNewToken, accessTokenExpiration);
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
    public String extractTokenType(String token) {
        return getCachedClaims(token).get(TOKEN_TYPE).toString();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        return (List<String>) getCachedClaims(token).get("roles");
    }

    @Override
    public Date extractExpiration(String token) {
        return getCachedClaims(token).getExpiration();
    }

    @Override
    public boolean isTokenValid(String token, String expectedEmail) {
        String email = extractEmail(token);
        return email.equals(expectedEmail) && !isTokenExpired(token);
    }

    private String buildToken(String email, Map<String, Object> claims, long expiration) {
        return Jwts.builder()
                .claims(claims)
                .subject(email)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(PRIVATE_KEY)
                .compact();
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
}
