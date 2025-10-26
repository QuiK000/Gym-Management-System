package com.dev.quikkkk.notification_service.dto;

public record PasswordReset(
        String email,
        String resetLink
) {
}
