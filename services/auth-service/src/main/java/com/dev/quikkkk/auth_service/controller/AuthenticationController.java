package com.dev.quikkkk.auth_service.controller;

import com.dev.quikkkk.auth_service.dto.request.ForgotPasswordRequest;
import com.dev.quikkkk.auth_service.dto.request.LoginRequest;
import com.dev.quikkkk.auth_service.dto.request.RefreshTokenRequest;
import com.dev.quikkkk.auth_service.dto.request.RegistrationRequest;
import com.dev.quikkkk.auth_service.dto.request.ResendVerificationRequest;
import com.dev.quikkkk.auth_service.dto.request.ResetPasswordRequest;
import com.dev.quikkkk.auth_service.dto.request.VerifyEmailRequest;
import com.dev.quikkkk.auth_service.dto.response.ApiResponse;
import com.dev.quikkkk.auth_service.dto.response.AuthenticationResponse;
import com.dev.quikkkk.auth_service.dto.response.ErrorResponse;
import com.dev.quikkkk.auth_service.service.IAuthenticationService;
import com.dev.quikkkk.auth_service.service.IEmailVerificationService;
import com.dev.quikkkk.auth_service.utils.NetworkUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(
        name = "Authentication",
        description = """
                Authentication endpoints for user registration, login, and account management.
                
                **Rate Limits:**
                - Registration: 3 per hour per IP
                - Login: 5 per 15 minutes per IP
                - Email verification: 3 per hour per email
                """
)
public class AuthenticationController {
    private final IAuthenticationService authenticationService;
    private final IEmailVerificationService emailVerificationService;

    @PostMapping("/register")
    @Operation(
            summary = "Register new user",
            description = """
                    Creates a new user account with email verification.
                    
                    **Process:**
                    1. Submit registration data
                    2. Receive verification code via email
                    3. Verify email using `/verify-email` endpoint
                    
                    **Password Requirements:**
                    - Minimum 8 characters
                    - At least one uppercase letter
                    - At least one lowercase letter
                    - At least one digit
                    - At least one special character
                    
                    **Rate Limit:** 3 attempts per hour per IP address
                    """,
            tags = {"Authentication"}
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Successfully registered. Verification email sent.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "Success",
                                    value = """
                                            {
                                                "success": true,
                                                "data": null,
                                                "error": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Validation error or email already exists",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Email already exists",
                                            value = """
                                                    {
                                                        "message": "Email already exists",
                                                        "code": "EMAIL_ALREADY_EXISTS",
                                                        "validationErrors": null
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "Weak password",
                                            value = """
                                                    {
                                                        "message": "Validation failed",
                                                        "code": "VALIDATION_ERROR",
                                                        "validationErrors": [
                                                            {
                                                                "field": "password",
                                                                "code": "VALIDATION.REGISTRATION.PASSWORD.WEAK",
                                                                "message": "Password must contain uppercase, lowercase, digit and special character"
                                                            }
                                                        ]
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "Password mismatch",
                                            value = """
                                                    {
                                                        "message": "Password mismatch",
                                                        "code": "PASSWORD_MISMATCH",
                                                        "validationErrors": null
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "429",
                    description = "Too many registration attempts",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "message": "Too many registration attempts. Please try again later.",
                                                "code": "TOO_MANY_ATTEMPTS"
                                            }
                                            """
                            )
                    )
            )
    })
    public ResponseEntity<ApiResponse<Void>> register(
            @Parameter(
                    description = "Registration data including email and password",
                    required = true,
                    schema = @Schema(implementation = RegistrationRequest.class)
            )
            @Valid @RequestBody RegistrationRequest request
    ) {
        authenticationService.register(request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/login")
    @Operation(
            summary = "User login",
            description = """
                    Authenticates user and returns JWT tokens (access + refresh).
                    
                    **Process:**
                    1. Submit email and password
                    2. Receive access token (24h expiry) and refresh token (7d expiry)
                    3. Use access token in Authorization header: `Bearer YOUR_TOKEN`
                    
                    **Security:**
                    - Maximum 5 failed attempts per 15 minutes per IP
                    - Account locked after suspicious activity
                    - Email must be verified before login
                    
                    **Rate Limit:** 5 attempts per 15 minutes per IP address
                    """,
            tags = {"Authentication"}
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Successfully authenticated",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthenticationResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "success": true,
                                                "data": {
                                                    "access_token": "eyJhbGciOiJSUzI1NiJ9...",
                                                    "refresh_token": "eyJhbGciOiJSUzI1NiJ9...",
                                                    "token_type": "Bearer "
                                                },
                                                "error": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Invalid credentials or account not activated",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "Invalid credentials",
                                            value = """
                                                    {
                                                        "message": "Invalid credentials",
                                                        "code": "INVALID_CREDENTIALS"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "Account locked",
                                            value = """
                                                    {
                                                        "message": "Account is locked",
                                                        "code": "ACCOUNT_LOCKED"
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "429",
                    description = "Too many login attempts",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "message": "Too many attempts. Please try again later.",
                                                "code": "TOO_MANY_ATTEMPTS"
                                            }
                                            """
                            )
                    )
            )
    })
    public ResponseEntity<ApiResponse<AuthenticationResponse>> login(
            @Parameter(
                    description = "Login credentials",
                    required = true
            )
            @Valid @RequestBody LoginRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(authenticationService.login(request)));
    }

    @PostMapping("/verify-email")
    @Operation(
            summary = "Verify email address",
            description = """
                    Verifies user email using the 6-digit code sent during registration.
                    
                    **Process:**
                    1. Check email for verification code
                    2. Submit email and 6-digit code
                    3. Account becomes active after successful verification
                    
                    **Code Properties:**
                    - 6 digits (e.g., 123456)
                    - Valid for 15 minutes
                    - Maximum 5 verification attempts per code
                    - Can request new code via `/resend-verification`
                    
                    **Rate Limit:** 5 attempts per 15 minutes per email
                    """,
            tags = {"Email Verification"}
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Email successfully verified",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "success": true,
                                                "data": null,
                                                "error": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid or expired code",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "Invalid code",
                                            value = """
                                                    {
                                                        "message": "Invalid verification code",
                                                        "code": "INVALID_VERIFICATION_CODE"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "Expired code",
                                            value = """
                                                    {
                                                        "message": "Verification code expired",
                                                        "code": "VERIFICATION_CODE_EXPIRED"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "Already verified",
                                            value = """
                                                    {
                                                        "message": "Email already verified",
                                                        "code": "EMAIL_ALREADY_VERIFIED"
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "429",
                    description = "Too many verification attempts",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "message": "Maximum verification attempts exceeded",
                                                "code": "MAX_VERIFICATION_ATTEMPTS_EXCEEDED"
                                            }
                                            """
                            )
                    )
            )
    })
    public ResponseEntity<ApiResponse<Void>> verifyEmail(
            @Parameter(description = "Email and verification code", required = true)
            @Valid @RequestBody VerifyEmailRequest request
    ) {
        emailVerificationService.verifyEmail(request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/resend-verification")
    @Operation(
            summary = "Resend verification code",
            description = """
                    Sends a new verification code to the user's email.
                    
                    **Use Cases:**
                    - Original code expired (15 minutes)
                    - Email not received
                    - Code was deleted accidentally
                    
                    **Limits:**
                    - Maximum 3 codes per email per hour
                    - Previous codes remain valid until expiry
                    
                    **Rate Limit:** 3 per hour per email address
                    """,
            tags = {"Email Verification"}
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "New verification code sent",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "success": true,
                                                "data": null,
                                                "error": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Email already verified",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "message": "Email already verified",
                                                "code": "EMAIL_ALREADY_VERIFIED"
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "message": "User not found",
                                                "code": "USER_NOT_FOUND"
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "429",
                    description = "Too many code requests",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "message": "Too many email attempts",
                                                "code": "TOO_MANY_EMAIL_ATTEMPTS"
                                            }
                                            """
                            )
                    )
            )
    })
    public ResponseEntity<ApiResponse<Void>> resendVerification(
            @Parameter(description = "Email address to resend code", required = true)
            @Valid @RequestBody ResendVerificationRequest request,
            HttpServletRequest httpRequest
    ) {
        String ipAddress = NetworkUtils.getClientIp(httpRequest);
        emailVerificationService.resendVerificationCode(request, ipAddress);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/refresh-token")
    @Operation(
            summary = "Refresh access token",
            description = """
                    Generates new access token using refresh token.
                    
                    **When to use:**
                    - Access token expired (24h)
                    - Receive 401 Unauthorized on API calls
                    - Proactive refresh before expiry
                    
                    **Process:**
                    1. Send refresh token in request body
                    2. Receive new access token
                    3. Continue using same refresh token (valid 7 days)
                    
                    **Security:**
                    - Refresh token valid for 7 days
                    - Can be used multiple times
                    - Invalidated on logout
                    """,
            tags = {"Token Management"},
            security = @SecurityRequirement(name = "Refresh Token")
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "New access token generated",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "success": true,
                                                "data": {
                                                    "access_token": "eyJhbGciOiJSUzI1NiJ9...",
                                                    "refresh_token": "eyJhbGciOiJSUzI1NiJ9...",
                                                    "token_type": "Bearer "
                                                },
                                                "error": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Invalid or expired refresh token",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "Invalid token",
                                            value = """
                                                    {
                                                        "message": "Invalid or expired token",
                                                        "code": "INVALID_TOKEN"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "Token revoked",
                                            value = """
                                                    {
                                                        "message": "Token has been revoked",
                                                        "code": "TOKEN_REVOKED"
                                                    }
                                                    """
                                    )
                            }
                    )
            )
    })
    public ResponseEntity<ApiResponse<AuthenticationResponse>> refreshToken(
            @Parameter(
                    description = "Refresh token from login response",
                    required = true,
                    example = """
                            {
                                "refreshToken": "eyJhbGciOiJSUzI1NiJ9..."
                            }
                            """
            )
            @RequestBody RefreshTokenRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(authenticationService.refreshToken(request)));
    }

    @PostMapping("/logout")
    @Operation(
            summary = "User logout",
            description = """
                    Invalidates current access token (adds to blacklist).
                    
                    **Process:**
                    1. Send logout request with access token
                    2. Token added to blacklist
                    3. Token cannot be used anymore
                    4. Refresh token also invalidated
                    
                    **Note:** Client should also clear stored tokens locally.
                    """,
            tags = {"Authentication"},
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Successfully logged out",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "success": true,
                                                "data": null,
                                                "error": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid authorization header",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "success": false,
                                                "data": null,
                                                "error": "Authorization header is missing or invalid"
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Invalid or expired token",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "message": "Invalid or expired token",
                                                "code": "INVALID_TOKEN"
                                            }
                                            """
                            )
                    )
            )
    })
    public ResponseEntity<ApiResponse<Void>> logout(@Parameter(hidden = true) HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer "))
            return ResponseEntity.badRequest().body(ApiResponse.error("Authorization header is missing or invalid"));
        String token = authHeader.substring(7);
        authenticationService.logout(token);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/forgot-password")
    @Operation(
            summary = "Request password reset",
            description = """
                    Initiates password reset process by sending reset link to email.
                    
                    **Process:**
                    1. Submit email address
                    2. Receive password reset link via email
                    3. Link valid for 1 hour
                    4. Use link to reset password
                    
                    **Security:**
                    - Reset token is single-use
                    - Previous reset tokens invalidated
                    - No information about account existence disclosed
                    """,
            tags = {"Password Management"}
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Password reset email sent (if account exists)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "success": true,
                                                "data": null,
                                                "error": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid email format",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "message": "Validation failed",
                                                "code": "VALIDATION_ERROR",
                                                "validationErrors": [
                                                    {
                                                        "field": "email",
                                                        "code": "VALIDATION.FORGOT.EMAIL.INVALID",
                                                        "message": "Invalid email format"
                                                    }
                                                ]
                                            }
                                            """
                            )
                    )
            )
    })
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Parameter(description = "Email address", required = true)
            @Valid @RequestBody ForgotPasswordRequest request
    ) {
        authenticationService.forgotPassword(request.getEmail());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/reset-password")
    @Operation(
            summary = "Reset password",
            description = """
                    Resets user password using the token from reset email.
                    
                    **Process:**
                    1. Click link from forgot-password email
                    2. Extract token from URL
                    3. Submit token with new password
                    
                    **Password Requirements:**
                    - Minimum 8 characters
                    - At least one uppercase letter
                    - At least one lowercase letter
                    - At least one digit
                    - At least one special character
                    
                    **Security:**
                    - Token valid for 1 hour
                    - Single-use token
                    - All sessions invalidated after reset
                    """,
            tags = {"Password Management"}
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Password successfully reset",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "success": true,
                                                "data": null,
                                                "error": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid request or password mismatch",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "Password mismatch",
                                            value = """
                                                    {
                                                        "success": false,
                                                        "data": null,
                                                        "error": "Passwords do not match"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "Invalid token",
                                            value = """
                                                    {
                                                        "message": "Invalid or expired password reset token",
                                                        "code": "PASSWORD_RESET_TOKEN_INVALID"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "Token already used",
                                            value = """
                                                    {
                                                        "message": "Password reset token already used",
                                                        "code": "PASSWORD_RESET_TOKEN_USED"
                                                    }
                                                    """
                                    )
                            }
                    )
            )
    })
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Parameter(
                    description = "Reset token and new password",
                    required = true
            )
            @Valid @RequestBody ResetPasswordRequest request
    ) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Passwords do not match"));
        }

        authenticationService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/verification-status")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Boolean>> getVerificationStatus(
            @Parameter(
                    description = "Email address to check",
                    required = true,
                    example = "user@example.com"
            )
            @RequestParam String email
    ) {
        boolean isVerified = emailVerificationService.isEmailRecentlyVerified(email);
        return ResponseEntity.ok(ApiResponse.success(isVerified));
    }

    @GetMapping("/validate-token")
    @Operation(
            summary = "Validate JWT token",
            description = """
                    Validates JWT token and returns token information.
                    
                    **Returns:**
                    - Token validity status
                    - User ID
                    - Email
                    - Roles
                    - Token type (ACCESS_TOKEN)
                    
                    **Use Cases:**
                    - Client-side token validation
                    - Debugging token issues
                    - Token introspection
                    - Inter-service communication
                    """,
            tags = {"Token Management"},
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Token is valid",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "success": true,
                                                "data": {
                                                    "valid": true,
                                                    "userId": "550e8400-e29b-41d4-a716-446655440000",
                                                    "email": "user@example.com",
                                                    "roles": ["ROLE_MEMBER"],
                                                    "tokenType": "ACCESS_TOKEN"
                                                },
                                                "error": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "No token provided",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "success": false,
                                                "data": null,
                                                "error": "No token provided"
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Invalid or expired token",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "Invalid token",
                                            value = """
                                                    {
                                                        "message": "Invalid or expired token",
                                                        "code": "INVALID_TOKEN"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "Token revoked",
                                            value = """
                                                    {
                                                        "message": "Token has been revoked",
                                                        "code": "TOKEN_REVOKED"
                                                    }
                                                    """
                                    )
                            }
                    )
            )
    })
    public ResponseEntity<ApiResponse<Map<String, Object>>> validateToken(
            @Parameter(hidden = true)
            HttpServletRequest request
    ) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer "))
            return ResponseEntity.badRequest().body(ApiResponse.error("No token provided"));

        String token = authHeader.substring(7);
        Map<String, Object> validationResult = authenticationService.validateToken(token);

        return ResponseEntity.ok(ApiResponse.success(validationResult));
    }
}
