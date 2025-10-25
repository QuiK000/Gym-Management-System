package com.dev.quikkkk.auth_service.service.impl;

import com.dev.quikkkk.auth_service.entity.UserCredentials;
import com.dev.quikkkk.auth_service.exception.BusinessException;
import com.dev.quikkkk.auth_service.repository.IUserCredentialsRepository;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import lombok.NonNull;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import static com.dev.quikkkk.auth_service.exception.ErrorCode.USER_NOT_FOUND;
import static java.util.concurrent.TimeUnit.MINUTES;

@Service
public class CachedUserDetailsServiceImpl implements UserDetailsService {
    private final IUserCredentialsRepository repository;
    private final LoadingCache<@NonNull String, UserCredentials> userCache;

    public CachedUserDetailsServiceImpl(IUserCredentialsRepository repository) {
        this.repository = repository;
        this.userCache = Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(15, MINUTES)
                .build(this::loadUser);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        try {
            return userCache.get(email.toLowerCase());
        } catch (Exception e) {
            throw new UsernameNotFoundException(e.getMessage());
        }
    }

    private UserCredentials loadUser(String email) {
        return repository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new BusinessException(USER_NOT_FOUND));
    }

    @CacheEvict("userCache")
    public void evictUser(String email) {
        userCache.invalidate(email.toLowerCase());
    }
}
