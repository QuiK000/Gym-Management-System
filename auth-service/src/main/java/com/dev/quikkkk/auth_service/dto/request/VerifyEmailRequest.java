package com.dev.quikkkk.auth_service.dto.request;

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
public class VerifyEmailRequest {
    @NotBlank(message = "VALIDATION.VERIFY.EMAIL.NOT_BLANK")
    @Email(message = "VALIDATION.VERIFY.EMAIL.INVALID")
    private String email;

    @NotBlank(message = "VALIDATION.VERIFY.CODE.NOT_BLANK")
    @Pattern(regexp = "^\\d{6}$", message = "VALIDATION.CODE.INVALID_FORMAT")
    private String code;
}
