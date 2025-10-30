package com.dev.quikkkk.user_service.service.impl;

import com.dev.quikkkk.user_service.dto.request.UpdateUserProfileRequest;
import com.dev.quikkkk.user_service.dto.response.UserProfileResponse;
import com.dev.quikkkk.user_service.exception.BusinessException;
import com.dev.quikkkk.user_service.mapper.UserMapper;
import com.dev.quikkkk.user_service.repository.IUserRepository;
import com.dev.quikkkk.user_service.service.IFileStorageService;
import com.dev.quikkkk.user_service.service.IUserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import static com.dev.quikkkk.user_service.exception.ErrorCode.USER_NOT_FOUND;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements IUserService {
    private final IUserRepository repository;
    private final IFileStorageService fileStorageService;
    private final UserMapper mapper;

    @Override
    @Cacheable(value = "users", key = "#userId")
    public UserProfileResponse getProfileCurrentUser(String userId) {
        return getUserById(userId);
    }

    @Override
    @Transactional
    @CacheEvict(value = "users", key = "#userId")
    public UserProfileResponse updateUserProfile(String userId, UpdateUserProfileRequest request) {
        var user = repository.findById(userId).orElseThrow(() -> new BusinessException(USER_NOT_FOUND));
        mapper.mergeUser(user, request);
        var updatedUser = repository.save(user);

        log.info("Profile updated successfully for user: {}", userId);
        return mapper.toUserProfile(updatedUser);
    }

    @Override
    @Cacheable(value = "users", key = "#userId")
    public UserProfileResponse getUserById(String userId) {
        log.info("Getting profile for user: {}", userId);
        return repository.findById(userId)
                .map(mapper::toUserProfile)
                .orElseThrow(() -> new BusinessException(USER_NOT_FOUND));
    }

    @Override
    @Transactional
    @CacheEvict(value = "users", key = "#userId")
    public String uploadAvatar(String userId, MultipartFile file) {
        log.info("Uploading avatar for user: {}", userId);
        var user = repository.findById(userId).orElseThrow(() -> new BusinessException(USER_NOT_FOUND));

        if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
            log.info("Deleting old avatar: {}", user.getAvatarUrl());
            fileStorageService.deleteFile(user.getAvatarUrl());
        }

        String avatarUrl = fileStorageService.uploadFile(file, userId);
        user.setAvatarUrl(avatarUrl);
        repository.save(user);

        log.info("Avatar uploaded successfully for user: {}. URL: {}", userId, avatarUrl);
        return avatarUrl;
    }
}
