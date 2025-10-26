package com.dev.quikkkk.auth_service.service;

import com.dev.quikkkk.auth_service.dto.request.ResendVerificationRequest;
import com.dev.quikkkk.auth_service.dto.request.VerifyEmailRequest;

public interface IEmailVerificationService {
    void sendVerificationCode(String userId, String email, String ipAddress);

    void verifyEmail(VerifyEmailRequest request);

    void resendVerificationCode(ResendVerificationRequest request, String ipAddress);

    boolean isEmailRecentlyVerified(String email);

    void cleanupExpiredVerifications();
}
