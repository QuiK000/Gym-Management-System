package com.dev.quikkkk.auth_service.service.impl;

import com.dev.quikkkk.auth_service.service.IJwtService;
import com.dev.quikkkk.auth_service.service.ITokenBlackListService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;

import static java.util.concurrent.TimeUnit.HOURS;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenBlackListServiceImpl implements ITokenBlackListService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final IJwtService jwtService;

    private static final String BLACKLIST_KEY_PREFIX = "blacklist_token_";

    @Override
    public void blacklistToken(String token) {
        try {
            String key = BLACKLIST_KEY_PREFIX + token;
            String email = jwtService.extractEmail(token);
            Date expiration = getTokenExpiration(token);

            if (expiration != null) {
                long ttl = expiration.getTime() - System.currentTimeMillis();
                if (ttl > 0) {
                    redisTemplate.opsForValue().set(key, Instant.now().toString(), Duration.ofMillis(ttl));
                    log.info("Token blacklisted successfully. TTL: {} ms", ttl);
                } else {
                    log.warn("Token already expired, not adding to blacklist.");
                }
            } else {
                redisTemplate.opsForValue().set(key, Instant.now().toString());
                redisTemplate.expire(key, Duration.ofHours(24));
                log.warn("Could not determine token expiration, using default TTL: 24 hours");
            }
        } catch (Exception e) {
            log.error("Error blacklisting token", e);
            String key = BLACKLIST_KEY_PREFIX + token;

            redisTemplate.opsForValue().set(key, Instant.now().toString());
            redisTemplate.expire(key, Duration.ofHours(24));
        }
    }

    @Override
    public boolean isTokenBlacklisted(String token) {
        String key = BLACKLIST_KEY_PREFIX + token;
        return redisTemplate.hasKey(key);
    }

    @Override
    @Scheduled(fixedRate = 60 * 60 * 1000)
    public void cleanupExpiredTokens() {
        log.info("Running cleanup of expired tokens");
    }

    private Date getTokenExpiration(String token) {
        try {
            return jwtService.extractExpiration(token);
        } catch (Exception e) {
            log.error("Could not extract expiration from token", e);
            return null;
        }
    }
}
