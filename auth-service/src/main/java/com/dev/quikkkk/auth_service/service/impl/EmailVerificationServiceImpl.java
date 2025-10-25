package com.dev.quikkkk.auth_service.service.impl;

import com.dev.quikkkk.auth_service.dto.kafka.CodeConfirmation;
import com.dev.quikkkk.auth_service.dto.request.ResendVerificationRequest;
import com.dev.quikkkk.auth_service.dto.request.VerifyEmailRequest;
import com.dev.quikkkk.auth_service.entity.EmailVerification;
import com.dev.quikkkk.auth_service.entity.UserCredentials;
import com.dev.quikkkk.auth_service.exception.BusinessException;
import com.dev.quikkkk.auth_service.repository.IEmailVerificationRepository;
import com.dev.quikkkk.auth_service.repository.IUserCredentialsRepository;
import com.dev.quikkkk.auth_service.service.IEmailVerificationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

import static com.dev.quikkkk.auth_service.exception.ErrorCode.EMAIL_ALREADY_VERIFIED;
import static com.dev.quikkkk.auth_service.exception.ErrorCode.INVALID_VERIFICATION_CODE;
import static com.dev.quikkkk.auth_service.exception.ErrorCode.MAX_VERIFICATION_ATTEMPTS_EXCEEDED;
import static com.dev.quikkkk.auth_service.exception.ErrorCode.TOO_MANY_EMAIL_ATTEMPTS;
import static com.dev.quikkkk.auth_service.exception.ErrorCode.USER_NOT_FOUND;
import static java.util.concurrent.TimeUnit.HOURS;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailVerificationServiceImpl implements IEmailVerificationService {
    private final IEmailVerificationRepository verificationRepository;
    private final IUserCredentialsRepository userRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final int CODE_EXPIRY_MINUTES = 15;
    private static final int MAX_SENDS_PER_HOUR = 3;
    private static final String RATE_LIMIT_KEY_PREFIX = "email_rate_";

    @Override
    @Transactional
    public void sendVerificationCode(String userId, String email, String ipAddress) {
        log.info("Sending verification code to user: {}, email: {}", userId, email);
        if (!canSendEmail(email)) throw new BusinessException(TOO_MANY_EMAIL_ATTEMPTS);

        verificationRepository.deleteByUserId(userId);
        String code = generateVerificationCode();

        EmailVerification verification = EmailVerification.builder()
                .userId(userId)
                .email(email)
                .code(code)
                .expiresAt(LocalDateTime.now().plusMinutes(CODE_EXPIRY_MINUTES))
                .ipAddress(ipAddress)
                .createdBy("SYSTEM")
                .build();

        verificationRepository.save(verification);
        CodeConfirmation notification = new CodeConfirmation(userId, email, code);

        kafkaTemplate.send("code-topic", notification);
        incrementEmailRateLimit(email);

        log.info("Verification code sent successfully to user: {}, email: {}", userId, email);
    }

    @Override
    @Transactional
    public void verifyEmail(VerifyEmailRequest request) {
        log.info("Verifying email: {} with code: {}", request.getEmail(), request.getCode());

        EmailVerification verification = verificationRepository
                .findByEmailAndCodeAndVerifiedFalse(request.getEmail(), request.getCode())
                .orElseThrow(() -> new BusinessException(INVALID_VERIFICATION_CODE));

        if (verification.isExpired()) {
            log.warn("Verification code for email: {} is expired", request.getEmail());
            throw new BusinessException(INVALID_VERIFICATION_CODE);
        }

        if (verification.hasExceededMaxAttempts()) {
            log.warn("Exceeded max verification attempts for email: {}", request.getEmail());
            throw new BusinessException(MAX_VERIFICATION_ATTEMPTS_EXCEEDED);
        }

        verification.incrementAttempts();

        if (!verification.getCode().equals(request.getCode())) {
            verificationRepository.save(verification);
            throw new BusinessException(INVALID_VERIFICATION_CODE);
        }

        verification.setVerified(true);
        verificationRepository.save(verification);

        UserCredentials user = userRepository.findById(verification.getUserId())
                .orElseThrow(() -> new BusinessException(USER_NOT_FOUND));

        user.setEmailVerified(true);
        user.setEnabled(true);
        userRepository.save(user);

        log.info("Email verified successfully for user: {}", verification.getUserId());
    }

    @Override
    @Transactional
    public void resendVerificationCode(ResendVerificationRequest request, String ipAddress) {
        log.info("Resending verification code for email: {}", request.getEmail());

        UserCredentials user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException(USER_NOT_FOUND));

        if (user.isEmailVerified()) throw new BusinessException(EMAIL_ALREADY_VERIFIED);
        sendVerificationCode(user.getId(), request.getEmail(), ipAddress);
    }

    @Override
    public boolean isEmailRecentlyVerified(String email) {
        return verificationRepository.existsByEmailAndVerifiedTrueAndCreatedDateAfter(
                email,
                LocalDateTime.now().minusHours(1)
        );
    }

    @Override
    @Scheduled(fixedRate = 60 * 60 * 1000)
    public void cleanupExpiredVerifications() {
        log.info("Cleaning up expired verification codes");
        verificationRepository.deleteExpiredVerifications(LocalDateTime.now());
    }

    private boolean canSendEmail(String email) {
        String key = RATE_LIMIT_KEY_PREFIX + email;
        Object countObj = redisTemplate.opsForValue().get(key);

        if (countObj == null) return true;
        try {
            int count;
            switch (countObj) {
                case Integer i -> count = i;
                case String s -> count = Integer.parseInt(s);
                case Long l -> count = l.intValue();
                default -> {
                    log.warn("Unexpected type of count object: {}", countObj.getClass().getName());
                    return true;
                }
            }

            return count < MAX_SENDS_PER_HOUR;
        } catch (NumberFormatException e) {
            log.warn("Invalid count value in Redis: {}", countObj);
            return true;
        }
    }

    private String generateVerificationCode() {
        SecureRandom random = new SecureRandom();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }

    private void incrementEmailRateLimit(String email) {
        String key = RATE_LIMIT_KEY_PREFIX + email;
        Long count = redisTemplate.opsForValue().increment(key, 1L);

        if (count != null && count == 1) redisTemplate.expire(key, 1, HOURS);
    }
}
