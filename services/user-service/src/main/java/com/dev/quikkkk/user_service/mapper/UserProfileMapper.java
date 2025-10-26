package com.dev.quikkkk.user_service.mapper;

import com.dev.quikkkk.user_service.dto.response.UserProfileResponse;
import com.dev.quikkkk.user_service.entity.UserProfile;
import org.springframework.stereotype.Service;

@Service
public class UserProfileMapper {
    public UserProfileResponse toUserProfile(UserProfile userProfile) {
        return UserProfileResponse.builder()
                .id(userProfile.getId())
                .email(userProfile.getEmail())
                .firstName(userProfile.getFirstName())
                .lastName(userProfile.getLastName())
                .phone(userProfile.getPhone())
                .dateOfBirth(userProfile.getDateOfBirth())
                .gender(userProfile.getGender())
                .avatarUrl(userProfile.getAvatarUrl())
                .address(userProfile.getAddress())
                .emergencyContactName(userProfile.getEmergencyContactName())
                .emergencyContactPhone(userProfile.getEmergencyContactPhone())
                .build();
    }
}
