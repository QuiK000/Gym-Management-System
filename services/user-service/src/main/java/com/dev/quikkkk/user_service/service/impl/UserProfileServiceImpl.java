package com.dev.quikkkk.user_service.service.impl;

import com.dev.quikkkk.user_service.dto.response.UserProfileResponse;
import com.dev.quikkkk.user_service.mapper.UserProfileMapper;
import com.dev.quikkkk.user_service.repository.IUserProfileRepository;
import com.dev.quikkkk.user_service.service.IUserProfileService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserProfileServiceImpl implements IUserProfileService {
    private final IUserProfileRepository repository;
    private final UserProfileMapper mapper;

    @Override
    public UserProfileResponse getProfileCurrentUser(String userId) {
        log.info("Getting profile for current user");
        return repository.findById(userId)
                .map(mapper::toUserProfile)
                .orElseThrow(() -> new EntityNotFoundException("dsa"));
    }
}
