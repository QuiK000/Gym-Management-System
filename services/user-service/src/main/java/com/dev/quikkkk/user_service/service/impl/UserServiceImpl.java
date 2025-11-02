package com.dev.quikkkk.user_service.service.impl;

import com.dev.quikkkk.user_service.client.IAuthClient;
import com.dev.quikkkk.user_service.dto.request.UpdateUserProfileRequest;
import com.dev.quikkkk.user_service.dto.request.UpdateUserRoleRequest;
import com.dev.quikkkk.user_service.dto.response.ApiResponse;
import com.dev.quikkkk.user_service.dto.response.RoleUpdatedResponse;
import com.dev.quikkkk.user_service.dto.response.UserProfileResponse;
import com.dev.quikkkk.user_service.entity.User;
import com.dev.quikkkk.user_service.exception.BusinessException;
import com.dev.quikkkk.user_service.mapper.UserMapper;
import com.dev.quikkkk.user_service.repository.IUserRepository;
import com.dev.quikkkk.user_service.service.IFileStorageService;
import com.dev.quikkkk.user_service.service.IUserService;
import feign.FeignException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import static com.dev.quikkkk.user_service.exception.ErrorCode.EMAIL_ALREADY_EXISTS;
import static com.dev.quikkkk.user_service.exception.ErrorCode.FILE_UPLOAD_ERROR;
import static com.dev.quikkkk.user_service.exception.ErrorCode.INTERNAL_SERVER_ERROR;
import static com.dev.quikkkk.user_service.exception.ErrorCode.USER_NOT_FOUND;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements IUserService {
    private final IUserRepository repository;
    private final IFileStorageService fileStorageService;
    private final UserMapper mapper;
    private final IAuthClient client;

    @Override
    @Cacheable(value = "users", key = "#userId", unless = "#result == null")
    public UserProfileResponse getProfileCurrentUser(String userId) {
        log.debug("Getting profile for user: {}", userId);
        return getUserById(userId);
    }

    @Override
    @Transactional
    @CacheEvict(value = "users", key = "#userId")
    public UserProfileResponse updateUserProfile(String userId, UpdateUserProfileRequest request) {
        var user = findUserById(userId);
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (repository.existsByEmail(request.getEmail())) {
                throw new BusinessException(EMAIL_ALREADY_EXISTS);
            }
        }

        mapper.mergeUser(user, request);
        var updatedUser = repository.save(user);

        log.info("Profile updated successfully for user: {}", userId);
        return mapper.toUserProfile(updatedUser);
    }

    @Override
    @Cacheable(value = "users", key = "#userId", unless = "#result == null")
    public UserProfileResponse getUserById(String userId) {
        log.info("Getting profile for user: {}", userId);
        return repository.findById(userId)
                .map(mapper::toUserProfile)
                .orElseThrow(() -> new BusinessException(USER_NOT_FOUND));
    }

    @Override
    public Page<UserProfileResponse> getAllUsers(int page, int size, String role, String search) {
        log.info("Getting all users. Page: {}, Size: {}, Role: {}, Search: {}", page, size, role, search);

        if (page < 0) page = 0;
        if (size <= 0) size = 20;
        if (size > 100) size = 100;

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        Page<User> users = repository.findAllWithFilters(role, search, pageable);
        Page<UserProfileResponse> responsePage = users.map(mapper::toUserProfile);

        log.info(
                "Retrieved {} users out of {} total, page {}/{}",
                responsePage.getNumberOfElements(),
                responsePage.getTotalElements(),
                responsePage.getNumber() + 1,
                responsePage.getTotalPages()
        );

        return responsePage;
    }

    @Override
    @Transactional
    @CacheEvict(value = "users", key = "#userId")
    public String uploadAvatar(String userId, MultipartFile file) {
        log.info("Uploading avatar for user: {}", userId);
        var user = findUserById(userId);

        if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
            log.debug("Deleting old avatar: {}", user.getAvatarUrl());
            try {
                fileStorageService.deleteFile(user.getAvatarUrl());
            } catch (Exception e) {
                log.error("Failed to delete old avatar: {}", user.getAvatarUrl(), e);
            }
        }

        String avatarUrl = fileStorageService.uploadFile(file, userId);
        user.setAvatarUrl(avatarUrl);
        repository.save(user);

        log.info("Avatar uploaded successfully for user: {}", userId);
        return avatarUrl;
    }

    @Override
    @Transactional
    @CacheEvict(value = "users", key = "#userId")
    public RoleUpdatedResponse updateRole(String userId, UpdateUserRoleRequest request) {
        log.info("Updating role for user: {}", userId);
        var user = findUserById(userId);

        try {
            ApiResponse<RoleUpdatedResponse> response = client.updateUserRole(userId, request);
            if (!response.success() || response.data() == null) {
                log.error("Failed to update in auth-service for user: {}", userId);
                throw new BusinessException(INTERNAL_SERVER_ERROR);
            }

            user.setRole(request.getRole());
            repository.save(user);

            log.info("Successfully updated role for user: {} to {}", userId, request.getRole());
            return response.data();
        } catch (FeignException e) {
            log.error("Failed to update in auth-service for user: {}", userId, e);
            if (e.status() == 404) throw new BusinessException(USER_NOT_FOUND);
            throw new BusinessException(INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = "users", key = "#userId")
    public void deleteAvatar(String userId) {
        log.info("Deleting avatar for user: {}", userId);
        var user = findUserById(userId);

        if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
            String avatarUrl = user.getAvatarUrl();

            try {
                fileStorageService.deleteFile(avatarUrl);
                user.setAvatarUrl(null);

                repository.save(user);
                log.info("Avatar deleted successfully for user: {}", userId);
            } catch (Exception e) {
                log.error("Failed to delete avatar: {}", avatarUrl, e);
                throw new BusinessException(FILE_UPLOAD_ERROR);
            }
        } else {
            log.info("User {} has no avatar to delete", userId);
        }
    }

    private User findUserById(String userId) {
        return repository.findById(userId).orElseThrow(() -> new BusinessException(USER_NOT_FOUND));
    }
}
