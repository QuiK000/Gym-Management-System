package com.dev.quikkkk.user_service.dto.response;

import com.dev.quikkkk.user_service.entity.GenderType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileResponse {
    private String id;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private LocalDateTime dateOfBirth;
    private GenderType gender;
    private String avatarUrl;
    private String address;
    private String emergencyContactName;
    private String emergencyContactPhone;
}
