package com.dev.quikkkk.auth_service.service.impl;

import com.dev.quikkkk.auth_service.service.IBruteForceProtectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;

import static java.util.concurrent.TimeUnit.MINUTES;

@Service
@RequiredArgsConstructor
@Slf4j
public class BruteForceProtectionServiceImpl implements IBruteForceProtectionService {
    private final RedisTemplate<String, Object> redisTemplate;

    private static final int MAX_ATTEMPTS = 5;
    private static final int BLOCK_DURATION_IN_MINUTES = 15;
    private static final int ATTEMPT_WINDOW_MINUTES = 5;
    private static final String ATTEMPT_KEY_PREFIX = "login_attempt_";
    private static final String BLOCK_KEY_PREFIX = "blocked_ip_";

    @Override
    public void registerFailedAttempt(String ipAddress) {
        log.info("Registering failed attempt for IP: {}", ipAddress);
        String attemptKey = ATTEMPT_KEY_PREFIX + ipAddress;
        String blockKey = BLOCK_KEY_PREFIX + ipAddress;

        if (redisTemplate.hasKey(blockKey)) return;
        Long attempts = redisTemplate.opsForValue().increment(attemptKey);

        if (attempts != null && attempts == 1) redisTemplate.expire(attemptKey, ATTEMPT_WINDOW_MINUTES, MINUTES);
        if (attempts != null && attempts > MAX_ATTEMPTS) {
            blockIp(ipAddress);
            redisTemplate.delete(attemptKey);
        }
    }

    @Override
    public void registerSuccessfulAttempt(String ipAddress) {
        String attemptKey = ATTEMPT_KEY_PREFIX + ipAddress;
        redisTemplate.delete(attemptKey);
    }

    @Override
    public boolean isBlocked(String ipAddress) {
        String blockKey = BLOCK_KEY_PREFIX + ipAddress;
        return redisTemplate.hasKey(blockKey);
    }

    @Override
    public int getRemainingAttempts(String ipAddress) {
        String attemptKey = ATTEMPT_KEY_PREFIX + ipAddress;
        Object attemptsObj = redisTemplate.opsForValue().get(attemptKey);

        if (attemptsObj == null) return MAX_ATTEMPTS;
        long attempts;

        if (attemptsObj instanceof Long) {
            attempts = (Long) attemptsObj;
        } else if (attemptsObj instanceof Integer) {
            attempts = ((Integer) attemptsObj).longValue();
        } else {
            try {
                attempts = Long.parseLong(attemptsObj.toString());
            } catch (NumberFormatException e) {
                log.warn("Invalid attempts value in Redis: {}", attemptsObj);
                return MAX_ATTEMPTS;
            }
        }

        return (int) (MAX_ATTEMPTS - attempts);
    }

    @Override
    @Scheduled(fixedRate = 60 * 60 * 1000)
    public void cleanupExpiredBlocks() {
        log.info("Running cleanup of expired IP blocks");
    }

    private void blockIp(String ipAddress) {
        String blockKey = BLOCK_KEY_PREFIX + ipAddress;

        redisTemplate.opsForValue().set(blockKey, Instant.now().toString());
        redisTemplate.expire(blockKey, BLOCK_DURATION_IN_MINUTES, MINUTES);

        log.warn("IP Address {} blocked due to too many failed login attempts", ipAddress);
    }
}
