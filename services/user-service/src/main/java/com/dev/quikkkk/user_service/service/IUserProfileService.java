package com.dev.quikkkk.user_service.service;

import com.dev.quikkkk.user_service.dto.response.UserProfileResponse;

public interface IUserProfileService {
    UserProfileResponse getProfileCurrentUser(String userId);
}
