package com.dev.quikkkk.user_service.controller;

import com.dev.quikkkk.user_service.dto.request.UpdateUserProfileRequest;
import com.dev.quikkkk.user_service.dto.request.UpdateUserRoleRequest;
import com.dev.quikkkk.user_service.dto.response.ApiResponse;
import com.dev.quikkkk.user_service.dto.response.AvatarUploadResponse;
import com.dev.quikkkk.user_service.dto.response.RoleUpdatedResponse;
import com.dev.quikkkk.user_service.dto.response.UserProfileResponse;
import com.dev.quikkkk.user_service.security.UserPrincipal;
import com.dev.quikkkk.user_service.service.IUserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {
    private final IUserService service;

    @PostMapping(value = "/profile/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<AvatarUploadResponse>> uploadAvatar(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        String avatarUrl = service.uploadAvatar(principal.id(), file);
        AvatarUploadResponse response = new AvatarUploadResponse(avatarUrl);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getUserProfile(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        UserProfileResponse user = service.getProfileCurrentUser(principal.id());
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @GetMapping("/{user-id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') || hasRole('ROLE_TRAINER')")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getUserById(
            @PathVariable("user-id") String userId
    ) {
        UserProfileResponse user = service.getUserById(userId);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Page<UserProfileResponse>>> getAllUsers(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(2) @Max(100) int size,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String search
    ) {
        Page<UserProfileResponse> users = service.getAllUsers(page, size, role, search);
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateUserProfile(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody UpdateUserProfileRequest request
    ) {
        UserProfileResponse user = service.updateUserProfile(principal.id(), request);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @PutMapping("{user-id}/role")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<RoleUpdatedResponse>> updateRole(
            @PathVariable("user-id") String userId,
            @RequestBody @Valid UpdateUserRoleRequest request
    ) {
        RoleUpdatedResponse user = service.updateRole(userId, request);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @DeleteMapping("/profile/avatar")
    public ResponseEntity<ApiResponse<Void>> deleteAvatar(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        service.deleteAvatar(principal.id());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @DeleteMapping("/{user-id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteUserById(
            @PathVariable("user-id") String userId
    ) {
        service.deleteUser(userId);
        return ResponseEntity.ok(ApiResponse.success(userId));
    }
}
