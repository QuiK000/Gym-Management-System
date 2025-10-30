package com.dev.quikkkk.user_service.service.impl;

import com.dev.quikkkk.user_service.dto.response.UserProfileResponse;
import com.dev.quikkkk.user_service.mapper.UserMapper;
import com.dev.quikkkk.user_service.repository.IUserRepository;
import com.dev.quikkkk.user_service.service.IUserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements IUserService {
    private final IUserRepository repository;
    private final UserMapper mapper;

    @Override
    public UserProfileResponse getProfileCurrentUser(String userId) {
        log.info("Getting profile for current user");
        return repository.findById(userId)
                .map(mapper::toUserProfile)
                .orElseThrow(() -> new EntityNotFoundException("dsa"));
    }
}
