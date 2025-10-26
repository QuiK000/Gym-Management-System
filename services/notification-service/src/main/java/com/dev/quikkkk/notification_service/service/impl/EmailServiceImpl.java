package com.dev.quikkkk.notification_service.service.impl;

import com.dev.quikkkk.notification_service.dto.EmailTemplate;
import com.dev.quikkkk.notification_service.service.IEmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.Map;

import static com.dev.quikkkk.notification_service.dto.EmailTemplate.CODE_CONFIRMATION;
import static com.dev.quikkkk.notification_service.dto.EmailTemplate.PASSWORD_RESET;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.mail.javamail.MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements IEmailService {
    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    @Value("${spring.mail.from:noreply@quikkkk.dev}")
    private String fromEmail;

    @Override
    @Async
    @Retryable(backoff = @Backoff(delay = 2000, multiplier = 2))
    public void sendCodeSuccessEmail(String destinationEmail, String code) throws MessagingException {
        log.info("Sending code confirmation email to {}", destinationEmail);
        sendTemplatedEmail(destinationEmail, CODE_CONFIRMATION, Map.of(
                "code", code,
                "expiryMinutes", 15,
                "supportEmail", fromEmail
        ));
    }

    @Override
    @Async
    @Retryable(backoff = @Backoff(delay = 2000, multiplier = 2))
    public void sendPasswordResetEmail(String destinationEmail, String resetLink) throws MessagingException {
        log.info("Sending password reset email to {}", destinationEmail);
        sendTemplatedEmail(destinationEmail, PASSWORD_RESET, Map.of(
                "email", destinationEmail,
                "resetLink", resetLink,
                "expiryMinutes", 60,
                "supportEmail", fromEmail
        ));
    }

    private void sendTemplatedEmail(
            String destinationEmail,
            EmailTemplate template,
            Map<String, Object> variables
    ) throws MessagingException {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    mimeMessage,
                    MULTIPART_MODE_MIXED_RELATED,
                    UTF_8.name()
            );

            helper.setFrom(fromEmail);
            helper.setTo(destinationEmail);
            helper.setSubject(template.getSubject());

            Context context = new Context();
            context.setVariables(variables);

            String htmlContent = templateEngine.process(template.getTemplate(), context);
            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);
            log.info("Templated email sent successfully to: {} using template: {}", destinationEmail, template);
        } catch (MessagingException e) {
            log.error("Failed to send templated email to: {} using template: {}", destinationEmail, template.getTemplate(), e);
            throw e;
        }
    }
}
