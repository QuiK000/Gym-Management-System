package com.dev.quikkkk.auth_service.controller;

import com.dev.quikkkk.auth_service.dto.request.LoginRequest;
import com.dev.quikkkk.auth_service.dto.request.RegistrationRequest;
import com.dev.quikkkk.auth_service.dto.request.ResendVerificationRequest;
import com.dev.quikkkk.auth_service.dto.request.VerifyEmailRequest;
import com.dev.quikkkk.auth_service.dto.response.ApiResponse;
import com.dev.quikkkk.auth_service.dto.response.AuthenticationResponse;
import com.dev.quikkkk.auth_service.service.IAuthenticationService;
import com.dev.quikkkk.auth_service.service.IEmailVerificationService;
import com.dev.quikkkk.auth_service.utils.NetworkUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final IAuthenticationService authenticationService;
    private final IEmailVerificationService emailVerificationService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody RegistrationRequest request) {
        authenticationService.register(request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authenticationService.login(request)));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        emailVerificationService.verifyEmail(request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<ApiResponse<Void>> resendVerification(
            @Valid @RequestBody ResendVerificationRequest request,
            HttpServletRequest httpRequest
    ) {
        String ipAddress = NetworkUtils.getClientIp(httpRequest);
        emailVerificationService.resendVerificationCode(request, ipAddress);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/verification-status")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Boolean>> getVerificationStatus(
            @RequestParam String email
    ) {
        boolean isVerified = emailVerificationService.isEmailRecentlyVerified(email);
        return ResponseEntity.ok(ApiResponse.success(isVerified));
    }
}
