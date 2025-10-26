package com.dev.quikkkk.auth_service.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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
@Schema(description = "Email verification request")
public class VerifyEmailRequest {
    @Schema(
            description = "Email address to verify",
            example = "user@example.com"
    )
    @NotBlank(message = "VALIDATION.VERIFY.EMAIL.NOT_BLANK")
    @Email(message = "VALIDATION.VERIFY.EMAIL.INVALID")
    private String email;

    @Schema(
            description = "6-digit verification code sent to email",
            example = "123456",
            pattern = "^\\d{6}$",
            minLength = 6,
            maxLength = 6
    )
    @NotBlank(message = "VALIDATION.VERIFY.CODE.NOT_BLANK")
    @Pattern(regexp = "^\\d{6}$", message = "VALIDATION.CODE.INVALID_FORMAT")
    private String code;
}
