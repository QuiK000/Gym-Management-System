package com.dev.quikkkk.user_service.service;

import java.util.List;

public interface IJwtService {
    String extractEmail(String token);

    String extractUserId(String token);

    List<String> extractRoles(String token);

    boolean isTokenValid(String token, String expectedEmail);
}
