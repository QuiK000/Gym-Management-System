package com.dev.quikkkk.auth_service.service.impl;

import com.dev.quikkkk.auth_service.service.ITokenBlackListService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import static java.util.concurrent.TimeUnit.HOURS;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenBlackListServiceImpl implements ITokenBlackListService {
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String BLACKLIST_KEY_PREFIX = "blacklist_token_";

    @Override
    public void blacklistToken(String token) {
        String key = BLACKLIST_KEY_PREFIX + token;

        redisTemplate.opsForValue().set(key, "blacklisted");
        redisTemplate.expire(key, 24, HOURS);

        log.info("Token {} blacklisted", token);
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
}
