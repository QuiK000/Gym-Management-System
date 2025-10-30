package com.dev.quikkkk.user_service.service.impl;

import com.dev.quikkkk.user_service.dto.request.UpdateUserProfileRequest;
import com.dev.quikkkk.user_service.dto.response.UserProfileResponse;
import com.dev.quikkkk.user_service.exception.BusinessException;
import com.dev.quikkkk.user_service.mapper.UserMapper;
import com.dev.quikkkk.user_service.repository.IUserRepository;
import com.dev.quikkkk.user_service.service.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.dev.quikkkk.user_service.exception.ErrorCode.USER_NOT_FOUND;

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
                .orElseThrow(() -> new BusinessException(USER_NOT_FOUND));
    }

    @Override
    public UserProfileResponse updateUserProfile(String userId, UpdateUserProfileRequest request) {
        var user = repository.findById(userId).orElseThrow(() -> new BusinessException(USER_NOT_FOUND));
        mapper.mergeUser(user, request);
        var updatedUser = repository.save(user);

        return mapper.toUserProfile(updatedUser);
    }
}
