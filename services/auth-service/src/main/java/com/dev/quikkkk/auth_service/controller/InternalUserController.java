package com.dev.quikkkk.auth_service.controller;

import com.dev.quikkkk.auth_service.dto.request.UpdateUserRoleRequest;
import com.dev.quikkkk.auth_service.dto.response.ApiResponse;
import com.dev.quikkkk.auth_service.dto.response.RoleUpdatedResponse;
import com.dev.quikkkk.auth_service.service.IUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/users")
@RequiredArgsConstructor
@Slf4j
public class InternalUserController {
    private final IUserService service;

    @PutMapping("/{user-id}/role")
    public ResponseEntity<ApiResponse<RoleUpdatedResponse>> updateRole(
            @PathVariable("user-id") String userId,
            @Valid @RequestBody UpdateUserRoleRequest request
    ) {
        log.info("Internal request to update role for user: {} to role: {}", userId, request.getRole());
        RoleUpdatedResponse response = service.updateUserRole(userId, request);

        log.info("Successfully updated role for user: {} to: {}", userId, response.getRole());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
