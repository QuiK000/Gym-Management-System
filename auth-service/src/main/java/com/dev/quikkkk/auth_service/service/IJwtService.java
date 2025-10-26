package com.dev.quikkkk.auth_service.service;

import com.dev.quikkkk.auth_service.entity.UserCredentials;

import java.util.Date;
import java.util.List;

public interface IJwtService {
    String generateAccessToken(UserCredentials userCredentials);

    String generateRefreshToken(UserCredentials userCredentials);

    String refreshAccessToken(String refreshToken);

    String extractEmail(String token);

    String extractUserId(String token);

    List<String> extractRoles(String token);

    Date extractExpiration(String token);

    boolean isTokenValid(String token, String expectedEmail);
}
