package com.dev.quikkkk.auth_service.dto.request;

import com.dev.quikkkk.auth_service.validation.NonDisposableEmail;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
@Schema(description = "User registration request")
public class RegistrationRequest {
    @Schema(
            description = "User email address",
            example = "user@example.com",
            maxLength = 50
    )
    @NotBlank(message = "VALIDATION.REGISTRATION.EMAIL.NOT_BLANK")
    @Size(max = 50, message = "VALIDATION.REGISTRATION.EMAIL.SIZE")
    @Pattern(regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$", message = "VALIDATION.REGISTRATION.EMAIL.INVALID")
    @NonDisposableEmail(message = "VALIDATION.REGISTRATION.EMAIL.DISPOSABLE")
    private String email;

    @Schema(
            description = "User password. Must contain at least one uppercase letter, one lowercase letter, one digit, and one special character",
            example = "SecurePass123!",
            minLength = 8,
            maxLength = 50,
            pattern = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*\\W).*$"
    )
    @NotBlank(message = "VALIDATION.REGISTRATION.PASSWORD.NOT_BLANK")
    @Size(min = 8, max = 50, message = "VALIDATION.REGISTRATION.PASSWORD.SIZE")
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*\\W).*$", message = "VALIDATION.REGISTRATION.PASSWORD.WEAK")
    private String password;

    @Schema(
            description = "Password confirmation (must match password)",
            example = "SecurePass123!",
            minLength = 8,
            maxLength = 50
    )
    @NotBlank(message = "VALIDATION.REGISTRATION.CONFIRM_PASSWORD.NOT_BLANK")
    @Size(min = 8, max = 50, message = "VALIDATION.REGISTRATION.CONFIRM_PASSWORD.SIZE")
    private String confirmPassword;
}
