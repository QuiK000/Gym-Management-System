package com.dev.quikkkk.user_service.controller;

import com.dev.quikkkk.user_service.dto.response.ApiResponse;
import com.dev.quikkkk.user_service.dto.response.UserProfileResponse;
import com.dev.quikkkk.user_service.security.UserPrincipal;
import com.dev.quikkkk.user_service.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {
    private final IUserService service;

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getUserProfile(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        UserProfileResponse user = service.getProfileCurrentUser(principal.id());
        return ResponseEntity.ok(ApiResponse.success(user));
    }
}
