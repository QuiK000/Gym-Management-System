package com.dev.quikkkk.notification_service.service;

import jakarta.mail.MessagingException;

public interface IEmailService {
    void sendCodeSuccessEmail(String destinationEmail, String code) throws MessagingException;

    void sendPasswordResetEmail(String destinationEmail, String resetLink) throws MessagingException;
}
