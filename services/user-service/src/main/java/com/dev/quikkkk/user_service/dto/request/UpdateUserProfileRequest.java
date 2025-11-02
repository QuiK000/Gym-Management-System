package com.dev.quikkkk.user_service.dto.request;

import com.dev.quikkkk.user_service.enums.GenderType;
import com.dev.quikkkk.user_service.validation.NonDisposableEmail;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
public class UpdateUserProfileRequest {
    @Size(min = 3, max = 255, message = "VALIDATION.UPDATE.USER.PROFILE.FIRST_NAME.REQUEST.SIZE")
    private String firstName;

    @Size(min = 3, max = 255, message = "VALIDATION.UPDATE.USER.PROFILE.REQUEST.LAST_NAME.SIZE")
    private String lastName;

    @Email(message = "VALIDATION.UPDATE.USER.PROFILE.REQUEST.EMAIL.INVALID.FORMAT")
    @NonDisposableEmail(message = "VALIDATION.UPDATE.USER.PROFILE.REQUEST.EMAIL.DISPOSABLE")
    private String email;

    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "VALIDATION.UPDATE.USER.PROFILE.REQUEST.PHONE.INVALID.FORMAT")
    private String phone;
    private LocalDateTime dateOfBirth;

    @NotNull(message = "VALIDATION.UPDATE.USER.PROFILE.REQUEST.GENDER.NOT_NULL")
    private GenderType gender;
    private String avatarUrl;
    private String address;
    private String emergencyContactName;
    private String emergencyContactPhone;
}
