package com.dev.quikkkk.user_service.service.impl;

import com.dev.quikkkk.user_service.dto.request.UpdateUserProfileRequest;
import com.dev.quikkkk.user_service.dto.response.UserProfileResponse;
import com.dev.quikkkk.user_service.entity.User;
import com.dev.quikkkk.user_service.exception.BusinessException;
import com.dev.quikkkk.user_service.mapper.UserMapper;
import com.dev.quikkkk.user_service.repository.IUserRepository;
import com.dev.quikkkk.user_service.service.IFileStorageService;
import com.dev.quikkkk.user_service.service.IUserService;
import jakarta.transaction.Transactional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
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
    private final PagedResourcesAssembler<UserProfileResponse> pagedResourcesAssembler;

    @Override
    @Cacheable(value = "users", key = "#userId")
    public UserProfileResponse getProfileCurrentUser(String userId) {
        return getUserById(userId);
    }

    @Override
    @Transactional
    @CacheEvict(value = "users", key = "#userId")
    public UserProfileResponse updateUserProfile(String userId, UpdateUserProfileRequest request) {
        var user = findUserById(userId);
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
    public PagedModel<@NonNull EntityModel<@NonNull UserProfileResponse>> getAllUsers(
            int page,
            int size,
            String role,
            String search
    ) {
        log.info("Getting all users with filters: role={}, search={}", role, search);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        Page<User> users = repository.findAllWithFilters(role, search, pageable);
        Page<UserProfileResponse> responsePage = users.map(mapper::toUserProfile);

        log.info("Found {} users, total pages: {}, total elements: {}",
                responsePage.getNumberOfElements(),
                responsePage.getTotalPages(),
                responsePage.getTotalElements()
        );

        return pagedResourcesAssembler.toModel(responsePage);
    }

    @Override
    @Transactional
    @CacheEvict(value = "users", key = "#userId")
    public String uploadAvatar(String userId, MultipartFile file) {
        log.info("Uploading avatar for user: {}", userId);
        var user = findUserById(userId);

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

    @Override
    @Transactional
    @CacheEvict(value = "users", key = "#userId")
    public void deleteAvatar(String userId) {
        log.info("Deleting avatar for user: {}", userId);
        var user = findUserById(userId);

        if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
            fileStorageService.deleteFile(user.getAvatarUrl());
            user.setAvatarUrl(null);

            repository.save(user);
            log.info("Avatar deleted successfully for user: {}", userId);
        } else {
            log.info("User {} has no avatar to delete", userId);
        }
    }

    private User findUserById(String userId) {
        return repository.findById(userId).orElseThrow(() -> new BusinessException(USER_NOT_FOUND));
    }
}
