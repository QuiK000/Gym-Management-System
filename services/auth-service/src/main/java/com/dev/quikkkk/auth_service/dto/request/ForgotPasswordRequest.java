package com.dev.quikkkk.auth_service.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ForgotPasswordRequest {
    @NotBlank(message = "VALIDATION.FORGOT.EMAIL.NOT_BLANK")
    @Email(message = "VALIDATION.FORGOT.EMAIL.INVALID")
    private String email;
}
