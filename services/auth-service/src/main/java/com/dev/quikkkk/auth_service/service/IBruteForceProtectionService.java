package com.dev.quikkkk.auth_service.service;

public interface IBruteForceProtectionService {
    void registerFailedAttempt(String ipAddress);

    void registerSuccessfulAttempt(String ipAddress);

    boolean isBlocked(String ipAddress);

    int getRemainingAttempts(String ipAddress);

    void cleanupExpiredBlocks();
}
