package com.dev.quikkkk.user_service.mapper;

import com.dev.quikkkk.user_service.dto.response.UserProfileResponse;
import com.dev.quikkkk.user_service.entity.User;
import org.springframework.stereotype.Service;

@Service
public class UserMapper {
    public UserProfileResponse toUserProfile(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .dateOfBirth(user.getDateOfBirth())
                .gender(user.getGender())
                .avatarUrl(user.getAvatarUrl())
                .address(user.getAddress())
                .emergencyContactName(user.getEmergencyContactName())
                .emergencyContactPhone(user.getEmergencyContactPhone())
                .build();
    }
}
