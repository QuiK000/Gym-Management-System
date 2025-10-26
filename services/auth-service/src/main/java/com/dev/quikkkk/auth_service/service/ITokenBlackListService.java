package com.dev.quikkkk.auth_service.service;

public interface ITokenBlackListService {
    void blacklistToken(String token);

    boolean isTokenBlacklisted(String token);

    void cleanupExpiredTokens();
}
