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
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResendVerificationRequest {
    @NotBlank(message = "VALIDATION.RESEND.EMAIL.NOT_BLANK")
    @Email(message = "VALIDATION.RESEND.EMAIL.INVALID")
    private String email;
}
