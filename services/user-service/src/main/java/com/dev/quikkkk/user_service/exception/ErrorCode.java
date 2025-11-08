package com.dev.quikkkk.user_service.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;


@Getter
public enum ErrorCode {
    USER_NOT_FOUND("USER_NOT_FOUND", "User not found with id %s", HttpStatus.NOT_FOUND),
    INTERNAL_SERVER_ERROR("INTERNAL_SERVER_ERROR", "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR),
    ENTITY_NOT_FOUND("ENTITY_NOT_FOUND", "Entity not found with id %s", HttpStatus.NOT_FOUND),
    BAD_REQUEST("BAD_REQUEST", "Bad request", HttpStatus.BAD_REQUEST),
    FORBIDDEN("FORBIDDEN", "Forbidden", HttpStatus.FORBIDDEN),
    UNAUTHORIZED("UNAUTHORIZED", "Unauthorized", HttpStatus.UNAUTHORIZED),
    BAD_CREDENTIALS("BAD_CREDENTIALS", "Bad credentials", HttpStatus.UNAUTHORIZED),
    USERNAME_NOT_FOUND("USERNAME_NOT_FOUND", "Username not found", HttpStatus.UNAUTHORIZED),
    EMAIL_ALREADY_EXISTS("EMAIL_ALREADY_EXISTS", "Email already exists", HttpStatus.BAD_REQUEST),
    USERNAME_ALREADY_EXISTS("USERNAME_ALREADY_EXISTS", "Username already exists", HttpStatus.BAD_REQUEST),
    PASSWORD_MISMATCH("PASSWORD_MISMATCH", "Password mismatch", HttpStatus.BAD_REQUEST),
    TOO_MANY_ATTEMPTS("TOO_MANY_ATTEMPTS", "Too many attempts", HttpStatus.UNAUTHORIZED),
    INVALID_CREDENTIALS("INVALID_CREDENTIALS", "Invalid credentials", HttpStatus.UNAUTHORIZED),
    ROLE_NOT_FOUND("ROLE_NOT_FOUND", "Role not found", HttpStatus.BAD_REQUEST),
    TOO_MANY_EMAIL_ATTEMPTS("TOO_MANY_EMAIL_ATTEMPTS", "Too many email attempts" , HttpStatus.TOO_MANY_REQUESTS),
    INVALID_VERIFICATION_CODE("INVALID_VERIFICATION_CODE", "Invalid verification code", HttpStatus.BAD_REQUEST),
    VERIFICATION_CODE_EXPIRED("VERIFICATION_CODE_EXPIRED", "Verification code expired", HttpStatus.BAD_REQUEST),
    MAX_VERIFICATION_ATTEMPTS_EXCEEDED("MAX_VERIFICATION_ATTEMPTS_EXCEEDED", "Maximum verification attempts exceeded", HttpStatus.TOO_MANY_REQUESTS),
    EMAIL_ALREADY_VERIFIED("EMAIL_ALREADY_VERIFIED", "Email already verified", HttpStatus.BAD_REQUEST),
    TOKEN_REVOKED("TOKEN_REVOKED", "Token has been revoked", HttpStatus.UNAUTHORIZED),
    INVALID_TOKEN("INVALID_TOKEN", "Invalid or expired token", HttpStatus.UNAUTHORIZED),
    TOKEN_EXPIRED("TOKEN_EXPIRED", "Token has expired", HttpStatus.UNAUTHORIZED),
    PASSWORD_RESET_TOKEN_INVALID("PASSWORD_RESET_TOKEN_INVALID", "Invalid or expired password reset token", HttpStatus.BAD_REQUEST),
    PASSWORD_RESET_TOKEN_USED("PASSWORD_RESET_TOKEN_USED", "Password reset token already used", HttpStatus.BAD_REQUEST),
    INVALID_FILE_FORMAT("INVALID_FILE_FORMAT", "Invalid file format", HttpStatus.BAD_REQUEST),
    FILE_UPLOAD_ERROR("FILE_UPLOAD_ERROR", "Failed to upload file", HttpStatus.INTERNAL_SERVER_ERROR),
    FILE_SIZE_EXCEEDED("FILE_SIZE_EXCEEDED", "File size exceeds maximum allowed size of 5MB", HttpStatus.BAD_REQUEST),
    INVALID_FILE_EXTENSION("INVALID_FILE_EXTENSION", "Invalid file extension. Allowed: jpg, jpeg, png, gif, webp", HttpStatus.BAD_REQUEST),
    INVALID_IMAGE_FILE("INVALID_IMAGE_FILE", "File is not a valid image", HttpStatus.BAD_REQUEST),
    INVALID_IMAGE_DIMENSIONS("INVALID_IMAGE_DIMENSIONS", "Image dimensions must be between 100x100 and 2000x2000 pixels", HttpStatus.BAD_REQUEST),
    INVALID_ROLE_TRAINER("INVALID_ROLE_TRAINER", "User is not a trainer", HttpStatus.FORBIDDEN),
    TRAINER_PROFILE_ALREADY_EXISTS("TRAINER_PROFILE_ALREADY_EXISTS", "Trainer profile already exists", HttpStatus.BAD_REQUEST),
    TRAINER_NOT_FOUND("TRAINER_NOT_FOUND", "Trainer not found", HttpStatus.NOT_FOUND);

    private final String code;
    private final String defaultMessage;
    private final HttpStatus status;

    ErrorCode(String code, String defaultMessage, HttpStatus status) {
        this.code = code;
        this.defaultMessage = defaultMessage;
        this.status = status;
    }
}
