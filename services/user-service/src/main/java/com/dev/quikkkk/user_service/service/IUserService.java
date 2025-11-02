package com.dev.quikkkk.user_service.service;

import com.dev.quikkkk.user_service.dto.request.UpdateUserProfileRequest;
import com.dev.quikkkk.user_service.dto.request.UpdateUserRoleRequest;
import com.dev.quikkkk.user_service.dto.response.RoleUpdatedResponse;
import com.dev.quikkkk.user_service.dto.response.UserProfileResponse;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

public interface IUserService {
    UserProfileResponse getProfileCurrentUser(String userId);

    UserProfileResponse updateUserProfile(String userId, UpdateUserProfileRequest request);

    UserProfileResponse getUserById(String userId);

    Page<UserProfileResponse> getAllUsers(int page, int size, String role, String search);

    String uploadAvatar(String userId, MultipartFile file);

    RoleUpdatedResponse updateRole(String userId, UpdateUserRoleRequest request);

    void deleteAvatar(String userId);
}
