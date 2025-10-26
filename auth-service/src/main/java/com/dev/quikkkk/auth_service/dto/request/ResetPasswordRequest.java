package com.dev.quikkkk.auth_service.dto.request;

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
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResetPasswordRequest {
    @NotBlank(message = "VALIDATION.RESET.PASSWORD.NOT_BLANK")
    private String token;

    @NotBlank(message = "VALIDATION.RESET.NEW_PASSWORD.NOT_BLANK")
    @Size(min = 8, max = 50, message = "VALIDATION.RESET_PASSWORD.PASSWORD.SIZE")
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*\\W).*$", message = "VALIDATION.RESET_PASSWORD.PASSWORD.WEAK")
    private String newPassword;

    @NotBlank(message = "VALIDATION.RESET_PASSWORD.CONFIRM_PASSWORD.NOT_BLANK")
    private String confirmPassword;
}
