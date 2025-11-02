package com.dev.quikkkk.user_service.mapper;

import com.dev.quikkkk.user_service.dto.request.UpdateUserProfileRequest;
import com.dev.quikkkk.user_service.dto.request.UpdateUserRoleRequest;
import com.dev.quikkkk.user_service.dto.response.RoleUpdatedResponse;
import com.dev.quikkkk.user_service.dto.response.UserProfileResponse;
import com.dev.quikkkk.user_service.entity.User;
import io.micrometer.common.util.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Objects;

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

    public void updateRole(User user, UpdateUserRoleRequest request) {
        user.setRole(request.getRole());
    }

    public void mergeUser(User user, UpdateUserProfileRequest request) {
        if (StringUtils.isNotBlank(request.getFirstName()) &&
                !request.getFirstName().equals(user.getFirstName())) {
            user.setFirstName(request.getFirstName());
        }

        if (StringUtils.isNotBlank(request.getLastName()) &&
                !request.getLastName().equals(user.getLastName())) {
            user.setLastName(request.getLastName());
        }

        if (StringUtils.isNotBlank(request.getEmail()) &&
                !request.getEmail().equals(user.getEmail())) {
            user.setEmail(request.getEmail());
        }

        if (StringUtils.isNotBlank(request.getPhone()) &&
                !request.getPhone().equals(user.getPhone())) {
            user.setPhone(request.getPhone());
        }

        if (request.getDateOfBirth() != null &&
                !Objects.equals(request.getDateOfBirth(), user.getDateOfBirth())) {
            user.setDateOfBirth(request.getDateOfBirth());
        }

        if (request.getGender() != null &&
                !Objects.equals(request.getGender(), user.getGender())) {
            user.setGender(request.getGender());
        }

        if (StringUtils.isNotBlank(request.getAvatarUrl()) &&
                !Objects.equals(request.getAvatarUrl(), user.getAvatarUrl())) {
            user.setAvatarUrl(request.getAvatarUrl());
        }

        if (StringUtils.isNotBlank(request.getAddress()) &&
                !Objects.equals(request.getAddress(), user.getAddress())) {
            user.setAddress(request.getAddress());
        }

        if (StringUtils.isNotBlank(request.getEmergencyContactName()) &&
                !Objects.equals(request.getEmergencyContactName(), user.getEmergencyContactName())) {
            user.setEmergencyContactName(request.getEmergencyContactName());
        }

        if (StringUtils.isNotBlank(request.getEmergencyContactPhone()) &&
                !Objects.equals(request.getEmergencyContactPhone(), user.getEmergencyContactPhone())) {
            user.setEmergencyContactPhone(request.getEmergencyContactPhone());
        }
    }

    public RoleUpdatedResponse toRoleResponse(User user) {
        return RoleUpdatedResponse.builder()
                .userId(user.getId())
                .role(user.getRole().toString())
                .build();
    }
}
