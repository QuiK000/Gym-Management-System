package com.dev.quikkkk.user_service.service;

import com.dev.quikkkk.user_service.dto.request.UpdateUserProfileRequest;
import com.dev.quikkkk.user_service.dto.response.UserProfileResponse;

public interface IUserService {
    UserProfileResponse getProfileCurrentUser(String userId);

    UserProfileResponse updateUserProfile(String userId, UpdateUserProfileRequest request);

    UserProfileResponse getUserById(String userId);
}
