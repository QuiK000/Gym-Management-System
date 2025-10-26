package com.dev.quikkkk.notification_service.dto;

public record CodeConfirmation(
        String userId,
        String email,
        String code
) {
}
