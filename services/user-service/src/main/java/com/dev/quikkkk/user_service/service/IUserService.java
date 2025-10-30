package com.dev.quikkkk.user_service.service;

import com.dev.quikkkk.user_service.dto.request.UpdateUserProfileRequest;
import com.dev.quikkkk.user_service.dto.response.UserProfileResponse;
import org.springframework.web.multipart.MultipartFile;

public interface IUserService {
    UserProfileResponse getProfileCurrentUser(String userId);

    UserProfileResponse updateUserProfile(String userId, UpdateUserProfileRequest request);

    UserProfileResponse getUserById(String userId);

    String uploadAvatar(String userId, MultipartFile file);
}
