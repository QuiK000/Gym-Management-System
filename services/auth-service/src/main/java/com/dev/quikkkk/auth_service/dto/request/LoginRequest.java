package com.dev.quikkkk.auth_service.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "User login request")
public class LoginRequest {
    @Schema(
            description = "User email address",
            example = "user@example.com"
    )
    @NotBlank(message = "VALIDATION.LOGIN.EMAIL.NOT_BLANK")
    @Email(message = "VALIDATION.LOGIN.EMAIL.INVALID.FORMAT")
    private String email;

    @Schema(
            description = "User password",
            example = "SecurePass123!",
            format = "password"
    )
    @NotBlank(message = "VALIDATION.LOGIN.PASSWORD.NOT_BLANK")
    private String password;
}
